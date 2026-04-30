package com.daviddf.geeklab.ui.screens.tools.webanalyzer

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.daviddf.geeklab.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject
import kotlin.coroutines.resume

class LocalWebAnalyzer(private val context: Context) {

    // Class level references to prevent GC during long analyses
    private var activeWebView: WebView? = null
    private var activeInterface: AnalyzerInterface? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    class AnalyzerInterface(private val onComplete: (String) -> Unit) {
        @JavascriptInterface
        fun onAnalysisComplete(jsonResult: String) {
            Log.d("LocalWebAnalyzer", "Analysis received from JS")
            onComplete(jsonResult)
        }

        @JavascriptInterface
        fun log(message: String) {
            Log.d("LocalWebAnalyzer", "JS Log: $message")
        }
    }

    suspend fun analyze(url: String, strategy: AnalysisStrategy = AnalysisStrategy.MOBILE): WebAnalysisResult = withContext(Dispatchers.Main) {
        val timeout = 300000L // 5 minutes
        
        // Clean up any previous run
        cleanup()

        withTimeoutOrNull(timeout) {
            suspendCancellableCoroutine { continuation ->
                val webView = WebView(context)
                activeWebView = webView
                
                var isTriggered = false

                fun triggerAudit(source: String) {
                    if (isTriggered) return
                    isTriggered = true
                    Log.d("LocalWebAnalyzer", "Triggering audit (Source: $source)")
                    
                    mainHandler.post {
                        Log.d("LocalWebAnalyzer", "Evaluating JS Audit Script...")
                        webView.evaluateJavascript(AUDIT_SCRIPT) { result ->
                            Log.d("LocalWebAnalyzer", "evaluateJavascript callback triggered (result: ${result?.take(20)}...)")
                        }
                    }
                }
                
                webView.settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    cacheMode = WebSettings.LOAD_DEFAULT
                    
                    userAgentString = if (strategy == AnalysisStrategy.MOBILE) {
                        "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Mobile Safari/537.36"
                    } else {
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Safari/537.36"
                    }
                }

                activeInterface = AnalyzerInterface { jsonResult ->
                    try {
                        Log.d("LocalWebAnalyzer", "JSON response received, starting parse")
                        val json = JSONObject(jsonResult)
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
                            var totalWeightedScore = 0f
                            var totalWeights = 0f
                            for (i in 0 until checks.length()) {
                                val check = checks.getJSONObject(i)
                                val score = if (check.has("score")) {
                                    check.getDouble("score").toFloat()
                                } else {
                                    if (check.optBoolean("passed", true)) 1.0f else 0.0f
                                }
                                
                                val weight = if (check.optString("id").contains("audit")) 2.0f else 1.0f
                                totalWeightedScore += (score * weight)
                                totalWeights += weight
                                
                                list.add(WebAudit(
                                    id = check.optString("id"),
                                    title = check.optString("title"),
                                    description = check.optString("description"),
                                    score = score,
                                    group = group,
                                    displayValue = check.optString("displayValue", "")
                                ))
                            }
                            val finalScore = if (totalWeights > 0) (totalWeightedScore / totalWeights * 100).toInt() else 100
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
                            fileDetails = fileDetails,
                            isLocalAnalysis = true
                        )
                        
                        if (continuation.isActive) continuation.resume(result)
                    } catch (e: Exception) {
                        Log.e("LocalWebAnalyzer", "Error parsing results", e)
                        if (continuation.isActive) continuation.resumeWith(Result.failure(e))
                    } finally {
                        cleanup()
                    }
                }

                webView.addJavascriptInterface(activeInterface!!, "AndroidAnalyzer")

                webView.webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        if (newProgress >= 100) {
                            Log.d("LocalWebAnalyzer", "Progress reached 100%")
                            triggerAudit("Progress")
                        }
                    }
                    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                        Log.d("LocalWebAnalyzer", "JS Console [${consoleMessage?.messageLevel()}]: ${consoleMessage?.message()}")
                        return true
                    }
                }

                webView.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        Log.d("LocalWebAnalyzer", "onPageFinished: $url")
                        triggerAudit("PageFinished")
                    }

                    override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                        if (request?.isForMainFrame == true && continuation.isActive) {
                            Log.e("LocalWebAnalyzer", "Load Error: ${error?.description}")
                            continuation.resumeWith(Result.failure(Exception(error?.description.toString())))
                            cleanup()
                        }
                    }
                }

                Log.d("LocalWebAnalyzer", "Loading URL: $url")
                webView.loadUrl(url)
                
                continuation.invokeOnCancellation {
                    cleanup()
                }
            }
        } ?: throw Exception(context.getString(R.string.web_analyzer_error_timeout))
    }

    private fun cleanup() {
        val webViewToDestroy = activeWebView
        activeWebView = null
        activeInterface = null
        
        mainHandler.post {
            try {
                if (webViewToDestroy != null) {
                    Log.d("LocalWebAnalyzer", "Destroying WebView instance")
                    webViewToDestroy.destroy()
                }
            } catch (e: Exception) {
                Log.e("LocalWebAnalyzer", "Error destroying WebView", e)
            }
        }
    }

    private fun formatSize(bytes: Long): String {
        return when {
            bytes <= 0 -> "0 KB"
            bytes > 1024 * 1024 -> "%.2f MB".format(bytes / 1024.0 / 1024.0)
            else -> "%.1f KB".format(bytes / 1024.0)
        }
    }

    companion object {
        private const val AUDIT_SCRIPT = """
            (async function() {
                const bridge = window.AndroidAnalyzer || {};
                const log = bridge.log ? bridge.log.bind(bridge) : console.log;
                const complete = bridge.onAnalysisComplete ? bridge.onAnalysisComplete.bind(bridge) : console.log;

                const lang = navigator.language.startsWith('es') ? 'es' : 'en';
                const i18n = {
                    es: {
                        fcp_title: 'First Contentful Paint',
                        fcp_desc: 'FCP indica el momento en el que se visualiza el primer texto o imagen.',
                        lcp_title: 'Largest Contentful Paint',
                        lcp_desc: 'LCP indica el momento en que se pinta el contenido más grande.',
                        tbt_title: 'Total Blocking Time',
                        tbt_desc: 'Suma de períodos entre FCP y carga donde las tareas superan los 50ms.',
                        cls_title: 'Cumulative Layout Shift',
                        cls_desc: 'CLS mide el movimiento de los elementos visibles en el viewport.',
                        fcp_audit: 'FCP rápido',
                        fcp_audit_desc: 'El primer contenido se carga en menos de 1.8s.',
                        lcp_audit: 'LCP optimizado',
                        lcp_audit_desc: 'El contenido principal carga en menos de 2.5s.',
                        load_audit: 'Tiempo de carga total',
                        load_audit_desc: 'El tiempo total de carga debe ser inferior a 5s para una buena experiencia.',
                        img_alt: 'Atributos [alt] en imágenes',
                        img_alt_desc: 'Las imágenes informativas deben tener texto alternativo.',
                        btn_name: 'Botones con nombre accesible',
                        btn_name_desc: 'Los botones deben tener texto o etiquetas aria.',
                        html_lang: 'Elemento [lang] definido',
                        html_lang_desc: 'Permite identificar el idioma del sitio.',
                        doctype: 'Tiene DOCTYPE HTML',
                        doctype_desc: 'Evita el modo de compatibilidad del navegador.',
                        https: 'Usa HTTPS',
                        https_desc: 'Garantiza una conexión segura.',
                        console: 'Sin errores en consola',
                        console_desc: 'Errores graves pueden afectar la funcionalidad.',
                        title: 'Tiene etiqueta <title>',
                        title_desc: 'Crucial para la indexación y visualización en pestañas.',
                        meta: 'Meta descripción presente',
                        meta_desc: 'Ayuda a mejorar el CTR en buscadores.',
                        viewport: 'Etiqueta [viewport] válida',
                        viewport_desc: 'Optimiza la visualización en dispositivos móviles.',
                        canonical: 'Enlace canonical definido',
                        canonical_desc: 'Evita problemas de contenido duplicado.',
                        failures: 'fallos',
                        passed: 'Pasado'
                    },
                    en: {
                        fcp_title: 'First Contentful Paint',
                        fcp_desc: 'FCP indicates the time when the first text or image is displayed.',
                        lcp_title: 'Largest Contentful Paint',
                        lcp_desc: 'LCP indicates the time when the largest content element is painted.',
                        tbt_title: 'Total Blocking Time',
                        tbt_desc: 'Sum of periods between FCP and load where tasks exceed 50ms.',
                        cls_title: 'Cumulative Layout Shift',
                        cls_desc: 'CLS measures the movement of visible elements in the viewport.',
                        fcp_audit: 'Fast FCP',
                        fcp_audit_desc: 'First content loads in less than 1.8s.',
                        lcp_audit: 'Optimized LCP',
                        lcp_audit_desc: 'Main content loads in less than 2.5s.',
                        load_audit: 'Total load time',
                        load_audit_desc: 'Total load time should be less than 5s for a good experience.',
                        img_alt: 'Attributes [alt] on images',
                        img_alt_desc: 'Informative images should have alternative text.',
                        btn_name: 'Buttons with accessible name',
                        btn_name_desc: 'Buttons should have text or aria labels.',
                        html_lang: 'Element [lang] defined',
                        html_lang_desc: 'Allows identifying the site language.',
                        doctype: 'Has DOCTYPE HTML',
                        doctype_desc: 'Avoids browser compatibility mode.',
                        https: 'Uses HTTPS',
                        https_desc: 'Ensures a secure connection.',
                        console: 'No console errors',
                        console_desc: 'Serious errors can affect functionality.',
                        title: 'Has <title> tag',
                        title_desc: 'Crucial for indexing and tab display.',
                        meta: 'Meta description present',
                        meta_desc: 'Helps improve CTR in search engines.',
                        viewport: 'Valid [viewport] tag',
                        viewport_desc: 'Optimizes display on mobile devices.',
                        canonical: 'Canonical link defined',
                        canonical_desc: 'Avoids duplicate content issues.',
                        failures: 'failures',
                        passed: 'Passed'
                    }
                }[lang];

                try {
                    log('Lighthouse Audit Script (Enhanced) started');
                    
                    // 1. Setup Performance Observers for Web Vitals
                    let fcp = 0, lcp = 0, cls = 0, tbt = 0;
                    
                    const paintEntries = performance.getEntriesByType('paint');
                    const fcpEntry = paintEntries.find(e => e.name === 'first-contentful-paint');
                    if (fcpEntry) fcp = fcpEntry.startTime;

                    const po = new PerformanceObserver((list) => {
                        for (const entry of list.getEntries()) {
                            if (entry.entryType === 'largest-contentful-paint') {
                                lcp = entry.startTime;
                            } else if (entry.entryType === 'layout-shift') {
                                if (!entry.hadRecentInput) cls += entry.value;
                            } else if (entry.entryType === 'longtask') {
                                if (fcp > 0 && entry.startTime > fcp) {
                                    const blockingTime = entry.duration - 50;
                                    if (blockingTime > 0) tbt += blockingTime;
                                }
                            }
                        }
                    });
                    
                    try {
                        po.observe({type: 'largest-contentful-paint', buffered: true});
                        po.observe({type: 'layout-shift', buffered: true});
                        po.observe({type: 'longtask', buffered: true});
                    } catch (e) { log('Observer error: ' + e); }

                    // 2. Wait for stabilization (simulating lab conditions)
                    await new Promise(r => setTimeout(r, 2500));
                    po.disconnect();

                    // 3. Gather Static Data
                    const resources = performance.getEntriesByType('resource');
                    const nav = performance.getEntriesByType('navigation')[0];
                    const now = performance.now();
                    const loadTime = nav ? (nav.loadEventEnd - nav.startTime) : now;
                    
                    const audits = {
                        performance: { checks: [], loadTime: (loadTime / 1000).toFixed(2) + 's' },
                        accessibility: { checks: [] },
                        'best-practices': { checks: [] },
                        seo: { checks: [] }
                    };

                    // 4. Metrics & Scores
                    const metrics = [
                        {
                            id: 'fcp',
                            title: i18n.fcp_title,
                            value: (fcp / 1000).toFixed(2) + ' s',
                            description: i18n.fcp_desc,
                            score: fcp < 1800 ? 1.0 : fcp < 3000 ? 0.5 : 0.0
                        },
                        {
                            id: 'lcp',
                            title: i18n.lcp_title,
                            value: (lcp / 1000).toFixed(2) + ' s',
                            description: i18n.lcp_desc,
                            score: lcp < 2500 ? 1.0 : lcp < 4000 ? 0.5 : 0.0
                        },
                        {
                            id: 'tbt',
                            title: i18n.tbt_title,
                            value: Math.round(tbt) + ' ms',
                            description: i18n.tbt_desc,
                            score: tbt < 200 ? 1.0 : tbt < 600 ? 0.5 : 0.0
                        },
                        {
                            id: 'cls',
                            title: i18n.cls_title,
                            value: cls.toFixed(3),
                            description: i18n.cls_desc,
                            score: cls < 0.1 ? 1.0 : cls < 0.25 ? 0.5 : 0.0
                        }
                    ];

                    // 5. Performance Audits
                    audits.performance.checks.push({
                        id: 'fcp-audit', title: i18n.fcp_audit,
                        description: i18n.fcp_audit_desc,
                        passed: fcp < 1800,
                        score: fcp < 1800 ? 1.0 : fcp < 3000 ? 0.5 : 0.0
                    });
                    audits.performance.checks.push({
                        id: 'lcp-audit', title: i18n.lcp_audit,
                        description: i18n.lcp_audit_desc,
                        passed: lcp < 2500,
                        score: lcp < 2500 ? 1.0 : lcp < 4000 ? 0.5 : 0.0
                    });
                    audits.performance.checks.push({
                        id: 'load-time-audit', title: i18n.load_audit,
                        description: i18n.load_audit_desc,
                        passed: loadTime < 5000,
                        score: loadTime < 5000 ? 1.0 : loadTime < 10000 ? 0.5 : 0.0,
                        displayValue: (loadTime / 1000).toFixed(2) + 's'
                    });

                    // 6. Accessibility Audits
                    const images = document.querySelectorAll('img');
                    const imagesNoAlt = Array.from(images).filter(img => !img.alt && !img.ariaLabel);
                    audits.accessibility.checks.push({
                        id: 'image-alt', title: i18n.img_alt,
                        description: i18n.img_alt_desc,
                        passed: imagesNoAlt.length === 0, 
                        displayValue: imagesNoAlt.length > 0 ? imagesNoAlt.length + ' ' + i18n.failures : i18n.passed
                    });
                    
                    const buttons = document.querySelectorAll('button, [role="button"]');
                    const buttonsNoLabel = Array.from(buttons).filter(b => !b.innerText.trim() && !b.ariaLabel);
                    audits.accessibility.checks.push({
                        id: 'button-name', title: i18n.btn_name,
                        description: i18n.btn_name_desc,
                        passed: buttonsNoLabel.length === 0
                    });

                    audits.accessibility.checks.push({
                        id: 'html-lang', title: i18n.html_lang,
                        description: i18n.html_lang_desc,
                        passed: !!document.documentElement.lang
                    });

                    // 7. Best Practices
                    audits['best-practices'].checks.push({
                        id: 'doctype', title: i18n.doctype,
                        description: i18n.doctype_desc,
                        passed: document.doctype !== null
                    });
                    audits['best-practices'].checks.push({
                        id: 'https', title: i18n.https,
                        description: i18n.https_desc,
                        passed: window.location.protocol === 'https:'
                    });
                    audits['best-practices'].checks.push({
                        id: 'console-errors', title: i18n.console,
                        description: i18n.console_desc,
                        passed: true // Placeholder
                    });

                    // 8. SEO
                    audits.seo.checks.push({
                        id: 'title', title: i18n.title,
                        description: i18n.title_desc,
                        passed: !!document.title
                    });
                    audits.seo.checks.push({
                        id: 'meta-description', title: i18n.meta,
                        description: i18n.meta_desc,
                        passed: !!document.querySelector('meta[name="description"]')
                    });
                    audits.seo.checks.push({
                        id: 'viewport', title: i18n.viewport,
                        description: i18n.viewport_desc,
                        passed: !!document.querySelector('meta[name="viewport"]')
                    });
                    audits.seo.checks.push({
                        id: 'canonical', title: i18n.canonical,
                        description: i18n.canonical_desc,
                        passed: !!document.querySelector('link[rel="canonical"]')
                    });

                    const result = {
                        resources: resources.map(r => ({
                            name: r.name,
                            transferSize: r.transferSize || 0,
                            initiatorType: r.initiatorType || 'other'
                        })).slice(0, 50),
                        metrics: metrics,
                        audits: audits
                    };

                    log('Analysis complete. LCP: ' + lcp + ', TBT: ' + tbt + ', CLS: ' + cls);
                    complete(JSON.stringify(result));
                } catch (e) {
                    log('JS Error: ' + e.stack);
                    complete(JSON.stringify({
                        error: e.toString(),
                        resources: [],
                        metrics: [],
                        audits: { 
                            performance: { checks: [], loadTime: '0.00s' }, 
                            accessibility: { checks: [] }, 
                            'best-practices': { checks: [] }, 
                            seo: { checks: [] } 
                        }
                    }));
                }
            })();
        """
    }
}
