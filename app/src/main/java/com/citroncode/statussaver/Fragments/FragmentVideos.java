package com.citroncode.statussaver.Fragments;

import static com.citroncode.statussaver.Fragments.FragmentPhotos.dir;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.citroncode.statussaver.Adapter.VideosAdapter;
import com.citroncode.statussaver.MainActivity;
import com.citroncode.statussaver.R;
import com.citroncode.statussaver.Utils.StorageFunctions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lazygeniouz.filecompat.file.DocumentFileCompat;
import com.tapadoo.alerter.Alerter;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;


public class FragmentVideos extends Fragment {

    ProgressBar progressBar;
    RecyclerView rvStatuses;
    TextView tvNoStatuses;
    Context ctx;
    RecyclerView.LayoutManager layoutManager;
    VideosAdapter rv_adapter;
    private Activity mActivity;
    public static final File STATUS_DIRECTORY = new File(Environment.getExternalStorageDirectory() + File.separator + "Android/media/com.whatsapp/WhatsApp/Media/.Statuses");
    List<DocumentFileCompat> fileListed2;
    FloatingActionButton fab_save_video;
    StorageFunctions storageHelper;
    SharedPreferences.Editor editor;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_videos, container, false);
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progressBar = view.findViewById(R.id.pg_videos);
        rvStatuses = view.findViewById(R.id.rv_videos);
        tvNoStatuses = view.findViewById(R.id.tv_no_videos);
        fab_save_video = view.findViewById(R.id.fab_save_videos);

        editor = MainActivity.sharedPreferences.edit();
        tvNoStatuses.setVisibility(View.GONE);
        layoutManager = new GridLayoutManager(ctx,3);
        storageHelper = new StorageFunctions();

        setUpRecyclerView();

        fab_save_video.setOnClickListener(view1 -> {
            for(int i = 0;i < MainActivity.filePathsVideos.size();i++){
                if (MainActivity.filePathsVideosChecked.get(i) != "0") {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {

                        if(storageHelper.saveVideoQ(Uri.parse(MainActivity.filePathsVideos.get(i)),ctx)){
                            displayAlerter(true);
                        }else{
                            displayAlerter(false);
                        }

                    }else{
                        if(storageHelper.save(new File(MainActivity.filePathsVideos.get(i)),1,ctx)){
                            displayAlerter(true);
                        }else{
                            displayAlerter(false);
                        }
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
            if (loadVideos()) {
                mActivity.runOnUiThread(() -> {
                    if (MainActivity.filePathsVideos.size() == 0) {
                        tvNoStatuses.setVisibility(View.VISIBLE);
                    }else{
                        tvNoStatuses.setVisibility(View.GONE);
                    }

                    rvStatuses.setLayoutManager(layoutManager);
                    rv_adapter = new VideosAdapter(FragmentVideos.this);
                    rvStatuses.setAdapter(rv_adapter);

                    progressBar.setVisibility(View.GONE);
                });

            }
        }).start();
    }
    public boolean loadVideos(){
        MainActivity.filePathsVideos.clear();
        MainActivity.statusMode = 0;
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                //Didn't work as expected, that's why we grab it from the first fragment.
                dir = DocumentFileCompat.Companion.fromTreeUri(ctx, MainActivity.uri);
                assert dir != null;
                fileListed2 = dir.listFiles();

                for(int i = 0;i < fileListed2.size();i++){
                    try{
                        if(fileListed2.get(i).getName().contains(".mp4") && !fileListed2.get(i).getName().contains(".nomedia")){

                            MainActivity.filePathsVideos.add(String.valueOf(fileListed2.get(i).getUri()));

                        }

                    }catch (Exception e){

                    }

                }

                for (int y = 0; y < MainActivity.filePathsVideos.size();y++){
                    MainActivity.filePathsVideosChecked.add("0");
                }

                return true;

            }else{
                File oldStatusPath = new File("/storage/emulated/0/WhatsApp/Media/.Statuses");
                File[] files = oldStatusPath.listFiles();
                for (int i = 0; i < files.length; ++i) {
                    File file = files[i];
                    if(!file.getAbsolutePath().contains("nomedia") && file.getAbsolutePath().endsWith(".mp4")){
                        MainActivity.filePathsVideos.add(file.getAbsolutePath());
                    }
                }
                for (int y = 0; y < MainActivity.filePathsVideos.size();y++){
                    MainActivity.filePathsVideosChecked.add("0");
                }


                return true;
            }

        }catch (Exception e){
            Log.d("ssw videos ", "stacktrace: " + getStackTrace(e));
            return false;
        }
    }
    public void checkFAB(){
        int anzahl = 0;
        for(int i = 0;i < MainActivity.filePathsVideosChecked.size();i++){
            anzahl = anzahl + Integer.parseInt(MainActivity.filePathsVideosChecked.get(i));
        }

        if (anzahl != 0) {
            fab_save_video.show();
        }else{
            fab_save_video.hide();
        }
    }
    private void reset(){
        MainActivity.filePathsVideos = new ArrayList<>();
        MainActivity.filePathsVideosChecked = new ArrayList<>();
        checkFAB();

        progressBar.setVisibility(View.VISIBLE);
        setUpRecyclerView();
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        //Always use context from onAttach instead of views, could produce exceptions!
        if (context instanceof Activity){
            mActivity = (Activity) context;
            ctx = context;
        }

    }
    //copied from StackOverflow to get Stacktraces easily
    public static String getStackTrace(final Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }
    private void displayAlerter(boolean hasSaved){
        if(hasSaved){
            Alerter.create(mActivity)
                    .setTitle(R.string.saved_s)
                    .setText(R.string.save_s_long_video)
                    .setBackgroundColorRes(R.color.backgroundPrimary)
                    .show();
        }else{
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