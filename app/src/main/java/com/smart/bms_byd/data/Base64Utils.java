package com.smart.bms_byd.data;

import android.util.Base64;

import java.io.UnsupportedEncodingException;

public class Base64Utils {

    /**
     * 字符Base64加密
     * @param str
     * @return
     */
    public static String encodeToString(String str){
        try {
            return Base64.encodeToString(str.getBytes("UTF-8"), Base64.DEFAULT);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }
    /**
     * 字符Base64解密
     * @param str
     * @return
     */
    public static String decodeToString(String str){
        try {
            return new String(Base64.decode(str.getBytes("UTF-8"), Base64.DEFAULT));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 字符Base64解密
     * @param bytes
     * @return
     */
    public static byte[] decodeToBytes(byte[] bytes){
        return Base64.decode(bytes, Base64.DEFAULT);
    }

}
