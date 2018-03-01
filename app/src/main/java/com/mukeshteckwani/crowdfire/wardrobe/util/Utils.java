package com.mukeshteckwani.crowdfire.wardrobe.util;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;

import com.mukeshteckwani.crowdfire.wardrobe.BuildConfig;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.content.ContentValues.TAG;
import static com.mukeshteckwani.crowdfire.wardrobe.view.MainActivity.CAMERA_CAPTURE;
import static com.mukeshteckwani.crowdfire.wardrobe.view.MainActivity.GALLERY_CAPTURE;

/**
 * Created by mukeshteckwani on 31/01/18.
 */

public class Utils {

    public static final String IMAGE_FILE_PATH = Environment.getExternalStorageDirectory() + "/Crowdfire";
    private static final String FILE_ENTENSION = ".jpg";

    public static Uri openCamera(Activity activity, String option) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String tempImageName = System.currentTimeMillis() + FILE_ENTENSION;
        PreferenceHelper.addString(PreferenceHelper.CAMERA_FILNAME, tempImageName);
        File photo = new File(IMAGE_FILE_PATH + "/" + option, tempImageName);

        Uri uri = FileProvider.getUriForFile(activity.getApplicationContext(),
                BuildConfig.APPLICATION_ID + ".provider",
                photo);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivityForResult(intent, CAMERA_CAPTURE);
        }
        return uri;
    }

    public static void openGallery(Activity activity, String option) {
        createDirIfNotExists(IMAGE_FILE_PATH + "/" + option);
        Intent captureIntent;
        int reqCode = 0;
        captureIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        captureIntent.setType("image/*");
        captureIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        captureIntent.setAction(Intent.ACTION_GET_CONTENT);
        reqCode = GALLERY_CAPTURE;
        if (captureIntent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivityForResult(captureIntent, reqCode);
        }
    }

    private static void createDirIfNotExists(String path) {
        File file = new File(path);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                android.util.Log.e(TAG, "Problem creating Image folder");
            }
        }
    }

    private static Bitmap getScaledImage(String filePath) {
        Bitmap scaledBitmap = null;
        Bitmap bmp = null;

        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            bmp = BitmapFactory.decodeFile(filePath, options);

            int actualHeight = options.outHeight;
            int actualWidth = options.outWidth;
            float maxHeight = 1632.0f;
            float maxWidth = 1224.0f;
            float imgRatio = actualWidth / actualHeight;
            float maxRatio = maxWidth / maxHeight;

            if (actualHeight > maxHeight || actualWidth > maxWidth) {
                if (imgRatio < maxRatio) {
                    imgRatio = maxHeight / actualHeight;
                    actualWidth = (int) (imgRatio * actualWidth);
                    actualHeight = (int) maxHeight;
                } else if (imgRatio > maxRatio) {
                    imgRatio = maxWidth / actualWidth;
                    actualHeight = (int) (imgRatio * actualHeight);
                    actualWidth = (int) maxWidth;
                } else {
                    actualHeight = (int) maxHeight;
                    actualWidth = (int) maxWidth;
                }
            }

            options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);
            options.inJustDecodeBounds = false;
            options.inDither = false;
            options.inPurgeable = true;
            options.inInputShareable = true;
            options.inTempStorage = new byte[16 * 1024];

            bmp = BitmapFactory.decodeFile(filePath, options);

            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight,
                    Bitmap.Config.ARGB_8888);

            float ratioX = actualWidth / (float) options.outWidth;
            float ratioY = actualHeight / (float) options.outHeight;
            float middleX = actualWidth / 2.0f;
            float middleY = actualHeight / 2.0f;

            Matrix scaleMatrix = new Matrix();
            scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

            if (null == scaledBitmap) {
                return null;
            }

            Canvas canvas = new Canvas(scaledBitmap);
            canvas.setMatrix(scaleMatrix);
            canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2,
                    middleY - bmp.getHeight() / 2, new Paint(
                            Paint.FILTER_BITMAP_FLAG));

            Matrix matrix = getRotationMatrix(filePath);
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);

            return scaledBitmap;

        } catch (OutOfMemoryError exception) {
            Log.e(TAG, "OutOfMemoryError " + exception);
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Exception " + e);
            return null;
        } finally {
            if (bmp != null)
                bmp.recycle();
        }
    }

    private static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }


    private static Matrix getRotationMatrix(String filePath) {

        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);

            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_NORMAL:
                    matrix.postRotate(0);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;
                case ExifInterface.ORIENTATION_UNDEFINED:
                    matrix.postRotate(0);
                    break;
                default:
                    matrix.postRotate(90);
            }

            return matrix;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap getBitmapObjectFromPath(String s) {
        if(TextUtils.isEmpty(s)) {
            return null;
        }
        return BitmapFactory.decodeFile(s);
    }

    public static String getCapturedImage(Context context, Uri imageUri, int requestCode, String mOption) {
        String picturePath;
        if (requestCode == CAMERA_CAPTURE) {
            File f = new File(IMAGE_FILE_PATH + "/" + mOption + "/" + PreferenceHelper.getString(PreferenceHelper.CAMERA_FILNAME));
            picturePath = f.getAbsolutePath();
        }
        else {
            picturePath = getDriveFileAbsolutePath(context, imageUri,mOption);
        }

        writeBitmapToFile(picturePath,getScaledImage(picturePath));
        return picturePath;
    }

    private static void writeBitmapToFile(String filename, Bitmap bmp) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(filename);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
        }
        catch (Exception ex) {

        }
    }

    private static String getDriveFileAbsolutePath(Context context, Uri uri, String mOption) {

        FileInputStream input = null;
        FileOutputStream output = null;
        String fileName = "";
        final String[] projection = {
                MediaStore.MediaColumns.DISPLAY_NAME
        };
        android.content.ContentResolver cr = context.getApplicationContext().getContentResolver();
        Cursor metaCursor = cr.query(uri, projection, null, null, null);
        if (metaCursor != null) {
            try {
                if (metaCursor.moveToFirst()) {
                    fileName = metaCursor.getString(0);
                }
            } finally {
                metaCursor.close();
            }
        } else {
            fileName = System.currentTimeMillis() + ".jpg";
        }

        android.content.ContentResolver resolver = context.getContentResolver();

        try {
            String outputFilePath = new File(IMAGE_FILE_PATH + "/" + mOption, fileName).getAbsolutePath();
            ParcelFileDescriptor pfd = resolver.openFileDescriptor(uri, "r");
            FileDescriptor fd = pfd.getFileDescriptor();
            input = new FileInputStream(fd);
            output = new FileOutputStream(outputFilePath);
            int read = 0;
            byte[] bytes = new byte[4096];
            while ((read = input.read(bytes)) != -1) {
                output.write(bytes, 0, read);
            }
            return new File(outputFilePath).getAbsolutePath();
        } catch (IOException ignored) {
            Log.i(TAG, "io exception");
        } catch (Exception e) {
            Log.i(TAG, "image processing failed");
        } finally {
            try {
                if (input != null)
                    input.close();
                if (output != null)
                    output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    public static void createPermissionDialog(final Context context, String title, String message, String posButtonText, String negButtonText, DialogInterface.OnClickListener btnListener) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);
        if (title != null) {
            alertDialogBuilder.setTitle(title);
        }
        alertDialogBuilder
                .setMessage(message)
                .setCancelable(true)
                .setPositiveButton(posButtonText, btnListener)
                .setNegativeButton(negButtonText, btnListener);

        AlertDialog alertDialog = alertDialogBuilder.create();
        if (!((Activity) context).isFinishing()) {
            alertDialog.show();
        }
    }
}
