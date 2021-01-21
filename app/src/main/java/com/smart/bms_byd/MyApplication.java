package com.smart.bms_byd;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.smart.bms_byd.util.BaseVolume;
import com.smart.bms_byd.util.NetWorkType;
import com.smartIPandeInfo.data.MessageInfo;

import org.greenrobot.eventbus.EventBus;

public class MyApplication extends Application {

    public String strNowSSID = "";
    public NetWorkType nowNetWorkType;
    private static final String TAG = "DemoApplication";
    private static MyApplication myApplication;
    public static MyApplication getInstance() {
        return myApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        myApplication = this;


        try {
            unregisterReceiver(myNetReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 添加网络监听
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(myNetReceiver, mFilter);

    }

    /** 网络监听 */
    private BroadcastReceiver myNetReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == ConnectivityManager.CONNECTIVITY_ACTION) {
                ConnectivityManager mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = mConnectivityManager.getActiveNetworkInfo();
                if (netInfo != null && netInfo.isAvailable()) {
                    // wifi网络
                    if (netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                        WifiManager wifiManager = (WifiManager) myApplication.getSystemService(Context.WIFI_SERVICE);
                        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                        strNowSSID = wifiInfo.getSSID();
                        int version = BaseVolume.getAndroidSDKVersion();
                        if (version > 13)
                            strNowSSID = strNowSSID.replace("\"", "");
                        if (strNowSSID.contains("BYD"))
                            nowNetWorkType = NetWorkType.WIFI_DEVICE;
                        else
                            nowNetWorkType = NetWorkType.WIFI_OTHER;

                        EventBus.getDefault().post(new MessageInfo(MessageInfo.i_NET_WORK_STATE, NetWorkType.WIFI_DEVICE));
                    }
                    // 移动网
                    else if (netInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                        nowNetWorkType = NetWorkType.MOBILE_NET;
                        EventBus.getDefault().post(new MessageInfo(MessageInfo.i_NET_WORK_STATE,nowNetWorkType));
                    }
                }
                // 没有网络
                else {
                    nowNetWorkType = NetWorkType.NOTHING_NET;
                    EventBus.getDefault().post(new MessageInfo(MessageInfo.i_NET_WORK_STATE,nowNetWorkType));
                }
            }
        }
    };





}
