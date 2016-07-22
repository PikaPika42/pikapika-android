package com.wamp42.pokeradar.utils;

import android.util.Log;

/**
 * Created by Flavio on 23/07/2015.
 */
public class Debug {

    private static boolean isDebug = true;

    public static void d(String data) {
        if(isDebug)
            Log.d("pikapika",data);
    }

    public static void Log(String data) {
        if(isDebug)
            Log.d("pikapika",data);
    }
    public static void Error(String data) {
        if(isDebug)
            Log.e("pikapika", data);
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

    /*

    Debug log what shouldn't be commited
     */
    public static void trash(String data) {
        if(isDebug)
            Log.e("### trash : " + "pikapika", data);
    }

    public static void generateNullPointerException(){
        String str = null;
        str.charAt(1);
    }

}
