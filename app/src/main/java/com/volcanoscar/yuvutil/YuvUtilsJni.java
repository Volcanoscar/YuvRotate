package com.volcanoscar.yuvutil;

import android.content.res.AssetManager;

public class YuvUtilsJni {

    // Used to load the 'YuvUtils' library on application startup.
    static {
        System.loadLibrary("yuvutils");
    }

    public static native String stringFromJNI();
    public static native void createAssetManager(AssetManager assetManager);
    public static native void releaseAssetManager();
    public static native void rotateYuv(int rotation, int width, int height);
}
