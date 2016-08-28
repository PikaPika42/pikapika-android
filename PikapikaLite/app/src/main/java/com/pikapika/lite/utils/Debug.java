package com.pikapika.lite.utils;

import android.util.Log;

/**
 * Created by Flavio on 23/07/2015.
 */
public class Debug {
    final private static String TAG = "pikapikalite";
    private static boolean isDebug = false;

    public static void setDebugLogActive(boolean debugActive){
        isDebug = debugActive;
    }

    public static void d(String data) {
        if(isDebug)
            Log.d(TAG,data);
    }

    public static void Log(String data) {
        if(isDebug)
            Log.d(TAG,data);
    }
    public static void Error(String data) {
        if(isDebug)
            Log.e(TAG, data);
    }

    public static void Log(String TAG, String data) {
        if(isDebug)
            Log.d(TAG, data);
    }

    public static void v(String TAG, String data) {
        if(isDebug)
            Log.v(TAG, data);
    }

    public static void d(String TAG, String data) {
        if(isDebug)
            Log.d(TAG, data);
    }


    public static void w(String TAG, String data) {
        if(isDebug)
            Log.w(TAG, data);
    }

    public static void e(String TAG, String data) {
        if(isDebug)
            Log.e(TAG, data);
    }


}
