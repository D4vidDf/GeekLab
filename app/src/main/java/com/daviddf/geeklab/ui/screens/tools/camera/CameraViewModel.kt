package com.daviddf.geeklab.ui.screens.tools.camera

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.util.SizeF
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daviddf.geeklab.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CameraInfo(
    val id: String,
    val facingResId: Int,
    val sensorSize: SizeF?,
    val resolution: String,
    val megapixels: Double?,
    val physicalResolution: String?,
    val physicalMegapixels: Double?,
    val focalLengths: List<Float>,
    val aperture: Float?,
    val isoRange: String,
    val hasFlash: Boolean,
    val capabilities: List<String>,
    val hardwareLevelResId: Int,
    val isPhysical: Boolean = false,
    val parentLogicalId: String? = null
)

data class CameraUiState(
    val cameras: List<CameraInfo> = emptyList(),
    val isLoading: Boolean = true
)

class CameraViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    fun loadCameraInfo(context: Context) {
        viewModelScope.launch {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraList = mutableListOf<CameraInfo>()

            try {
                cameraManager.cameraIdList.forEach { logicalId ->
                    val logicalChars = cameraManager.getCameraCharacteristics(logicalId)
                    
                    // Add the logical camera
                    cameraList.add(extractInfo(logicalId, logicalChars))

                    // Check for physical cameras (Android 9+)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val physicalIds = logicalChars.physicalCameraIds
                        physicalIds.forEach { physicalId ->
                            try {
                                val physicalChars = cameraManager.getCameraCharacteristics(physicalId)
                                cameraList.add(extractInfo(physicalId, physicalChars, isPhysical = true, parentId = logicalId))
                            } catch (_: Exception) {}
                        }
                    }
                }
            } catch (_: Exception) {
                // Log error
            }

            // Remove duplicates (if any physical ID was also exposed as logical)
            val distinctCameras = cameraList.distinctBy { it.id + (it.parentLogicalId ?: "") }
            _uiState.value = CameraUiState(cameras = distinctCameras, isLoading = false)
        }
    }

    private fun extractInfo(id: String, chars: CameraCharacteristics, isPhysical: Boolean = false, parentId: String? = null): CameraInfo {
        val facingResId = when (chars.get(CameraCharacteristics.LENS_FACING)) {
            CameraCharacteristics.LENS_FACING_BACK -> R.string.camera_facing_back
            CameraCharacteristics.LENS_FACING_FRONT -> R.string.camera_facing_front
            CameraCharacteristics.LENS_FACING_EXTERNAL -> R.string.camera_facing_external
            else -> R.string.unknown
        }

        val sensorSize = chars.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)
        
        // 1. Default (Binned) Resolution
        val streamMap = chars.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        val maxBinnedSize = streamMap?.getOutputSizes(android.graphics.ImageFormat.JPEG)?.maxByOrNull { it.width * it.height }
        val resolution = maxBinnedSize?.let { "${it.width}x${it.height}" } ?: "Unknown"
        val megapixels = maxBinnedSize?.let { (it.width.toDouble() * it.height.toDouble()) / 1_000_000.0 }

        // 2. Physical / Ultra-High Resolution
        val pixelArray = chars.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE)
        var physicalRes = pixelArray?.let { "${it.width}x${it.height}" }
        var physicalMP = pixelArray?.let { (it.width.toDouble() * it.height.toDouble()) / 1_000_000.0 }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val highResMap = chars.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP_MAXIMUM_RESOLUTION)
            val maxHighResSize = highResMap?.getOutputSizes(android.graphics.ImageFormat.JPEG)?.maxByOrNull { it.width * it.height }
            if (maxHighResSize != null) {
                physicalRes = "${maxHighResSize.width}x${maxHighResSize.height}"
                physicalMP = (maxHighResSize.width.toDouble() * maxHighResSize.height.toDouble()) / 1_000_000.0
            }
        }

        // Hardware Level
        val hardwareLevelResId = when (chars.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)) {
            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY -> R.string.camera_level_legacy
            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED -> R.string.camera_level_limited
            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL -> R.string.camera_level_full
            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3 -> R.string.camera_level_level3
            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_EXTERNAL -> R.string.camera_level_external
            else -> R.string.unknown
        }

        val focalLengths = chars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)?.toList() ?: emptyList()
        val aperture = chars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES)?.firstOrNull()
        
        val isoRange = chars.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)?.let {
            "${it.lower} - ${it.upper}"
        } ?: "N/A"

        val hasFlash = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
        
        val capabilities = chars.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)?.map {
            when(it) {
                CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_BACKWARD_COMPATIBLE -> "Backward Compatible"
                CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR -> "Manual Sensor"
                CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_POST_PROCESSING -> "Manual Post-processing"
                CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_RAW -> "RAW"
                CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_PRIVATE_REPROCESSING -> "Private Reprocessing"
                CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_READ_SENSOR_SETTINGS -> "Read Sensor Settings"
                CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_BURST_CAPTURE -> "Burst Capture"
                CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_YUV_REPROCESSING -> "YUV Reprocessing"
                CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_DEPTH_OUTPUT -> "Depth Output"
                CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_CONSTRAINED_HIGH_SPEED_VIDEO -> "High Speed Video"
                CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_MOTION_TRACKING -> "Motion Tracking"
                CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_LOGICAL_MULTI_CAMERA -> "Logical Multi-camera"
                CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_MONOCHROME -> "Monochrome"
                CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_SECURE_IMAGE_DATA -> "Secure Image Data"
                14 -> "System Camera"
                15 -> "Offline Processing"
                16 -> "Ultra High Resolution Sensor"
                17 -> "Remosaic Reprocessing"
                18 -> "10-bit Dynamic Range"
                19 -> "Stream Use Case"
                20 -> "Color Space Profiles"
                else -> "Other ($it)"
            }
        } ?: emptyList()

        return CameraInfo(
            id = id,
            facingResId = facingResId,
            sensorSize = sensorSize,
            resolution = resolution,
            megapixels = megapixels,
            physicalResolution = physicalRes,
            physicalMegapixels = physicalMP,
            focalLengths = focalLengths,
            aperture = aperture,
            isoRange = isoRange,
            hasFlash = hasFlash,
            capabilities = capabilities,
            hardwareLevelResId = hardwareLevelResId,
            isPhysical = isPhysical,
            parentLogicalId = parentId
        )
    }
}
