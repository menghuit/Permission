package com.light.permission.runtime;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.content.PermissionChecker;
import android.util.Log;

import com.light.permission.annotations.CheckPermissions;
import com.light.permission.annotations.IsPermissionHandler;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZhangDi on 2017/3/22.
 * 负责处理从权限申请到获取权限结果的整个流程
 */
public class PermissionProcessor {
    private static final String TAG = "PermissionProcessor";
    private final static int PERMISSION_REQUEST = 1200;

    public static Object process(ProceedingJoinPoint joinPoint, Object source) {
        String[] permissions = getPermissionsRequired(joinPoint);
        if (permissions != null && permissions.length > 0){
            Operator operator = OperatorFactory.getSuitableOperator(source);
            if (!operator.hasPermissions(source, permissions)) {
                new PermissionRequest(source, permissions).request();
                return null;
            }
        } else {
            Log.e(TAG, "No found the permissions that you request!");
        }
        return runOriginLogic(joinPoint, source);
    }

    /**
     * 从注解中获取要请求的权限
     * @param joinPoint 切入点
     * @return 权限
     */
    private static String[] getPermissionsRequired(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        return method.getAnnotation(CheckPermissions.class).value();
    }

    private static Object runOriginLogic(ProceedingJoinPoint joinPoint, Object source) {
        try {
            return joinPoint.proceed();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return null;
    }

    private static PermissionHandler findPermissionHandler(Object source) {
        if (source == null) {
            return null;
        }
        Class clazz = source.getClass();
        if (clazz == null) {
            return null;
        }
        PermissionHandler handler;
        for (final Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(IsPermissionHandler.class)) {
                field.setAccessible(true);
                try {
                    handler = (PermissionHandler) field.get(source);
                    return handler;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    Log.e(TAG, String.format("Error:%1s is not instance of %2s", field.getName(), PermissionHandler.class.getName()));
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * 处理权限请求之后的结果
     * @param ctx          上下文
     * @param handler      {@link PermissionHandler}
     * @param permissions  申请的权限
     * @param grantResults 对应权限的申请结果
     */
    public static void handleResult(Context ctx, PermissionHandler handler, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (handler == null || permissions.length != grantResults.length) {
            return;
        }
        PermissionGrantedResult[] results = new PermissionGrantedResult[permissions.length];
        if (grantResults.length > 0) {
            Operator operator = OperatorFactory.getSuitableOperator(ctx);
            boolean hasNoGranted = false;
            for (int i = 0; i < grantResults.length; i++) {
                boolean isGranted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
                results[i] = new PermissionGrantedResult(permissions[i], isGranted,
                        operator.shouldShowRequestPermissionRationale(ctx, permissions[i]));
                if (!isGranted) {
                    hasNoGranted = true;
                }
            }
            if (hasNoGranted) {
                handler.onPermissionsFailed(results);
            } else {
                handler.onAllPermissionsGranted(permissions);
            }

        } else {
            handler.onPermissionsFailed(results);
        }
    }

    private static class PermissionRequest {
        private Object source;
        private String[] permissionsRequired;

        PermissionRequest(Object source, String[] permissionsRequired) {
            this.source = source;

            this.permissionsRequired = permissionsRequired;
        }

        void request() {
//            if (permissionsRequired.length == 0) {
//                return runOriginLogic(joinPoint, source);
//            }
            final Operator operator = OperatorFactory.getSuitableOperator(source);
            PermissionHandler handler = findPermissionHandler(source);
            // 没有指定handler时，只申请权限，不做其他处理。
            if (handler == null) {
                operator.requestPermissions(source, permissionsRequired, PERMISSION_REQUEST);
                return;
            }
            PermissionConfig config = getConfig(handler);

            // 当没有权限时，申请前需要向用户解释申请理由
            if (config.showExplanationWhenNoPermission) {
                List<String> permissionsShouldShowRationale = new ArrayList<>();
                for (String permission : permissionsRequired) {
                    // 不参考系统shouldShowRequestPermissionRationale的决定
                    // 或者
                    // 参考系统决定，并且 系统返回true
                    // 这样才会展示“对申请权限解释”的Dialog
                    if (!config.referToSysDecisionAboutShowExplanation ||
                            operator.shouldShowRequestPermissionRationale(source, permission)) {
                        permissionsShouldShowRationale.add(permission);
                    }
                }
                if (!permissionsShouldShowRationale.isEmpty()) {
                    // Generate an explanation dialog and show it.
                    Dialog dialog = handler.getExplanationDialog(operator.getContext(source), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            operator.requestPermissions(source, permissionsRequired, PERMISSION_REQUEST);
                        }
                    }, permissionsShouldShowRationale.toArray(new String[]{}));

                    if(dialog!= null){
                        dialog.setCancelable(false);
                        dialog.show();
                        return;
                    }
                }
            }
            operator.requestPermissions(source, permissionsRequired, PERMISSION_REQUEST);
        }

        private PermissionConfig getConfig(PermissionHandler handler) {
            PermissionConfig config = null;
            if(handler != null) {
                config = handler.getConfig();
            }
            if(config == null) {
                config = getDefaultConfig();
            }
            return config;
        }

        private PermissionConfig getDefaultConfig() {
            return new PermissionConfig();
        }
    }
}
