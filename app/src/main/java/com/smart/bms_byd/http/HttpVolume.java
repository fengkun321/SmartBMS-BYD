package com.smart.bms_byd.http;

import android.util.Log;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.callback.StringCallback;
import org.json.JSONException;
import org.json.JSONObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;

public class HttpVolume {

    public static final boolean isLocalNetWork = false; // false:正式环境,true:本地调试
    public static final String BASE_URL = "http://yigeer.gicisky.net/api/v1/Http2/";
    // 账号相关接口
    public static final String Register_User = BASE_URL + "register";
    private static final String TAG = "HttpVolume";


    /**
     * http接口调用时的一些参数
     * @return [0]随机数,[1]加密签名,[2]时间戳
     */
    public static String[] getHttpParameter() {
        String[] strThing = new String[3];
        Random rand = new Random();

        String strNonce = rand.nextInt(9999)+"";// 随机数
        String strTimeStamp = System.currentTimeMillis()+"";// 时间戳
//        String strNonce = "AAAAAAAAAAAAA";// 随机数
//        String strTimeStamp = "BBBBBBBBBBBBB";// 时间戳
        String strToken = "Yigeer20200923";

        String[] array = new String[]{strTimeStamp,strNonce,strToken};
        Arrays.sort(array);// 升序排列
        String strArrayInfo = "";
        for (String str : array) {
            strArrayInfo += str;
        }
        // 加密签名
        String strSha1 = "";
        try {
            strSha1 = getSha1(strArrayInfo.getBytes());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        strThing[0] = strNonce;
        strThing[1] = strSha1;// 489e313470c26e21aa473a29a0ce7246907901f9
        strThing[2] = strTimeStamp;

        for (String str : strThing) {
            Log.e(TAG, "getHttpParameter: "+str);
        }

        return strThing;

    }

    public static String getSha1(byte[] input) throws NoSuchAlgorithmException{
        MessageDigest mDigest = MessageDigest.getInstance("SHA1");
        byte[] result = mDigest.digest(input);
        String strReuslt = bytesToHexString(result);
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < result.length; i++) {
            sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    /**
     * 字节转16进制字符串
     * @param src
     * @return
     */
    public static String bytesToHexString(byte[] src){
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }



    public interface GetNewTokenCallBack {
        void onSuccess(int iCode, Object strResultInfo);

        void onError(int iCode, String strErrorInfo);

    }





}
