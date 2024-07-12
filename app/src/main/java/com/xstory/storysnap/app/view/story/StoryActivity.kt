package com.xstory.storysnap.app.view.story

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.activity.viewModels
import com.bumptech.glide.Glide
import com.dicoding.picodiploma.loginwithanimation.R
import com.dicoding.picodiploma.loginwithanimation.databinding.ActivityStoryBinding
import com.xstory.storysnap.app.data.handler.GeneralHandler
import com.xstory.storysnap.app.data.factory.ViewModelFactory
import com.xstory.storysnap.app.view.home.HomeActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale

class StoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStoryBinding
    private val storyViewModel: StoryViewModel by viewModels {
        ViewModelFactory.getStoryInstance(this)
    }
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val timeStamp: String = SimpleDateFormat("dd-MM-yyyy", Locale("in", "ID")).format(System.currentTimeMillis())
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                if (!it.value) {
                    showPermissionDeniedDialog()
                    return@registerForActivityResult
                }
            }
        }
    private val timeoutHandler = Handler(Looper.getMainLooper())
    private val timeoutRunnable = Runnable {
        showTimeoutDialog()
        showLoading(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val token = intent.getStringExtra(TOKEN).toString()
        if (!allPermissionsGranted()) {
            requestPermissions()
        }
        setupToolbar()
        setupListeners(token)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        collectUploadFlow()
    }

    private fun requestPermissions() {
        requestPermissionLauncher.launch(arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }

    private fun collectUploadFlow() {
        lifecycleScope.launch {
            storyViewModel.uploadStatus.collect { result ->
                handleUploadResult(result)
            }
        }
    }

    private fun setupToolbar() {
        findViewById<Toolbar>(R.id.toolbar).apply {
            setSupportActionBar(this)
            supportActionBar?.apply {
                title = getString(R.string.upload)
                setDisplayHomeAsUpEnabled(true)
                setDisplayShowHomeEnabled(true)
            }
            setNavigationOnClickListener {
                val intent = Intent(this@StoryActivity, HomeActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun setupListeners(token: String) {
        binding.btnCamera.setOnClickListener { startTakePhoto() }
        binding.btnGallery.setOnClickListener { startGallery() }
        binding.btnUpload.setOnClickListener { uploadImage(token) }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun uploadImage(token: String) {
        val includeLocation = binding.locationToggle.isChecked
        getLastLocation(token, includeLocation)
    }

    private fun getLastLocation(token: String, includeLocation: Boolean) {
        if (includeLocation && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                continueUpload(token, location?.latitude, location?.longitude)
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to get location", Toast.LENGTH_SHORT).show()
                continueUpload(token, null, null)
            }
        } else {
            continueUpload(token, null, null)
        }
    }

    private fun continueUpload(token: String, latitude: Double?, longitude: Double?) {
        val file = getFile()
        val description = binding.editDescription.text.toString().trim()
        if (file == null) {
            showImageInputErrorDialog()
            return
        }
        if (description.isEmpty()) {
            binding.editDescription.error = getString(R.string.message_validation, "description")
            return
        }

        val reducedFile = reduceFileImage(file)
        val descMedia = description
        val imageMultipart = prepareFilePart("photo", reducedFile)

        showLoading(true)
        timeoutHandler.postDelayed(timeoutRunnable, TIMEOUT_DURATION)
        storyViewModel.uploadStory("Bearer $token", imageMultipart, descMedia, latitude, longitude)
    }


    private fun handleUploadResult(result: GeneralHandler<Any>) {
        timeoutHandler.removeCallbacks(timeoutRunnable)
        when (result) {
            is GeneralHandler.Loading -> showLoading(true)
            is GeneralHandler.Success -> {
                showLoading(false)
                showSuccess("Story Berhasil Diupload!")
                Handler(Looper.getMainLooper()).postDelayed({
                    startHomeActivity()
                }, 1000)
            }
            is GeneralHandler.Error -> {
                showLoading(false)
                showErrorDialog(result.error)
            }
            else -> {}
        }
    }

    private fun startHomeActivity() {
        val intent = Intent(this@StoryActivity, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun startGallery() {
        launcherIntentGallery.launch(arrayOf("image/*"))
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            lifecycleScope.launch {
                val myFile = withContext(Dispatchers.IO) {
                    uriToFile(it, this@StoryActivity)
                }
                setFile(myFile)
                binding.previewImageView.setImageURI(it)
            }
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun startTakePhoto() {
        val intent = Intent(this, CameraActivity::class.java)
        launcherIntentCamera.launch(intent)
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageUri: Uri? = result.data?.data
            imageUri?.let {
                lifecycleScope.launch {
                    val myFile = withContext(Dispatchers.IO) {
                        uriToFile(it, this@StoryActivity)
                    }
                    setFile(myFile)
                    binding.previewImageView.setImageURI(it)
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.apply {
            btnCamera.isEnabled = !isLoading
            btnGallery.isEnabled = !isLoading
            btnUpload.isEnabled = !isLoading
            editDescription.isEnabled = !isLoading
            progressBar.animateVisibility(isLoading)
        }
    }

    private fun View.animateVisibility(isVisible: Boolean, duration: Long = 400) {
        ObjectAnimator
            .ofFloat(this, View.ALPHA, if (isVisible) 1f else 0f)
            .setDuration(duration)
            .start()
    }

    private fun showSuccess(message: String) {
        val snackbar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
        snackbar.setAction("OK") {
            startHomeActivity()
        }
        snackbar.show()
    }

    private fun showErrorDialog(error: String) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.error_title))
            .setMessage(error)
            .setPositiveButton(getString(R.string.okay), null)
            .create()
            .show()
    }

    private fun showImageInputErrorDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.input_error_title))
            .setMessage(getString(R.string.input_your_image))
            .setPositiveButton(getString(R.string.okay), null)
            .create()
            .show()
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.permission_denied_title))
            .setMessage(getString(R.string.permission_denied_message))
            .setPositiveButton(getString(R.string.okay), null)
            .create()
            .show()
    }

    private fun createTempFile(context: Context): File {
        return try {
            val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            File.createTempFile(timeStamp, ".jpg", storageDir)
        } catch (e: IOException) {
            Log.e(TAG, "Error creating temporary file", e)
            throw RuntimeException("Error creating temporary file", e)
        }
    }

    private fun uriToFile(selectedImg: Uri, context: Context): File {
        val myFile = createTempFile(context)
        try {
            val bitmap = Glide.with(context)
                .asBitmap()
                .load(selectedImg)
                .submit()
                .get()

            var compressQuality = 100
            var streamLength: Int
            do {
                val bmpStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
                val bmpPicByteArray = bmpStream.toByteArray()
                streamLength = bmpPicByteArray.size
                compressQuality -= 5
            } while (streamLength > 1000000 && compressQuality > 0)

            FileOutputStream(myFile).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, outputStream)
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error converting URI to file", e)
            throw RuntimeException("Error converting URI to file", e)
        }
        return myFile
    }

    private fun reduceFileImage(file: File): File {
        return try {
            val bitmap = BitmapFactory.decodeFile(file.path)
            val scaleFactor =
                calculateScaleFactor(bitmap.width, bitmap.height, maxWidth = 1080, maxHeight = 1920)
            val scaledBitmap = Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width / scaleFactor).toInt(),
                (bitmap.height / scaleFactor).toInt(),
                true
            )

            var compressQuality = 100
            var streamLength: Int
            do {
                val bmpStream = ByteArrayOutputStream()
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
                val bmpPicByteArray = bmpStream.toByteArray()
                streamLength = bmpPicByteArray.size
                compressQuality -= 5
            } while (streamLength > 1000000 && compressQuality > 0)

            FileOutputStream(file).use { out ->
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, out)
            }

            file
        } catch (e: Exception) {
            Log.e(TAG, "Error reducing file image size", e)
            throw RuntimeException("Error reducing file image size", e)
        }
    }

    private fun calculateScaleFactor(
        originalWidth: Int,
        originalHeight: Int,
        maxWidth: Int,
        maxHeight: Int,
    ): Float {
        val widthScaleFactor = originalWidth.toFloat() / maxWidth.toFloat()
        val heightScaleFactor = originalHeight.toFloat() / maxHeight.toFloat()
        return maxOf(widthScaleFactor, heightScaleFactor)
    }

    private fun prepareFilePart(partName: String, file: File): MultipartBody.Part {
        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(partName, file.name, requestFile)
    }

    private fun getFile(): File? {
        return binding.previewImageView.tag as? File
    }

    private fun setFile(file: File) {
        binding.previewImageView.tag = file
    }

    private fun showTimeoutDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.timeout_title))
            .setMessage(getString(R.string.timeout_message))
            .setPositiveButton(getString(R.string.okay), null)
            .create()
            .show()
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        const val TOKEN = "token_story"
        private const val TAG = "StoryActivity"
        private const val TIMEOUT_DURATION = 10000L
    }
}
