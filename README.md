# CameraXCrop

Add a movn in project build.gradle

```
maven { url 'https://jitpack.io' }
```

Then add dependency on Module build.gradle

```
implementation 'com.github.SimbuIdeas2it:CameraXCrop:0.0.7'
```

To open a camerax librray using
For com.github.SimbuIdeas2it:CameraXCrop:0.0.3
``` kotlin
val intent = Intent(this, CameraxActivity::class.java)
startActivityForResult(intent, 1)
```
For com.github.SimbuIdeas2it:CameraXCrop:0.0.7
```
SelectImage.ActivityBuilder()
                .crop(false)
                .setCropShape(CropImageView.CropShape.OVAL)
                .setAspectRatio(600, 600)
                .savePath("<Directory Path>")
                .start(this)
```
crop - true if the captured image wants to crop\
setCropShape - Shape of the crop image(OVAL or Rectangle)\
savePath - If the image wants to save in internal memory in specific path.


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
