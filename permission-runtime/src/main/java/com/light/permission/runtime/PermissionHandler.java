package com.light.permission.runtime;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;

/**
 * Created by ZhangDi on 2017/3/26.
 */

public abstract class PermissionHandler {

    public PermissionConfig getConfig(){
        return null;
    }

    public Dialog getExplanationDialog(Context ctx, @NonNull final DialogInterface.OnClickListener onPositiveClickListener, String... permissions) {
        // 这里的dialog可以自定义
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setMessage(R.string.permission_rationale);
        builder.setPositiveButton(R.string.permission_rationale_dialog_positive_btn_text, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onPositiveClickListener.onClick(dialog, which);
                dialog.dismiss();
            }
        });
        return builder.create();
    }

    public abstract void onAllPermissionsGranted(@NonNull String[] permissions);

    public abstract void onPermissionsFailed(@NonNull PermissionGrantedResult[] results);
}
