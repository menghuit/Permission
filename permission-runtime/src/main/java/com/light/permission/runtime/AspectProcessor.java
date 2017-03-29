package com.light.permission.runtime;

import android.os.Build;
import android.util.Log;

import com.light.permission.annotations.CheckPermissions;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

import static android.content.ContentValues.TAG;

/**
 * Created by ZhangDi on 2017/3/22.
 */

@Aspect
public class AspectProcessor {

    private static final String POINT_CUT_CHECK_PERMISSION = "execution(@com.light.permission.annotations.CheckPermissions * *(..))";

    @Pointcut(POINT_CUT_CHECK_PERMISSION)
    public void methodCheckPermission() {}

    @Around("methodCheckPermission() && this(source)")
    public Object CheckPermissions(final ProceedingJoinPoint joinPoint, Object source) throws Throwable{
        Log.e("TAGA", "CheckPermission:"+source.toString()/*+",handler:"+(handler ==null)*/);
        Log.e("TAGA", "source.getClass:"+source.getClass());
        Log.e("TAGA", "target:"+joinPoint.getTarget().getClass());
        Log.e("TAGA", "this:"+joinPoint.getThis().getClass());


        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        final String[] permissionList = method.getAnnotation(CheckPermissions.class).value();
        Log.e("TAGA", ">>> " + signature.getName() + "() requires " + permissionList.length + " permission");

        if (Build.VERSION.SDK_INT < 23) {
            return joinPoint.proceed();
        }
        return PermissionProcessor.process(joinPoint, source);
    }

//    @Before("execution(* android.app.Activity.on**(..))")
//    public void onActivityMethodBefore(JoinPoint joinPoint) throws Throwable {
//        String key = joinPoint.getSignature().toString();
//        Log.e("TAGA", "onActivityMethodBefore: " + key);
//    }

//    @After("execution(public void onRequestPermissionsResult* *.*(..)) && this(source)")
//    public void onResumeAspectJ(ProceedingJoinPoint joinPoint, Object source) throws Throwable {
//        Log.e("TAGA", "joinPoint:"+(joinPoint == null? "joinPoint is null":joinPoint.toString())+","+source.toString());
//        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
//        Method method = signature.getMethod();
//        Log.e("TAGA", "this is onResume AspectJ："+method.getName());
//    }
}
