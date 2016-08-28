package com.pikapika.lite.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

import com.pikapika.lite.R;

/**
 * Created by flavioreyes on 8/23/16.
 */
public class PermissionUtils {
    public final static int READ_PERMISSION_REQUESTED = 1300;
    public final static int WRITE_PERMISSION_REQUESTED = 1400;

    public static boolean doWeHaveReadWritePermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean doWeHaveReadExternalStoragePermission(Context context) {
        return (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    public static boolean doWeHaveWriteExternalStoragePermission(Context context) {
        return (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }
}
