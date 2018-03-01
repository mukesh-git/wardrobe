package com.mukeshteckwani.crowdfire.wardrobe.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by mukeshteckwani on 31/01/18.
 */

public class PermissionRequestHandler {

    public static final int PERMISSIONS_REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 14;
    public static final int PERMISSIONS_REQUEST_CODE_CAMERA = 15;
    public static final int PERMISSIONS_REQUEST_CAMERA_STORAGE = 16;

    public static final String WRITE_EXTERNAL_STORAGE_PERMISSION = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
    public static final String CAMERA_PERMISSION = android.Manifest.permission.CAMERA;

    public static int checkRequestPermission(Activity activity, String permission) {
        return ContextCompat.checkSelfPermission(activity,
                permission);
    }

    public static void openPermissionRequestDialog(Activity activity, String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(activity,
                permissions,
                requestCode);
    }

    public static void openPermissionSettingsScreen(Context context) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", context.getPackageName(), null));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

}
