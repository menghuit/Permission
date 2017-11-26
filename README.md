# Permission
**这是一个利用注解处理运行时权限的库，简化代码，配置方便，使用简单**

## Gradle配置
### 添加插件到你的项目中:
在工程根目录下或者Module下的build.gradle添加
```
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'com.light.permission:permission-plugin:1.0.0'
    }
}
```
### 应用插件
```
apply plugin: 'com.android.application'
apply plugin: 'light-permission'
```

## java代码
在需要检测权限的方法上添加`CheckPermissions`注解，并指明需要的权限
```
findViewById(R.id.btn_start_camera).setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        openCamera();
    }
});

...

@CheckPermissions({Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE})
public void openCamera(){
    Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    startActivityForResult(it, 128);
    Log.e("TAGA", "now is openCamera method");
}
```
如果需要定制化，可重写PermissionHandler 提供的方法。注意：`必须写@IsPermissionHandler`，否则不起作用
```
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
        // 重写了此方法只是示例说明，请从项目实际出发，看是否需要重写
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
    
    /**
     * 你可以自定义配置, 配置参数参考{@link PermissionConfig}
     * @return
     */
    @Override
    public PermissionConfig getConfig() {
        return config;
    }
};
```

```
@Override
public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    PermissionProcessor.handleResult(this, pHandler, permissions, grantResults);
}
```


## TODO

- 要支持同一个类内不同类型的权限检测（比如：相机，录音）  
- 定制化时，需要在onRequestPermissionsResult调用PermissionProcessor.handleResult方法，可不可以改成添加注解的形式或者自动生成onRequestPermissionsResult方法。


