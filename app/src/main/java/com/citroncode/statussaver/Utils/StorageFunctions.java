package com.citroncode.statussaver.Utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class StorageFunctions {


    @RequiresApi(api = Build.VERSION_CODES.Q)
    public boolean savePhotoQ(Context context, Bitmap image){
        String currentDate = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String filename = "status_" + currentDate + "_" + new Random().nextInt(61) + 20;
        OutputStream fos;
        boolean saved = false;
        try{
            ContentResolver resolver = context.getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, filename  + ".png");
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/");
            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            fos = resolver.openOutputStream(imageUri);

            saved = image.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();

            return saved;
        } catch (IOException e) {
            Toast.makeText(context, "Sticker couldn't be saved. Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return saved;
        }

    }

    public boolean save(File fileStatus, int statusMode, Context ctx){


        String fileName;
        File destFile;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String currentDateTime = sdf.format(new Date());

        if (statusMode == 1) {
            fileName = "status_" + currentDateTime + new Random().nextInt(61) + 20 + ".mp4";
            destFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + File.separator + fileName);

        } else {
            fileName = "status_" + currentDateTime + new Random().nextInt(61) + 20 + ".jpg";
            destFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + fileName);

        }


        try {

            org.apache.commons.io.FileUtils.copyFile(fileStatus, destFile);
            new SingleMediaScanner(ctx, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM));
            //TODO Notify
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(ctx, "error:  " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
        //TODO Shows ads

    }
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public boolean saveVideoQ(Uri uri3, Context ctx){
        String videoFileName = "status_" + System.currentTimeMillis() + ".mp4";

        ContentValues valuesvideos;
        valuesvideos = new ContentValues();
        valuesvideos.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/");
        valuesvideos.put(MediaStore.Video.Media.TITLE, videoFileName);
        valuesvideos.put(MediaStore.Video.Media.DISPLAY_NAME, videoFileName);
        valuesvideos.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        valuesvideos.put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
        valuesvideos.put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis());
        valuesvideos.put(MediaStore.Video.Media.IS_PENDING, 1);
        ContentResolver resolver = ctx.getContentResolver();
        Uri collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY); //all video files on primary external storage
        Uri uriSavedVideo = resolver.insert(collection, valuesvideos);

        ParcelFileDescriptor pfd;

        try {
            pfd = ctx.getContentResolver().openFileDescriptor(uriSavedVideo,"w");

            assert pfd != null;
            FileOutputStream out = new FileOutputStream(pfd.getFileDescriptor());

            // Get the already saved video as fileinputstream from here
            InputStream in = ctx.getContentResolver().openInputStream(uri3);


            byte[] buf = new byte[8192];

            int len;
            int progress = 0;
            while ((len = in.read(buf)) > 0) {
                progress = progress + len;

                out.write(buf, 0, len);
            }
            out.close();
            in.close();
            pfd.close();
            valuesvideos.clear();
            valuesvideos.put(MediaStore.Video.Media.IS_PENDING, 0);
            valuesvideos.put(MediaStore.Video.Media.IS_PENDING, 0); //only your app can see the files until pending is turned into 0

            ctx.getContentResolver().update(uriSavedVideo, valuesvideos, null, null);

            return true;
        } catch (Exception e) {
            Toast.makeText(ctx, "error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();

            return false;
        }
    }
}
