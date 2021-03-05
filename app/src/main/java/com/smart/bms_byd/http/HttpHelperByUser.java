package com.smart.bms_byd.http;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.callback.StringCallback;
import com.smart.bms_byd.BaseActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Gabriel on 2018/12/26.
 */
public class HttpHelperByUser {
    private static HttpHelperByUser helper;
    private String TAG = "HttpHelperByUser";

    public interface HttpHelperCallBack {
        void onSuccess(int iCode, Object strResultInfo);

        void onError(int iCode, String strErrorInfo);

    }

    public static HttpHelperByUser getInstance() {
        if (helper == null) {
            helper = new HttpHelperByUser();
        }
        return helper;
    }

    /**
     * 注册账号
     * @param strPhone
     * @param strPwd
     * @param strSmsCode
     * @param httpHelperCallBack
     */
    public void registerNewUser(BaseActivity baseActivity, String strPhone, String strPwd, String strSmsCode, HttpHelperCallBack httpHelperCallBack) {
        baseActivity.loadingDialog.showAndMsg("请稍后...");
        String strMethodTag = "registerNewUser";
        String[] strParameter = HttpVolume.getHttpParameter();

        OkGo.<String>post(HttpVolume.Register_User)
                .tag(this)
                .cacheMode(CacheMode.DEFAULT)
                .params("Nonce", strParameter[0])
                .params("Signature", strParameter[1])
                .params("TimeStamp", strParameter[2])
                .params("Mobile", strPhone)
                .params("Password", strPwd)
                .params("SmsCode", strSmsCode)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(com.lzy.okgo.model.Response<String> response) {
                        baseActivity.loadingDialog.dismiss();
                        if (response.code() == 200) {
                            try {
                                // {"Code":2,"Message":"\u9a8c\u8bc1\u9519\u8bef\uff01","OperationTime":"2020-01-14 17:38:43","Data":null}
                                JSONObject jsonObject = new JSONObject(response.body());
                                Log.e(TAG, strMethodTag + ",onSuccess，Info:" + jsonObject);
                                int iCode = jsonObject.optInt("Code");
                                String strMsg = jsonObject.optString("Message");
                                String strData = jsonObject.optString("Data");
                                if (iCode == 1)
                                    httpHelperCallBack.onSuccess(iCode, strMsg);
                                else
                                    httpHelperCallBack.onError(iCode, strMsg);

                            } catch (JSONException e) {
                                e.printStackTrace();
                                httpHelperCallBack.onError(response.code(), e.getMessage());
                            }
                        } else {
                            Log.e(TAG, strMethodTag + ",response,code:" + response.code() + "，Info:" + response.message());
                            httpHelperCallBack.onError(response.code(), response.message());
                        }
                    }

                    public void onError(com.lzy.okgo.model.Response<String> response) {
                        baseActivity.loadingDialog.dismiss();
                        Log.e(TAG, strMethodTag + ",onError,code:" + response.code() + "，Info:" + response.message());
                        httpHelperCallBack.onError(response.code(), response.message());
                    }
                });
    }









}
