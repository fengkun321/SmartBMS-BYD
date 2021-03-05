package com.smart.bms_byd.otherPage

import android.content.Intent
import android.os.Bundle
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
    private var iNowDownloadCount = 0;
    private var iEveryProgress = 0;
    private val TAG = "UpdateFirmwareActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_firmware)

        myNetState.initView(this, true, this);

        EventBus.getDefault().register(this)

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





//        val fileList = DownloadUtil.get().getFilesList(DownloadUtil.strDownloadFolder)
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



        checkNetworkState()

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
        BaseApplication.getInstance().StartSendDataByTCP(strSendData)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onReceiveMessageInfo(msg: MessageInfo) {
        when(msg.iCode) {
            MessageInfo.i_NET_WORK_STATE -> {
//                checkNetworkState()
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

        }

    }

    /** 目前先跳过固件下载和升级，只要连接上查到信息直接进入配置页面 fixme */
    private fun checkData(analysisInfo: AnalysisInfo) {
        // BMU 系统参数信息
        if (analysisInfo.iReadNumber == 102) {
            val strSendData = CreateControlData.readInfoByAddress("0500",25)
            BaseApplication.getInstance().StartSendDataByTCP(strSendData)
//            showConfigDialog()
        }
        // 5分钟查询一次的BMU信息
        else if (analysisInfo.iReadNumber == 25) {
            initDevVersionInfo()
            updateBMUFirmware()
        }

    }

    var strBMUFileSign = ""
    var strBMSFileSign = ""
    var strFanArea = ""
    var fBMUFanVersion = 0.0f
    var strInverterSign = ""

    /** 获取部分版本号 */
    private fun initDevVersionInfo() {
        // 低压
        if ((DeviceStateInfo.getInstance().BCU_SN.indexOf("P02") == 0) || (DeviceStateInfo.getInstance().BCU_SN.indexOf(
                "P01"
            ) == 0)) {
            strBMUFileSign = "BMU-P2"
            strBMSFileSign = "BMS-P2"
            when(DeviceStateInfo.getInstance().BMS_Type.toInt()) {
                0 -> strInverterSign = "HVL-TAB-"
                1 -> strInverterSign = "HVM-TAB-"
                2 -> strInverterSign = "HVS-TAB-"
            }
        }
        // 高压
        else if (DeviceStateInfo.getInstance().BCU_SN.indexOf("P03") == 0){
            strBMUFileSign = "BMU-P3"
            strBMSFileSign = "BMS-P3"
            when(DeviceStateInfo.getInstance().BMS_Type.toInt()) {
                0 -> strInverterSign = "LVL-TAB-"
                1 -> strInverterSign = "LV Flex-TAB-"
                2 -> strInverterSign = "LVS-TAB-"
            }
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
            if (fFileVer > fBMUFanVersion && strFileNameArray[3].equals(strFanArea)) {
                // 校验位
                strFileCheck = strFileNameArray[4].substring(0, 4)
                updateFile = fileList[iN]
                break
            }
        }

        if (updateFile == null) {
            showToast("当前BMU固件已是最新的啦！")
            updateBMSFirmware()
            return
        }
        val fileByteArray = FileHelperInfo.readFileStream(updateFile)
        val strNewFileCheck = NetworkUtils.bytesToHexString(CRC16.getCrc16(fileByteArray))
        // 校验未通过，则认为是无效文件
        if (!strNewFileCheck.equals(strFileCheck,true)) {
            showToast("BMU固件有误，无法升级！")
            updateBMSFirmware()
            return
        }

        Log.e(TAG,"开始升级BMU固件:${updateFile.name} , size:${updateFile.length()}")
        updateProgressBar.progress = 0
        tvProgressValue.text = "0%"
        tvUpdateInfo.text = "update:${updateFile.name},size:${updateFile.length()}"
        // 下一步
        updateBMSFirmware()

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
            if (fFileVer > DeviceStateInfo.getInstance().BMS_Version.toFloat() && strFileNameArray[3].equals(strFanArea)) {
                // 校验位
                strFileCheck = strFileNameArray[4].substring(0, 4)
                updateFile = fileList[iN]
                break
            }
        }

        if (updateFile == null) {
            showToast("当前BMS固件已是最新的啦！")
            updateBMSFirmware()
            return
        }
        val fileByteArray = FileHelperInfo.readFileStream(updateFile)
        val strNewFileCheck = NetworkUtils.bytesToHexString(CRC16.getCrc16(fileByteArray))
        // 校验未通过，则认为是无效文件
        if (!strNewFileCheck.equals(strFileCheck,true)) {
            showToast("BMS固件有误，无法升级！")
            updateBMSFirmware()
            return
        }

        Log.e(TAG,"开始升级BMS固件:${updateFile.name} , size:${updateFile.length()}")
        updateProgressBar.progress = 34
        tvProgressValue.text = "33%"
        tvUpdateInfo.text = "update:${updateFile.name},size:${updateFile.length()}"
        // 下一步
        updateInverter()
    }

    /** 更新阈值表 */
    private fun updateInverter() {
        val fileList = DownloadUtil.get().getFilesList(
            DownloadUtil.strDownloadFolder,
            strInverterSign
        )
        var updateFile : File? = null
        for (iN in fileList.indices) {
            // 形如：HVL-TAB-0-4.0.bin
            val strFileNameArray = fileList[iN].name.split("-")
            // 固件版本号
            val fFileVer = strFileNameArray[3].split(".")[0].toFloat()
            if (fFileVer > DeviceStateInfo.getInstance().VPT_Table_Version.toFloat()) {
                updateFile = fileList[iN]
                break
            }
        }

        if (updateFile == null) {
            showToast("当前阈值表已是最新的啦！")
            updateBMSFirmware()
            return
        }
        val fileByteArray = FileHelperInfo.readFileStream(updateFile)
        val fileWillUpdate = FileHelperInfo.getContentByteArrayByBase64(fileByteArray)
        // 校验未通过，则认为是无效文件
        if (fileWillUpdate == null) {
            showToast("阈值表校验有误，无法升级！")
            updateBMSFirmware()
            return
        }

        Log.e(TAG,"开始升级阈值表:${updateFile.name} , size:${updateFile.length()}")
        updateProgressBar.progress = 67
        tvProgressValue.text = "67%"
        tvUpdateInfo.text = "update:${updateFile.name},size:${updateFile.length()}"

        showToast("升级完成！")
    }

    override fun onDestroy() {
        super.onDestroy()
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


}