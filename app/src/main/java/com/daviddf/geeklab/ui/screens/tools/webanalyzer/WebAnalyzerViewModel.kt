package com.daviddf.geeklab.ui.screens.tools.webanalyzer

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import com.daviddf.geeklab.R
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Scanner
import androidx.core.content.edit

enum class AnalysisMethod {
    LOCAL, ANONYMOUS, API_KEY
}

enum class AnalysisStrategy {
    MOBILE, DESKTOP
}

data class WebOpportunity(
    val title: String,
    val description: String,
    val score: Float,
    val displayValue: String = ""
)

data class WebFileDetail(
    val name: String,
    val size: String,
    val type: String
)

data class WebAudit(
    val id: String,
    val title: String,
    val description: String,
    val score: Float?,
    val displayValue: String = "",
    val group: String = "",
)

data class WebMetric(
    val id: String,
    val title: String,
    val value: String,
    val description: String,
    val score: Float
)

data class CategoryInfo(
    val title: String,
    val score: Int,
    val audits: List<WebAudit>,
)

data class WebAnalysisResult(
    val url: String = "",
    val performance: CategoryInfo = CategoryInfo("Performance", 0, emptyList()),
    val accessibility: CategoryInfo = CategoryInfo("Accessibility", 0, emptyList()),
    val bestPractices: CategoryInfo = CategoryInfo("Best Practices", 0, emptyList()),
    val seo: CategoryInfo = CategoryInfo("SEO", 0, emptyList()),
    val totalSize: String = "0 MB",
    val loadTime: String = "0s",
    val requests: Int = 0,
    val metrics: List<WebMetric> = emptyList(),
    val opportunities: List<WebOpportunity> = emptyList(),
    val fileDetails: List<WebFileDetail> = emptyList(),
    val screenshotBase64: String? = null,
    val isLocalAnalysis: Boolean = false
)

data class WebAnalyzerUiState(
    val urlInput: String = "",
    val apiKey: String = "",
    val selectedMethod: AnalysisMethod = AnalysisMethod.LOCAL,
    val selectedStrategy: AnalysisStrategy = AnalysisStrategy.MOBILE,
    val isLoading: Boolean = false,
    val statusMessage: String = "",
    val result: WebAnalysisResult? = null,
    val error: String? = null,
    val isQuotaError: Boolean = false,
    val saveSettings: Boolean = false
)

class WebAnalyzerViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(WebAnalyzerUiState())
    val uiState: StateFlow<WebAnalyzerUiState> = _uiState.asStateFlow()

    private val localAnalyzer = LocalWebAnalyzer(application)

    private val masterKey = MasterKey.Builder(application)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPrefs: SharedPreferences = EncryptedSharedPreferences.create(
        application,
        "web_analyzer_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    init {
        loadSettings()
    }

    private fun loadSettings() {
        val shouldSave = sharedPrefs.getBoolean("save_settings", false)
        val savedApiKey = sharedPrefs.getString("api_key", "") ?: ""
        val savedMethodName = sharedPrefs.getString("analysis_method", AnalysisMethod.LOCAL.name) ?: AnalysisMethod.LOCAL.name
        val savedStrategyName = sharedPrefs.getString("analysis_strategy", AnalysisStrategy.MOBILE.name) ?: AnalysisStrategy.MOBILE.name

        if (shouldSave) {
            _uiState.update { 
                it.copy(
                    apiKey = savedApiKey,
                    selectedMethod = try { AnalysisMethod.valueOf(savedMethodName) } catch (_: Exception) { AnalysisMethod.LOCAL },
                    selectedStrategy = try { AnalysisStrategy.valueOf(savedStrategyName) } catch (_: Exception) { AnalysisStrategy.MOBILE },
                    saveSettings = true
                )
            }
        }
    }

    fun onUrlChange(newUrl: String) {
        _uiState.update { it.copy(urlInput = newUrl, error = null) }
    }

    fun onApiKeyChange(newKey: String) {
        _uiState.update { it.copy(apiKey = newKey) }
        if (_uiState.value.saveSettings) {
            sharedPrefs.edit {putString("api_key", newKey)}
        }
    }

    fun onMethodSelect(method: AnalysisMethod) {
        _uiState.update { it.copy(selectedMethod = method) }
        if (_uiState.value.saveSettings) {
            sharedPrefs.edit { putString("analysis_method", method.name) }
        }
    }

    fun onStrategySelect(strategy: AnalysisStrategy) {
        _uiState.update { it.copy(selectedStrategy = strategy) }
        if (_uiState.value.saveSettings) {
            sharedPrefs.edit { putString("analysis_strategy", strategy.name) }
        }
    }

    fun onSaveSettingsChange(save: Boolean) {
        _uiState.update { it.copy(saveSettings = save) }
        sharedPrefs.edit { putBoolean("save_settings", save) }

        sharedPrefs.edit {
            if (save) {
                putString("api_key", _uiState.value.apiKey)
                putString("analysis_method", _uiState.value.selectedMethod.name)
                putString("analysis_strategy", _uiState.value.selectedStrategy.name)
            } else {
                remove("api_key")
                remove("analysis_method")
                remove("analysis_strategy")
            }
        }
    }

    fun analyzeUrl() {
        val inputUrl = _uiState.value.urlInput
        if (inputUrl.isBlank()) {
            _uiState.update { it.copy(error = getApplication<Application>().getString(R.string.web_analyzer_error_empty_url)) }
            return
        }

        if (_uiState.value.isLoading) return

        val formattedUrl = if (!inputUrl.startsWith("http")) "https://$inputUrl" else inputUrl

        viewModelScope.launch {
            val app = getApplication<Application>()
            _uiState.update { it.copy(isLoading = true, error = null, result = null, isQuotaError = false, statusMessage = app.getString(R.string.web_analyzer_status_starting)) }
            
            try {
                val result = if (_uiState.value.selectedMethod == AnalysisMethod.LOCAL) {
                    _uiState.update { it.copy(statusMessage = app.getString(R.string.web_analyzer_status_local)) }
                    localAnalyzer.analyze(formattedUrl, _uiState.value.selectedStrategy)
                } else {
                    performLighthouseAnalysis(formattedUrl)
                }
                _uiState.update { it.copy(isLoading = false, result = result) }
            } catch (e: Exception) {
                val isQuota = e.message?.contains("429") == true || e.message?.contains("quota") == true
                val errorMessage = if (isQuota) {
                    app.getString(R.string.web_analyzer_error_quota)
                } else {
                    e.message ?: app.getString(R.string.web_analyzer_error_failed)
                }
                _uiState.update { it.copy(isLoading = false, error = errorMessage, isQuotaError = isQuota) }
            }
        }
    }

    private suspend fun performLighthouseAnalysis(targetUrl: String): WebAnalysisResult = withContext(Dispatchers.IO) {
        val encodedUrl = URLEncoder.encode(targetUrl, "UTF-8")
        val apiKey = _uiState.value.apiKey
        val strategy = _uiState.value.selectedStrategy.name.lowercase()
        val app = getApplication<Application>()
        val locale = java.util.Locale.getDefault().language
        
        // Official PSI API v5 endpoint
        val apiUrl = buildString {
            append("https://www.googleapis.com/pagespeedonline/v5/runPagespeed")
            append("?url=$encodedUrl")
            append("&category=performance&category=accessibility&category=best-practices&category=seo")
            append("&strategy=$strategy")
            append("&locale=$locale")
            if (apiKey.isNotBlank()) append("&key=$apiKey")
        }
        
        _uiState.update { it.copy(statusMessage = app.getString(R.string.web_analyzer_status_psi_connecting)) }
        
        val connection = URL(apiUrl).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 60000
        connection.readTimeout = 60000

        val responseCode = connection.responseCode
        if (responseCode != 200) {
            val errorText = connection.errorStream?.bufferedReader()?.readText() ?: ""
            throw Exception(errorText.ifBlank { app.getString(R.string.web_analyzer_error_http, responseCode) })
        }

        _uiState.update { it.copy(statusMessage = app.getString(R.string.web_analyzer_status_extracting)) }
        
        val response = Scanner(connection.inputStream).useDelimiter("\\A").next()
        val json = JSONObject(response)
        val lighthouseResult = json.getJSONObject("lighthouseResult")
        val categories = lighthouseResult.getJSONObject("categories")
        val auditsJson = lighthouseResult.getJSONObject("audits")

        fun parseCategory(key: String, title: String): CategoryInfo {
            if (!categories.has(key)) return CategoryInfo(title, 0, emptyList())
            val catObj = categories.getJSONObject(key)
            val score = (catObj.optDouble("score", 0.0) * 100).toInt()
            val auditRefs = catObj.getJSONArray("auditRefs")
            val auditsList = mutableListOf<WebAudit>()
            
            for (i in 0 until auditRefs.length()) {
                val ref = auditRefs.getJSONObject(i)
                val auditId = ref.getString("id")
                val group = ref.optString("group", "")
                
                if (auditsJson.has(auditId)) {
                    val auditObj = auditsJson.getJSONObject(auditId)
                    val auditScore = auditObj.optDouble("score", -1.0)
                    auditsList.add(WebAudit(
                        id = auditId,
                        title = auditObj.getString("title"),
                        description = auditObj.optString("description", ""),
                        score = if (auditScore == -1.0) null else auditScore.toFloat(),
                        displayValue = auditObj.optString("displayValue", ""),
                        group = group
                    ))
                }
            }
            return CategoryInfo(title, score, auditsList)
        }

        val performance = parseCategory("performance", "Performance")
        val accessibility = parseCategory("accessibility", "Accessibility")
        val bestPractices = parseCategory("best-practices", "Best Practices")
        val seo = parseCategory("seo", "SEO")

        // Parse Core Web Vitals and Metrics
        val metricsList = mutableListOf<WebMetric>()
        val metricKeys = listOf(
            "first-contentful-paint" to "First Contentful Paint",
            "largest-contentful-paint" to "Largest Contentful Paint",
            "total-blocking-time" to "Total Blocking Time",
            "cumulative-layout-shift" to "Cumulative Layout Shift",
            "speed-index" to "Speed Index"
        )

        metricKeys.forEach { (key, title) ->
            if (auditsJson.has(key)) {
                val audit = auditsJson.getJSONObject(key)
                metricsList.add(WebMetric(
                    id = key,
                    title = title,
                    value = audit.optString("displayValue", "N/A"),
                    description = audit.optString("description", ""),
                    score = audit.optDouble("score", 0.0).toFloat()
                ))
            }
        }

        // Parse Opportunities
        val opportunities = mutableListOf<WebOpportunity>()
        val oppKeys = listOf("render-blocking-resources", "modern-image-formats", "unused-javascript", "unminified-css", "offscreen-images")
        oppKeys.forEach { key ->
            if (auditsJson.has(key)) {
                val audit = auditsJson.getJSONObject(key)
                val score = audit.optDouble("score", 1.0).toFloat()
                if (score < 0.9f) {
                    opportunities.add(WebOpportunity(
                        title = audit.getString("title"),
                        description = audit.optString("description", ""),
                        score = score,
                        displayValue = audit.optString("displayValue", "")
                    ))
                }
            }
        }

        // Parse Technical Details
        val totalByteWeight = auditsJson.optJSONObject("total-byte-weight")?.optLong("numericValue", 0L) ?: 0L
        val interactive = auditsJson.optJSONObject("interactive")?.optString("displayValue", "N/A") ?: "N/A"
        val networkRequests = auditsJson.optJSONObject("network-requests")?.optInt("numericValue", 0) ?: 0

        // Parse Main Resources
        val fileDetails = mutableListOf<WebFileDetail>()
        val networkAudit = auditsJson.optJSONObject("network-requests")
        if (networkAudit != null && networkAudit.has("details")) {
            val details = networkAudit.getJSONObject("details")
            if (details.has("items")) {
                val items = details.getJSONArray("items")
                for (i in 0 until minOf(items.length(), 10)) {
                    val item = items.getJSONObject(i)
                    val urlStr = item.getString("url")
                    val size = item.optLong("transferSize", 0L)
                    val type = item.optString("resourceType", "Other")
                    fileDetails.add(WebFileDetail(
                        name = urlStr.substringAfterLast("/").substringBefore("?").ifBlank { "Resource $i" },
                        size = if (size > 1024 * 1024) (size / 1024.0 / 1024.0).format(2) + " MB" else (size / 1024.0).format(1) + " KB",
                        type = type
                    ))
                }
            }
        }

        val screenshot = auditsJson.optJSONObject("final-screenshot")
            ?.optJSONObject("details")
            ?.optString("data")

        WebAnalysisResult(
            url = targetUrl,
            performance = performance,
            accessibility = accessibility,
            bestPractices = bestPractices,
            seo = seo,
            totalSize = if (totalByteWeight > 1024 * 1024) (totalByteWeight / 1024.0 / 1024.0).format(2) + " MB" else (totalByteWeight / 1024.0).format(1) + " KB",
            loadTime = interactive,
            requests = networkRequests,
            metrics = metricsList,
            opportunities = opportunities,
            fileDetails = fileDetails,
            screenshotBase64 = screenshot,
            isLocalAnalysis = false
        )
    }

    private fun Double.format(digits: Int) = "%.${digits}f".format(this)
}
