/*
 * Copyright (c) 2020 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * This project and source code may use libraries or frameworks that are
 * released under various Open-Source licenses. Use of those libraries and
 * frameworks are governed by their own individual licenses.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.raywenderlich.android.lememeify

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.*

object Utils {

  const val EXTRA_IMAGE = "extra.image"

  private const val ANDROID_R = "R"
  private const val DATA_PATTERN = "MMM d, yyyy  •  HH:mm"
  private const val MEGABYTE = 1000.0

  private const val IMAGE_EXTENSION_JPG = "jpg"
  private const val IMAGE_EXTENSION_PNG = "png"

  fun hasSdkHigherThan(sdk: Int): Boolean {
    return Build.VERSION.SDK_INT > sdk
  }

  fun hasAndroid11(): Boolean {
    return Build.VERSION.CODENAME == ANDROID_R
  }

  fun hasStoragePermission(context: Context): Boolean {
    return hasPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
  }

  fun requestStoragePermission(fragment: Fragment, requestCode: Int) {
    fragment.requestPermissions(
        arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE),
        requestCode)
  }

  fun hasMediaLocationPermission(context: Context): Boolean {
    return hasPermission(context, Manifest.permission.ACCESS_MEDIA_LOCATION)
  }

  fun requestMediaLocationPermission(activity: Activity, requestCode: Int) {
    requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_MEDIA_LOCATION), requestCode)
  }

  private fun hasPermission(context: Context, permission: String): Boolean {
    return ActivityCompat.checkSelfPermission(context,
        permission) == PackageManager.PERMISSION_GRANTED
  }

  private fun requestPermissions(activity: Activity, list: Array<String>, code: Int) {
    ActivityCompat.requestPermissions(activity, list, code)
  }

  fun getFormattedDateFromMillis(millis: Long): String {
    val date = Date()
    date.time = millis * 1000L
    return SimpleDateFormat(DATA_PATTERN, Locale.getDefault()).format(date)
  }

  fun getImageFormat(type: String): Bitmap.CompressFormat {
    return when (type) {
      Bitmap.CompressFormat.PNG.name -> {
        Bitmap.CompressFormat.PNG
      }
      Bitmap.CompressFormat.JPEG.name -> {
        Bitmap.CompressFormat.JPEG
      }
      Bitmap.CompressFormat.WEBP.name -> {
        Bitmap.CompressFormat.WEBP
      }
      else -> {
        Bitmap.CompressFormat.JPEG
      }
    }
  }

  fun getImageExtension(format: Bitmap.CompressFormat): String {
    return when (format) {
      Bitmap.CompressFormat.PNG -> {
        IMAGE_EXTENSION_PNG
      }
      Bitmap.CompressFormat.JPEG -> {
        IMAGE_EXTENSION_JPG
      }
      else -> {
        IMAGE_EXTENSION_JPG
      }
    }
  }
}