package com.smart.bms_byd.http;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;

import com.smart.bms_byd.BuildConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadUtil {

    private static DownloadUtil downloadUtil;
    private final OkHttpClient okHttpClient;
    private Context context;
    private String TAG = "下载页面";
    public static final String strDownloadFolder = "Battery-box";
    public static final String strDownloadFolder_PDF = "PDF";

    public static DownloadUtil get() {
        if (downloadUtil == null) {
            downloadUtil = new DownloadUtil();
        }
        return downloadUtil;
    }

    private DownloadUtil() {
        okHttpClient = new OkHttpClient();
    }

    /**
     * @param url 下载连接
     * @param saveDir 储存下载文件的上一级目录
     * @param listener 下载监听
     */
    public void download(Context context, final String url, final String saveDir,final String fileName, final OnDownloadListener listener) {
        this.context= context;
        Request request = new Request.Builder().url(url).build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 下载失败
                listener.onDownloadFailed(e.getMessage());
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;
                // 储存下载文件的目录
                String savePath = isExistDir(saveDir);
//                Log.e(TAG,"存储下载目录："+savePath);
                try {
                    is = response.body().byteStream();
                    long total = response.body().contentLength();
                    File file = new File(savePath, getNameFromUrl(fileName));
                    Log.e(TAG,"最终路径："+file);
                    fos = new FileOutputStream(file);
                    long sum = 0;
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                        sum += len;
                        int progress = (int) (sum * 1.0f / total * 100);
                        // 下载中
                        listener.onDownloading(progress);
                    }
                    fos.flush();
                    // 下载完成
                    listener.onDownloadSuccess();
                } catch (Exception e) {
                    listener.onDownloadFailed(e.getMessage());
                } finally {
                    try {
                        if (is != null)
                            is.close();
                    } catch (IOException e) {
                    }
                    try {
                        if (fos != null)
                            fos.close();
                    } catch (IOException e) {
                    }
                }
            }
        });
    }

    /**
     * @param saveDir
     * @return
     * @throws IOException
     * 判断下载目录是否存在
     */
    private String isExistDir(String saveDir) throws IOException {
        // 下载位置
        File downloadFile = new File(Environment.getExternalStorageDirectory().getPath() + "/"+strDownloadFolder+"/", saveDir);
        if (!downloadFile.mkdirs()) {
            downloadFile.createNewFile();
        }
        String savePath = downloadFile.getAbsolutePath();
//        Log.e(TAG,"下载目录："+savePath);
        return savePath;
    }

    public File isHaveFileByTypeName(String strFolder,String strFileName) {
        File downloadFile = new File(Environment.getExternalStorageDirectory().getPath() + "/"+strFolder+"/", strFileName);
        if (downloadFile.exists())
            return downloadFile;
        else
            return null;
    }

    public void deleteFileByName(String strFolder,String strFileName) {
        File downloadFile = new File(Environment.getExternalStorageDirectory().getPath() + "/"+strFolder+"/", strFileName);
        if (downloadFile.exists()) {
            downloadFile.delete();
        }
    }

    public ArrayList<File> getFilesList(String strFolder,String strFileSign) {
        File downloadFile = new File(Environment.getExternalStorageDirectory().getPath() + "/"+strFolder+"/");
        ArrayList<File> files = new ArrayList<>();
        for (File file : downloadFile.listFiles()) {
            if (file.getName().indexOf(strFileSign) >= 0) {
                files.add(file);
            }
        }
        return files;
    }



    /**
     * @param url
     * @return
     * 传入文件名
     */
    @NonNull
    public String getNameFromUrl(String url) {
        return url;
    }

    public interface OnDownloadListener {
        /**
         * 下载成功
         */
        void onDownloadSuccess();

        /**
         * @param progress
         * 下载进度
         */
        void onDownloading(int progress);

        /**
         * 下载失败
         */
        void onDownloadFailed(String strFaileMsg);
    }
}


