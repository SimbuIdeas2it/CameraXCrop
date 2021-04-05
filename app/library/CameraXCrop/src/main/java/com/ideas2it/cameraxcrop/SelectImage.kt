package com.ideas2it.cameraxcrop


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.ideas2it.cameraxcrop.CameraxActivity
import com.theartofdev.edmodo.cropper.CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE
import com.theartofdev.edmodo.cropper.CropImage.CROP_IMAGE_EXTRA_BUNDLE
import com.theartofdev.edmodo.cropper.CropImageOptions
import com.theartofdev.edmodo.cropper.CropImageView

class SelectImage(
    val cropShape: CropImageView.CropShape?
    , val shouldCrop: Boolean?
) {
    class ActivityBuilder {
        var mOptions = CropImageOptions()
            private set

        var shouldCrop: Boolean = false
            private set

        var cropShape: CropImageView.CropShape = CropImageView.CropShape.RECTANGLE
            private set

        fun crop(shouldCrop: Boolean) = apply{
            this.shouldCrop = shouldCrop
        }

        fun setCropShape(cropShape: CropImageView.CropShape)  = apply {
            this.cropShape = cropShape
        }

        fun start(activity: Activity) {

            mOptions.cropShape = this.cropShape

            val intent = Intent(activity, CameraxActivity::class.java)
            val bundle = Bundle()
            bundle.putBoolean("crop", this.shouldCrop)
            bundle.putParcelable("cropShape", mOptions)
            intent.putExtra(CROP_IMAGE_EXTRA_BUNDLE, bundle)
            activity.startActivityForResult(intent, CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        }

        fun setAspectRatio(aspectRatioX: Int, aspectRatioY: Int) = apply {
            mOptions.aspectRatioX = aspectRatioX
            mOptions.aspectRatioY = aspectRatioY
            mOptions.fixAspectRatio = true
        }
    }
}