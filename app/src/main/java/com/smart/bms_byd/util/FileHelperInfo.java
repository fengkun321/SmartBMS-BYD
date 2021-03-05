package com.smart.bms_byd.util;

import android.os.Environment;

import com.smart.bms_byd.data.Base64Utils;
import com.smart.bms_byd.data.CRC16;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
     * base64解密，并校验文件数据
     * @param fileBytes
     * @return byte[] 如果有返回字节，说明校验通过，无返回则说明校验失败！
     */
    public static byte[] getContentByteArrayByBase64(byte[] fileBytes) {
        byte[] byteDeCode = Base64Utils.decodeToBytes(fileBytes);
        byte[] byteContent = new byte[byteDeCode.length - 2];
        byte[] byteCrc = new byte[2];
        System.arraycopy(byteDeCode, 0, byteContent, 0, byteContent.length);
        System.arraycopy(byteDeCode, byteDeCode.length - 2, byteCrc, 0, byteCrc.length);
        String strContentNewCrc = NetworkUtils.bytesToHexString(CRC16.getCrc16(byteContent));
        String strOldCrc = NetworkUtils.bytesToHexString(byteCrc);
        if (strContentNewCrc.equalsIgnoreCase(strOldCrc))
            return byteContent;
        else
            return null;

    }

    public static byte[] getContentByteArrayFirmwareByBase64(byte[] fileBytes) {
        byte[] byteContent = new byte[fileBytes.length - 2];
        byte[] byteCrc = new byte[2];
        System.arraycopy(fileBytes, 0, byteContent, 0, byteContent.length);
        System.arraycopy(fileBytes, fileBytes.length - 2, byteCrc, 0, byteCrc.length);
        String strContentNewCrc = NetworkUtils.bytesToHexString(CRC16.getCrc16(byteContent));
        String strOldCrc = NetworkUtils.bytesToHexString(byteCrc);
        if (strContentNewCrc.equalsIgnoreCase(strOldCrc))
            return byteContent;
        else
            return null;

    }


}
