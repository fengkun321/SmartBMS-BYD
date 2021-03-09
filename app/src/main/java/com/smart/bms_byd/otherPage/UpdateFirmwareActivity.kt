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

class UpdateFirmwareActivity : BaseActivity(),DownloadUtil.OnDownloadListener{

    private val downloadList = arrayOf(
        "http://120.24.12.116:8090/hlk/BMU-P2-1.20-B-C795.bin",
        "http://120.24.12.116:8090/hlk/BMU-P2-1.20-A-615D.bin",
        "http://120.24.12.116:8090/hlk/BMS-P2-1.10-B-FA61.bin",
        "http://120.24.12.116:8090/hlk/BMS-P2-1.10-A-724F.bin"
    )
    private var iNowDownloadCount = 0
    private var iEveryProgress = 0
    private val TAG = "UpdateFirmwareActivity"
    private var isCheckNetState = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_firmware)

        myNetState.initView(this, true, this);

        EventBus.getDefault().register(this)

        updateProgressBar.tag = "download"
//        updateProgressBar.tag = "update"
        updateProgressBar.progress = 100
//        updateProgressBar.progress = 0

        tvProgressValue.setOnClickListener {
//            var iProgress = updateProgressBar.progress
//            tvProgressValue.text = "${iProgress+50}%"
//            updateProgressBar.progress = (iProgress+50)
//            checkNetworkState()

//            TCPClientS.getInstance(BaseApplication.getInstance()).connect(BaseVolume.TCP_IP,BaseVolume.TCP_PORT)

        }

        tvUpdateInfo.setOnClickListener {
            if (!updateProgressBar.tag.toString().equals("download")) {
                showDialog(
                    "Update failed",
                    "Since the verification failed 3 times,you can choose the following to continue.",
                    object : AreaAddWindowHint.PeriodListener {
                        override fun refreshListener(string: String?) {
                        }

                        override fun cancelListener() {
                        }
                    },
                    false,
                    "Skip",
                    "Retry"
                )
            }
        }

    }

    override fun onResume() {
        super.onResume()



//        val fileList = DownloadUtil.get().getFilesList(DownloadUtil.strDownloadFolder,"TAB")
//        for (iN in fileList.indices) {
//            // 阈值表，开始验证文件对错
//            if (fileList[iN].name.indexOf(".bin") > 0) {
//                val byteArrayFile = FileHelperInfo.readFileStream(fileList[iN])
//                if (byteArrayFile == null) {
//                    showToast("文件读取失败！")
//                    return
//                }
//                val byteArrayContent = FileHelperInfo.getContentByteArrayByBase64(byteArrayFile)
//                val byteArrayContent1 = FileHelperInfo.getContentByteArrayFirmwareByBase64(byteArrayFile)
//                if (byteArrayContent != null)
//                    showToast("文件校验通过！可以进行升级！")
//                else
//                    showToast("文件校验未通过！")
//                return
//            }
//        }

        isCheckNetState = true
        checkNetworkState()

    }

    override fun onStop() {
        super.onStop()
        isCheckNetState = false
    }

    /** 校验网络情况 */
    private fun checkNetworkState() {
        // 未联网
        if (BaseApplication.getInstance().nowNetWorkType == NetWorkType.NOTHING_NET) {
            // 正在下载
            if (updateProgressBar.tag.toString().equals("download")) {
                tvTitleName.text = "DOWNLOAD FIRMWARE"
                // 下载完成，并提示连接设备热点
                if (updateProgressBar.progress == 100)
                    showConnectDevDialog()
                // 未下载完成，则提示连其他wifi或打开移动网
                else
                    showConnectNetDialogByNo()
            }
            // 正在更新固件
            else
                showConnectDevDialog()
        }
        // 设备热点
        else if (BaseApplication.getInstance().nowNetWorkType == NetWorkType.WIFI_DEVICE) {
            // 正在下载
            if (updateProgressBar.tag.toString().equals("download")) {
                tvTitleName.text = "DOWNLOAD FIRMWARE"
                // 下载完成，则开始连接设备，然后发数据，升级固件等
                if (updateProgressBar.progress == 100) {
                    if (TCPClientS.getInstance(BaseApplication.getInstance()).connectionState != TCPClientS.TCP_CONNECT_STATE_CONNECTED) {
                        loadingDialog.showAndMsg("connecting...")
                        TCPClientS.getInstance(BaseApplication.getInstance()).connect(
                            BaseVolume.TCP_IP,
                            BaseVolume.TCP_PORT
                        )
                    }
                    else {
                        startUpdateFirmware()
                    }
//                    loadingDialog.showAndMsg("connecting...")
//                    TCPClientS.getInstance(BaseApplication.getInstance()).connect(BaseVolume.TCP_IP,BaseVolume.TCP_PORT)
                }

                // 下载未完成，则提示连其他wifi，或移动网
                else
                    showConnectNetDialogByDev()
            }
            // 正在更新固件
            else {
                // 更新完成，则询问是否配置
                if (updateProgressBar.progress == 100)
                    showConfigDialog()
                // 未更新，则先连接设备
                else if (updateProgressBar.progress == 0) {

                }
            }
        }
        // 移动网，则提醒用户消耗流量
        else if (BaseApplication.getInstance().nowNetWorkType == NetWorkType.MOBILE_NET) {
            // 正在下载
            if (updateProgressBar.tag.toString().equals("download")) {
                tvTitleName.text = "DOWNLOAD FIRMWARE"
                // 下载完成，则提示去连接设备热点
                if (updateProgressBar.progress == 100)
                    showConnectDevDialog()
                // 未下载，则提示连其他wifi，或移动网
                else if (updateProgressBar.progress == 0)
                    showSureDownLoad()

            }
            // 正在更新固件
            else
                showConnectDevDialog()
        }
        // 其他wifi
        else {
            // 正在下载
            if (updateProgressBar.tag.toString().equals("download")) {
                tvTitleName.text = "DOWNLOAD FIRMWARE"
                // 下载完成，则提示去连接设备热点
                if (updateProgressBar.progress == 100)
                    showConnectDevDialog()
                // 未下载，则开始下载
                else if (updateProgressBar.progress == 0) {
                    FileHelperInfo.deleteFileByFolder(DownloadUtil.strDownloadFolder)
                    iNowDownloadCount = 0
                    startDownload()
                }

            }
            // 正在更新固件，提示连接设备热点
            else
                showConnectDevDialog()
        }

    }

    /** 开始下载 */
    private fun startDownload() {
        ++iNowDownloadCount
        if (iNowDownloadCount > downloadList.size) {
            tvUpdateInfo.text = "The firmware download is successful"
            checkNetworkState()
        }
        else {
            // 取出每个文件占总进度的权重比
            iEveryProgress = 100/downloadList.size
            updateProgressBar.tag = "download"
            updateProgressBar.progress = (iNowDownloadCount-1)*iEveryProgress
            tvProgressValue.text = "${(iNowDownloadCount-1)*iEveryProgress}%"
            tvTitleName.text = "UPDATE DOWNLOAD"
            var downloadFileName = downloadList[iNowDownloadCount - 1].substring(
                downloadList[iNowDownloadCount - 1].lastIndexOf(
                    "/"
                ) + 1
            )
            if (DownloadUtil.get().isHaveFileByTypeName(
                    DownloadUtil.strDownloadFolder,
                    downloadFileName
                )) {
                updateProgressBar.progress = (iNowDownloadCount)*iEveryProgress
                tvProgressValue.text = "${(iNowDownloadCount)*iEveryProgress}%"
                startDownload()
            }
            else {
//                showToast("Start the download...")
                tvUpdateInfo.text = "downloading file:$downloadFileName"
//                startDownload()
                DownloadUtil.get().download(
                    mContext,
                    downloadList[iNowDownloadCount - 1],
                    "",
                    downloadFileName,
                    this
                )
            }



        }



    }

    /** 开始更新固件 */
    private fun startUpdateFirmware() {
        updateProgressBar.tag = "update"
        updateProgressBar.progress = 0
        tvProgressValue.text = "0%"
        tvTitleName.text = "UPDATE FIRMWARE"

        // 已连接的，则查询版本号
        queryBMSBMUVerInfo()

    }

    /** 确认是否用移动网下载 */
    private fun showSureDownLoad() {
        showDialog(
            "Non-WiFi Environment",
            "whether to download the firmware use traffic,the firmware is about 236M.",
            object : AreaAddWindowHint.PeriodListener {
                override fun refreshListener(string: String?) {
                    FileHelperInfo.deleteFileByFolder(DownloadUtil.strDownloadFolder)
                    iNowDownloadCount = 0
                    startDownload()
                }

                override fun cancelListener() {
                }
            },
            false
        )
    }

    /** 显示连接设备热点的窗口 */
    private fun showConnectDevDialog() {
        showDialog(
            "Connect Device",
            "The firmware download has been completed. Please connect the device to WIFI",
            object : AreaAddWindowHint.PeriodListener {
                override fun refreshListener(string: String?) {
                    startActivity(
                        Intent(mContext, ConnectWIFIActivity().javaClass).putExtra(
                            "wifiSign",
                            BaseApplication.DEVICE_WIFI_SIGN
                        )
                    )
                }

                override fun cancelListener() {
                }
            },
            true
        )
    }

    /** 显示连接外网的窗口（当前没有网络） */
    private fun showConnectNetDialogByNo() {

        myNetState.setMessage("Network unavailable")

        showDialog(
            "Not Connected",
            "Please go to connect to external WiFi or open the data connection.",
            object : AreaAddWindowHint.PeriodListener {
                override fun refreshListener(string: String?) {
                    // 进入wifi选择界面
//                startActivity(Intent(mContext,ConnectWIFIActivity().javaClass))
                }

                override fun cancelListener() {
                    // 打开移动网
//                BaseApplication.getInstance().setMobileDataEnabled(true)
//                BaseApplication.getInstance().toggleMobileData(mContext,true)
                }
            },
            true
        )
    }
    /** 显示连接外网的窗口（当前是Dev热点） */
    private fun showConnectNetDialogByDev() {
        myNetState.setMessage("Network unavailable")
        showDialog(
            "Device Network",
            "The current device network,please go to connect to the external network.",
            object : AreaAddWindowHint.PeriodListener {
                override fun refreshListener(string: String?) {
                    // 进入wifi选择界面
//                startActivity(Intent(mContext,ConnectWIFIActivity().javaClass))
                }

                override fun cancelListener() {
                    // 打开移动网
//                BaseApplication.getInstance().setMobileDataEnabled(true)
                }
            },
            false,
            "cellular",
            "WIFI"
        )
    }
    /** 显示是否配置或跳过 */
    private fun showConfigDialog() {
        showDialog(
            "Configuation",
            "Do you want to configure the system?Configuation is necessary when you commission the system",
            object : AreaAddWindowHint.PeriodListener {
                override fun refreshListener(string: String?) {
                    startActivity(Intent(mContext, ConfigSystemActivity().javaClass))
                    finish()
                }

                override fun cancelListener() {
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
                val strFailInfo = msg.anyInfo as String
                showToast(strFailInfo)
            }// 接收数据
            MessageInfo.i_RECEIVE_DATA -> {
                val analysisInfo = msg.anyInfo as AnalysisInfo
                if (analysisInfo.iErrprCode != 0) {
                    showToast("${analysisInfo.iErrprCode},${analysisInfo.strErrorInfo}")
                    return
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
            // BMU 系统参数信息
            if (analysisInfo.iReadNumber == 102) {
                val strSendData = CreateControlData.readInfoByAddress("0500",25)
                BaseApplication.getInstance().StartSendDataByTCPTimeOut(strSendData)
//            showConfigDialog()
            }
            // 5分钟查询一次的BMU信息
            else if (analysisInfo.iReadNumber == 25) {
                BaseApplication.getInstance().StopSend()
                loadingDialog.dismiss()
                initDevVersionInfo()
                showConfigDialog()
//                updateBMUFirmware()
            }
        }
        else if (analysisInfo.strType.equals(BaseVolume.CMD_TYPE_WRITE_MORE, ignoreCase = true) ||
            analysisInfo.strType.equals(BaseVolume.CMD_TYPE_WRITE_MORE_ERROR, ignoreCase = true)) {
            BaseApplication.getInstance().StopSend()
            UpdateFirmwareHelper.getInstance().keepUpdate(analysisInfo)
        }


    }

    var strBMUFileSign = ""
    var strBMSFileSign = ""
    var strFanArea = ""
    var fBMUFanVersion = 0.0f
    var strTabSign = ""

    /** 获取部分版本号 */
    private fun initDevVersionInfo() {
        // 低压
        if ((DeviceStateInfo.getInstance().BCU_SN.indexOf("P02") == 0) || (DeviceStateInfo.getInstance().BCU_SN.indexOf("P01") == 0)) {
            strBMUFileSign = "BMU-P2"
            strBMSFileSign = "BMS-P2"
            strTabSign = "L"
        }
        // 高压
        else if (DeviceStateInfo.getInstance().BCU_SN.indexOf("P03") == 0){
            strBMUFileSign = "BMU-P3"
            strBMSFileSign = "BMS-P3"
            strTabSign = "H"
        }
        // 手动选择高低压
        else {

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

    }
    /** 更新BMU的固件 */
    private fun updateBMUFirmware() {

        val fileList = DownloadUtil.get().getFilesList(
            DownloadUtil.strDownloadFolder,
            strBMUFileSign
        )
        var updateFile : File? = null
        var strFileCheck = ""
        for (iN in fileList.indices) {
            // 形如：BMU-P2-1.16-B-A3FD.bin
            val strFileNameArray = fileList[iN].name.split("-")
            // 固件版本号
            val fFileVer = strFileNameArray[2].toFloat()
//            if (fFileVer > fBMUFanVersion && strFileNameArray[3].equals(strFanArea)) {
            // 测试时，只判断区域
            if (strFileNameArray[3].equals(strFanArea)) {
                // 校验位
                strFileCheck = strFileNameArray[4].substring(0, 4)
                updateFile = fileList[iN]
                break
            }
        }

        if (updateFile == null) {
            showToast("当前BMU固件已是最新的啦！")
            UpdateFirmwareHelper.getInstance().strUpdateStartAddress = BaseVolume.CMD_UPDATE_BMU_START_ADDRESS
            mHandler.sendEmptyMessage(UpdateFirmwareHelper.iUPDATA_PROGRESS_SUCCESS)
            return
        }
        val fileByteArray = FileHelperInfo.readFileStream(updateFile)
        val strNewFileCheck = NetworkUtils.bytesToHexString(CRC16.getCrc16(fileByteArray))
        // 校验未通过，则认为是无效文件
        if (!strNewFileCheck.equals(strFileCheck,true)) {
            showDialog("Update failed","BMU固件有误，无法升级！",object : AreaAddWindowHint.PeriodListener{
                override fun refreshListener(string: String?) {
                    updateBMUFirmware()
                }
                override fun cancelListener() {
                }
            },false,"Skip","Retry")
            return
        }
        Log.e(TAG,"开始升级BMU固件:${updateFile.name} , size:${updateFile.length()}")
        UpdateFirmwareHelper.getInstance().startUpdateFirmwareByTypeFile(BaseVolume.CMD_UPDATE_BMU_START_ADDRESS,BaseVolume.CMD_UPDATE_BMU_ADDRESS,updateFile.name,fileByteArray,updateHandler)
    }

    /** 更新BMS的固件 */
    private fun updateBMSFirmware() {
        val fileList = DownloadUtil.get().getFilesList(
            DownloadUtil.strDownloadFolder,
            strBMSFileSign
        )
        var updateFile : File? = null
        var strFileCheck = ""
        for (iN in fileList.indices) {
            // 形如：BMS-P2-1.16-B-A3FD.bin
            val strFileNameArray = fileList[iN].name.split("-")
            // 固件版本号
            val fFileVer = strFileNameArray[2].toFloat()
//            if (fFileVer > DeviceStateInfo.getInstance().BMS_Version.toFloat() && strFileNameArray[3].equals(strFanArea)) {
            // 测试时只判断区域
            if (strFileNameArray[3].equals(strFanArea)) {
                // 校验位
                strFileCheck = strFileNameArray[4].substring(0, 4)
                updateFile = fileList[iN]
                break
            }
        }

        if (updateFile == null) {
            showToast("当前BMS固件已是最新的啦！")
            UpdateFirmwareHelper.getInstance().strUpdateStartAddress = BaseVolume.CMD_UPDATE_BMS_START_ADDRESS
            mHandler.sendEmptyMessage(UpdateFirmwareHelper.iUPDATA_PROGRESS_SUCCESS)
            return
        }
        val fileByteArray = FileHelperInfo.readFileStream(updateFile)
        val strNewFileCheck = NetworkUtils.bytesToHexString(CRC16.getCrc16(fileByteArray))
        // 校验未通过，则认为是无效文件
        if (!strNewFileCheck.equals(strFileCheck,true)) {
            showDialog("Update failed","BMS固件有误，无法升级！",object : AreaAddWindowHint.PeriodListener{
                override fun refreshListener(string: String?) {
                    updateBMUFirmware()
                }
                override fun cancelListener() {
                }
            },false,"Skip","Retry")
            return
        }

        Log.e(TAG,"开始升级BMS固件:${updateFile.name} , size:${updateFile.length()}")
        UpdateFirmwareHelper.getInstance().startUpdateFirmwareByTypeFile(BaseVolume.CMD_UPDATE_BMS_START_ADDRESS,BaseVolume.CMD_UPDATE_BMS_ADDRESS,updateFile.name,fileByteArray,updateHandler)

    }

    private var willUpdateTableList = arrayListOf<File>()
    /** 更新阈值表 */
    private fun updateTable() {
        willUpdateTableList.clear()
        val fileList = DownloadUtil.get().getFilesList(
            DownloadUtil.strDownloadFolder,
            "TAB"
        )
        var updateFile : File? = null
        for (iN in fileList.indices) {
            // 形如：HVL-TAB-0-4.0.bin
            val strFileNameArray = fileList[iN].name.split("-")
            // 固件版本号
            val fFileVer = strFileNameArray[3].split(".")[0].toFloat()
            if (fFileVer > DeviceStateInfo.getInstance().Five_Table_Version.toFloat()) {
                updateFile = fileList[iN]
                break
            }
        }

        if (updateFile == null) {
            showToast("当前阈值表已是最新的啦！")
            UpdateFirmwareHelper.getInstance().strUpdateStartAddress = BaseVolume.CMD_UPDATE_TABLE_START_ADDRESS
            mHandler.sendEmptyMessage(UpdateFirmwareHelper.iUPDATA_PROGRESS_SUCCESS)
            return
        }
        val fileByteArray0 = FileHelperInfo.readFileStream(updateFile)
        val fileWillUpdate0 = FileHelperInfo.getContentByteArrayByBase64(fileByteArray0)
        // 校验未通过，则认为是无效文件
        if (fileWillUpdate0 == null) {
            showDialog("Update failed","阈值表校验有误，无法升级！",object : AreaAddWindowHint.PeriodListener{
                override fun refreshListener(string: String?) {
                    updateBMUFirmware()
                }
                override fun cancelListener() {
                }
            },false,"Skip","Retry")
            return
        }
        Log.e(TAG,"开始升级阈值表:${updateFile.name} , size:${updateFile.length()}")
        UpdateFirmwareHelper.getInstance().startUpdateFirmwareByTypeFile(BaseVolume.CMD_UPDATE_TABLE_START_ADDRESS,BaseVolume.CMD_UPDATE_TABLE_ADDRESS,updateFile.name,fileByteArray0,updateHandler)

        if (willUpdateTableList.size == 0) {
            showToast("当前阈值表已是最新的啦！")
            UpdateFirmwareHelper.getInstance().strUpdateStartAddress = BaseVolume.CMD_UPDATE_TABLE_START_ADDRESS
            mHandler.sendEmptyMessage(UpdateFirmwareHelper.iUPDATA_PROGRESS_SUCCESS)
        }
        else {
            startTableByPosition(0)
        }


    }

    /**
     * 升级某张阈值表
     */
    private fun startTableByPosition(iPos : Int) {
        val fileByteArray = FileHelperInfo.readFileStream(willUpdateTableList[iPos])
        val fileWillUpdate = FileHelperInfo.getContentByteArrayByBase64(fileByteArray)
        // 校验未通过，则认为是无效文件
        if (fileWillUpdate == null) {
            showDialog("Update failed","阈值表校验有误，无法升级！",object : AreaAddWindowHint.PeriodListener{
                override fun refreshListener(string: String?) {
                    updateBMUFirmware()
                }
                override fun cancelListener() {
                }
            },false,"Skip","Retry")
            return
        }
        Log.e(TAG,"开始升级阈值表:${willUpdateTableList[iPos].name} , size:${willUpdateTableList[iPos].length()}")
        UpdateFirmwareHelper.getInstance().startUpdateFirmwareByTypeFile(BaseVolume.CMD_UPDATE_TABLE_START_ADDRESS,BaseVolume.CMD_UPDATE_TABLE_ADDRESS,willUpdateTableList[iPos].name,fileByteArray,updateHandler)

    }

    override fun onDestroy() {
        super.onDestroy()
        UpdateFirmwareHelper.getInstance().stopUpdateFile()
        EventBus.getDefault().unregister(this)
        myNetState.unRegisterEventBus()
    }

    override fun onDownloadSuccess() {
        runOnUiThread {
            startDownload()
        }

    }

    override fun onDownloading(progress: Int) {
        runOnUiThread {
//            updateProgressBar.progress = progress
//            tvProgressValue.text = "$progress%"

            updateProgressBar.progress = ((iNowDownloadCount-1)*iEveryProgress+(progress/100.0*iEveryProgress)).toInt()
            tvProgressValue.text = "${String.format(
                "%.1f",
                (iNowDownloadCount - 1) * iEveryProgress + (progress / 100.0 * iEveryProgress)
            )}%"

        }

    }

    override fun onDownloadFailed(strFaileMsg: String?) {
        showDialog(
            "Verification Failed",
            "Since the verification failed 3 times,you can choose the following to continue.",
            object : AreaAddWindowHint.PeriodListener {
                override fun refreshListener(string: String?) {
                    // 删除后重新下载
                    fileList().forEach {
                        val strFileName =
                            it.substring(downloadList[iNowDownloadCount - 1].lastIndexOf("/") + 1)
                        DownloadUtil.get()
                            .deleteFileByName(DownloadUtil.strDownloadFolder, strFileName)
                    }
                    FileHelperInfo.deleteFileByFolder(DownloadUtil.strDownloadFolder)
                    iNowDownloadCount = 0
                    startDownload()
                }

                override fun cancelListener() {
                    updateProgressBar.progress = 100
                    tvProgressValue.text = "100%"
                    startActivity(
                        Intent(mContext, ConnectWIFIActivity().javaClass).putExtra(
                            "wifiSign",
                            BaseApplication.DEVICE_WIFI_SIGN
                        )
                    )
                }
            },
            false,
            "Skip",
            "Retry"
        )
        showToast(strFaileMsg)
    }

    /** 更新进度条 */
    val updateHandler = object : Handler(){
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)

            when(msg?.what) {
                // 更新顺序：BMU → BMS → 阈值表
                UpdateFirmwareHelper.iUPDATA_PROGRESS_START,UpdateFirmwareHelper.iUPDATA_PROGRESS_SENDING, -> {
                    var iBaseCount = 0
                    var fTableProgress = 1.0f
                    var strUpdateInfo = "update:${UpdateFirmwareHelper.getInstance().strFileName},size:${UpdateFirmwareHelper.getInstance().willUpdateFileArray.size}"
                    if (UpdateFirmwareHelper.getInstance().strUpdateStartAddress.equals(BaseVolume.CMD_UPDATE_BMU_START_ADDRESS))
                        iBaseCount = 0
                    else if (UpdateFirmwareHelper.getInstance().strUpdateStartAddress.equals(BaseVolume.CMD_UPDATE_BMS_START_ADDRESS))
                        iBaseCount = 1
                    else if (UpdateFirmwareHelper.getInstance().strUpdateStartAddress.equals(BaseVolume.CMD_UPDATE_TABLE_START_ADDRESS)) {
                        iBaseCount = 2
                        var fPos = 1.0f
                        for (iPo in 1 .. willUpdateTableList.size) {
                            if (willUpdateTableList[iPo-1].name.equals(UpdateFirmwareHelper.getInstance().strFileName)) {
                                fPos = iPo.toFloat()
                                break
                            }
                        }
                        fTableProgress = fPos/willUpdateTableList.size
                    }

                    var fProcess = msg.obj.toString().toFloat()
                    updateProgressBar.progress = (iBaseCount*33+fProcess*(33.0f*fTableProgress)).toInt()
                    tvProgressValue.text = "${String.format("%.1f", iBaseCount*33+fProcess*(33.0f*fTableProgress))}%"
                    tvUpdateInfo.text = strUpdateInfo
                    Log.e(TAG,"整体升级进度:${String.format("%.1f", iBaseCount*33+fProcess*(33.0f*fTableProgress))}%")
                }
                // 更新顺序：BMU → BMS → 阈值表
                UpdateFirmwareHelper.iUPDATA_PROGRESS_SUCCESS -> {
                    if (UpdateFirmwareHelper.getInstance().strUpdateStartAddress.equals(BaseVolume.CMD_UPDATE_BMU_START_ADDRESS)) {
                        updateBMSFirmware()
                    }
                    else if (UpdateFirmwareHelper.getInstance().strUpdateStartAddress.equals(BaseVolume.CMD_UPDATE_BMS_START_ADDRESS)) {
//                        updateTable()
                        showToast("The firmware update is successful!")
                        Log.e(TAG,"升级完成！")
                        updateProgressBar.progress = 100
                        tvProgressValue.text = "100%"
                        tvUpdateInfo.text = "The firmware update is successful!"
                        UpdateFirmwareHelper.getInstance().stopUpdateFile()
                        showConfigDialog()
                    }
                    else if (UpdateFirmwareHelper.getInstance().strUpdateStartAddress.equals(BaseVolume.CMD_UPDATE_TABLE_START_ADDRESS)) {
                        var iPos = 0
                        // 判断当前升级的阈值表是第几张
                        for (iPo in 0 until willUpdateTableList.size) {
                            if (willUpdateTableList[iPo].name.equals(UpdateFirmwareHelper.getInstance().strFileName)) {
                                iPos = iPo
                                break
                            }
                        }
                        // 如果没有阈值表 或 是最后一张，则提示升级完成！
                        if (willUpdateTableList.size == 0 || (iPos+1 == willUpdateTableList.size)) {
                            showToast("The firmware update is successful!")
                            Log.e(TAG,"升级完成！")
                            updateProgressBar.progress = 100
                            tvProgressValue.text = "100%"
                            tvUpdateInfo.text = "The firmware update is successful!"
                            UpdateFirmwareHelper.getInstance().stopUpdateFile()
                            showConfigDialog()
                        }
                        else {
                            startTableByPosition(iPos+1)
                        }

                    }
                }
                UpdateFirmwareHelper.iUPDATA_PROGRESS_FAULT -> {
                    val strMsg = msg.obj.toString()
                    myNetState.setMessage(strMsg)
                    UpdateFirmwareHelper.getInstance().stopUpdateFile()
                    showDialog("Update failed",strMsg,object : AreaAddWindowHint.PeriodListener{
                        override fun refreshListener(string: String?) {
                            updateBMUFirmware()
                        }
                        override fun cancelListener() {
                        }
                    },false,"Skip","Retry")
                }
            }

        }
    }


}