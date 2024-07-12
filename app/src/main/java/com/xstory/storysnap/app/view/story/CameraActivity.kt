package com.xstory.storysnap.app.view.story

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.lifecycleScope
import com.dicoding.picodiploma.loginwithanimation.databinding.ActivityCameraBinding
import com.dicoding.picodiploma.loginwithanimation.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding
    private var cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var imageCapture: ImageCapture? = null
    private lateinit var outputDirectory: File
    private lateinit var currentPhotoPath: String

    companion object {
        private const val TAG = "CameraActivity"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val PHOTO_EXTENSION = ".jpg"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initialize()
        setupListeners()
    }

    private fun initialize() {
        outputDirectory = getOutputDirectory()
        startCamera()
    }

    private fun setupListeners() {
        binding.switchCamera.setOnClickListener { switchCamera() }
        binding.captureImage.setOnClickListener { takePhoto() }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = buildPreview()
            imageCapture = buildImageCapture()

            bindCameraUseCases(cameraProvider, preview)
        }, ContextCompat.getMainExecutor(this))
    }

    private fun buildPreview(): Preview {
        return Preview.Builder()
            .build()
            .also { it.setSurfaceProvider(binding.viewFinder.surfaceProvider) }
    }

    private fun buildImageCapture(): ImageCapture {
        return ImageCapture.Builder()
            .setTargetRotation(binding.viewFinder.display.rotation)
            .build()
    }

    private fun bindCameraUseCases(cameraProvider: ProcessCameraProvider, preview: Preview) {
        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                preview,
                imageCapture
            )
        } catch (exc: Exception) {
            Toast.makeText(this, "Failed to start camera.", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "startCamera: ${exc.message}", exc)
        }
    }

    private fun switchCamera() {
        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
        startCamera()
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val photoFile = createFile(outputDirectory, FILENAME_FORMAT, PHOTO_EXTENSION)
        currentPhotoPath = photoFile.absolutePath

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    lifecycleScope.launch {
                        handleImageCaptureSuccess(photoFile)
                    }
                }
            })
    }

    private suspend fun handleImageCaptureSuccess(photoFile: File) {
        withContext(Dispatchers.IO) {
            rotateImageIfRequired(photoFile)
        }
        val savedUri = Uri.fromFile(photoFile)
        val intent = Intent().apply { data = savedUri }
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun rotateImageIfRequired(photoFile: File) {
        try {
            val exif = ExifInterface(photoFile.absolutePath)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
            val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
            val rotatedBitmap = getRotatedBitmap(bitmap, orientation)
            saveRotatedBitmap(rotatedBitmap, photoFile)
        } catch (e: IOException) {
            Log.e(TAG, "Error rotating image", e)
        }
    }

    private fun getRotatedBitmap(bitmap: Bitmap, orientation: Int): Bitmap {
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270f)
            else -> bitmap
        }
    }

    private fun rotateImage(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(angle) }
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    private fun saveRotatedBitmap(bitmap: Bitmap, photoFile: File) {
        try {
            FileOutputStream(photoFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error saving rotated image", e)
        }
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else filesDir
    }

    private fun createFile(baseFolder: File, format: String, extension: String): File {
        return File(baseFolder, SimpleDateFormat(format, Locale.US).format(System.currentTimeMillis()) + extension)
    }
}
