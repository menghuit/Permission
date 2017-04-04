package com.light.permission.runtime;

import android.app.Activity;

/**
 * Created by ZhangDi on 2017/3/27.
 */

public class PermissionConfig {
    /**
     * 当检查没有权限时，自动申请权限
     */
    public boolean autoRequest = true;

    /**
     * 当检查发现没有权限时，是否要给用户展示申请原因
     */
    public boolean showExplanationWhenNoPermission = false;

    /**
     * 是否要参考 android.support.v4.app.ActivityCompat#shouldShowRequestPermissionRationale(Activity, String) 的返回值
     */
    public boolean referToSysDecisionAboutShowExplanation = false;

    // 当申请权限，用户拒绝时，是否要弹出解释对话框
//    public boolean showExplanationWhenPermissionDenied = true;
}
