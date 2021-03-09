package com.smart.bms_byd.data;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.smart.bms_byd.BaseApplication;
import com.smart.bms_byd.util.BaseVolume;
import com.smart.bms_byd.util.NetworkUtils;

public class UpdateFirmwareHelper {

    private Context mContext;
    private static UpdateFirmwareHelper updateFirmwareHelper;
    private Handler mHandler;
    // 将用于更新的文件流
    private byte[] willUpdateFileArray = null;
    // 当前更新的数据块数量
    private int iNowUpdateCount = 0;
    // 更新开始的状态地址
    private String strUpdateStartAddress = "";
    private String strUpdateAddress = "";
    private String strFileName = "";
    // 当前发送的数据长度（固件数据）
    private int iLastSendNum = 0;
    // 上次发送到的数据位置
    private int iLastSendLocation = 0;

    public static final int iUPDATA_PROGRESS_START = 111;
    public static final int iUPDATA_PROGRESS_SENDING = 222;
    public static final int iUPDATA_PROGRESS_SUCCESS = 333;
    public static final int iUPDATA_PROGRESS_FAULT = 444;

    public String getStrUpdateStartAddress() {
        return strUpdateStartAddress;
    }

    public String getStrFileName() {
        return strFileName;
    }

    public byte[] getWillUpdateFileArray() {
        return willUpdateFileArray;
    }

    public void setStrUpdateStartAddress(String strUpdateStartAddress) {
        this.strUpdateStartAddress = strUpdateStartAddress;
    }

    public static synchronized UpdateFirmwareHelper getInstance() {
        if (updateFirmwareHelper == null)
            updateFirmwareHelper = new UpdateFirmwareHelper(BaseApplication.getInstance());
        return updateFirmwareHelper;
    }

    public UpdateFirmwareHelper(Context mCon) {
        this.mContext = mCon;
    }

    /** 开始更新
     * @param strUpdateStartAddress
     * @param strUpdateAddress
     * @param willUpdateFileArray
     */
    public void startUpdateFirmwareByTypeFile(String strUpdateStartAddress,String strUpdateAddress,String strFileName,byte[] willUpdateFileArray,Handler mHandler) {
        this.strUpdateStartAddress = strUpdateStartAddress;
        this.strUpdateAddress = strUpdateAddress;
        this.strFileName = strFileName;
        this.willUpdateFileArray = willUpdateFileArray;
        this.mHandler = mHandler;

        iLastSendNum = 0;
        iNowUpdateCount = 0;
        iLastSendLocation = 0;

        String strData = "";
        String strVerInfo = "";
        if (strUpdateStartAddress.equalsIgnoreCase(BaseVolume.CMD_UPDATE_TABLE_START_ADDRESS)) {
            int iNumber = Integer.parseInt(strFileName.split("-")[2]);
            String[] strVer = strFileName.split("-")[3].split("\\.");
            strVerInfo = String.format("%02X",iNumber)+String.format("%02X",Integer.parseInt(strVer[0]))+String.format("%02X",Integer.parseInt(strVer[1]));

        }
        else if (strUpdateStartAddress.equalsIgnoreCase(BaseVolume.CMD_UPDATE_BMS_START_ADDRESS)) {
            String[] strVer = strFileName.split("-")[2].split("\\.");
            String strArea = strFileName.split("-")[3];
            // A区固件
            if (strArea.equalsIgnoreCase("A"))
                strVerInfo = String.format("%02X",Integer.parseInt(strVer[0]))+String.format("%02X",Integer.parseInt(strVer[1]))+"0000";
            // B区固件
            else
                strVerInfo = String.format("%02X",Integer.parseInt(strVer[0]))+String.format("%02X",Integer.parseInt(strVer[1]))+"0001";

        }
        else if (strUpdateStartAddress.equalsIgnoreCase(BaseVolume.CMD_UPDATE_BMU_START_ADDRESS)) {
            String[] strVer = strFileName.split("-")[2].split("\\.");
            String strArea = strFileName.split("-")[3];
            // A区固件
            if (strArea.equalsIgnoreCase("A"))
                strVerInfo = String.format("%02X",Integer.parseInt(strVer[0]))+String.format("%02X",Integer.parseInt(strVer[1]))+"0000";
                // B区固件
            else
                strVerInfo = String.format("%02X",Integer.parseInt(strVer[0]))+String.format("%02X",Integer.parseInt(strVer[1]))+"0001";
        }

        strData += strVerInfo;// 版本号
        strData += "8100";// 更新状态
        String strMaxLength = String.format("%08X",willUpdateFileArray.length);
        strData = strData + strMaxLength.substring(4)+strMaxLength.substring(0,4);

        Message msg = new Message();
        msg.what = iUPDATA_PROGRESS_START;
        msg.obj = 0.0f;
        mHandler.sendMessage(msg);

        String strSendData = CreateControlData.Companion.writeMoreByAddress(strUpdateStartAddress,strData);
        BaseApplication.getInstance().StartSendDataByTCPTimeOut(strSendData);

    }

    /**
     * 继续更新
     * @param analysisInfo
     */
    public void keepUpdate(AnalysisInfo analysisInfo) {
        // 写入失败
        if (analysisInfo.getStrType().equalsIgnoreCase(BaseVolume.CMD_TYPE_WRITE_MORE_ERROR)) {
            Message msg = new Message();
            msg.what = iUPDATA_PROGRESS_FAULT;
            msg.obj = analysisInfo.getStrErrorInfo();
            mHandler.sendMessage(msg);
            return;
        }

        int iResultLength = analysisInfo.getIWriteMoreRegisterNumber();
        // 第一条数据的回复
        if (iResultLength == 5 && (analysisInfo.getStrWriteMoreAddress().equalsIgnoreCase(strUpdateStartAddress))) {
            nextSendData();
        }
        // 当前回复的寄存器数量和现在的是否对的上
        else if (iResultLength*2 == (iLastSendNum+4+2) && analysisInfo.getStrWriteMoreAddress().equalsIgnoreCase(strUpdateAddress)) {
            // 当前发送的位置和文件位置一致，则说明全部发送完成啦。
            if (iLastSendLocation == willUpdateFileArray.length) {
                Message msg = new Message();
                msg.what = iUPDATA_PROGRESS_SUCCESS;
                msg.obj = 1.0f;
                mHandler.sendMessage(msg);
            }
            else {
                nextSendData();
            }

        }
    }

    /**
     * 继续更新
     */
    private void nextSendData() {
        ++iNowUpdateCount;
        String strCount = String.format("%04X",iNowUpdateCount);// 最后拼接时要翻转一下
        int iNowSendNum = 0;
        if (iLastSendLocation+128 <= willUpdateFileArray.length)
            iNowSendNum = 128;
        else
            iNowSendNum = willUpdateFileArray.length - iLastSendLocation;
        byte[] byteWillSend = new byte[iNowSendNum];
        System.arraycopy(willUpdateFileArray,iLastSendLocation,byteWillSend,0,iNowSendNum);
        String strUpdateLength = String.format("%08X",iNowSendNum);
        String strFileData = NetworkUtils.bytesToHexString(byteWillSend);
        String strData = strCount.substring(4)+strCount.substring(0,4)+strUpdateLength+strFileData;

        iLastSendLocation += iNowSendNum;
        iLastSendNum = iNowSendNum;

        float fProgress = iLastSendLocation/(float)willUpdateFileArray.length;
        Message msg = new Message();
        msg.what = iUPDATA_PROGRESS_SENDING;
        msg.obj = fProgress; // 更新当前更新的比例
        mHandler.sendMessage(msg);

        Log.e("UpdateFirmwareHelper","当前文件的升级进度："+fProgress);

        String strSendData = CreateControlData.Companion.writeMoreByAddress(strUpdateAddress,strData);
        BaseApplication.getInstance().StartSendDataByTCPTimeOut(strSendData);

    }

    public void stopUpdateFile() {
        strUpdateStartAddress = "";
        strUpdateAddress = "";
        iLastSendNum = 0;
        iLastSendLocation = 0;
    }



}
