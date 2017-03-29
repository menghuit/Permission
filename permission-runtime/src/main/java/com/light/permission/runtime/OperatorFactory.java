package com.light.permission.runtime;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ZhangDi on 2017/3/27.
 * Operator Factory
 * {@link OperatorFactory#getSuitableOperator(Object)}
 */

public class OperatorFactory {
    private static class Activity implements Operator {
        static final String TAG = "activity_operator";

        @Override
        public Context getContext(Object source) {
            return (Context) source;
        }

        @Override
        public boolean hasPermissions(Object source, String... permissions) {
            return Utils.hasPermissions(getContext(source), permissions);
        }

        @Override
        public void requestPermissions(Object source, @NonNull String[] permissions, int requestCode) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ((android.app.Activity)source).requestPermissions(permissions, requestCode);
            } else {
                ActivityCompat.requestPermissions((android.app.Activity) source, permissions, requestCode);
            }
        }

        @Override
        public boolean shouldShowRequestPermissionRationale(Object source, @NonNull String permission) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return ((android.app.Activity)source).shouldShowRequestPermissionRationale(permission);
            } else {
                return ActivityCompat.shouldShowRequestPermissionRationale((android.app.Activity) source, permission);
            }
        }
    }

    private static class AppFragment implements Operator {
        static final String TAG = "app_fragment_operator";

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        public Context getContext(Object source) {
            return ((Fragment) source).getActivity();
        }

        @Override
        public boolean hasPermissions(Object source, String... permissions) {
            return Utils.hasPermissions(getContext(source), permissions);
        }

        @Override
        public void requestPermissions(Object source,  @NonNull String[] permissions, int requestCode) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ((Fragment)source).requestPermissions(permissions, requestCode);
            }
        }

        @Override
        public boolean shouldShowRequestPermissionRationale(Object source, @NonNull String permission) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return ((Fragment)source).shouldShowRequestPermissionRationale(permission);
            }
            return false;
        }
    }

    private static class V4Fragment implements Operator {
        static final String TAG = "v4_fragment_operator";

        @Override
        public Context getContext(Object source) {
            return ((android.support.v4.app.Fragment)source).getContext();
        }

        @Override
        public boolean hasPermissions(Object source, String... permissions) {
            return Utils.hasPermissions(getContext(source), permissions);
        }

        @Override
        public void requestPermissions(Object source, @NonNull String[] permissions, int requestCode) {
            ((android.support.v4.app.Fragment)source).requestPermissions(permissions, requestCode);
        }

        @Override
        public boolean shouldShowRequestPermissionRationale(Object source, @NonNull String permission) {
            return ((android.support.v4.app.Fragment)source).shouldShowRequestPermissionRationale(permission);
        }
    }

    static class Utils {
        private static final String TAG = "Operator.Utils";

        static boolean hasPermissions(Context context, String... permissions) {
            // Always return true for SDK < M, let the system deal with the permissions
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                Log.w(TAG, "hasPermissions: API version < M, returning true by default");
                // DANGER ZONE!!! Changing this will break the library.
                return true;
            }

            for (String perm : permissions) {
                boolean hasPerm = (ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED);
                if (!hasPerm) {
                    return false;
                }
            }
            return true;
        }
    }

    private static Map<String, Operator> operators = new HashMap<>();

    public static Operator getSuitableOperator(Object source) {
        Operator operator;
        if (source instanceof android.app.Activity) {
            operator = operators.get(Activity.TAG);
            if(null == operator) {
                operator = new Activity();
            }
            operators.put(Activity.TAG, operator);
            return operator;
        } else if(source instanceof Fragment) {
            operator = operators.get(AppFragment.TAG);
            if (operator == null) {
                operator = new AppFragment();
            }
            operators.put(AppFragment.TAG, operator);
            return operator;
        } else if(source instanceof android.support.v4.app.Fragment) {
            operator = operators.get(V4Fragment.TAG);
            if (operator == null) {
                operator = new V4Fragment();
            }
            operators.put(V4Fragment.TAG, operator);
            return operator;
        } else {
            throw new IllegalArgumentException(String.format("This source(%s) is not supported!", (source == null) ? "null" : source.toString()));
        }
    }

    public static void release() {
        if (operators != null) {
            operators.clear();
        }
    }
}
