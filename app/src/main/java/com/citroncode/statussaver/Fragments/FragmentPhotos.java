package com.citroncode.statussaver.Fragments;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lazygeniouz.filecompat.file.DocumentFileCompat;
import com.tapadoo.alerter.Alerter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class FragmentPhotos extends Fragment {

    ProgressBar progressBar;
    RecyclerView rvStatuses;
    TextView tvNoStatuses;
    Context ctx;
    RecyclerView.LayoutManager layoutManager;
    PhotosAdapter rv_adapter;
    private Activity mActivity;
    static public DocumentFileCompat dir;
    public List<DocumentFileCompat> fileListed;
    FloatingActionButton fab_save_photo;
    StorageFunctions storageHelper;
    SharedPreferences.Editor editor;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_photos, container, false);

    }
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progressBar = view.findViewById(R.id.pg_photos);
        rvStatuses = view.findViewById(R.id.rv_photos);
        tvNoStatuses = view.findViewById(R.id.tv_no_photos);
        fab_save_photo = view.findViewById(R.id.fab_save_photos);

        tvNoStatuses.setVisibility(View.GONE);
        layoutManager = new GridLayoutManager(ctx,3);

        editor = MainActivity.sharedPreferences.edit();
        storageHelper = new StorageFunctions();
        setUpRecyclerView();

        fab_save_photo.setOnClickListener(view1 -> {
            for(int i = 0;i < MainActivity.filePathsPhotos.size();i++){

                if (!MainActivity.filePathsPhotosChecked.get(i).equals("0")) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        displayAlerter(storageHelper.savePhotoQ(ctx, getBitmapOnAndroidQ(Uri.parse(MainActivity.filePathsPhotos.get(i)))));
                    }else{
                        displayAlerter(storageHelper.save(new File(MainActivity.filePathsPhotos.get(i)), 0, ctx));
                    }

                }
            }
            if(MainActivity.sharedPreferences.getInt("counter",0) >= 5){
                showFullscreenAd();
                editor.putInt("counter", 0);
            }else{
                editor.putInt("counter", MainActivity.sharedPreferences.getInt("counter",0) + 1);
            }
            editor.apply();
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
            MainActivity.filePathsPhotos.clear();
            MainActivity.statusMode = 0;
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                     dir = DocumentFileCompat.Companion.fromTreeUri(ctx, MainActivity.uri);
                    assert dir != null;
                    fileListed = dir.listFiles();

                    for(int i = 0;i < fileListed.size();i++){
                        try{
                            if(!Objects.requireNonNull(fileListed.get(i).getName()).contains(".mp4") && !Objects.requireNonNull(fileListed.get(i).getName()).contains(".nomedia")){
                                MainActivity.filePathsPhotos.add(String.valueOf(fileListed.get(i).getUri()));
                            }

                        }catch (NullPointerException e){
                            Toast.makeText(ctx, R.string.error_contact_dev, Toast.LENGTH_SHORT).show();
                        }

                    }

                    for (int y = 0; y < MainActivity.filePathsPhotos.size();y++){
                        MainActivity.filePathsPhotosChecked.add("0");
                    }

                }else{
                    File oldStatusPath = new File("/storage/emulated/0/WhatsApp/Media/.Statuses");
                    File[] files = oldStatusPath.listFiles();
                    for (int i = 0; i < files.length; ++i) {
                        File file = files[i];
                        if(!file.getAbsolutePath().contains("nomedia") && !file.getAbsolutePath().endsWith(".mp4")){
                            MainActivity.filePathsPhotos.add(file.getAbsolutePath());
                        }
                    }
                    for (int y = 0; y < MainActivity.filePathsPhotos.size();y++){
                        MainActivity.filePathsPhotosChecked.add("0");
                    }

                }
                return true;

            }catch (NullPointerException e){
                Toast.makeText(ctx, R.string.error_contact_dev, Toast.LENGTH_SHORT).show();
            }
            return false;
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
        MainActivity.filePathsPhotos.clear();
        MainActivity.filePathsPhotosChecked.clear();
        checkFAB();

        progressBar.setVisibility(View.VISIBLE);
        setUpRecyclerView();
    }
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof Activity){
            mActivity =(Activity) context;
            ctx = context;
        }

    }
    public Bitmap getBitmapOnAndroidQ(Uri uri) {
        Bitmap bitmap = null;
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
    public void showFullscreenAd(){
        if (MainActivity.mInterstitialAd != null) {
            MainActivity.mInterstitialAd.show(mActivity);
            Toast.makeText(ctx, R.string.saved_s, Toast.LENGTH_SHORT).show();
        } else {
            Log.d("show fullscreen", "Cant display -> No ad available!");
        }
    }
}
