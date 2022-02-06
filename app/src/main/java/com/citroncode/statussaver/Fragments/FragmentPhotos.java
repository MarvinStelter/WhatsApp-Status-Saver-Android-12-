package com.citroncode.statussaver.Fragments;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.citroncode.statussaver.MainActivity;
import com.citroncode.statussaver.R;
import com.citroncode.statussaver.Adapter.PhotosAdapter;
import com.citroncode.statussaver.Utils.StorageFunctions;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tapadoo.alerter.Alerter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


public class FragmentPhotos extends Fragment {

    ProgressBar progressBar;
    RecyclerView rvStatuses;
    TextView tvNoStatuses;
    Context ctx;
    RecyclerView.LayoutManager layoutManager;
    PhotosAdapter rv_adapter;
    private Activity mActivity;
    public static final File STATUS_DIRECTORY = new File(Environment.getExternalStorageDirectory() + File.separator + "Android/media/com.whatsapp/WhatsApp/Media/.Statuses");
    static public DocumentFile dir;
    public DocumentFile[] fileListed;
    FloatingActionButton fab_save_photo;
    StorageFunctions storageHelper;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_photos, container, false);

    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progressBar = view.findViewById(R.id.pg_photos);
        rvStatuses = view.findViewById(R.id.rv_photos);
        tvNoStatuses = view.findViewById(R.id.tv_no_photos);
        fab_save_photo = view.findViewById(R.id.fab_save_photos);

        tvNoStatuses.setVisibility(View.GONE);
        layoutManager = new GridLayoutManager(ctx,3);

        storageHelper = new StorageFunctions();
        setUpRecyclerView();

        fab_save_photo.setOnClickListener(view1 -> {
                for(int i = 0;i < MainActivity.filePathsPhotos.size();i++){

                    if (!MainActivity.filePathsPhotosChecked.get(i).equals("0")) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {

                            if(storageHelper.savePhotoQ(ctx,getBitmapOnAndroidQ(Uri.parse(MainActivity.filePathsPhotos.get(i))))){
                                displayAlerter(true);
                            }else{
                                displayAlerter(false);
                            }

                        }else{
                            if(storageHelper.save(new File(MainActivity.filePathsPhotos.get(i)),0,ctx)){
                                displayAlerter(true);
                            }else{
                                displayAlerter(false);
                            }

                        }

                    }
                }
               reset();
        });
    }
    private void setUpRecyclerView(){
        new Thread(() -> {
            if (loadPhotos()) {

                mActivity.runOnUiThread(() -> {
                    if (MainActivity.filePathsPhotos.size() == 0) {
                        tvNoStatuses.setVisibility(View.VISIBLE);
                    }else{
                        tvNoStatuses.setVisibility(View.GONE);
                    }

                    rvStatuses.setLayoutManager(layoutManager);
                    rv_adapter = new PhotosAdapter(FragmentPhotos.this);
                    rvStatuses.setAdapter(rv_adapter);

                    progressBar.setVisibility(View.GONE);
                });

            }
        }).start();
    }
    public boolean loadPhotos(){

            MainActivity.statusMode = 0;
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                     dir = DocumentFile.fromTreeUri(ctx, MainActivity.uri);
                     fileListed = dir.listFiles();

                    for(int i = 0;i < fileListed.length;i++){
                        try{
                            if(!fileListed[i].getName().contains(".mp4") && !fileListed[i].getName().contains(".nomedia")){
                                MainActivity.filePathsPhotos.add(String.valueOf(fileListed[i].getUri()));
                            }

                        }catch (Exception e){
                            //TODO Show error
                        }

                    }

                    for (int y = 0; y < MainActivity.filePathsPhotos.size();y++){
                        MainActivity.filePathsPhotosChecked.add("0");
                    }
                    return true;

                }else{

                    File[] files = STATUS_DIRECTORY.listFiles();
                    for (int i = 0; i < files.length; ++i) {
                        File file = files[i];
                        if(!file.getAbsolutePath().contains("nomedia") && !file.getAbsolutePath().endsWith(".mp4")){
                            MainActivity.filePathsPhotos.add(file.getAbsolutePath());
                        }
                    }
                    for (int y = 0; y < MainActivity.filePathsPhotos.size();y++){
                        MainActivity.filePathsPhotosChecked.add("0");
                    }

                    return true;
                }

            }catch (Exception e){
                Log.i("ssw photos ", e.getMessage());
                return false;
            }
    }
    public void checkFAB(){
        int anzahl = 0;
        for(int i = 0;i < MainActivity.filePathsPhotosChecked.size();i++){
            anzahl = anzahl + Integer.parseInt(MainActivity.filePathsPhotosChecked.get(i));
        }

        if (anzahl != 0) {
            fab_save_photo.show();
        }else{
            fab_save_photo.hide();
        }
    }
    private void reset(){
        //TODO Improve
        MainActivity.filePathsPhotos = new ArrayList<>();
        MainActivity.filePathsPhotosChecked = new ArrayList<>();
        checkFAB();

        progressBar.setVisibility(View.VISIBLE);
        setUpRecyclerView();
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity){
            mActivity =(Activity) context;
            ctx = context;
        }

    }
    public Bitmap getBitmapOnAndroidQ(Uri uri) {
        Bitmap bitmap = null;
        /*try {
            InputStream is = ctx.getContentResolver().openInputStream(uri);
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(ctx, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } */
        ContentResolver contentResolver = mActivity.getContentResolver();
        try {
            if(Build.VERSION.SDK_INT < 28) {
                bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri);
            } else {
                ImageDecoder.Source source = ImageDecoder.createSource(contentResolver, uri);
                bitmap = ImageDecoder.decodeBitmap(source);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }
    private void displayAlerter(boolean hasSaved){
        if(hasSaved) {
            Alerter.create(mActivity)
                    .setTitle(R.string.saved_s)
                    .setText(R.string.save_s_long)
                    .setBackgroundColorRes(R.color.backgroundPrimary)
                    .show();

            } else {
                Alerter.create(mActivity)
                        .setTitle(R.string.error)
                        .setText(R.string.error_long)
                        .setBackgroundColorRes(R.color.backgroundPrimary)
                        .show();
            }
    }
}