package com.daviddf.geeklab.ui.screens.tools.camera

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.hardware.camera2.CaptureRequest
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.view.Surface
import android.widget.Toast
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.*
import androidx.camera.extensions.ExtensionMode
import androidx.camera.extensions.ExtensionsManager
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.video.VideoCapture
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.core.content.ContextCompat
import com.daviddf.geeklab.R

enum class CameraMode {
    PHOTO, VIDEO
}

data class CameraXUiState(
    val lensFacing: Int = CameraSelector.LENS_FACING_BACK,
    val zoomRatio: Float = 1f,
    val minZoom: Float = 1f,
    val maxZoom: Float = 1f,
    val isCapturing: Boolean = false,
    val isRecording: Boolean = false,
    val currentEffect: Int = CaptureRequest.CONTROL_EFFECT_MODE_OFF,
    val aspectRatio: Int = AspectRatio.RATIO_4_3,
    val extensionMode: Int = ExtensionMode.NONE,
    val supportedExtensions: List<Int> = emptyList(),
    val cameraMode: CameraMode = CameraMode.PHOTO
)

class CameraXViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CameraXUiState())
    val uiState: StateFlow<CameraXUiState> = _uiState.asStateFlow()

    private var cameraProvider: ProcessCameraProvider? = null
    private var extensionsManager: ExtensionsManager? = null
    private var camera: Camera? = null
    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var activeRecording: Recording? = null
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    @androidx.annotation.OptIn(ExperimentalCamera2Interop::class)
    @OptIn(ExperimentalCamera2Interop::class)
    fun setupCamera(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView
    ) {
        viewModelScope.launch {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            
            cameraProviderFuture.addListener({
                cameraProvider = cameraProviderFuture.get()
                
                val extensionsManagerFuture = ExtensionsManager.getInstanceAsync(context, cameraProvider!!)
                extensionsManagerFuture.addListener({
                    extensionsManager = extensionsManagerFuture.get()
                    updateSupportedExtensions()
                    bindCameraUseCases(lifecycleOwner, previewView)
                }, ContextCompat.getMainExecutor(context))
                
            }, ContextCompat.getMainExecutor(context))
        }
    }

    private fun updateSupportedExtensions() {
        val manager = extensionsManager ?: return
        
        val baseSelector = CameraSelector.Builder()
            .requireLensFacing(_uiState.value.lensFacing)
            .build()
            
        val modes = mutableListOf<Int>()
        val possibleModes = listOf(
            ExtensionMode.AUTO,
            ExtensionMode.HDR,
            ExtensionMode.NIGHT,
            ExtensionMode.BOKEH,
            ExtensionMode.FACE_RETOUCH
        )
        
        for (mode in possibleModes) {
            if (manager.isExtensionAvailable(baseSelector, mode)) {
                modes.add(mode)
            }
        }
        
        _uiState.update { it.copy(supportedExtensions = modes) }
    }

    @SuppressLint("UnsafeOptInUsageError")
    @ExperimentalCamera2Interop
    private fun bindCameraUseCases(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView
    ) {
        val cameraProvider = cameraProvider ?: return
        val manager = extensionsManager ?: return

        val baseSelector = CameraSelector.Builder()
            .requireLensFacing(_uiState.value.lensFacing)
            .build()

        val cameraSelector = if ((_uiState.value.extensionMode != ExtensionMode.NONE) && 
            manager.isExtensionAvailable(baseSelector, _uiState.value.extensionMode)) {
            manager.getExtensionEnabledCameraSelector(baseSelector, _uiState.value.extensionMode)
        } else {
            baseSelector
        }

        val previewBuilder = Preview.Builder()
            .setTargetAspectRatio(_uiState.value.aspectRatio)
            .setTargetRotation(previewView.display?.rotation ?: Surface.ROTATION_0)
        
        Camera2Interop.Extender(previewBuilder).setCaptureRequestOption(
            CaptureRequest.CONTROL_EFFECT_MODE, _uiState.value.currentEffect
        )

        val preview = previewBuilder.build().also {
            it.surfaceProvider = previewView.surfaceProvider
        }

        val captureBuilder = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetAspectRatio(_uiState.value.aspectRatio)
            .setTargetRotation(previewView.display?.rotation ?: Surface.ROTATION_0)
            
        Camera2Interop.Extender(captureBuilder).setCaptureRequestOption(
            CaptureRequest.CONTROL_EFFECT_MODE, _uiState.value.currentEffect
        )

        imageCapture = captureBuilder.build()

        val recorder = Recorder.Builder()
            .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
            .build()
        videoCapture = VideoCapture.withOutput(recorder)

        try {
            cameraProvider.unbindAll()
            camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture,
                videoCapture
            )
            
            observeCameraState()
        } catch (exc: Exception) {
            Log.e("CameraXVM", "Use case binding failed", exc)
        }
    }

    private fun observeCameraState() {
        camera?.cameraInfo?.zoomState?.observeForever { zoomState ->
            _uiState.update { it.copy(
                zoomRatio = zoomState.zoomRatio,
                minZoom = zoomState.minZoomRatio,
                maxZoom = zoomState.maxZoomRatio
            ) }
        }
    }

    @androidx.annotation.OptIn(ExperimentalCamera2Interop::class)
    @OptIn(ExperimentalCamera2Interop::class)
    fun toggleCamera(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        val newLensFacing = if (_uiState.value.lensFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }
        _uiState.update { it.copy(lensFacing = newLensFacing, extensionMode = ExtensionMode.NONE) }
        updateSupportedExtensions()
        bindCameraUseCases(lifecycleOwner, previewView)
    }

    fun setZoom(ratio: Float) {
        camera?.cameraControl?.setZoomRatio(ratio)
    }

    @androidx.annotation.OptIn(ExperimentalCamera2Interop::class)
    @OptIn(ExperimentalCamera2Interop::class)
    fun setAspectRatio(ratio: Int, lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        _uiState.update { it.copy(aspectRatio = ratio) }
        bindCameraUseCases(lifecycleOwner, previewView)
    }

    @androidx.annotation.OptIn(ExperimentalCamera2Interop::class)
    @OptIn(ExperimentalCamera2Interop::class)
    fun setExtension(mode: Int, lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        _uiState.update { it.copy(extensionMode = mode) }
        bindCameraUseCases(lifecycleOwner, previewView)
    }

    @androidx.annotation.OptIn(ExperimentalCamera2Interop::class)
    @OptIn(ExperimentalCamera2Interop::class)
    fun setEffect(effect: Int, lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        _uiState.update { it.copy(currentEffect = effect) }
        bindCameraUseCases(lifecycleOwner, previewView)
    }

    @OptIn(ExperimentalCamera2Interop::class)
    fun cycleEffect(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        val effects = listOf(
            CaptureRequest.CONTROL_EFFECT_MODE_OFF,
            CaptureRequest.CONTROL_EFFECT_MODE_MONO,
            CaptureRequest.CONTROL_EFFECT_MODE_NEGATIVE,
            CaptureRequest.CONTROL_EFFECT_MODE_SEPIA
        )
        val currentIndex = effects.indexOf(_uiState.value.currentEffect)
        val nextIndex = (currentIndex + 1) % effects.size
        setEffect(effects[nextIndex], lifecycleOwner, previewView)
    }

    fun setCameraMode(mode: CameraMode) {
        if (_uiState.value.isRecording) return
        _uiState.update { it.copy(cameraMode = mode) }
    }

    fun capturePhoto(context: Context) {
        val imageCapture = imageCapture ?: return

        _uiState.update { it.copy(isCapturing = true) }

        val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "IMG_$name")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/GeekLab-CameraX")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(context.contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            .build()

        imageCapture.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e("CameraXVM", "Photo capture failed: ${exc.message}", exc)
                    _uiState.update { it.copy(isCapturing = false) }
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Log.d("CameraXVM", "Photo capture succeeded")
                    _uiState.update { it.copy(isCapturing = false) }
                    ContextCompat.getMainExecutor(context).execute {
                        Toast.makeText(context, context.getString(R.string.camera_image_saved), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    @SuppressLint("MissingPermission")
    fun toggleRecording(context: Context) {
        val videoCapture = videoCapture ?: return

        val curRecording = activeRecording
        if (curRecording != null) {
            curRecording.stop()
            activeRecording = null
            return
        }

        val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "VID_$name")
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/GeekLab-CameraX")
            }
        }

        val mediaStoreOutputOptions = MediaStoreOutputOptions
            .Builder(context.contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()

        activeRecording = videoCapture.output
            .prepareRecording(context, mediaStoreOutputOptions)
            .withAudioEnabled()
            .start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                when(recordEvent) {
                    is VideoRecordEvent.Start -> {
                        _uiState.update { it.copy(isRecording = true) }
                    }
                    is VideoRecordEvent.Finalize -> {
                        _uiState.update { it.copy(isRecording = false) }
                        if (!recordEvent.hasError()) {
                            Toast.makeText(context, context.getString(R.string.camera_video_saved), Toast.LENGTH_SHORT).show()
                        } else {
                            activeRecording?.stop()
                            activeRecording = null
                            Log.e("CameraXVM", "Video recording error: ${recordEvent.error}")
                        }
                    }
                }
            }
    }

    override fun onCleared() {
        cameraExecutor.shutdown()
        activeRecording?.stop()
        activeRecording = null
    }
}
