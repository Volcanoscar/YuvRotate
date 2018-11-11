package com.volcanoscar.yuvutil;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final static int STORAGE_PERMISSION = 100;
    private final static int ROTATION_90_CW = 90;
    private final static int ROTATION_180_CW = 180;
    private final static int ROTATION_270_CW = 270;
    private final static int INPUT_YUV_WIDTH = 640;
    private final static int INPUT_YUV_HEIGHT = 480;
    private final static String WORKER_THREAD = "WORKER_THREAD";

    private Button mButton = null;
    private HandlerThread mWorkerThread = null;
    private Handler mWorkerHandler = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION);
        } else {
            init();
        }
    }

    private void init() {
        if (null == mWorkerThread) {
            mWorkerThread = new HandlerThread(WORKER_THREAD);
            mWorkerThread.start();
            mWorkerHandler = new Handler(mWorkerThread.getLooper());
        }

        TextView tv = findViewById(R.id.sample_text);
        tv.setText(YuvUtilsJni.stringFromJNI());
        mButton = findViewById(R.id.button);
        mButton.setOnClickListener(this);

        if (null != mWorkerHandler) {
            mWorkerHandler.post(new Runnable() {
                @Override
                public void run() {
                    YuvUtilsJni.createAssetManager(getAssets());
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (STORAGE_PERMISSION == requestCode) {
            for (int i = 0; i < permissions.length; i++) {
                if (PackageManager.PERMISSION_GRANTED != grantResults[i]) {
                    finish();
                    return;
                }
            }

            init();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                if (null != mWorkerHandler) {
                    mWorkerHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            YuvUtilsJni.rotateYuv(ROTATION_180_CW, INPUT_YUV_WIDTH, INPUT_YUV_HEIGHT);
                        }
                    });
                }

                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (null != mWorkerHandler) {
            mWorkerHandler.post(new Runnable() {
                @Override
                public void run() {
                    YuvUtilsJni.releaseAssetManager();
                }
            });

            mWorkerThread.quitSafely();
        }
    }
}
