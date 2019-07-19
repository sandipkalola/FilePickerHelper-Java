package com.sk.FilePickerHelper;

import android.content.Intent;
import android.net.Uri;

public interface OnFilePickedListener {
    /**
     * This is use for Send image with path and image Uri
     */
    void onFilePickedResult(Uri aFileUri, String aFilePath, int aRequestCode);

    /**
     * aRequestCode
     * public static final int REQUEST_IMAGE_PICK = 999 for ;
     * public static final int REQUEST_IMAGE_CAPTURE = 998;
     * public static final int REQUEST_FILE_PICK = 997;
     *
     * @param aRequestCode
     * @param aResultCode
     * @param aIntentData
     */
    void onActivityResult(int aRequestCode, int aResultCode, Intent aIntentData);


    void onRequestPermissionsResults(int requestCode, String permissions[], int[] grantResults);
}