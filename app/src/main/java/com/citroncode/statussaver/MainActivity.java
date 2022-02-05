package com.citroncode.statussaver;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.UriPermission;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.citroncode.statussaver.Adapter.FragmentAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.hbisoft.pickit.PickiT;
import com.hbisoft.pickit.PickiTCallbacks;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;


public class MainActivity extends AppCompatActivity implements PickiTCallbacks {

    public static ArrayList<String> filePathsPhotos;
    public static ArrayList<String> filePathsSaved;
    public static  ArrayList<String> filePathsVideos;
    public static ArrayList<String> filePathsPhotosChecked;
    public static  ArrayList<String> filePathsVideosChecked;
    public static int statusMode = 0;
    final int REQ_CODE_EXTERNAL_STORAGE_PERMISSION = 45;
    ViewPager vp_fragments;
    RelativeLayout rl_main;
    FloatingActionButton fab_save;
    FragmentAdapter fragmentAdapter;
    PickiT pickiT;
    public static boolean darkmode_state;
    public static Uri uri;
    String stringWA = "primary:Android/media/com.whatsapp/WhatsApp/Media/.Statuses/";
    private InterstitialAd mInterstitialAd;
    TabLayout tabLayout;
    ActivityResultLauncher<Intent> resultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        darkmode_state = getSharedPreferences(getPackageName(), MODE_PRIVATE).getBoolean("darkmode", true);
        if (darkmode_state) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        Objects.requireNonNull(getSupportActionBar()).setElevation(0);
        iniApp();

        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();

                        uri = null;
                        uri = data.getData();

                        getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        Log.i("wss","uri loaded: " + uri.toString());

                        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(),0);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("path", uri.toString());
                        editor.apply();

                        loadFragments();

                    }

                });

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
          getAccess();
        }else{
            if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                loadFragments();
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,new  String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQ_CODE_EXTERNAL_STORAGE_PERMISSION);
            }
        }
        //start the ad after a few seconds to keep the app starting fast otherwise it's slow as f*ck
        //Maybe there's another fix for that
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> loadAds());
            }
        }, 5000);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                new Thread(() -> {
                  if(tab.getPosition() == 0){
                      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                          //loadPhotosQ(uri);
                      }else{
                          //photos();
                      }

                  }else{
                      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                          //loadVideosQ(uri);
                      }else{
                        //  videos();
                      }

                  }
                }).start();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


    }
    private void loadAds(){
        MobileAds.initialize(this, initializationStatus -> {
        });
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        InterstitialAd.load(this,"ca-app-pub-2797112522958944/7231654693", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                        Log.i("load fullscreen", "onAdLoaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.i("load fullscreen", loadAdError.getMessage());
                        mInterstitialAd = null;
                    }
                });
    }
    private Boolean checkIfGotAccess() {
        List<UriPermission> permissionList = getContentResolver().getPersistedUriPermissions();
        for (int i = 0; i < permissionList.size(); i++) {
            UriPermission it = permissionList.get(i);
            if (it.getUri().equals((Parcelable)DocumentsContract.buildDocumentUri((String)"com.android.externalstorage.documents", (String)stringWA)) && it.isReadPermission())
                return true;
        }
        return false;
    }
    private void getAccess(){
       /* uri = DocumentsContract.buildDocumentUri("com.android.externalstorage.documents", stringWA);
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        resultLauncher.launch(intent.putExtra("android.provider.extra.INITIAL_URI", (Parcelable)DocumentsContract.buildDocumentUri((String)"com.android.externalstorage.documents", (String)stringWA)));
        */

        if (checkIfGotAccess()) {
            uri = DocumentsContract.buildDocumentUri("com.android.externalstorage.documents", stringWA);
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            resultLauncher.launch(intent.putExtra("android.provider.extra.INITIAL_URI", (Parcelable)DocumentsContract.buildDocumentUri((String)"com.android.externalstorage.documents", (String)stringWA)));
        }else{
            SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(),0);
            uri = Uri.parse(sharedPreferences.getString("path",""));
            Log.i("wss","uri loaded: " + uri.toString());

            loadFragments();
        }

    }




    private void showFullscreenAd(){
        if (mInterstitialAd != null) {
            mInterstitialAd.show(MainActivity.this);
        } else {
            Log.d("show fullscreen", "Cant display -> No ad available!");
        }
    }
    private void iniApp(){
        pickiT = new PickiT(this, this);
        filePathsPhotos = new ArrayList<>();
        filePathsVideos = new ArrayList<>();
        filePathsSaved = new ArrayList<>();
        filePathsPhotosChecked = new ArrayList<>();
        filePathsVideosChecked = new ArrayList<>();

        tabLayout = findViewById(R.id.tabs);
        rl_main = findViewById(R.id.rl_main);
        vp_fragments = findViewById(R.id.viewpager_fragments);



    }
    private void loadFragments(){
        fragmentAdapter = new FragmentAdapter(getSupportFragmentManager());
        vp_fragments.setAdapter(fragmentAdapter);
        tabLayout.setupWithViewPager(vp_fragments);
    }
    private void instagramDialog(){
        RelativeLayout rl_instagram = findViewById(R.id.rl_insta);
        rl_instagram.setVisibility(View.VISIBLE);
        CardView cv_instagram = findViewById(R.id.cv_instagram);
        ImageView close = findViewById(R.id.iv_close);
        close.setOnClickListener(view -> {
            rl_instagram.setVisibility(View.GONE);
            rl_main.setVisibility(View.VISIBLE);
        });

        cv_instagram.setOnClickListener(view -> {
            rl_instagram.setVisibility(View.GONE);
            rl_main.setVisibility(View.VISIBLE);
            Uri uri = Uri.parse("https://www.instagram.com/marvin_stelter");
            Intent likeIng = new Intent(Intent.ACTION_VIEW, uri);
            likeIng.setPackage("com.instagram.android");


            try {
                startActivity(likeIng);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://instagram.com/marvin_stelter")));
            }

        });
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem settingsItem = menu.findItem(R.id.action_theme);
        // set your desired icon here based on a flag if you like
        if (darkmode_state) {
            settingsItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_night));
        }else {
            settingsItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_day));
        }
        return super.onPrepareOptionsMenu(menu);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.action_about:
                    instagramDialog();
                break;
            case R.id.action_privacy:
                CustomTabsIntent.Builder customIntent = new CustomTabsIntent.Builder();
                customIntent.setToolbarColor(ContextCompat.getColor(MainActivity.this, R.color.backgroundPrimary));
                openCustomTab(MainActivity.this, customIntent.build(), Uri.parse("https://api.citroncode.com/android/ss/ss_privacy.html"));
                break;
            case R.id.action_theme:
                    if (darkmode_state) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        getSharedPreferences(getPackageName(), MODE_PRIVATE)
                                .edit()
                                .putBoolean("darkmode", false)
                                .apply();
                    }else{
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        getSharedPreferences(getPackageName(), MODE_PRIVATE)
                                .edit()
                                .putBoolean("darkmode", true)
                                .apply();
                    }
                invalidateOptionsMenu();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void PickiTonStartListener() {

    }

    @Override
    public void PickiTonProgressUpdate(int progress) {

    }

    @Override
    public void PickiTonCompleteListener(String path, boolean wasDriveFile, boolean wasUnknownProvider, boolean wasSuccessful, String Reason) {


    }
    public Bitmap getBitmapOnAndroidQ(Uri uri) {
        Bitmap bitmap = null;
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }
    public static void openCustomTab(Activity activity, CustomTabsIntent customTabsIntent, Uri uri) {
        customTabsIntent.intent.setPackage("com.android.chrome");
        customTabsIntent.launchUrl(activity, uri);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == REQ_CODE_EXTERNAL_STORAGE_PERMISSION && grantResults.length >0 &&grantResults[0] == PackageManager.PERMISSION_GRANTED){
            loadFragments();
        }
    }
}