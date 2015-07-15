package com.example.deepak.pushimages;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;


public class MainActivity extends Activity {

    // Holds static reference of camera capture image.
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    // Holds tag for log message.
    private static final String TAG = MainActivity.class.getName();
    // Holds file path String holder.
    static String sFilepath = "filepath";
    // Holds instance of GridView.
    GridView mGridView;

    // Holds instance of Adapter GridViewAdapter.
    GridViewAdapter mGridViewAdapter;
    private File mDirectory;
    // Holds instance of file path.
    private ArrayList<String> mFilePathStrings = new ArrayList<String>();
    // Holds instance of take file in directory.
    private ArrayList<File> mListFile = new ArrayList<File>();
    // Holds instance of take picture button.
    private Button mBtnTakePicture;
    // Holds instance of take gallery view button.
    private Button mBtnUpload;

    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.v(TAG, "onCreate()");

        ((Button) findViewById(R.id.takePicture))
                .setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        dispatchTakePictureIntent();
                    }
                });

        mBtnUpload = (Button) findViewById(R.id.btnUpload);
        mBtnUpload.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mListFile.size() > 0) {
                    intent = new Intent(MainActivity.this, UploadService.class);
                    intent.putStringArrayListExtra(sFilepath, mFilePathStrings);
                    startService(intent);
                } else {
                    Toast.makeText(MainActivity.this, "Application do not have images to upload", Toast.LENGTH_SHORT).show();
                }
            }
        });


        setData();

        mGridView = (GridView) findViewById(R.id.gridview);
        // Initialize GridView Adapter.
        mGridViewAdapter = new GridViewAdapter();
        // Set the LazyAdapter to the GridView
        mGridView.setAdapter(mGridViewAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(intent != null) {
            stopService(intent);
        }
    }

    private void setData() {
        Log.v(TAG, "setData()");
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        mDirectory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        mFilePathStrings = new ArrayList<String>();
        mListFile = new ArrayList<File>();

        if (mDirectory.isDirectory()) {
            mListFile = new ArrayList<File>(Arrays.asList(mDirectory.listFiles()));

            if (mListFile.size() > 0) {
                // Create a String array for FilePathStrings
                mFilePathStrings = new ArrayList<String>(mListFile.size());
                // Create a String array for FileNameStrings
//                mFileNameStrings = new String[mListFile.length];

                for (int i = 0; i < mListFile.size(); i++) {
//                     Get the path of the image file
                    mFilePathStrings.add(mListFile.get(i).getAbsolutePath());
                }
            }
        }
    }

    /*
     * Method to invoke camera event.
      */
    private void dispatchTakePictureIntent() {
        Log.v(TAG, "dispatchTakePictureIntent()");
        PackageManager packageManager = this.getPackageManager();
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA) == false) {
            Toast.makeText(
                    this,
                    getResources().getString(
                            R.string.device_does_not_have_camera),
                    Toast.LENGTH_SHORT).show();
        } else {
            Intent takePictureIntent = new Intent(
                    MediaStore.ACTION_IMAGE_CAPTURE);
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                    .format(new Date());
            String imageFileName = "JPEG_" + timeStamp;
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v(TAG, String.format("onActivityResult :: requestCode = %d, requestCode = %d, data = %b ", requestCode, resultCode, data.getAction()));
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            File file = saveToInternalStorage(imageBitmap);
            mListFile.add(file);
            mFilePathStrings.add(file.getAbsolutePath());
            if (mListFile.size() > 0)
                mGridViewAdapter.notifyDataSetChanged();
        }
    }

    private File saveToInternalStorage(Bitmap bitmapImage) {
        Log.v(TAG, "saveToInternalStorage");
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        String imageFileName = "JPEG_" + timeStamp;
        File path = new File(mDirectory, imageFileName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path);
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return path;
    }

    public class GridViewAdapter extends BaseAdapter {
        private LayoutInflater inflater = null;

        public GridViewAdapter() {
            inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {
            if (mListFile != null && mListFile.size() > 0)
                return mListFile.size();
            else
                return 0;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View vi = convertView;
            if (convertView == null)
                // Locate the TextView in gridview_item.xml
                vi = inflater.inflate(R.layout.gridview_item, null);

            // Locate the ImageView in gridview_item.xml
            ImageView image = (ImageView) vi.findViewById(R.id.image);

            Picasso.with(MainActivity.this).load("file:///" + mListFile.get(position)).resize(50, 50)
                    .centerCrop().placeholder(R.mipmap.ic_launcher).error(R.drawable.abc_ic_ab_back_mtrl_am_alpha).into(image);
            return vi;
        }
    }
}
