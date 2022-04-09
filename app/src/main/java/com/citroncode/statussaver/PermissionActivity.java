package com.citroncode.statussaver;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.UriPermission;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.DocumentsContract;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.List;
import java.util.Objects;

public class PermissionActivity extends AppCompatActivity{

    Uri uri;
    ActivityResultLauncher<Intent> resultLauncher;
    String stringWA = "primary:Android/media/com.whatsapp/WhatsApp/Media/.Statuses/";
    final int REQ_CODE_EXTERNAL_STORAGE_PERMISSION = 45;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions);
        showAccessDiaalog();

        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();

                        uri = null;
                        uri = Objects.requireNonNull(data).getData();

                        getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        Log.i("wss","uri loaded: " + uri.toString());

                        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(),0);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("path", uri.toString());
                        editor.apply();
                        startApp();

                    }

                });
    }
    private void showAccessDiaalog(){
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this,R.style.AlertCostumTheme);
        builder.setTitle(R.string.storage_access_required);
        builder.setMessage(R.string.alert_long);
        builder.setCancelable(true);

        builder.setPositiveButton(
                R.string.allow,
                (dialog, id) -> {

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        getAccess();
                    }else{
                        if(ActivityCompat.checkSelfPermission(PermissionActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                            startApp();
                        } else {
                            ActivityCompat.requestPermissions(PermissionActivity.this,new  String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQ_CODE_EXTERNAL_STORAGE_PERMISSION);
                        }
                    }

                });

        builder.setNegativeButton(
                R.string.cancel,
                (dialog, id) -> {
                    Toast.makeText(PermissionActivity.this, R.string.toast_no_permissions, Toast.LENGTH_SHORT).show();
                    finish();

                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    private Boolean checkIfGotAccess() {
        List<UriPermission> permissionList = getContentResolver().getPersistedUriPermissions();
        for (int i = 0; i < permissionList.size(); i++) {
            UriPermission it = permissionList.get(i);
            if (it.getUri().equals(DocumentsContract.buildDocumentUri((String)"com.android.externalstorage.documents", (String)stringWA)) && it.isReadPermission())
                return true;
        }
        return false;
    }
    private void getAccess(){
        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(),0);
        String path = sharedPreferences.getString("path","");
        if (!checkIfGotAccess() && path.length() != 0) {

            uri = Uri.parse(sharedPreferences.getString("path",""));
            Log.i("wss","uri loaded: " + uri.toString());

            startApp();

        }else{


            uri = DocumentsContract.buildDocumentUri("com.android.externalstorage.documents", stringWA);
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            resultLauncher.launch(intent.putExtra("android.provider.extra.INITIAL_URI", (Parcelable)DocumentsContract.buildDocumentUri((String)"com.android.externalstorage.documents", (String)stringWA)));

        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == REQ_CODE_EXTERNAL_STORAGE_PERMISSION && grantResults.length >0 &&grantResults[0] == PackageManager.PERMISSION_GRANTED){
            startApp();
        }
    }
    private void startApp(){
        startActivity(new Intent(PermissionActivity.this, MainActivity.class));
        finish();
    }
}