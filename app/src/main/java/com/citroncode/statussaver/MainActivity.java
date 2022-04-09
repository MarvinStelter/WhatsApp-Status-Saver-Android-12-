package com.citroncode.statussaver;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.browser.customtabs.CustomTabColorSchemeParams;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.citroncode.statussaver.Adapter.FragmentAdapter;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.hbisoft.pickit.PickiT;
import com.hbisoft.pickit.PickiTCallbacks;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;



public class MainActivity extends AppCompatActivity implements PickiTCallbacks{

    public static ArrayList<String> filePathsPhotos;
    public static ArrayList<String> filePathsSaved;
    public static  ArrayList<String> filePathsVideos;
    public static ArrayList<String> filePathsPhotosChecked;
    public static  ArrayList<String> filePathsVideosChecked;
    public static int statusMode = 0;
    ViewPager vp_fragments;
    RelativeLayout rl_main;
    FragmentAdapter fragmentAdapter;
    PickiT pickiT;
    public static boolean darkmode_state;
    public static Uri uri;
    public static InterstitialAd mInterstitialAd;
    TabLayout tabLayout;
    public static int adCounter = 0;
    public static SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        darkmode_state = getSharedPreferences(getPackageName(), MODE_PRIVATE).getBoolean("darkmode", true);
        changeThemeOnStart();
        iniApp();


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), 0);
            if (sharedPreferences.getString("path", "").length() != 0) {
                uri = Uri.parse(sharedPreferences.getString("path", ""));
                loadFragments();
            } else {
                startActivity(new Intent(MainActivity.this, PermissionActivity.class));
            }
        } else {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                loadFragments();
            } else {
                startActivity(new Intent(MainActivity.this, PermissionActivity.class));
            }
        }
        //start the ad after a few seconds to keep the app starting fast otherwise it's slow as f*ck
        //Maybe there's another fix for that
        if(sharedPreferences.getString("consent_showed","").length() == 0){
            GDPRConsentDialog();
        }else{
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(() -> loadAds());
                }
            }, 5000);
        }
    }
    
    private void loadAds(){
        Bundle extras = new Bundle();
        extras.putString("npa", String.valueOf(sharedPreferences.getInt("consent",0)));

        MobileAds.initialize(this, initializationStatus -> {
        });
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                .build();
        mAdView.loadAd(adRequest);

        InterstitialAd.load(this,"ca-app-pub-2797112522958944/7231654693", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                        Log.i("load fullscreen", "onAdLoaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        Log.i("load fullscreen", loadAdError.getMessage());
                        mInterstitialAd = null;
                    }
                });

    }

    private void changeThemeOnStart(){
        if (darkmode_state) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }else{
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
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

        sharedPreferences = getSharedPreferences(getPackageName(),0);
        adCounter = sharedPreferences.getInt("counter",0);

    }
    private void loadFragments(){
        fragmentAdapter = new FragmentAdapter(getSupportFragmentManager());
        vp_fragments.setAdapter(fragmentAdapter);
        tabLayout.setupWithViewPager(vp_fragments);
    }
    private void GDPRConsentDialog(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        AlertDialog dialog;
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this);
        LayoutInflater inflater = this.getLayoutInflater();

        View dialogView= inflater.inflate(R.layout.dialog_gdpr, null);
        dialogBuilder.setView(dialogView);

        TextView tv_more = dialogView.findViewById(R.id.tv_gdpr_text_three);
        Button btn_allow = dialogView.findViewById(R.id.btn_conset_true);
        RelativeLayout btn_reject = dialogView.findViewById(R.id.btn_consent_false);

        dialogBuilder.setCancelable(false);

        dialog = dialogBuilder.create();
        dialog.show();

        btn_allow.setOnClickListener(view -> {
            editor.putInt("consent",1);
            editor.putString("consent_showed","true");
            editor.apply();

            dialog.dismiss();

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(() -> loadAds());
                }
            }, 5000);
        });
        btn_reject.setOnClickListener(view -> {
            editor.putInt("consent",0);
            editor.putString("consent_showed","true");
            editor.apply();

            dialog.dismiss();

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(() -> loadAds());
                }
            }, 5000);
        });

        tv_more.setOnClickListener(view -> {
            dialog.dismiss();
            AlertDialog dialog2;
            MaterialAlertDialogBuilder dialogBuilder2 = new MaterialAlertDialogBuilder(this);
            LayoutInflater inflater2 = this.getLayoutInflater();

            View dialogView2  = inflater2.inflate(R.layout.dialog_gdpr_short, null);
            dialogBuilder2.setView(dialogView2);

            dialogBuilder2.setCancelable(false);

            dialog2 = dialogBuilder2.create();
            dialog2.show();

            TextView tv_partners = dialogView2.findViewById(R.id.tv_show_admob_parnters);
            tv_partners.setOnClickListener(view2 ->{
                openCustomTab("https://policies.google.com/privacy");
            });
            Button btn_back = dialogView2.findViewById(R.id.btn_back);
            btn_back.setOnClickListener(view2 -> {
                dialog2.dismiss();
                GDPRConsentDialog();
            });
        });
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
            Uri uri = Uri.parse("https://www.instagram.com/marvin__stelter");
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
                openCustomTab("https://api.citroncode.com/android/ss/ss_privacy.html");
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
            case  R.id.action_infos:

                MaterialAlertDialogBuilder builder= new MaterialAlertDialogBuilder(MainActivity.this);
                builder.setTitle(R.string.alert_libs_title);

                String[] librarys = {"Glide", "Alerter", "PickIt"};
                builder.setItems(librarys, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            openCustomTab("https://github.com/bumptech/glide");
                        case 1:
                            openCustomTab("https://github.com/Tapadoo/Alerter");
                        case 2:
                            openCustomTab("https://github.com/HBiSoft/PickiT");
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
                break;
            case R.id.action_privacy_settings:
                GDPRConsentDialog();
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
    private  void openCustomTab(String url) {
        Uri uri = Uri.parse(url);

        CustomTabsIntent.Builder customIntent = new CustomTabsIntent.Builder();

        CustomTabColorSchemeParams params = new CustomTabColorSchemeParams.Builder()
                .setNavigationBarColor(ContextCompat.getColor(MainActivity.this,R.color.backgroundPrimaryDark))
                .setToolbarColor(ContextCompat.getColor(MainActivity.this,R.color.backgroundPrimary))
                .setSecondaryToolbarColor(ContextCompat.getColor(MainActivity.this,R.color.backgroundPrimary))
                .build();

        customIntent.setDefaultColorSchemeParams(params);

        CustomTabsIntent customTabsIntent = customIntent.build();
        customTabsIntent.launchUrl(MainActivity.this,uri);

    }


}