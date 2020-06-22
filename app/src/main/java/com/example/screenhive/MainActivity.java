package com.example.screenhive;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));

            startActivityForResult(intent, PERMISSION_REQUEST_CODE);
        }
    }

    public void startService(View V) {
        Intent serviceIntent = new Intent(MainActivity.this, ChatHeadService.class);
        startService(serviceIntent);
    }

    public void stopService(View V) {

        AlertDialog.Builder altdial= new AlertDialog.Builder(MainActivity.this);
        altdial.setMessage("Please remember that the timer will get paused").setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent serviceIntent = new Intent(MainActivity.this, ChatHeadService.class);
                        stopService(serviceIntent);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = altdial.create();
        alert.setTitle("Are you sure?");
        alert.show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PERMISSION_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                //showChatHead();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopService(new Intent(MainActivity.this, ChatHeadService.class));
        Intent serviceIntent = new Intent(MainActivity.this, ChatHeadService.class);
        startService(serviceIntent);

    }

    @Override
    public void onBackPressed() {
    }


}
