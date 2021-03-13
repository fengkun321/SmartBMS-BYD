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
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheEntity;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.cookie.CookieJarImpl;
import com.lzy.okgo.cookie.store.DBCookieStore;
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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import okhttp3.OkHttpClient;

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

        initOkGo();

//        byte[] sj = new byte[] {01,03,00,00,00,0x0C};
//        String strCRC16 = NetworkUtils.bytesToHexString(CRC16.getCrc16(sj));


    }

    private void initOkGo() {
        //okGo网络框架初始化和全局配置
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        //builder.cookieJar(new CookieJarImpl(new SPCookieStore(this)));      //使用sp保持cookie，如果cookie不过期，则一直有效
        builder.cookieJar(new CookieJarImpl(new DBCookieStore(this)));//使用数据库保持cookie，如果cookie不过期，则一直有效
        //builder.cookieJar(new CookieJarImpl(new MemoryCookieStore()));      //使用内存保持cookie，app退出后，cookie消失
        //设置请求头
        //---------这里给出的是示例代码,告诉你可以这么传,实际使用的时候,根据需要传,不需要就不传-------------//
//        HttpHeaders headers = new HttpHeaders();
//        headers.put("commonHeaderKey1", "commonHeaderValue1");    //header不支持中文，不允许有特殊字符
//        headers.put("commonHeaderKey2", "commonHeaderValue2");
//        //设置请求参数
//        HttpParams params = new HttpParams();
//        params.put("commonParamsKey1", "commonParamsValue1");     //param支持中文,直接传,不要自己编码
//        params.put("commonParamsKey2", "这里支持中文参数");
        OkGo.getInstance().init(this)                           //必须调用初始化
                .setOkHttpClient(builder.build())               //建议设置OkHttpClient，不设置会使用默认的
                .setCacheMode(CacheMode.NO_CACHE)               //全局统一缓存模式，默认不使用缓存，可以不传
                .setCacheTime(CacheEntity.CACHE_NEVER_EXPIRE)   //全局统一缓存时间，默认永不过期，可以不传
                .setRetryCount(3);                              //全局统一超时重连次数，默认为三次，那么最差的情况会请求4次(一次原始请求，三次重连请求)，不需要可以设置为0

    }

    public void registerReceiverNetwork() {
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

    public static final String DEVICE_WIFI_SIGN = "BYD-";
    public static final String DEVICE_WIFI_PWD = "BYDB-Box";

//    public static final String DEVICE_WIFI_SIGN = "FK";
//    public static final String DEVICE_WIFI_PWD = "fk12345678";
    private void checkSSIDTYPE(String strSSID) {
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

    //android关闭或开启移动网络数据（关闭后，设备不可以上网，但可以打电话和发短信） 
    public void setMobileDataEnabled(boolean mobileDataEnabled)
    {
        TelephonyManager telephonyService = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            Method setMobileDataEnabledMethod = telephonyService.getClass()
                    .getDeclaredMethod("setDataEnabled", boolean.class);
            if (null != setMobileDataEnabledMethod) {
                setMobileDataEnabledMethod.invoke(telephonyService,mobileDataEnabled);
            }
        } catch (Exception e) {
            Log.e("InstallActivity", "Errot setting"
                    + ((InvocationTargetException) e).getTargetException()
                    + telephonyService);
        }
    }

    /**
     * 移动网络开关
     */
    public void toggleMobileData(Context context, boolean enabled) {
        ConnectivityManager conMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        Class<?> conMgrClass = null; // ConnectivityManager类
        Field iConMgrField = null; // ConnectivityManager类中的字段
        Object iConMgr = null; // IConnectivityManager类的引用
        Class<?> iConMgrClass = null; // IConnectivityManager类
        Method setMobileDataEnabledMethod = null; // setMobileDataEnabled方法
        try {
            // 取得ConnectivityManager类
            conMgrClass = Class.forName(conMgr.getClass().getName());
            // 取得ConnectivityManager类中的对象mService
            iConMgrField = conMgrClass.getDeclaredField("mService");
            // 设置mService可访问
            iConMgrField.setAccessible(true);
            // 取得mService的实例化类IConnectivityManager
            iConMgr = iConMgrField.get(conMgr);
            // 取得IConnectivityManager类
            iConMgrClass = Class.forName(iConMgr.getClass().getName());
            // 取得IConnectivityManager类中的setMobileDataEnabled(boolean)方法
            setMobileDataEnabledMethod = iConMgrClass.getDeclaredMethod(
                    "setMobileDataEnabled", Boolean.TYPE);
            // 设置setMobileDataEnabled方法可访问
            setMobileDataEnabledMethod.setAccessible(true);
            // 调用setMobileDataEnabled方法
            setMobileDataEnabledMethod.invoke(iConMgr, enabled);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
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
        Log.e(TAG, "Connect，连接成功！");
        strOldReceiveBuffer = "";
        EventBus.getDefault().post(new MessageInfo(MessageInfo.i_TCP_CONNECT_SUCCESS,""));
    }

    @Override
    public void onConnectFail(String strFailMsg) {
        Log.e(TAG, "Connect，连接失败：" + strFailMsg);
        StopSend();
        Toast.makeText(this,"tcp,fail:"+strFailMsg,Toast.LENGTH_SHORT).show();
        EventBus.getDefault().post(new MessageInfo(MessageInfo.i_TCP_CONNECT_FAIL,strFailMsg));
    }

    @Override
    public void onDataReceive(byte[] receiveData) {
        String str = NetworkUtils.bytesToHexString(receiveData);
        Log.e(TAG, "Connect，接收数据：" + str);
        checkData(str);
    }

    @Override
    public void onDataResultInfo(boolean isOK, String strErrorInfo) {
        if (!isOK) {
            Log.e(TAG, "Connect，发送数据，结果：" + isOK+" "+strErrorInfo);
            EventBus.getDefault().post(new MessageInfo(MessageInfo.i_SEND_DATA_ERROR,"tcp,send fail:"+strErrorInfo));
            // 断开连接
            if (strErrorInfo.equalsIgnoreCase("Broken pipe") || strErrorInfo.equalsIgnoreCase("Socket closed")) {
                EventBus.getDefault().post(new MessageInfo(MessageInfo.i_TCP_CONNECT_FAIL,strErrorInfo));
                TCPClientS.getInstance(this).manuallyDisconnect();
            }
//            Looper.prepare();
//            Toast.makeText(this,"tcp,send fail:"+strErrorInfo,Toast.LENGTH_SHORT).show();
//            Looper.loop();

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
            if (iCmdLength*2 > strOldReceiveBuffer.length())
                continue;
            String strWillGoodData = strOldReceiveBuffer.substring(0,iCmdLength*2);
            String strOldCrc = strWillGoodData.substring(strWillGoodData.length() - 4);
            String strWillD = strWillGoodData.substring(0,strWillGoodData.length() - 4);// 将要计算校验值的数据
            String strNewCrc = NetworkUtils.bytesToHexString(CRC16.getCrc16(strWillD));
            // 校验通过，说明是有效数据
            if (strOldCrc.equalsIgnoreCase(strNewCrc)) {
//                Log.e(TAG, "有效数据：" + strWillGoodData);
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
            int iDataLen = Integer.parseInt(strAllData.substring(8,12),16)*2;
            iLenght = 4; // 写的寄存器地址，要写的寄存器数量，字节长度，数据
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
        Log.e(TAG, "Connect，发送数据："+strNowSendData);
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
                EventBus.getDefault().post(new MessageInfo(MessageInfo.i_SEND_DATA_ERROR,"send timeout!"));
//                sendBroadcast(new Intent(BaseVolume.COMMAND_SEND_TIMEOUT));
            }
        }
    };

    /** 停止发送数据 */
    public void StopSend() {
        mHandler.removeCallbacks(runnableSendData);
        sendBroadcast(new Intent(BaseVolume.COMMAND_SEND_STOP));
    }



}
