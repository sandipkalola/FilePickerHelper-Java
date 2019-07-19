package sample.filepickerhelper.com.filepickerhelper_java;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView ;

import com.sk.FilePickerHelper.FilePickerHelper;
import com.sk.FilePickerHelper.OnFilePickedListener;

public class MainActivity extends AppCompatActivity implements OnFilePickedListener {

    FilePickerHelper mFilePickerHelper;
    public static final int REQUEST_CODE_IMAGE_PICK = 105;
    Button btnPickImage;
    ImageView imageView;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //mFilePickerHelper = new FilePickerHelper(this);
        mFilePickerHelper = FilePickerHelper.get(this);
        mFilePickerHelper.setOnFilePickedListener(this);
        imageView = (ImageView) findViewById(R.id.img);
        btnPickImage = (Button) findViewById(R.id.btnPickImage);
        textView = (TextView) findViewById(R.id.txtFileName);
        btnPickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setText("");
                mFilePickerHelper.openFilePickerDialog(REQUEST_CODE_IMAGE_PICK);
            }
        });
    }

    @Override
    public void onFilePickedResult(Uri aFileUri, String aFilePath, int aRequestCode) {
        if (aRequestCode == REQUEST_CODE_IMAGE_PICK) {
            mFilePickerHelper.loadImageFromLocal(MainActivity.this,
                    aFileUri,
                    R.drawable.ic_launcher_background,
                    R.drawable.ic_launcher_background,
                    imageView);
            Log.d("FilePickerHelper", "Path-> " + aFilePath);
            Log.d("FilePickerHelper", "Uri-> " + aFileUri);
            textView.setText("Path:" + aFilePath + "\n\nUri:" + aFileUri);
        }
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
}
