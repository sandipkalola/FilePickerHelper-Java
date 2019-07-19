package com.sk.FilePickerHelper;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FilePickerHelper {
    private Dialog mBottomSheetDialog;
    private boolean mIsDisableImagePick = false;  // Used for Disable image picker
    private boolean mIsDisableImageCapture = false; // Used for Disable image capture
    private boolean mIsDisableFilePick = false; // Used for Disable file picker
    private boolean mIsPdfFileOnly = false; // Used for pick only PDF files
    private boolean mIsVideoFileOnly = false; // Used for Pick only Video files
    private boolean mIsXmlFileOnly = false; // Used for Pick only Xml files
    private boolean mIsAlertDialog = false; //  Used for show alert dialog
    private final String CAMERA_FILE_NAME_PREFIX = "CAMERA_"; // Prefix for image name
    public static final int STORAGE = 2;
    private Uri mFileUri = null; // Store File Uri
    private String mFileFullPath = null; // Store File path
    private int mRequestCode = 0;
    private int mFileSize = 50; // Default 50 mb file size
    public static final int REQUEST_IMAGE_PICK = 999; // Request code for image pick
    public static final int REQUEST_IMAGE_CAPTURE = 998; // Request code for image capture
    public static final int REQUEST_FILE_PICK = 997; // Request code for file pick
    public static final int REQUEST_XML_FILE_PICK = 998; // Request code for Xml file pick
    OnFilePickedListener mOnFilePickedListener; // File picker listener
    private Activity mActivity; // Object of activity class
    private Fragment mFragment; // Object of fragment class
    private String appName = "";
    private String message = "app needs permissions to run. Enable from Settings > Apps > " + appName + " > Permissions > Enable all.";
    private int mGalleryImage = R.drawable.ic_gallery; // Default drawable of Gallery
    private int mCameraImage = R.drawable.ic_camera; // Default drawable of Camera
    private int mFileImage = R.drawable.ic_file; // Default drawable of File


    public static FilePickerHelper get(Activity activity) {
        return new FilePickerHelper(activity);
    }

    public FilePickerHelper get(Fragment fragment) {
        return new FilePickerHelper(fragment);
    }

    /**
     * This constructor is use for activity object
     *
     * @param activity
     */
    public FilePickerHelper(Activity activity) {
        mActivity = activity;
        appName = getApplicationName(activity);
    }

    /**
     * This constructor is use for fragment object
     *
     * @param fragment
     */
    public FilePickerHelper(Fragment fragment) {
        mFragment = fragment;
        mActivity = fragment.getActivity();
        appName = getApplicationName(mActivity);
    }

    /**
     * Set Drawable of Gallery image
     *
     * @param drawable
     * @return
     */
    public FilePickerHelper setGalleryDrawable(int drawable) {
        mGalleryImage = drawable;
        return this;
    }

    /**
     * Set Drawable of Camera image
     *
     * @param drawable
     * @return
     */
    public FilePickerHelper setCameraDrawable(int drawable) {
        mCameraImage = drawable;
        return this;
    }

    /**
     * Set Drawable of File image
     *
     * @param drawable
     * @return
     */
    public FilePickerHelper setFileDrawable(int drawable) {
        mFileImage = drawable;
        return this;
    }

    /**
     * Get Application name
     *
     * @param context
     * @return
     */
    public String getApplicationName(Context context) {
        if (context != null) {
            ApplicationInfo applicationInfo = context.getApplicationInfo();
            int stringId = applicationInfo.labelRes;
            return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
        }
        return "";
    }

    /**
     * Permission Dialog
     */
    public void openPermissionSettingDialog() {
        new AlertDialog.Builder(mActivity)
                .setMessage(appName + message)
                .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        final Intent i = new Intent();
                        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        i.addCategory(Intent.CATEGORY_DEFAULT);
                        i.setData(Uri.parse("package:" + mActivity.getPackageName()));
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                        mActivity.startActivity(i);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }

    /**
     * Function is open view for display different file picker option
     * <p>
     * If mIsAlertDialog is true then show AlertDialog otherwise show Bottom Sheets view.
     */
    public void openFilePickerDialog(int aRequestCode) {
        mRequestCode = aRequestCode;
        // Check condition for Alert dialog
        if (mIsAlertDialog) {
            mBottomSheetDialog = new Dialog(mActivity);
            mBottomSheetDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        } else {
            mBottomSheetDialog = new Dialog(mActivity, R.style.MaterialDialogSheet);
        }

        final View view = mActivity.getLayoutInflater().inflate(R.layout.activity_dialogbottomsheet, null);
        RelativeLayout rlCameraView = (RelativeLayout) view.findViewById(R.id.rlCameraView);
        RelativeLayout rlGalleyView = (RelativeLayout) view.findViewById(R.id.rlGalleryView);
        RelativeLayout rlFileView = (RelativeLayout) view.findViewById(R.id.rlFileView);
        ImageView faGallery = (ImageView) view.findViewById(R.id.FAB_gallery);
        ImageView faCamera = (ImageView) view.findViewById(R.id.FAB_Camera);
        ImageView faFile = (ImageView) view.findViewById(R.id.FAB_File);

        faGallery.setImageDrawable(ContextCompat.getDrawable(mActivity, mGalleryImage));
        faCamera.setImageDrawable(ContextCompat.getDrawable(mActivity, mCameraImage));
        faFile.setImageDrawable(ContextCompat.getDrawable(mActivity, mFileImage));

        rlGalleyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkStoragePermissions()) {
                    takePhotoFromGallery();
                }
            }
        });

        rlFileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkStoragePermissions()) {
                    pickFileFromSDCard();
                }
            }
        });

        rlCameraView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkStoragePermissions()) {
                    takePhotoFromCamera();
                }
            }
        });

        // If Disable image capture option then hide camera view
        if (mIsDisableImageCapture) {
            rlCameraView.setVisibility(View.GONE);
        }

        // If Disable image pick option then hide gallery view
        if (mIsDisableImagePick) {
            rlGalleyView.setVisibility(View.GONE);
        }

        // If Disable File picker option then hide file pick view
        if (mIsDisableFilePick) {
            rlFileView.setVisibility(View.GONE);
        }

        mBottomSheetDialog.setContentView(view);
        mBottomSheetDialog.setCanceledOnTouchOutside(true);
        if (mBottomSheetDialog.getWindow() != null) {
            mBottomSheetDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            if (mIsAlertDialog) {
                mBottomSheetDialog.getWindow().setGravity(Gravity.CENTER);
            } else {
                mBottomSheetDialog.getWindow().setGravity(Gravity.BOTTOM);
            }
        }
        mBottomSheetDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                mBottomSheetDialog.dismiss();
            }
        });
        mBottomSheetDialog.show();
    }

    /**
     * Function is use for pick file from SDCard
     */
    public void pickFileFromSDCard() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        if (mIsPdfFileOnly) {
            intent.setType(MimeType.PDF_MIME);
        } else {
            intent.setType(MimeType.STREAM_MIME);
        }
        if (mIsXmlFileOnly) {
            intent.setType(MimeType.XML_MIME);
        } else {
            intent.setType(MimeType.STREAM_MIME);
        }
        if (Build.VERSION.SDK_INT < 20) {
            intent.setAction(Intent.ACTION_GET_CONTENT);
        } else {
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        if (mFragment != null) {
            mFragment.startActivityForResult(Intent.createChooser(intent, mFragment.getString(R.string.title_image_picker)), REQUEST_FILE_PICK);
        } else {
            mActivity.startActivityForResult(Intent.createChooser(intent, mActivity.getString(R.string.title_image_picker)), REQUEST_FILE_PICK);
        }

    }

    /**
     * This function use for pick image from SDCard
     */
    public void takePhotoFromGallery() {
        Intent intent = new Intent();
        intent.setType(MimeType.IMAGE_MIME);
        if (Build.VERSION.SDK_INT < 22) {
            intent.setAction(Intent.ACTION_GET_CONTENT);
        } else {
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        if (mFragment != null) {
            mFragment.startActivityForResult(Intent.createChooser(intent, mFragment.getString(R.string.title_image_picker)), REQUEST_IMAGE_PICK);
        } else {
            mActivity.startActivityForResult(Intent.createChooser(intent, mActivity.getString(R.string.title_image_picker)), REQUEST_IMAGE_PICK);
        }

    }

    /**
     * This function use for capture image from camera
     */
    public void takePhotoFromCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(mActivity.getPackageManager()) != null) {
            File photoFile = getTemporaryCameraFile();
            mFileUri = FileProvider.getUriForFile(mActivity,
                    mActivity.getPackageName() + ".provider",
                    photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mFileUri);
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                takePictureIntent.setClipData(ClipData.newRawUri("", mFileUri));
                takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
            if (mFragment != null) {
                mFragment.startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            } else {
                mActivity.startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }


    /**
     * Get Last used file from camera
     *
     * @return
     */
    private File getLastUsedCameraFile() {
        File dataDir = getAppExternalDataDirectoryFile();
        File[] files = dataDir.listFiles();
        List<File> filteredFiles = new ArrayList<>();
        for (File file : files) {
            if (file.getName().startsWith(CAMERA_FILE_NAME_PREFIX)) {
                filteredFiles.add(file);
            }
        }

        Collections.sort(filteredFiles);
        if (!filteredFiles.isEmpty()) {
            return filteredFiles.get(filteredFiles.size() - 1);
        } else {
            return null;
        }
    }

    private File getTemporaryCameraFile() {
        File storageDir = getAppExternalDataDirectoryFile();
        File file = new File(storageDir, getTemporaryCameraFileName());
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    private String getTemporaryCameraFileName() {
        return CAMERA_FILE_NAME_PREFIX + System.currentTimeMillis() + ".jpg";
    }

    /**
     * Get File size on MB
     *
     * @return
     */
    public int getFileSize() {
        return mFileSize * 1024;
    }

    /**
     * This function is use for check file size is grater then file size limit or not
     */
    private void checkFileSize() {
        if (!isTrimmedEmpty(mFileFullPath)) {
            File file = new File(mFileFullPath);
            int file_size = Integer.parseInt(String.valueOf(file.length() / 1024));
            if (file_size > getFileSize()) {
                Toast.makeText(mActivity, "You cannot upload more than " + getFileSize() / 1024 + " MB file.", Toast.LENGTH_SHORT).show();
            } else {
                if (mOnFilePickedListener != null) {
                    mOnFilePickedListener.onFilePickedResult(mFileUri, mFileFullPath, mRequestCode);
                }
            }
        }
    }

    public void onFilePicked(int aRequestCode, int aResultCode, Intent aIntentData) {
        if (aRequestCode == REQUEST_IMAGE_PICK && aResultCode == Activity.RESULT_OK && aIntentData != null && aIntentData.getData() != null) {
            mBottomSheetDialog.dismiss();
            mFileUri = aIntentData.getData();
            try {
                mFileFullPath = PathUtil.getPath(mActivity, mFileUri);
                checkFileSize();
            } catch (URISyntaxException aE) {
                aE.printStackTrace();
            }
        } else if (aRequestCode == REQUEST_FILE_PICK && aResultCode == Activity.RESULT_OK) {
            mBottomSheetDialog.dismiss();
            mFileUri = aIntentData.getData();
            try {
                mFileFullPath = PathUtil.getPath(mActivity, mFileUri);
                checkFileSize();
            } catch (URISyntaxException aE) {
                aE.printStackTrace();
            }
        } else if (aRequestCode == REQUEST_IMAGE_CAPTURE && aResultCode == Activity.RESULT_OK) {
            mBottomSheetDialog.dismiss();
            aIntentData = new Intent();
            aIntentData.setData(Uri.fromFile(getLastUsedCameraFile()));
            mFileUri = aIntentData.getData();
            mFileFullPath = mFileUri.getPath();
            checkFileSize();
        }
    }

    /**
     * This function is use for Show alert dialog instead of bottom sheet dialog
     *
     * @param aValue
     */
    public FilePickerHelper setAlertDialog(boolean aValue) {
        mIsAlertDialog = aValue;
        return this;
    }

    /**
     * This function use for set file size limit for upload
     *
     * @param aFileSize
     */
    public FilePickerHelper setMaxFileSizeInMB(int aFileSize) {
        mFileSize = aFileSize;
        return this;
    }

    /**
     * This function use for disable image pick option from sdcard
     *
     * @param aValue
     */
    public FilePickerHelper setDisableImagePick(boolean aValue) {
        mIsDisableImagePick = aValue;
        return this;
    }

    /**
     * This function use for disable image capture from camera option
     *
     * @param aValue
     */
    public FilePickerHelper setDisableImageCapture(boolean aValue) {
        mIsDisableImageCapture = aValue;
        return this;
    }

    /**
     * This function use for disable file pick option from sdcard
     *
     * @param aValue
     */
    public FilePickerHelper setDisableFilePick(boolean aValue) {
        mIsDisableFilePick = aValue;
        return this;
    }


    /**
     * This function use for pick only pdf file from sdcard
     *
     * @param aValue
     */
    public FilePickerHelper setPickOnlyPdfFile(boolean aValue) {
        mIsPdfFileOnly = aValue;
        return this;
    }

    /**
     * This function is use for pick only Video file from camera and gallery
     *
     * @param aValue
     */
    public FilePickerHelper setPickOnlyVideoFile(boolean aValue) {
        mIsVideoFileOnly = aValue;
        return this;
    }

    /**
     * This function use for pick only xml file from sdcard
     *
     * @param aValue
     */
    public FilePickerHelper setPickOnlyXmlFile(boolean aValue) {
        mIsXmlFileOnly = aValue;
        return this;
    }

    public boolean checkStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(mActivity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(mActivity,
                            Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(mActivity,
                            Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                if (mFragment != null) {
                    mFragment.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, STORAGE);
                } else {
                    mActivity.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, STORAGE);
                }
            } else {
                return true;
            }
        } else {
            return true;
        }
        return false;
    }


    public String getAppExternalDataDirectoryPath() {
        StringBuilder sb = new StringBuilder();
        sb.append(Environment.getExternalStorageDirectory())
                .append(File.separator)
                .append("Android")
                .append(File.separator)
                .append("data")
                .append(File.separator)
                .append(BuildConfig.class.getPackage().toString())
                .append(File.separator);

        return sb.toString();
    }

    public File getAppExternalDataDirectoryFile() {
        File dataDirectoryFile = new File(getAppExternalDataDirectoryPath());
        dataDirectoryFile.mkdirs();

        return dataDirectoryFile;
    }

    public void writeStringToFile(String string, File file) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(string.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void writeObjectToFile(Object object, File file) {
        ObjectOutputStream oos = null;
        try {
            FileOutputStream fos = new FileOutputStream(file);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(object);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (oos != null) {
                    oos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T readObjectFromFile(File file) {
        ObjectInputStream ois = null;
        T object = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            ois = new ObjectInputStream(fis);
            object = (T) ois.readObject();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return object;
    }

    /**
     * To check out that given string is null or empty after trimming
     *
     * @param aString String to check
     * @return true if String is empty even if it is pad with white space
     */
    public boolean isTrimmedEmpty(String aString) {
        return aString == null || aString.trim().length() == 0 || aString.trim().equalsIgnoreCase("null");
    }


    /**
     * Load Image From local devise
     *
     * @param aContext          Activity Context
     * @param aImageUri         String Image Uri
     * @param aPlaceHolderImage int Default PlaceHolder Image
     * @param aErrorImage       int Error PlaceHolder Image
     * @param aImageView        View View name which show image from Url
     */
    public void loadImageFromLocal(Context aContext, Uri aImageUri, int aPlaceHolderImage, int aErrorImage, ImageView aImageView) {
        Glide.with(aContext)
                .load(aImageUri)
                .thumbnail(0.5f)
                .dontAnimate()
                .placeholder(aPlaceHolderImage)
                .error(aErrorImage)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(aImageView);
    }

    private boolean verifyPermissions(int[] grantResults) {
        if (grantResults.length < 1) {
            return false;
        }
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public void setOnFilePickedListener(OnFilePickedListener onFilePickedListener) {
        this.mOnFilePickedListener = onFilePickedListener;
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == STORAGE) {
            if (!verifyPermissions(grantResults)) {
                openPermissionSettingDialog();
            }
        }
    }


}
