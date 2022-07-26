package com.luckmerlin.browser;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.luckmerlin.debug.Debug;
import com.luckmerlin.plugin.PluginApkContext;
import com.luckmerlin.plugin.PluginContext;
import com.luckmerlin.plugin.PluginFileContext;
import com.luckmerlin.view.Content;

public class BrowserActivity extends BrowserContentActivity {
    private final static int REQUEST_PERMISSION_CODE=1099;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!checkPermission()){
            requestPermission();
        }
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            int result = ContextCompat.checkSelfPermission(BrowserActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
            int result1 = ContextCompat.checkSelfPermission(BrowserActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int result2 = ContextCompat.checkSelfPermission(BrowserActivity.this, Manifest.permission.MANAGE_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED &&
                    result1 == PackageManager.PERMISSION_GRANTED&&
                    result2==PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s",getApplicationContext().getPackageName())));
                startActivityForResult(intent, 2296);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, 2296);
            }
        } else {
            //below android 11
            ActivityCompat.requestPermissions(BrowserActivity.this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.MANAGE_EXTERNAL_STORAGE
            }, REQUEST_PERMISSION_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2296) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    // perform action when allow permission success
                } else {
                    Toast.makeText(this, "Allow permission for storage access!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_CODE:
                if (grantResults.length > 0) {
                    boolean READ_EXTERNAL_STORAGE = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean WRITE_EXTERNAL_STORAGE = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (READ_EXTERNAL_STORAGE && WRITE_EXTERNAL_STORAGE) {
                        // perform action when allow permission success
                    } else {
                        Toast.makeText(this, "Allow permission for storage access!", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    @Override
    public Content onResolveContent(Context context) {
//        return new BrowserActivityModel();
//        Object DDD= new ObjectCreator().createObject(FilesDeleteTask.class.getName());
//        Debug.D("eeeeeee "+DDD);
//        new PluginLoader().loadInstalled(this);
//        PluginContext context=new TestPlugin().test(this,"com.browser.dark");
//        Debug.D("DDDDDDDDDD "+context.sss());
//        return null;
//        PluginApkContext pluginApkContext=new PluginApkContext(context,"com.browser.dark");
//        Content content=null!=pluginApkContext?pluginApkContext.createPluginContent(getClass()):null;
//        return null!=content?content:new BrowserModel();
        return new BrowserModel();
        //
//        FilesDeleteTask filesDeleteTask=new FilesDeleteTask(new File(LocalClient.createLocalFile
//                (new java.io.File("/sdcard"),true)));
//        filesDeleteTask.setName("我爱你是第发散发");
//        filesDeleteTask.setCursor(3333);
//        byte[] bytes= ParcelObject.Parceler1.write(filesDeleteTask);
//        Debug.D("EEEEEE "+bytes.length);
//        FilesDeleteTask ddd=ParcelObject.Parceler1.read(bytes,null);
//        Debug.D("EEEEEE "+ddd.getName()+" "+ddd.getCursor()+" "+ddd.getFiles());
        //
//        return null;
    }
}
