FilePickerHelper is a library to get multi-type files written in Java.


### Gradle

Add following dependency to your root project `build.gradle` file:

```groovy
allprojects {
    repositories {
        ...
        jcenter()
        maven { url "https://jitpack.io" }
        ...
    }
}
```

Add following dependency to your app module `build.gradle` file:

```groovy
dependencies {
    ...
    implementation "com.sk.FilePickerHelper:filepickerhelper:1.0.1"
    ...
}
```

## How to initialize object of `FilePickerHelper`

First way to initialize object of `FilePickerHelper`:
```java
FilePickerHelper mFilePickerHelper = FilePickerHelper.get(this); // Activity or Fragment
```

Complete features of what you can do with `FilePickerHelper`:

```java
  mFilePickerHelper = FilePickerHelper.get(this)  // Activity or Fragment
                .setAlertDialog(true) // Show Alert dialog (by default bottom sheet)
                .setDisableImageCapture(false) // Disable image capture from camera. Default false
                .setDisableFilePick(false) // Disable File pick option. Default false
                .setDisableImagePick(false) // Disable image pick from gallery. Default false
                .setPickOnlyVideoFile(false) // Pick only Video file from file. Default false
                .setPickOnlyPdfFile(false) // Pick only Pdf file from file. Default false
                .setPickOnlyXmlFile(false) // Pick only Video file from file. Default false
                .setFileDrawable(R.drawable.ic_file) // Set drawable of file
                .setGalleryDrawable(R.drawable.ic_gallery) // Set drawable of gallery
                .setCameraDrawable(R.drawable.ic_camera) // Set drawable of  Camera
                .setMaxFileSizeInMB(100); // Set Max size of file. Default 50 mb
```
All the above methods are options. You can use the method as per your requirement.

## Implements 
implements by `OnFilePickedListener` for receive result on your activity or fragment class
```java
public class MainActivity extends AppCompatActivity implements OnFilePickedListener {
    @Override
       protected void onCreate(Bundle savedInstanceState) {
           super.onCreate(savedInstanceState);
           ....
            mFilePickerHelper.setOnFilePickedListener(this);
        .....
    }
}
```
## Open File picker dialog 
```java
mFilePickerHelper.openFilePickerDialog(REQUEST_CODE);
```

## Receive result
```java
 @Override
    public void onFilePickedResult(Uri aFileUri, String aFilePath, int aRequestCode) {
          // aFileUri -> get URI of file
          // aFilePath -> get Storage of file
          // aRequestCode -> Request code
    }

    @Override
    public void onActivityResult(int aRequestCode, int aResultCode, Intent aIntentData) {
        super.onActivityResult(aRequestCode, aResultCode, aIntentData);
        mFilePickerHelper.onFilePicked(aRequestCode, aResultCode, aIntentData);
    }

    @Override
    public void onRequestPermissionsResults(int requestCode, String[] requestPermissions, int[] grantResults) {
        mFilePickerHelper.onRequestPermissionsResult(requestCode, requestPermissions, grantResults);
    }
```

## Load Image with glide
Load Image From local devise
- @param aContext          Activity Context
- @param aImageUri         String Image Uri
- @param aPlaceHolderImage int Default PlaceHolder Image
- @param aErrorImage       int Error PlaceHolder Image
- @param aImageView        View View name which show image from Url
   
`loadImageFromLocal(Context aContext, Uri aImageUri, int aPlaceHolderImage, int aErrorImage, ImageView aImageView)`

Example

```java
mFilePickerHelper.loadImageFromLocal(MainActivity.this,
                    mUri,
                    R.drawable.ic_launcher_background,
                    R.drawable.ic_launcher_background,
                    imageView);
```

### Features

- [x] Custom Icons
- [x] Runtime Permissions
- [x] File Pick From SD Card or internal storage 
- [x] Image/Video Capture From Camera
- [x] Image/Video Capture From Gallery
- [x] Max File Sixe Support
- [x] File URI and Path support
- [x] File Picker with Alert Dialog View
- [x] File Picker with Bottom Sheet View
- [x] Disable Image Capture From Camera
- [x] Disable Image Pick from Gallery
- [x] Pick PDF/Video/XML File Support

## Enable Multidex support

Enable Multidex support as explained in this [Android Doc](https://developer.android.com/studio/build/multidex)

## Reporting Issue

See [KNOWN_ISSUES](https://github.com/SandipVKalola/FilePickerHelper-Java/blob/master/KNOWN_ISSUES.md) and [CHANGELOG](https://github.com/filepickerhelper/FilePickerHelper/blob/master/CHANGELOG.md) first before reporting any issue. <br />
Please follow [Issue Template](https://github.com/SandipVKalola/FilePickerHelper-Java/blob/master/Issue_Template_Examples.md) to report any issue.

## Share your application
If you are using FilePickerHelper in your application, share your application link in [this issue](https://github.com/SandipVKalola/FilePickerHelper-Java/issues/1)

### Credits
1. <a href="https://github.com/bumptech/glide">Glide</a>

