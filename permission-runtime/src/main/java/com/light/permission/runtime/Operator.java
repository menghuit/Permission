package com.light.permission.runtime;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * Created by ZhangDi on 2017/3/21.
 * 这里对一些权限相关的方法做一次抽象
 */

public interface Operator {
    Context getContext(Object source);
//    int checkPermission(Object source, String permission);
    boolean hasPermissions(Object source, String... permissions);
    void requestPermissions(Object source, @NonNull String[] permissions, int requestCode);
    // 1、APP没有申请这个权限的话，返回false
    // 2、用户拒绝时，勾选了不再提示的话，返回false
    // 3、用户拒绝，但是没有勾选不再提示的话，返回true
    boolean shouldShowRequestPermissionRationale(Object source, @NonNull String permission);
}
