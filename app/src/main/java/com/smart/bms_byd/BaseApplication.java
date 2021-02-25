package com.smart.bms_byd;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.smart.bms_byd.data.AnalysisInfo;
import com.smart.bms_byd.data.CRC16;
import com.smart.bms_byd.data.CreateControlData;
import com.smart.bms_byd.tcpclient.TCPClientS;
import com.smart.bms_byd.util.BaseVolume;
import com.smart.bms_byd.util.NetWorkType;
import com.smart.bms_byd.util.NetworkUtils;
import com.smartIPandeInfo.data.MessageInfo;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

public class BaseApplication extends Application implements TCPClientS.OnDataReceiveListener {

    public String strNowSSID = "";
    public NetWorkType nowNetWorkType = NetWorkType.NOTHING_NET;
    private static final String TAG = "BaseApplication";
    private static BaseApplication myApplication;

    public String strErrorInfo = "";
    public String strMessageInfo = "";
    public int isShowErrorInfo = View.VISIBLE;
    public int isShowMessageInfo = View.VISIBLE;

    public static BaseApplication getInstance() {
        return myApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        myApplication = this;
        pref = getSharedPreferences("SharedPreferencesInfo", 0);

        try {
            unregisterReceiver(myNetReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 添加网络监听
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(myNetReceiver, mFilter);

//        byte[] sj = new byte[] {01,03,00,00,00,0x0C};
//        String strCRC16 = NetworkUtils.bytesToHexString(CRC16.getCrc16(sj));


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
                        checkSSIDTYPE(wifiInfo.getSSID());
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

//    public static final String DEVICE_WIFI_SIGN = "BYDB";
//    public static final String DEVICE_WIFI_PWD = "BYDB-Box";

    public static final String DEVICE_WIFI_SIGN = "FK";
    public static final String DEVICE_WIFI_PWD = "fk12345678";
    public void checkSSIDTYPE(String strSSID) {
        strNowSSID = strSSID;
        int version = BaseVolume.getAndroidSDKVersion();
        if (version > 13)
            strNowSSID = strNowSSID.replace("\"", "");
        if (strNowSSID.contains(DEVICE_WIFI_SIGN))
            nowNetWorkType = NetWorkType.WIFI_DEVICE;
        else
            nowNetWorkType = NetWorkType.WIFI_OTHER;

        EventBus.getDefault().post(new MessageInfo(MessageInfo.i_NET_WORK_STATE, NetWorkType.WIFI_DEVICE));

    }

    /** 发布消息至顶部消息栏 */
    public void updateMessageInfo(String strMsg) {
        this.strErrorInfo = strMsg;
        EventBus.getDefault().post(new MessageInfo(MessageInfo.i_MESSAGE_INFO,strMsg));
    }
    /** 发布异常提醒至顶部异常栏 */
    public void updateErrorInfo(String strError) {
        this.strErrorInfo = strError;
        EventBus.getDefault().post(new MessageInfo(MessageInfo.i_ERROR_INFO,strError));
    }

    private SharedPreferences pref  = null;
    public void saveBooleanBySharedPreferences(String strKey,Boolean isBl) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(strKey,isBl);
        editor.commit();
    }
    public Boolean getBooleanBySharedPreferences(String strKey,boolean isDefValue) {
        return pref.getBoolean(strKey,isDefValue);
    }

    public void saveValueBySharedPreferences(String strKey,String strValue) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(strKey, strValue);
        editor.commit();
    }
    public void removeValueBySharedPreferences(String strKey) {
        SharedPreferences.Editor editor = pref.edit();
        editor.remove(strKey);
        editor.commit();
    }

    public String getValueBySharedPreferences(String strKey) {
        String string = pref.getString(strKey,"");
        return string;
    }

    // TCP的连接状态回调
    @Override
    public void onConnectSuccess() {
        Log.e(TAG, "TCP，连接成功！");
        strOldReceiveBuffer = "";
        EventBus.getDefault().post(new MessageInfo(MessageInfo.i_TCP_CONNECT_SUCCESS,""));
    }

    @Override
    public void onConnectFail(String strFailMsg) {
        Log.e(TAG, "TCP，连接失败：" + strFailMsg);
        stopSend();
        Toast.makeText(this,"tcp,fail:"+strFailMsg,Toast.LENGTH_SHORT).show();
        EventBus.getDefault().post(new MessageInfo(MessageInfo.i_TCP_CONNECT_FAIL,strFailMsg));
    }

    @Override
    public void onDataReceive(byte[] receiveData) {
        String str = NetworkUtils.bytesToHexString(receiveData);
        Log.e(TAG, "TCP，接收数据：" + str);
        checkData(str);
    }

    @Override
    public void onDataResultInfo(boolean isOK, String strErrorInfo) {
        Log.e(TAG, "TCP，发送数据，结果：" + isOK+" "+strErrorInfo);
        if (!isOK) {
            Looper.prepare();
            Toast.makeText(this,"tcp,send fail:"+strErrorInfo,Toast.LENGTH_SHORT).show();
            Looper.loop();

        }

    }

    private String strOldReceiveBuffer = "";

    /**
     * 数据分包组包
     * @param data
     */
    private void checkData(String data) {
        strOldReceiveBuffer = strOldReceiveBuffer + data;
        while (strOldReceiveBuffer.length() >= 10) {
            // BMU地址：0x01;以太网模块地址：0x02
            String strAddress = strOldReceiveBuffer.substring(0,2);
            // 数据类型
            String strType = strOldReceiveBuffer.substring(2,4);
            // 根据类型，计算该条数据的有效长度
            int iCmdLength = checkLengthByType(strType,strOldReceiveBuffer);
            // 长度不够，继续等待接收
            if (iCmdLength > strOldReceiveBuffer.length())
                continue;
            String strWillGoodData = strOldReceiveBuffer.substring(0,iCmdLength*2);
            String strOldCrc = strWillGoodData.substring(strWillGoodData.length() - 4);
            String strWillD = strWillGoodData.substring(0,strWillGoodData.length() - 4);// 将要计算校验值的数据
            String strNewCrc = NetworkUtils.bytesToHexString(CRC16.getCrc16(strWillD));
            // 校验通过，说明是有效数据
            if (strOldCrc.equalsIgnoreCase(strNewCrc)) {
                Log.e(TAG, "有效数据：" + strWillGoodData);
                // 数据解析，并抛到上层
                AnalysisInfo analysisInfo0 = new AnalysisInfo(strType,strWillGoodData);
                EventBus.getDefault().post(new MessageInfo(MessageInfo.i_RECEIVE_DATA,analysisInfo0));
                strOldReceiveBuffer = strOldReceiveBuffer.substring(iCmdLength*2);
            }
            // 校验不通过，继续下一个字节
            else {
                strOldReceiveBuffer = strOldReceiveBuffer.substring(2);
            }
        }


    }


    /**
     * 根据数据类型，判断这条完整数据的长度
     * @param strType
     * @return
     */
    private int checkLengthByType(String strType,String strAllData) {
        int iLenght = 0;
        // 读的回复
        if (strType.equalsIgnoreCase(BaseVolume.CMD_TYPE_READ_DATA)) {
            int iDataLen = Integer.parseInt(strAllData.substring(4,6),16);
            iLenght = 1+iDataLen; // 寄存器数量，数据
        }
        // 写单个寄存器的回复
        else if (strType.equalsIgnoreCase(BaseVolume.CMD_TYPE_WRITE_ONLY)) {
            iLenght = 4;
        }
        // 写多个寄存器的回复
        else if (strType.equalsIgnoreCase(BaseVolume.CMD_TYPE_WRITE_MORE)) {
            int iDataLen = Integer.parseInt(strAllData.substring(12,14),16);
            iLenght = 2+2+1+iDataLen; // 写的寄存器地址，要写的寄存器数量，字节长度，数据
        }
        // 操作错误的返回 83 86 90
        else if (strType.equalsIgnoreCase(BaseVolume.CMD_TYPE_READ_DATA_ERROR)
                || strType.equalsIgnoreCase(BaseVolume.CMD_TYPE_WRITE_ONLY_ERROR)
                || strType.equalsIgnoreCase(BaseVolume.CMD_TYPE_WRITE_MORE_ERROR)) {
            iLenght = 1;
        }
        return (1+1+iLenght+2);// 地址，命令，数据域，校验位
    }

    /** 开始发送数据 */
    public void StartSendDataByTCP(String strNowSendData) {
        Log.e(TAG, "TCP，发送数据："+strNowSendData);
        TCPClientS.getInstance(this).sendHexCmd(strNowSendData);
    }

    /** 开始发送数据 */
    public void StartSendDataByTCPTimeOut(String strNowSendData) {
        mHandler.removeCallbacks(runnableSendData);
        strSendDataBuffer = strNowSendData;
        iSendCount = 3;
        mHandler.post(runnableSendData);
    }

    int iSendCount = 3;
    String strSendDataBuffer = "";
    Handler mHandler = new Handler();
    Runnable runnableSendData = new Runnable() {
        @Override
        public void run() {
            if (iSendCount > 0) {
                StartSendDataByTCP(strSendDataBuffer);
                --iSendCount;
                mHandler.postDelayed(this,1000*5);
            }
            else {
                sendBroadcast(new Intent(BaseVolume.COMMAND_SEND_TIMEOUT));
            }
        }
    };

    /** 停止发送数据 */
    public void stopSend() {
        mHandler.removeCallbacks(runnableSendData);
        sendBroadcast(new Intent(BaseVolume.COMMAND_SEND_STOP));
    }



}
