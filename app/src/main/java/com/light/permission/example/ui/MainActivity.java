package com.light.permission.example.ui;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.light.permission.R;
import com.light.permission.annotations.CheckPermissions;
import com.light.permission.annotations.IsPermissionHandler;
import com.light.permission.runtime.PermissionGrantedResult;
import com.light.permission.runtime.PermissionHandler;
import com.light.permission.runtime.PermissionProcessor;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends /*BaseActivity*/AppCompatActivity {

    @BindView(R.id.button)
    Button btn;

    @BindView(R.id.button2)
    Button btn2;

    private boolean mIsJump2Settings;
    @IsPermissionHandler
    private PermissionHandler pHandler = new PermissionHandler() {
        @Override
        public Dialog getExplanationDialog(Context ctx, @NonNull DialogInterface.OnClickListener onPositiveClickListener, String... permissions) {
            return super.getExplanationDialog(ctx, onPositiveClickListener, permissions);
        }

        @Override
        public void onAllPermissionsGranted(@NonNull String[] permissions) {
            openCamera();
        }

        @Override
        public void onPermissionsFailed(@NonNull PermissionGrantedResult[] results) {
            Log.e("TAGA", "onPermissionsFailed_results:"+ Arrays.toString(results));

            boolean shouldShowRationale = false;
            for(PermissionGrantedResult result : results) {
                if(result.isShouldShowRationale()) {
                    shouldShowRationale = true;
                    break;
                }
            }

            final boolean finalShouldShowRationale = shouldShowRationale;
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage(shouldShowRationale ? "拒绝我！我想我需要跟你再解释解释" : "没有权限，我不干");
            builder.setPositiveButton("去设置", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mIsJump2Settings = true;
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                }
            });
            builder.setCancelable(false);
            builder.create().show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera();
                Log.e("TAGA", "after openCamera");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mIsJump2Settings) {
            mIsJump2Settings = false;
        }
    }

    @CheckPermissions({Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void openCamera(){
        Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(it, 128);
        Log.e("TAGA", "now is openCamera method");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionProcessor.handleResult(this, pHandler, permissions, grantResults);
    }
}
