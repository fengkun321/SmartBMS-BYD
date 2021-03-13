package com.smart.bms_byd.otherPage

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import com.smart.bms_byd.BaseActivity
import com.smart.bms_byd.BaseApplication
import com.smart.bms_byd.MainActivityTest
import com.smart.bms_byd.R
import com.smart.bms_byd.data.*
import com.smart.bms_byd.http.DownloadUtil
import com.smart.bms_byd.tcpclient.TCPClientS
import com.smart.bms_byd.util.BaseVolume
import com.smart.bms_byd.util.FileHelperInfo
import com.smart.bms_byd.util.NetWorkType
import com.smart.bms_byd.util.NetworkUtils
import com.smart.bms_byd.view.AreaAddWindowHint
import com.smartIPandeInfo.data.MessageInfo
import kotlinx.android.synthetic.main.activity_update_firmware.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File

class UpdateFirmwareActivity : BaseActivity(){

    private var downloadList = arrayListOf<FileDataInfo>()
    /** 当前需要更新的文件列表 */
    var willUpdateFileList = arrayListOf<FileDataInfo>()
    private var iEveryProgress = 0
    private var iNowUpdateCount = 0
    private val TAG = "UpdateFirmwareActivity"
    private var isCheckNetState = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_firmware)

        downloadList = intent.getSerializableExtra("fileList") as ArrayList<FileDataInfo>


        myNetState.initView(this, true, this);
        EventBus.getDefault().register(this)
        updateProgressBar.progress = 0

    }

    override fun onResume() {
        super.onResume()

        isCheckNetState = true
        checkNetworkState()

//        val fileList = DownloadUtil.get().getFilesList(DownloadUtil.strDownloadFolder,"TAB")
//        for (iN in fileList.indices) {
//            // 阈值表，开始验证文件对错
//            if (fileList[iN].name.indexOf(".bin") > 0) {
//                val byteArrayContent = FileHelperInfo.getContentByteArrayByBase64(fileList[iN])
//                if (byteArrayContent != null)
//                    showToast("文件校验通过！可以进行升级！")
//                else
//                    showToast("文件校验未通过！")
//                return
//            }
//        }


    }

    override fun onStop() {
        super.onStop()
        isCheckNetState = false
    }


    /** 校验网络情况 */
    private fun checkNetworkState() {
        // 未连接设备网络
        if (BaseApplication.getInstance().nowNetWorkType != NetWorkType.WIFI_DEVICE) {
            showConnectDevDialog()
        }
        // 设备热点
        else {
            // 更新完成，则询问是否配置
            if (updateProgressBar.progress == 100)
                showConfigDialog()
            // 未更新，则先连接设备
            else {
                if (TCPClientS.getInstance(BaseApplication.getInstance()).connectionState != TCPClientS.TCP_CONNECT_STATE_CONNECTED) {
                    loadingDialog.showAndMsg("create channel...")
                    TCPClientS.getInstance(BaseApplication.getInstance()).connect(BaseVolume.TCP_IP,BaseVolume.TCP_PORT)
                }
                else {// 已连接，则查询信息
                    queryBMSBMUVerInfo()
                }
            }
        }

    }


    /** 判断当前files是否需要更新 */
    private fun checkUpdateFiles() {
        // 默认低压
        var strBMUFileSign = "BMU-P2"
        var strBMSFileSign = "BMS-P2"
        var strTabSign = "LV"
        var strFanArea = "A"
        var fBMUFanVersion = 0.0f
        var tabTypeArray = DeviceStateInfo.getInstance().lowSystemArray

        // 高压
        if (DeviceStateInfo.getInstance().isHighVolInfo()) {
            strBMUFileSign = "BMU-P3"
            strBMSFileSign = "BMS-P3"
            strTabSign = "HV"
            tabTypeArray = DeviceStateInfo.getInstance().highSystemArray
        }
        // BCU版本号（根据当前在哪个区） 0:在A区，则选B的版本号
        if (DeviceStateInfo.getInstance().BCU_APP_Area.equals("0")) {
            strFanArea = "B"
            fBMUFanVersion = DeviceStateInfo.getInstance().BCU_APP_B_Version.toFloat()
        }
        else { // 1:B区，则选A的版本号
            strFanArea = "A"
            fBMUFanVersion = DeviceStateInfo.getInstance().BCU_APP_A_Version.toFloat()
        }
        willUpdateFileList.clear()
        var tabList = arrayListOf<FileDataInfo>()
        downloadList.forEach {
            // BMU文件
            if (it.getType() == FileDataInfo.FILE_TYPE_BMU) {
                // 当前固件的版本号 大于 设备版本，且是目标区的
                if (it.strFileType.indexOf(strBMUFileSign) >= 0 &&
                    it.fVersion > fBMUFanVersion &&
                    it.strArea.equals(strFanArea)
                ) {
                    willUpdateFileList.add(it)
                }
            }
            // BMS文件
            else if (it.getType() == FileDataInfo.FILE_TYPE_BMS) {
                // 当前固件的版本号 大于 设备版本，且是目标区的
                if (it.strFileType.indexOf(strBMSFileSign) >= 0 &&
                    it.fVersion > DeviceStateInfo.getInstance().BMS_Version.toFloat() &&
                    it.strArea.equals(strFanArea)
                ) {
                    willUpdateFileList.add(it)
                }
            }
            // 阈值表 这一步只能调出高压/低压 的所有阈值表，还要进一步判断
            if (it.getType() == FileDataInfo.FILE_TYPE_TAB) {
                if (it.strFileType.indexOf(strTabSign) >= 0)
                    tabList.add(it)
            }
        }

        var isHaveTab = false
        val strBMSType = tabTypeArray[DeviceStateInfo.getInstance().BMS_Type]
        // 阈值表还需要进一步判断是否升级
        tabList.forEach {
            // 找到对应的阈值表，且文件版本大于设备版本，则升级
            if (it.strFileType.equals(strBMSType)) {
                isHaveTab = true
                if (it.fVersion > DeviceStateInfo.getInstance().Five_Table_Version.toFloat())
                    willUpdateFileList.add(it)

            }
        }
        // 如果没找到对应的阈值表，则全部升级
        if (!isHaveTab)
            willUpdateFileList.addAll(tabList)

        if (willUpdateFileList.size == 0) {
            showToast("当前没有需要升级的文件！")
            showConfigDialog()
            return
        }
        else {
            iEveryProgress = 100/willUpdateFileList.size
            iNowUpdateCount = 0
            Log.e(TAG,"共有${willUpdateFileList.size}个文件需要升级！！！")
            showToast("共有${willUpdateFileList.size}个文件需要升级！！！")
            startUpdateFirmware()
        }


    }


    /** 开始更新固件 */
    private fun startUpdateFirmware() {
        if (iNowUpdateCount == willUpdateFileList.size) {
            tvUpdateInfo.text = "The firmware update is successful"
            UpdateFirmwareHelper.getInstance().stopUpdateFile()
            showConfigDialog()
        }
        else {
            // 取出每个文件占总进度的权重比
            updateProgressBar.progress = iNowUpdateCount*iEveryProgress
            tvProgressValue.text = "${iNowUpdateCount*iEveryProgress}%"
            val updateFile = willUpdateFileList[iNowUpdateCount]
            Log.e(TAG,"开始升级:${updateFile.strFileName} , size:${updateFile.fileDataArray.size}")
            var strStartAddress = ""
            var strUpdateAddress = ""
            // BMU文件
            if (updateFile.getType() == FileDataInfo.FILE_TYPE_BMU) {
                strStartAddress = BaseVolume.CMD_UPDATE_BMU_START_ADDRESS
                strUpdateAddress = BaseVolume.CMD_UPDATE_BMU_ADDRESS
            }
            // BMS文件
            else if (updateFile.getType() == FileDataInfo.FILE_TYPE_BMS) {
                strStartAddress = BaseVolume.CMD_UPDATE_BMS_START_ADDRESS
                strUpdateAddress = BaseVolume.CMD_UPDATE_BMS_ADDRESS
            }
            // 阈值表
            else if (updateFile.getType() == FileDataInfo.FILE_TYPE_TAB) {
                strStartAddress = BaseVolume.CMD_UPDATE_TABLE_START_ADDRESS
                strUpdateAddress = BaseVolume.CMD_UPDATE_TABLE_ADDRESS
            }
            UpdateFirmwareHelper.getInstance().startUpdateFirmwareByTypeFile(strStartAddress,strUpdateAddress,updateFile.strFileName,updateFile.fileDataArray,updateHandler)
        }

    }



    /** 显示连接设备热点的窗口 */
    private fun showConnectDevDialog() {
        showDialog("Connect Device",
            "The firmware download has been completed. Please connect the device to WIFI",
            object : AreaAddWindowHint.PeriodListener {
                override fun refreshListener(string: String?) {
                    startActivity(Intent(mContext, ConnectWIFIActivity().javaClass).putExtra("wifiSign",BaseApplication.DEVICE_WIFI_SIGN))
                }
                override fun cancelListener() {
                }
            },
            true
        )
    }


    /** 显示是否配置或跳过 */
    private fun showConfigDialog() {
        showDialog(
            "Configuation",
            "Do you want to configure the system?Configuation is necessary when you commission the system",
            object : AreaAddWindowHint.PeriodListener {
                override fun refreshListener(string: String?) {
                    finish()
                    startActivity(Intent(mContext, ConfigSystemActivity().javaClass))
                }

                override fun cancelListener() {
                    finish()
                    val intent: Intent = Intent(mContext, MainActivityTest().javaClass)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)

                }
            },
            false,
            "No",
            "Yes"
        )
    }


    /** 读取BMU 系统参数寄存器:  0x0000 寄存器个数：102 */
    private fun queryBMSBMUVerInfo() {
        loadingDialog.showAndMsg("waiting...")
        val strSendData = CreateControlData.readInfoByAddress("0000", "0065")
        BaseApplication.getInstance().StartSendDataByTCPTimeOut(strSendData)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onReceiveMessageInfo(msg: MessageInfo) {
        when(msg.iCode) {
            MessageInfo.i_NET_WORK_STATE -> {
                if (isCheckNetState)
                    checkNetworkState()
            }
            MessageInfo.i_TCP_CONNECT_SUCCESS -> {
                queryBMSBMUVerInfo()
            }
            MessageInfo.i_TCP_CONNECT_FAIL -> {
                val strFailInfo= msg.anyInfo as String
                showToast(strFailInfo)
                showDialog("Connection Failed","Network connection failed,please try again.",object : AreaAddWindowHint.PeriodListener{
                    override fun refreshListener(string: String?) {
                        loadingDialog.showAndMsg("create channel...")
                        TCPClientS.getInstance(BaseApplication.getInstance()).connect(BaseVolume.TCP_IP,BaseVolume.TCP_PORT)
                    }
                    override fun cancelListener() {
                    }
                },false,"cancel","Retry")
            }// 接收数据
            MessageInfo.i_RECEIVE_DATA -> {
                val analysisInfo = msg.anyInfo as AnalysisInfo
                if (analysisInfo.iErrprCode != 0) {
                    showToast("${analysisInfo.iErrprCode},${analysisInfo.strErrorInfo}")
//                    return
                }
                checkData(analysisInfo)
            }
            MessageInfo.i_SEND_DATA_ERROR -> {
                val strError = msg.anyInfo.toString()
                BaseApplication.getInstance().StopSend()
                loadingDialog.dismiss()
                showToast(strError)
            }

        }

    }

    /** 目前先跳过固件下载和升级，只要连接上查到信息直接进入配置页面 fixme */
    private fun checkData(analysisInfo: AnalysisInfo) {
        // 读取的返回
        if (analysisInfo.strType.equals(BaseVolume.CMD_TYPE_READ_DATA, ignoreCase = true)) {
            if (!loadingDialog.isShowing) return
            // BMU 系统参数信息
            if (analysisInfo.iReadNumber == 102) {
                val strSendData = CreateControlData.readInfoByAddress("0500",25)
                BaseApplication.getInstance().StartSendDataByTCPTimeOut(strSendData)
            }
            // 5分钟查询一次的BMU信息
            else if (analysisInfo.iReadNumber == 25) {
                BaseApplication.getInstance().StopSend()
                loadingDialog.dismiss()
                // 查询完成，开始判断哪些文件需要升级
//                checkUpdateFiles()
                showConfigDialog()
            }
        }
        else if (analysisInfo.strType.equals(BaseVolume.CMD_TYPE_WRITE_MORE, ignoreCase = true) ||
            analysisInfo.strType.equals(BaseVolume.CMD_TYPE_WRITE_MORE_ERROR, ignoreCase = true)) {
            BaseApplication.getInstance().StopSend()
            UpdateFirmwareHelper.getInstance().keepUpdate(analysisInfo)
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        UpdateFirmwareHelper.getInstance().stopUpdateFile()
        EventBus.getDefault().unregister(this)
        myNetState.unRegisterEventBus()
    }


    /** 更新进度条 */
    val updateHandler = object : Handler(){
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)

            when(msg?.what) {
                // 更新顺序：BMU → BMS → 阈值表
                UpdateFirmwareHelper.iUPDATA_PROGRESS_START,UpdateFirmwareHelper.iUPDATA_PROGRESS_SENDING, -> {
                    var strUpdateInfo = "update:${UpdateFirmwareHelper.getInstance().strFileName},size:${UpdateFirmwareHelper.getInstance().willUpdateFileArray.size}"
                    var fProcess = msg.obj.toString().toFloat()
                    val fMaxProgress = (iNowUpdateCount)*iEveryProgress+(fProcess*iEveryProgress)
                    updateProgressBar.progress = fMaxProgress.toInt()
                    tvProgressValue.text = "${String.format("%.1f",fMaxProgress)}%"
                    tvUpdateInfo.text = strUpdateInfo
                    Log.e(TAG,"当前文件进度：$fProcess ，整体升级进度:${String.format("%.1f",fMaxProgress)}%")
                }
                // 更新顺序：BMU → BMS → 阈值表
                UpdateFirmwareHelper.iUPDATA_PROGRESS_SUCCESS -> {
                    ++iNowUpdateCount
                    startUpdateFirmware()
                }
                UpdateFirmwareHelper.iUPDATA_PROGRESS_FAULT -> {
                    val strMsg = msg.obj.toString()
                    myNetState.setMessage(strMsg)
                    UpdateFirmwareHelper.getInstance().stopUpdateFile()
                    showDialog("Update failed",strMsg,object : AreaAddWindowHint.PeriodListener{
                        override fun refreshListener(string: String?) {
                            iEveryProgress = 100/willUpdateFileList.size
                            iNowUpdateCount = 0
                            startUpdateFirmware()
                        }
                        override fun cancelListener() {
                        }
                    },false,"Skip","Retry")
                }
            }

        }
    }


}