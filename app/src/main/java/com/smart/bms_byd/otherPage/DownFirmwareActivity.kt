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
import kotlinx.android.synthetic.main.activity_down_firmware.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File

class DownFirmwareActivity : BaseActivity(),DownloadUtil.OnDownloadListener{


    private var iNowDownloadCount = 0
    private var iEveryProgress = 0
    private val TAG = "DownFirmwareActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_down_firmware)

        myNetState.initView(this, true, null)

        EventBus.getDefault().register(this)
//        updateProgressBar.progress = 100

    }

    override fun onResume() {
        super.onResume()

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

        chechFileOK()
//        checkNetworkState()

    }


    /** 校验网络情况 */
    private fun checkNetworkState() {
        if (downloadProgressBar.progress == 100) {
            chechFileOK()
        }
        else {
            // 未联网
            if (BaseApplication.getInstance().nowNetWorkType == NetWorkType.NOTHING_NET)
                showConnectNetDialogByNo("Not Connected","Please go to connect to external WiFi or open the data connection.")
            // 设备热点
            else if (BaseApplication.getInstance().nowNetWorkType == NetWorkType.WIFI_DEVICE)
                showConnectNetDialogByNo("Device Network","The current device network,please go to connect to the external network.")
            // 移动网 或 其他wifi
            else if (BaseApplication.getInstance().nowNetWorkType == NetWorkType.MOBILE_NET ||
                BaseApplication.getInstance().nowNetWorkType == NetWorkType.WIFI_OTHER)
                checkDownloadFiles()
        }

    }

    /** 需要下载的列表，通过http动态过去的 */
    private var downloadList0 = arrayOf(
        "http://120.24.12.116:8090/hlk/BMU-P2-1.20-B-C795.bin",
        "http://120.24.12.116:8090/hlk/BMU-P2-1.20-A-615D.bin",
        "http://120.24.12.116:8090/hlk/BMS-P2-1.10-B-FA61.bin",
        "http://120.24.12.116:8090/hlk/BMS-P2-1.10-A-724F.bin",
        "http://120.24.12.116:8090/hlk/HVL-TAB-0-4.0.bin",
        "http://120.24.12.116:8090/hlk/HVM-TAB-1-7.1.bin",
        "http://120.24.12.116:8090/hlk/HVS-TAB-2-7.2.bin",
        "http://120.24.12.116:8090/hlk/LVL-TAB-0-0.8.bin",
        "http://120.24.12.116:8090/hlk/LVS-TAB-2-0.7.bin"
    )

    // 将要下载的地址链接
    private var willDownloadList = arrayListOf<String>()
    private fun checkDownloadFiles() {
        willDownloadList.clear()

        // 动态获取要下载的文件

        // 遍历本地文件，哪些没有，则下哪些
        downloadList0.forEach {
            var downloadFileName = it.substring(it.lastIndexOf("/") + 1)
            val nowFile = DownloadUtil.get().isHaveFileByTypeName(DownloadUtil.strDownloadFolder,downloadFileName)
            // 文件存在，则删掉，重新下
            if (nowFile != null)
                DownloadUtil.get().deleteFileByName(DownloadUtil.strDownloadFolder,downloadFileName)
            willDownloadList.add(it)
        }
        // 没有需要下载的文件，则直接判断更新去啦
        if (willDownloadList.size == 0) {
            chechFileOK()
            return
        }
        else {
            iEveryProgress = 100/willDownloadList.size
            // 移动网，则提醒用户消耗流量
            if (BaseApplication.getInstance().nowNetWorkType == NetWorkType.MOBILE_NET)
                showSureDownLoad()
            else {
                iNowDownloadCount = 0
                startDownload()
            }
        }

    }



    /** 开始下载 */
    private fun startDownload() {
        if (iNowDownloadCount == willDownloadList.size) {
            chechFileOK()
        }
        else {
            // 取出每个文件占总进度的权重比
            downloadProgressBar.progress = iNowDownloadCount*iEveryProgress
            tvProgressValue.text = "${iNowDownloadCount*iEveryProgress}%"
            val downloadPath = willDownloadList[iNowDownloadCount]
            var downloadFileName = downloadPath.substring(downloadPath.lastIndexOf("/") + 1)
            tvUpdateInfo.text = "downloading file:$downloadFileName"
            DownloadUtil.get().download(mContext,downloadPath,"",downloadFileName,this)
        }

    }

    /** 校验固件是否完整 */
    private fun chechFileOK() {
        loadingDialog.showAndMsg("check files...")
        var goodFileList = arrayListOf<FileDataInfo>()
        var isOK = true
        for (iN in 0 until downloadList0.size) {
            var downloadFileName = downloadList0[iN].substring(downloadList0[iN].lastIndexOf("/") + 1)
            val nowFile = DownloadUtil.get().isHaveFileByTypeName(DownloadUtil.strDownloadFolder,downloadFileName)
            // BMU文件 或 BMS文件 校验
            if (downloadFileName.indexOf("BMU-P") >= 0 || downloadFileName.indexOf("BMS-P") >= 0) {
                // 形如：BMU-P2-1.16-B-A3FD.bin
                val strFileNameArray = downloadFileName.split("-")
                // 校验位
                val strFileCheck = strFileNameArray[4].split(".")[0]
                val fileDataArray = FileHelperInfo.getContentByArrayByCRC(nowFile,strFileCheck)
                // 校验通过
                if (fileDataArray == null) {
                    isOK = false
                    break
                }
                else {
                    goodFileList.add(FileDataInfo(downloadFileName, fileDataArray))
                }
            }
            // 阈值表校验
            else if (((downloadFileName.indexOf("HV") >= 0) && (downloadFileName.indexOf("TAB") >= 0)) ||
                ((downloadFileName.indexOf("LV") >= 0) && (downloadFileName.indexOf("TAB") >= 0))) {
                val fileDataArray = FileHelperInfo.getContentByteArrayByBase64(nowFile)
                // 校验通过
                if (fileDataArray == null) {
                    isOK = false
                    break
                }
                else
                    goodFileList.add(FileDataInfo(downloadFileName,fileDataArray))
            }
        }

        loadingDialog.dismiss()
        if (!isOK) {
            goodFileList.clear()
            showDialog("Verification Failed","Since the verification failed 3 times,you can choose the following to continue.",
                object : AreaAddWindowHint.PeriodListener {
                    override fun refreshListener(string: String?) {
                        checkDownloadFiles()
                    }
                    override fun cancelListener() {
                    }
                },false,"Skip","Retry"
            )
        }
        else {
            showToast("下载完成，开始跳转！")
            startActivity(Intent(mContext,UpdateFirmwareActivity().javaClass).putExtra("fileList",goodFileList))
            finish()
        }


    }

    /** 确认是否用移动网下载 */
    private fun showSureDownLoad() {
        showDialog(
            "Non-WiFi Environment",
            "whether to download the firmware use traffic,the firmware is about 236M.",
            object : AreaAddWindowHint.PeriodListener {
                override fun refreshListener(string: String?) {
//                    FileHelperInfo.deleteFileByFolder(DownloadUtil.strDownloadFolder)
                    iNowDownloadCount = 0
                    startDownload()
                }
                override fun cancelListener() {
                }
            },
            false
        )
    }

    /** 显示连接外网的窗口（当前没有网络） */
    private fun showConnectNetDialogByNo(strTitle:String,strMsg:String) {
        myNetState.setMessage("Network unavailable")
        showDialog(strTitle,strMsg,object : AreaAddWindowHint.PeriodListener {
                override fun refreshListener(string: String?) {
                    // 进入wifi选择界面
                }
                override fun cancelListener() {
                    // 打开移动网
                }
            },
            true
        )
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onReceiveMessageInfo(msg: MessageInfo) {
        when(msg.iCode) {
            MessageInfo.i_NET_WORK_STATE -> {
                checkNetworkState()
            }
        }

    }


    override fun onDownloadSuccess() {
        runOnUiThread {
            ++iNowDownloadCount
            startDownload()
        }

    }

    override fun onDownloading(progress: Int) {
        runOnUiThread {
            downloadProgressBar.progress = ((iNowDownloadCount)*iEveryProgress+(progress/100.0*iEveryProgress)).toInt()
            tvProgressValue.text = "${String.format("%.1f",(iNowDownloadCount) * iEveryProgress + (progress / 100.0 * iEveryProgress))}%"
        }

    }

    override fun onDownloadFailed(strFaileMsg: String?) {
        runOnUiThread {
            showDialog("Verification Failed","Since the verification failed 3 times,you can choose the following to continue.",
                object : AreaAddWindowHint.PeriodListener {
                    override fun refreshListener(string: String?) {
                        checkDownloadFiles()
                    }
                    override fun cancelListener() {
                    }
                },false,"Skip","Retry"
            )
            showToast(strFaileMsg)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        myNetState.unRegisterEventBus()
    }


}