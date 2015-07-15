package com.example.deepak.pushimages;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Service to upload multiple images to server.
 */
public class UploadService extends IntentService {

    private static final String TAG = UploadService.class.getName();
    ArrayList<String> mFilePath = new ArrayList<String>();
    Handler mHandler;

    public UploadService() {
        super("name");
        Log.v(TAG, "UploadService()");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "onCreate()");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v(TAG, String.format("onHandleIntent() :: intent = %s", intent.getAction()));
        mFilePath = intent.getStringArrayListExtra(MainActivity.sFilepath);
        uploadImages();
    }

    void uploadImages() {
        Log.v(TAG, "uploadImage()");

        if (!mFilePath.isEmpty() && mFilePath.size() > 0) {
            while ((mFilePath.size() > 0)) {

        try {
            Thread.sleep(30 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
                int rand = ThreadLocalRandom.current().nextInt(1, 10);

                if (rand % 2 == 1) {
                    try {
                        throw new RuntimeException();
                    } catch (RuntimeException e) {
                        showToast(String.format("Uploaded Failed: %s", mFilePath.get(mFilePath.size() - 1)));
                    }
                } else {
                    showToast(String.format("Uploaded Successfully: %s", mFilePath.get(mFilePath.size() - 1)));
                    mFilePath.remove(mFilePath.get(mFilePath.size() - 1));
                }
            }
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, String.format("onStartCommand() :: intent = %s, flags = %d, startId = %d", intent.getAction(), flags, startId));
        mHandler = new Handler();
        return super.onStartCommand(intent, flags, startId);
    }

    private void showToast(String message) {
        mHandler.post(new ToastRunnable(message));
    }

    private class ToastRunnable implements Runnable {
        String mText;

        public ToastRunnable(String text) {
            mText = text;
        }

        @Override
        public void run() {
            Toast.makeText(getApplicationContext(), mText, Toast.LENGTH_SHORT).show();
        }
    }
}
