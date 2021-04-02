# CameraXCrop

Add a movn in project build.gradle

```
maven { url 'https://jitpack.io' }
```

Then add dependency on Module build.gradle

```
implementation 'com.github.SimbuIdeas2it:CameraXCrop:0.0.3'
```

To open a camerax librray using

``` kotlin
val intent = Intent(this, CameraxActivity::class.java)
startActivityForResult(intent, 1)
```
and get the response from camerax library using 

``` kotlin
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                val uri = data?.getStringExtra("Uri")
                val u = Uri.parse(uri)
                print(u)
            }
        }
 }
```
 
To save the image to external memory using the following lines from your activity
``` kotlin
CameraxActivity.saveImage(imgView, this)
```