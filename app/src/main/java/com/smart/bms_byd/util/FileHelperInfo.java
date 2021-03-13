package com.smart.bms_byd.util;

import android.os.Environment;

import com.smart.bms_byd.data.Base64Utils;
import com.smart.bms_byd.data.CRC16;
import com.smart.bms_byd.http.DownloadUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

public class FileHelperInfo {

    /** 读出文件字节数组 */
    public static byte[] readFileStream(File file) {
        ArrayList<byte[]> byteArrays = new ArrayList<byte[]>();
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            byte[] readBuffer0 = new byte[1024];
            int iReadNum = 0;
            int iMaxLength = 0;
            while ((iReadNum = fileInputStream.read(readBuffer0)) != -1) {
                byte[] readInfo = new byte[iReadNum];
                System.arraycopy(readBuffer0, 0, readInfo, 0,iReadNum);
                byteArrays.add(readInfo);
                iMaxLength += iReadNum;
            }
            fileInputStream.close();
            byte[] fileByteInfo = new byte[iMaxLength];
            int iLoc = 0;
            for (byte[] by : byteArrays) {
                System.arraycopy(by, 0, fileByteInfo, iLoc, by.length);
                iLoc += by.length;
            }
            return fileByteInfo;
        } catch (Exception e) {
            e.printStackTrace();
            try {
                fileInputStream.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            return null;
        }

    }

    public static void deleteFileByFolder(String strFolder) {
        File downloadFile = new File(Environment.getExternalStorageDirectory().getPath() + "/"+strFolder+"/");
        for (File file : downloadFile.listFiles()) {
            if (file.exists()) {
                file.delete();
            }
        }
    }

    /**
     * 把数据流写入文件
     * @param path
     * @param bytes
     */
    private static void writeFile(String path, byte[] bytes) {
        try {
            FileOutputStream out = new FileOutputStream(path);//指定写到哪个路径中
            FileChannel fileChannel = out.getChannel();
            fileChannel.write(ByteBuffer.wrap(bytes)); //将字节流写入文件中
            fileChannel.force(true);//强制刷新
            fileChannel.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * base64解密，并校验文件数据
     * @param file
     * @return byte[] 如果有返回字节，说明校验通过，无返回则说明校验失败！
     */
    public static byte[] getContentByteArrayByBase64(File file) {
        byte[] fileBytes = readFileStream(file);
        byte[] byteDeCode = Base64Utils.decodeToBytes(fileBytes);
        String strDecodeData0 = new String(byteDeCode);
        String strDecodeData = strDecodeData0.replace(" ","");
        String strOldCrc = strDecodeData.substring(strDecodeData.length() - 4);
        String strContent = strDecodeData.substring(0,strDecodeData.length() - 4);
        String strNewCrc = NetworkUtils.bytesToHexString(CRC16.getCrc16(strContent));
        if (strOldCrc.equalsIgnoreCase(strNewCrc)) {
            return NetworkUtils.hexStringToBytes(strContent);
//            String strG = strDecodeData0.substring(0,strDecodeData0.length() - 4);
//            return strG.getBytes();
        }
        return null;


    }

    /**
     * 校验固件的完整性
     * @param file
     * @param strOldCRC
     * @return
     */
    public static byte[] getContentByArrayByCRC(File file,String strOldCRC) {
        byte[] fileBytes = readFileStream(file);
        String strNewCRC = NetworkUtils.bytesToHexString(CRC16.getCrc16(fileBytes));
        if (strNewCRC.equalsIgnoreCase(strOldCRC))
            return fileBytes;
        else
            return null;


    }




}
