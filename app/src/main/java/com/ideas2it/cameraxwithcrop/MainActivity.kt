package com.ideas2it.cameraxwithcrop

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.ideas2it.cameraxcrop.CameraxActivity
import com.ideas2it.cameraxcrop.SelectImage
import com.raywenderlich.android.lememeify.Utils
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.io.InputStream

class MainActivity : AppCompatActivity() {
    lateinit var imgView: ImageView
    var imageExist: Boolean  = false
    var tempUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btn: Button = findViewById(R.id.capturebtn)
        btn.setOnClickListener {
            SelectImage.ActivityBuilder()
                .crop(true)
                .setCropShape(CropImageView.CropShape.OVAL)
                .setAspectRatio(600, 600)
                .savePath("Simbu")
                .start(this)
        }

        val savebtn: Button = findViewById(R.id.savebtn)
        imgView = findViewById(R.id.imgView)

        savebtn.setOnClickListener {
            if(imageExist) {
                val type = getMimeType(tempUri.toString())
                val format = Utils.getImageFormat(type!!)
                CameraxActivity.saveImage(this, imgView, format, "Simbu1/Test")
            }

            else
                Toast.makeText(this, "Image doesn't exist", Toast.LENGTH_LONG).show()
        }
    }

    fun getMimeType(path: String): String {
        var type = "image/jpeg" // Default Value
        val extension = MimeTypeMap.getFileExtensionFromUrl(path);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension).toString()
        }
        return type
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Check which request we're responding to
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            // Make sure the request was successful
            if (resultCode == Activity.RESULT_OK) {
                val uri = data?.getStringExtra("Uri")
                val u = Uri.parse(uri)
                tempUri = u
                Glide.with(this).load(uri).into(imgView)
//                imgView.setImageURI(u)

                val ist: InputStream? = contentResolver.openInputStream(u)
                val bitm: Bitmap = BitmapFactory.decodeStream(ist)
                ist?.close()
                imageExist = true


            }
        }
    }
}