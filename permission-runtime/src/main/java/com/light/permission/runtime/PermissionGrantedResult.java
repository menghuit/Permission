package com.light.permission.runtime;

/**
 * Created by ZhangDi on 2017/3/29.
 * 权限被授予情况的结果类
 */

public class PermissionGrantedResult{
    String permissionName;
    boolean isGranted;
    boolean shouldShowRationale;

    public PermissionGrantedResult(String permissionName, boolean isGranted, boolean shouldShowRationale) {
        this.permissionName = permissionName;
        this.isGranted = isGranted;
        this.shouldShowRationale = shouldShowRationale;
    }

    public String getPermissionName() {
        return permissionName;
    }

    public boolean isGranted() {
        return isGranted;
    }

    public boolean isShouldShowRationale() {
        return shouldShowRationale;
    }

    @Override
    public String toString() {
        return "PermissionGrantedResult{" +
                "permissionName='" + permissionName + '\'' +
                ", isGranted=" + isGranted +
                ", shouldShowRationale=" + shouldShowRationale +
                '}';
    }
}
