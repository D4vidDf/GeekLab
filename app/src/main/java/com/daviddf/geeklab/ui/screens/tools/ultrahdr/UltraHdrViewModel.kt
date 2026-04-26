package com.daviddf.geeklab.ui.screens.tools.ultrahdr

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.display.DisplayManager
import android.net.Uri
import android.os.Build
import android.view.Display
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.daviddf.geeklab.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.URL
import java.util.function.Consumer

data class GainmapMetadata(
    val ratioMin: FloatArray,
    val ratioMax: FloatArray,
    val gamma: FloatArray,
    val epsilonSdr: FloatArray,
    val epsilonHdr: FloatArray,
    val displayRatioSdr: Float,
    val displayRatioHdr: Float
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GainmapMetadata

        if (displayRatioSdr != other.displayRatioSdr) return false
        if (displayRatioHdr != other.displayRatioHdr) return false
        if (!ratioMin.contentEquals(other.ratioMin)) return false
        if (!ratioMax.contentEquals(other.ratioMax)) return false
        if (!gamma.contentEquals(other.gamma)) return false
        if (!epsilonSdr.contentEquals(other.epsilonSdr)) return false
        if (!epsilonHdr.contentEquals(other.epsilonHdr)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = displayRatioSdr.hashCode()
        result = 31 * result + displayRatioHdr.hashCode()
        result = 31 * result + ratioMin.contentHashCode()
        result = 31 * result + ratioMax.contentHashCode()
        result = 31 * result + gamma.contentHashCode()
        result = 31 * result + epsilonSdr.contentHashCode()
        result = 31 * result + epsilonHdr.contentHashCode()
        return result
    }
}

enum class UltraHdrViewMode {
    HDR, SDR, GAINMAP, SHADER_SIMULATION
}

data class UltraHdrState(
    val imageBitmap: Bitmap? = null,
    val gainmapBitmap: Bitmap? = null,
    val hasGainmap: Boolean = false,
    val gainmapMetadata: GainmapMetadata? = null,
    val isHdrPreviewEnabled: Boolean = true,
    val currentDisplayRatio: Float = 1.0f,
    val simulationRatio: Float = 1.0f,
    val viewMode: UltraHdrViewMode = UltraHdrViewMode.HDR,
    val exampleIndex: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

class UltraHdrViewModel(application: Application) : AndroidViewModel(application) {
    private val _internalState = MutableStateFlow(UltraHdrState())
    
    private val exampleImages = listOf(
        "https://raw.githubusercontent.com/android/platform-samples/main/samples/graphics/ultrahdr/src/main/assets/ultrahdr/ultrahdr_cityscape.jpg",
        "https://raw.githubusercontent.com/android/platform-samples/main/samples/graphics/ultrahdr/src/main/assets/gainmaps/grand_canyon.jpg",
        "https://raw.githubusercontent.com/android/platform-samples/main/samples/graphics/ultrahdr/src/main/assets/gainmaps/train_station_night.jpg"
    )

    private val hdrSdrRatioFlow = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        callbackFlow {
            val displayManager = application.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
            val display = displayManager.getDisplay(Display.DEFAULT_DISPLAY)
            
            if (display != null && display.isHdrSdrRatioAvailable) {
                val listener = Consumer<Display> { d ->
                    trySend(d.hdrSdrRatio)
                }
                
                display.registerHdrSdrRatioChangedListener(application.mainExecutor, listener)
                trySend(display.hdrSdrRatio)
                
                awaitClose {
                    display.unregisterHdrSdrRatioChangedListener(listener)
                }
            } else {
                trySend(1.0f)
                awaitClose { }
            }
        }
    } else {
        MutableStateFlow(1.0f)
    }

    val uiState: StateFlow<UltraHdrState> = combine(_internalState, hdrSdrRatioFlow) { state, ratio ->
        state.copy(
            currentDisplayRatio = ratio,
            simulationRatio = if (state.viewMode == UltraHdrViewMode.SHADER_SIMULATION) state.simulationRatio else ratio
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UltraHdrState())

    fun toggleHdrPreview(enabled: Boolean) {
        _internalState.value = _internalState.value.copy(isHdrPreviewEnabled = enabled)
    }

    fun setViewMode(mode: UltraHdrViewMode) {
        _internalState.value = _internalState.value.copy(viewMode = mode)
    }

    fun setSimulationRatio(ratio: Float) {
        _internalState.value = _internalState.value.copy(simulationRatio = ratio)
    }

    fun loadImageFromUri(uri: Uri) {
        viewModelScope.launch {
            _internalState.value = _internalState.value.copy(isLoading = true, error = null)
            try {
                val bitmap = withContext(Dispatchers.IO) {
                    val inputStream: InputStream? = getApplication<Application>().contentResolver.openInputStream(uri)
                    BitmapFactory.decodeStream(inputStream)
                }
                processBitmap(bitmap)
            } catch (_: Exception) {
                _internalState.value = _internalState.value.copy(isLoading = false, error = getApplication<Application>().getString(R.string.error_load_image))
            }
        }
    }

    fun loadExampleImage() {
        viewModelScope.launch {
            val currentIndex = _internalState.value.exampleIndex
            val nextIndex = (currentIndex + 1) % exampleImages.size
            _internalState.value = _internalState.value.copy(isLoading = true, error = null, exampleIndex = nextIndex)
            
            try {
                val exampleUrl = exampleImages[nextIndex]
                val bitmap = withContext(Dispatchers.IO) {
                    val url = URL(exampleUrl)
                    val connection = url.openConnection()
                    connection.connect()
                    val inputStream = connection.getInputStream()
                    BitmapFactory.decodeStream(inputStream)
                }
                processBitmap(bitmap)
            } catch (_: Exception) {
                _internalState.value = _internalState.value.copy(isLoading = false, error = getApplication<Application>().getString(R.string.error_load_image))
            }
        }
    }

    private fun processBitmap(bitmap: Bitmap?) {
        if (bitmap == null) {
            _internalState.value = _internalState.value.copy(isLoading = false, error = getApplication<Application>().getString(R.string.error_load_image))
            return
        }

        var hasGainmap = false
        var metadata: GainmapMetadata? = null
        var gainmapBitmap: Bitmap? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            hasGainmap = bitmap.hasGainmap()
            if (hasGainmap) {
                val gainmap = bitmap.gainmap
                if (gainmap != null) {
                    gainmapBitmap = gainmap.gainmapContents
                    metadata = GainmapMetadata(
                        ratioMin = gainmap.ratioMin,
                        ratioMax = gainmap.ratioMax,
                        gamma = gainmap.gamma,
                        epsilonSdr = gainmap.epsilonSdr,
                        epsilonHdr = gainmap.epsilonHdr,
                        displayRatioSdr = gainmap.minDisplayRatioForHdrTransition,
                        displayRatioHdr = gainmap.displayRatioForFullHdr
                    )
                }
            }
        }

        _internalState.value = _internalState.value.copy(
            imageBitmap = bitmap,
            gainmapBitmap = gainmapBitmap,
            hasGainmap = hasGainmap,
            gainmapMetadata = metadata,
            isLoading = false
        )
    }
}
