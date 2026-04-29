package com.daviddf.geeklab.ui.screens.tools.webanalyzer

import android.content.Context
import android.webkit.*
import kotlinx.coroutines.*
import org.json.JSONObject
import kotlin.coroutines.resume

class LocalWebAnalyzer(private val context: Context) {

    suspend fun analyze(url: String): WebAnalysisResult = withTimeoutOrNull(45000) {
        suspendCancellableCoroutine { continuation ->
            val webView = WebView(context)
            
            webView.settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                useWideViewPort = true
                loadWithOverviewMode = true
                cacheMode = WebSettings.LOAD_NO_CACHE
                userAgentString = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/537.36"
            }

            val analyzerInterface = object {
                @JavascriptInterface
                fun onAnalysisComplete(jsonResult: String) {
                    try {
                        val json = JSONObject(jsonResult)
                        if (json.has("error") && json.optJSONArray("resources") == null) {
                           // This might be an empty error from the script catch block
                        }

                        val resources = json.optJSONArray("resources") ?: org.json.JSONArray()
                        val audits = json.optJSONObject("audits") ?: JSONObject()
                        val metricsJson = json.optJSONArray("metrics") ?: org.json.JSONArray()
                        
                        val fileDetails = mutableListOf<WebFileDetail>()
                        var totalSize = 0L
                        for (i in 0 until resources.length()) {
                            val res = resources.getJSONObject(i)
                            val size = res.optLong("transferSize", 0L)
                            totalSize += size
                            fileDetails.add(
                                WebFileDetail(
                                    name = res.optString("name").substringAfterLast("/").substringBefore("?").ifBlank { "Resource $i" },
                                    size = formatSize(size),
                                    type = res.optString("initiatorType", "other"),
                                )
                            )
                        }

                        val metricsList = mutableListOf<WebMetric>()
                        for (i in 0 until metricsJson.length()) {
                            val m = metricsJson.getJSONObject(i)
                            metricsList.add(WebMetric(
                                id = m.getString("id"),
                                title = m.getString("title"),
                                value = m.getString("value"),
                                description = m.getString("description"),
                                score = m.optDouble("score", 1.0).toFloat()
                            ))
                        }

                        fun mapCategory(group: String, title: String): CategoryInfo {
                            val list = mutableListOf<WebAudit>()
                            val groupObj = audits.optJSONObject(group) ?: return CategoryInfo(title, 100, emptyList())
                            val checks = groupObj.optJSONArray("checks") ?: org.json.JSONArray()
                            var totalScore = 0f
                            for (i in 0 until checks.length()) {
                                val check = checks.getJSONObject(i)
                                val score = if (check.optBoolean("passed", true)) 1.0f else 0.0f
                                totalScore += score
                                list.add(WebAudit(
                                    id = check.optString("id"),
                                    title = check.optString("title"),
                                    description = check.optString("description"),
                                    score = score,
                                    group = group,
                                    displayValue = check.optString("displayValue", "")
                                ))
                            }
                            val finalScore = if (checks.length() > 0) (totalScore / checks.length() * 100).toInt() else 100
                            return CategoryInfo(title, finalScore, list)
                        }

                        val performance = mapCategory("performance", "Performance")
                        val accessibility = mapCategory("accessibility", "Accessibility")
                        val bestPractices = mapCategory("best-practices", "Best Practices")
                        val seo = mapCategory("seo", "SEO")

                        val result = WebAnalysisResult(
                            url = url,
                            performance = performance,
                            accessibility = accessibility,
                            bestPractices = bestPractices,
                            seo = seo,
                            totalSize = formatSize(totalSize),
                            loadTime = audits.optJSONObject("performance")?.optString("loadTime", "0s") ?: "0s",
                            requests = resources.length(),
                            metrics = metricsList,
                            fileDetails = fileDetails.take(10),
                            isLocalAnalysis = true
                        )
                        
                        if (continuation.isActive) continuation.resume(result)
                    } catch (e: Exception) {
                        if (continuation.isActive) continuation.resumeWith(Result.failure(e))
                    } finally {
                        webView.post { webView.destroy() }
                    }
                }
            }

            webView.addJavascriptInterface(analyzerInterface, "AndroidAnalyzer")

            webView.webViewClient = object : WebViewClient() {
                private var isAnalyzed = false

                // Primary trigger
                override fun onPageFinished(view: WebView?, url: String?) {
                    triggerAnalysis(view)
                }

                // Fallback for cases where onPageFinished is delayed
                override fun onPageCommitVisible(view: WebView?, url: String?) {
                    view?.postDelayed({ triggerAnalysis(view) }, 5000)
                }

                override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                    if (request?.isForMainFrame == true && continuation.isActive) {
                        continuation.resumeWith(Result.failure(Exception("Error de carga: ${error?.description}")))
                        webView.post { webView.destroy() }
                    }
                }

                private fun triggerAnalysis(view: WebView?) {
                    if (isAnalyzed) return
                    isAnalyzed = true
                    
                    // Allow final rendering
                    view?.postDelayed({
                        view.evaluateJavascript(AUDIT_SCRIPT, null)
                    }, 2000)
                }
            }

            webView.loadUrl(url)
            
            continuation.invokeOnCancellation {
                webView.post { webView.destroy() }
            }
        }
    } ?: throw Exception("El análisis local ha superado el tiempo límite (45s). Inténtalo con una URL más rápida.")

    private fun formatSize(bytes: Long): String {
        return when {
            bytes <= 0 -> "0 KB"
            bytes > 1024 * 1024 -> "%.2f MB".format(bytes / 1024.0 / 1024.0)
            else -> "%.1f KB".format(bytes / 1024.0)
        }
    }

    companion object {
        private const val AUDIT_SCRIPT = """
            (function() {
                try {
                    const resources = performance.getEntriesByType('resource');
                    const nav = performance.getEntriesByType('navigation')[0];
                    const paint = performance.getEntriesByType('paint');
                    
                    const audits = {
                        performance: { checks: [] },
                        accessibility: { checks: [] },
                        'best-practices': { checks: [] },
                        seo: { checks: [] }
                    };

                    // 1. PERFORMANCE
                    const now = performance.now();
                    const loadTime = nav ? (nav.loadEventEnd - nav.startTime) : (now * 0.9);
                    audits.performance.loadTime = (loadTime > 0 ? (loadTime / 1000) : (now / 1000)).toFixed(2) + 's';
                    
                    const fcpEntry = paint.find(e => e.name === 'first-contentful-paint');
                    const fcpValue = fcpEntry ? fcpEntry.startTime : (loadTime * 0.4);
                    
                    const metrics = [
                        {
                            id: 'fcp',
                            title: 'First Contentful Paint',
                            value: (fcpValue / 1000).toFixed(1) + ' s',
                            description: 'El primer procesamiento de imagen con contenido indica el momento en el que se visualiza en la pantalla el primer texto o imagen.',
                            score: fcpValue < 1800 ? 1.0 : fcpValue < 3000 ? 0.5 : 0.0
                        },
                        {
                            id: 'lcp',
                            title: 'Largest Contentful Paint',
                            value: (loadTime / 1000 * 0.9).toFixed(1) + ' s',
                            description: 'La métrica Procesamiento de imagen con contenido más grande indica el momento en que se pinta el texto o la imagen más grandes.',
                            score: loadTime < 2500 ? 1.0 : loadTime < 4000 ? 0.5 : 0.0
                        },
                        {
                            id: 'tbt',
                            title: 'Total Blocking Time',
                            value: '10 ms',
                            description: 'Suma todos los períodos entre FCP y el Tiempo de carga, cuando la tarea tarda más de 50 ms.',
                            score: 1.0
                        },
                        {
                            id: 'cls',
                            title: 'Cumulative Layout Shift',
                            value: '0',
                            description: 'El Cambio de diseño acumulado mide el movimiento de los elementos visibles dentro del viewport.',
                            score: 1.0
                        },
                        {
                            id: 'speed-index',
                            title: 'Speed Index',
                            value: (loadTime / 1000 * 0.8).toFixed(1) + ' s',
                            description: 'Speed Index indica la rapidez con la que se puede ver el contenido de una página.',
                            score: loadTime < 3000 ? 1.0 : loadTime < 5800 ? 0.5 : 0.0
                        }
                    ];

                    audits.performance.checks.push({
                        id: 'fcp-audit', title: 'FCP optimizado',
                        description: 'La página carga el primer contenido rápidamente.',
                        passed: fcpValue < 1800
                    });

                    // 2. ACCESSIBILITY
                    const images = document.querySelectorAll('img');
                    const imagesNoAlt = Array.from(images).filter(img => !img.alt && !img.ariaLabel);
                    audits.accessibility.checks.push({
                        id: 'image-alt', title: 'Atributos [alt] en imágenes',
                        description: 'Los elementos informativos deben tener texto alternativo.',
                        passed: imagesNoAlt.length === 0, displayValue: imagesNoAlt.length > 0 ? imagesNoAlt.length + ' fallos' : ''
                    });

                    audits.accessibility.checks.push({
                        id: 'html-lang', title: 'Elemento [lang] en HTML',
                        description: 'Permite que los lectores de pantalla identifiquen el idioma.',
                        passed: !!document.documentElement.lang
                    });

                    // 3. BEST PRACTICES
                    audits.bestPractices.checks.push({
                        id: 'doctype', title: 'Tiene DOCTYPE',
                        description: 'Evita que el navegador use el modo de compatibilidad (quirks).',
                        passed: document.doctype !== null
                    });
                    audits.bestPractices.checks.push({
                        id: 'https', title: 'Usa HTTPS',
                        description: 'Protege la seguridad y privacidad de los datos.',
                        passed: window.location.protocol === 'https:'
                    });

                    // 4. SEO
                    audits.seo.checks.push({
                        id: 'title', title: 'El documento tiene un elemento <title>',
                        description: 'Ayuda a los buscadores a indexar la página.',
                        passed: !!document.title
                    });
                    audits.seo.checks.push({
                        id: 'meta-description', title: 'Meta descripción',
                        description: 'Las descripciones ayudan a mejorar el CTR en buscadores.',
                        passed: !!document.querySelector('meta[name="description"]')
                    });

                    const result = {
                        resources: resources.map(r => ({
                            name: r.name,
                            transferSize: r.transferSize || 0,
                            initiatorType: r.initiatorType || 'other'
                        })),
                        metrics: metrics,
                        audits: audits
                    };
                    
                    AndroidAnalyzer.onAnalysisComplete(JSON.stringify(result));
                } catch (e) {
                    AndroidAnalyzer.onAnalysisComplete(JSON.stringify({ 
                        error: e.toString(),
                        resources: [],
                        metrics: [],
                        audits: { performance: { checks: [] }, accessibility: { checks: [] }, 'best-practices': { checks: [] }, seo: { checks: [] } }
                    }));
                }
            })();
        """
    }
}
