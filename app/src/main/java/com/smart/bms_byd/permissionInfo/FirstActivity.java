package com.smart.bms_byd.permissionInfo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.smart.bms_byd.BaseActivity;
import com.smart.bms_byd.MainActivity;
import com.smart.bms_byd.BaseApplication;
import com.smart.bms_byd.R;
import com.smart.bms_byd.otherPage.PrivacyActivity;
import com.smart.bms_byd.util.BaseVolume;

import java.util.Timer;
import java.util.TimerTask;

public class FirstActivity extends BaseActivity {

    private static final int REQUEST_CODE = 0; // 请求�?
    private ImageView imgLogo;

    // �?�?的全部权�?
    @SuppressLint("InlinedApi")
    static final String[] PERMISSIONS = new String[]{
            Manifest.permission.CAMERA,// 拍照
            Manifest.permission.WRITE_EXTERNAL_STORAGE,// 修改本地文件权限
            Manifest.permission.READ_EXTERNAL_STORAGE,// 读取本地文件权限

            Manifest.permission.REQUEST_INSTALL_PACKAGES,
            Manifest.permission.ACCESS_COARSE_LOCATION,// 位置权限
            Manifest.permission.ACCESS_FINE_LOCATION,
//            Manifest.permission.ACCESS_BACKGROUND_LOCATION, //全新的定位权限
            Manifest.permission.READ_PHONE_STATE,// 设备唯一标识
            Manifest.permission.ACCESS_MEDIA_LOCATION,  //访问照片的位置信息

    };

    private PermissionsChecker mPermissionsChecker; // 权限�?测器


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
        mPermissionsChecker = new PermissionsChecker(this);
        imgLogo = findViewById(R.id.imgLogo);
    }

    public void onResume() {
        super.onResume();

        imgLogo.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.alpha_logo));

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // 缺少权限�?, 进入权限配置页面
                if (mPermissionsChecker.lacksPermissions(PERMISSIONS)) {
                    startPermissionsActivity();
                } else {
                    gotoNextActivity();

                }
            }
        },1000);

    }

    private void startPermissionsActivity() {
        PermissionsActivity.startActivityForResult(this, REQUEST_CODE, PERMISSIONS);
    }

    private boolean isCheckLogin = true;
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 拒绝�?, 关闭页面, 缺少主要权限, 无法运行
        if (requestCode == REQUEST_CODE && resultCode == PermissionsActivity.PERMISSIONS_DENIED) {
            finish();
        } else {
            if (isCheckLogin) {
                isCheckLogin = false;
                gotoNextActivity();
            }
        }
    }

    /** 前往下一个页面 */
    private void gotoNextActivity() {

        // 第一次打开，则先进入隐私政策页面
        if (BaseApplication.getInstance().getBooleanBySharedPreferences(BaseVolume.FIRST_RUN_APPLICATION,true)) {
            startActivity(new Intent(FirstActivity.this, PrivacyActivity.class));
            finish();
        }
        else {
            startActivity(new Intent(FirstActivity.this, MainActivity.class));
            finish();
        }

    }




}
