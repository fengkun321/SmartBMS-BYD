package com.smart.bms_byd.permissionInfo;

import android.content.Context;
import android.content.pm.PackageManager;

/**
 * 妫?鏌ユ潈闄愮殑宸ュ叿绫?
 * <p/>
 * Created by wangchenlong on 16/1/26.
 */
public class PermissionsChecker {

	private final Context mContext;
	PackageManager pm = null;
    public PermissionsChecker(Context context) {
        mContext = context.getApplicationContext();
        pm = mContext.getPackageManager();  
    }

    // 鍒ゆ柇鏉冮檺闆嗗悎
    public boolean lacksPermissions(String... permissions) {
        for (String permission : permissions) {
            if (lacksPermission(permission)) {
                return true;
            }
        }
        return false;
    }

    // 鍒ゆ柇鏄惁缂哄皯鏉冮檺
    private boolean lacksPermission(String permission) {
    	 boolean isPer = (PackageManager.PERMISSION_DENIED ==
 	            pm.checkPermission(permission, mContext.getPackageName())); 
    	 return isPer;
    }
	
	
}
