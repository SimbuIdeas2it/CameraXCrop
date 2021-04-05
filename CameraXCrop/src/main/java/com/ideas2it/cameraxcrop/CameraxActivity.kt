package com.ideas2it.cameraxcrop

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.raywenderlich.android.lememeify.Utils
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImage.CROP_IMAGE_EXTRA_BUNDLE
import com.theartofdev.edmodo.cropper.CropImageOptions
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_camerax.*
import java.io.File
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

typealias LumaListener = (luma: Double) -> Unit
class CameraxActivity : AppCompatActivity() {
    private var imageCapture: ImageCapture? = null

    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    var shouldCrop: Boolean? = false
    var mOptions: CropImageOptions? = null
    var filePath: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camerax)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        val bundle: Bundle? = intent.getBundleExtra(CROP_IMAGE_EXTRA_BUNDLE)
        shouldCrop = bundle?.getBoolean("crop", false)
        mOptions = bundle?.getParcelable(CropImage.CROP_IMAGE_EXTRA_OPTIONS)
        filePath = bundle?.getString("path", "")
        // Set up the listener for take photo button
        camera_capture_button.setOnClickListener { takePhoto() }
        outputDirectory = getOutputDirectory()

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        // Create time-stamped output file to hold the image
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(FILENAME_FORMAT, Locale.US
            ).format(System.currentTimeMillis()) + ".jpg")

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    val msg = "Photo capture succeeded: $savedUri"
                    Log.d(TAG, msg)

                    if(shouldCrop == true) {
                        launchImageCrop(savedUri)
                    }
                    else {
                        if(filePath != "") {
                            val type = getMimeType(savedUri.toString())
                            val format = Utils.getImageFormat(type!!)
                            val bitmap = getBitmapFromUri(savedUri)
                            if(bitmap != null) {
                                saveImage(this@CameraxActivity, bitmap, format, filePath!!)
                            }
                        }

                        val intent = Intent()
                        intent.putExtra("Uri", savedUri.toString())
                        setResult(Activity.RESULT_OK,intent)
                        finish()
                    }

                }
            })
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.createSurfaceProvider())
                }

            imageCapture = ImageCapture.Builder()
                .build()

            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, LuminosityAnalyzer { luma ->
                        Log.d(TAG, "Average luminosity: $luma")
                    })
                }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, imageAnalyzer)
            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() } }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraXBasic"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val QUALITY = 100

        fun getBitmapFromView(view: View): Bitmap {
            return Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888).apply {
                Canvas(this).apply {
                    view.draw(this)
                }
            }
        }

        fun saveImage(context: Context, bitmap: Bitmap, format: Bitmap.CompressFormat, directory: String) {
            val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            var dirDest = File(Environment.DIRECTORY_PICTURES)

            if(!directory.isEmpty()) {
                dirDest = File(Environment.DIRECTORY_PICTURES, directory)
            }

            val date = System.currentTimeMillis()
            val extension = Utils.getImageExtension(format)

            val newImage = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "$date.$extension")
                put(MediaStore.MediaColumns.MIME_TYPE, "image/$extension")
                put(MediaStore.MediaColumns.DATE_ADDED, date)
                put(MediaStore.MediaColumns.DATE_MODIFIED, date)
                put(MediaStore.MediaColumns.SIZE, bitmap.byteCount)
                put(MediaStore.MediaColumns.WIDTH, bitmap.width)
                put(MediaStore.MediaColumns.HEIGHT, bitmap.height)
                put(MediaStore.MediaColumns.RELATIVE_PATH, "$dirDest${File.separator}")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }

            val newImageUri = context.contentResolver.insert(collection, newImage)
            context.contentResolver.openOutputStream(newImageUri!!, "w").use {
                bitmap.compress(format, QUALITY, it)
            }

            newImage.clear()
            newImage.put(MediaStore.Images.Media.IS_PENDING, 0)
            context.contentResolver.update(newImageUri, newImage, null, null)
        }

        fun saveImage(context: Context, itemImage: View, format: Bitmap.CompressFormat, directory: String) {
            val bitmap = getBitmapFromView(itemImage)
            saveImage(context, bitmap, format, directory)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val result = CropImage.getActivityResult(data)
                if (resultCode == Activity.RESULT_OK) {
                    val msg = "Photo capture succeeded: $result.uri"

                    if(filePath != "") {
                        val type = getMimeType(result.uri.toString())
                        val format = Utils.getImageFormat(type!!)
                        val bitmap = getBitmapFromUri(result.uri)
                        if(bitmap != null) {
                            saveImage(this@CameraxActivity, bitmap, format, filePath!!)
                        }
                    }

                    val intent = Intent()
                    intent.putExtra("Uri", result.uri.toString())
                    setResult(Activity.RESULT_OK,intent)
                    finish()
                }
                else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Log.e(TAG, "Crop error: ${result.getError()}" )
                }
            }
        }

    }

    fun getBitmapFromUri(uri: Uri): Bitmap? {
        var bitmap: Bitmap? = null
//        if(Build.VERSION.SDK_INT < 28) {
//            bitmap = MediaStore.Images.Media.getBitmap(this@CameraxActivity.contentResolver, uri)
//        }
//        else {
//            val source = ImageDecoder.createSource(this@CameraxActivity.contentResolver, uri)
//            bitmap = ImageDecoder.decodeBitmap(source)
//        }
        bitmap = MediaStore.Images.Media.getBitmap(this@CameraxActivity.contentResolver, uri)
        return bitmap
    }

    private fun launchImageCrop(uri: Uri) {
        var cropShape: CropImageView.CropShape = CropImageView.CropShape.RECTANGLE
        var aspectRatioX = 1920
        var aspectRatioY = 1080
        if (mOptions?.aspectRatioX != null) {
            aspectRatioX = mOptions?.aspectRatioX!!
        }
        if (mOptions?.aspectRatioY != null) {
            aspectRatioY = mOptions?.aspectRatioY!!
        }
        if (mOptions?.cropShape != null) {
            cropShape = mOptions?.cropShape!!
        }
        CropImage.activity(uri)
            .setGuidelines(CropImageView.Guidelines.ON)
            .setAspectRatio(aspectRatioX, aspectRatioY)
            .setCropShape(cropShape) // default is rectangle
            .start(this)
    }

    fun getMimeType(path: String): String {
        var type = "image/jpeg" // Default Value
        val extension = MimeTypeMap.getFileExtensionFromUrl(path);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension).toString()
        }
        return type
    }


}
private class LuminosityAnalyzer(private val listener: LumaListener) : ImageAnalysis.Analyzer {

    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()    // Rewind the buffer to zero
        val data = ByteArray(remaining())
        get(data)   // Copy the buffer into a byte array
        return data // Return the byte array
    }

    override fun analyze(image: ImageProxy) {

        val buffer = image.planes[0].buffer
        val data = buffer.toByteArray()
        val pixels = data.map { it.toInt() and 0xFF }
        val luma = pixels.average()

        listener(luma)

        image.close()
    }
}