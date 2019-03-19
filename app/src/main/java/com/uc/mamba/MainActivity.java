package com.uc.mamba;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.github.dfqin.grantor.PermissionListener;
import com.github.dfqin.grantor.PermissionsUtil;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_CODE_CHOOSE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_camera).setOnClickListener(this);
        findViewById(R.id.btn_player).setOnClickListener(this);
        findViewById(R.id.btn_picture).setOnClickListener(this);
        if (PermissionsUtil.hasPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            //有访问摄像头的权限
        } else {
            PermissionsUtil.requestPermission(this, new PermissionListener() {
                @Override
                public void permissionGranted(@NonNull String[] permissions) {
                    //用户授予了访问摄像头的权限
                }


                @Override
                public void permissionDenied(@NonNull String[] permissions) {
                    //用户拒绝了访问摄像头的申请
                }
            }, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE});
        }
        if (PermissionsUtil.hasPermission(this, Manifest.permission.CAMERA)) {
            //有访问摄像头的权限
        } else {
            PermissionsUtil.requestPermission(this, new PermissionListener() {
                @Override
                public void permissionGranted(@NonNull String[] permissions) {
                    //用户授予了访问摄像头的权限
                }


                @Override
                public void permissionDenied(@NonNull String[] permissions) {
                    //用户拒绝了访问摄像头的申请
                }
            }, new String[]{Manifest.permission.CAMERA});
        }
    }

    @Override
    public void onClick(View v) {
        Intent it=new Intent(v.getContext(),PreviewActivity.class);
        switch (v.getId()) {
            case R.id.btn_picture:
                it.putExtra("type",1);
                break;
            case R.id.btn_camera:
                it.putExtra("type",2);
                break;
            case R.id.btn_player:
                it.putExtra("type",3);
                break;
        }
        startActivity(it);
    }
public static   List<Uri> mSelected;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
           mSelected = Matisse.obtainResult(data);
            Log.d("Matisse", "mSelected: " + mSelected);
        }

    }
}
