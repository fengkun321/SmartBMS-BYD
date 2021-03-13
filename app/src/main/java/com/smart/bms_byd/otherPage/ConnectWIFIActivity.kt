package com.smart.bms_byd.otherPage

import android.content.Context
import android.net.wifi.*
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.smart.bms_byd.BaseActivity
import com.smart.bms_byd.BaseApplication
import com.smart.bms_byd.R
import com.smart.bms_byd.adapter.WiFiLsitAdapter
import com.smart.bms_byd.tcpclient.TCPClientS
import com.smart.bms_byd.util.BaseVolume
import com.smart.bms_byd.util.NetWorkType
import com.smart.bms_byd.view.AreaAddWindowHint
import com.smart.bms_byd.view.NetStateInfoView
import com.smart.bms_byd.wifiInfo.WIFIConnectionManager
import com.smart.bms_byd.wifiInfo.WIFIConnectionTest
import com.smartIPandeInfo.data.MessageInfo
import kotlinx.android.synthetic.main.activity_connectwifi.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*


class ConnectWIFIActivity : BaseActivity(),
    NetStateInfoView.NetStateInfoListener,WiFiLsitAdapter.OnItemClickListener{


    private lateinit var wiFiLsitAdapter : WiFiLsitAdapter
    private var wifiList = arrayListOf<ScanResult>()
    private var nowSelectSSID = ""
    private var wifiSign = ""
    private var strSelectPwd = BaseApplication.DEVICE_WIFI_PWD
    private lateinit var wifiConnectionManager: WIFIConnectionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connectwifi)

        wifiConnectionManager = WIFIConnectionManager(this)
        EventBus.getDefault().register(this);

        wifiSign = intent.getStringExtra("wifiSign")
        myNetState.initView(this, true, null);
        wiFiLsitAdapter = WiFiLsitAdapter(wifiList, mContext)
        wiFiLsitAdapter.onItemClickListener = this
        //设置布局管理器
        recyclerDevice.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        //设置adapter
        recyclerDevice.adapter = wiFiLsitAdapter
        //设置Item增加、移除动画
        recyclerDevice.itemAnimator = DefaultItemAnimator()
        //添加分割线
//        recyclerDevice.addItemDecoration(SpaceItemDecoration(1, mContext))

        wifiConnectionManager.openWifi()

        imgLeft.setOnClickListener { finish() }

    }

    override fun onResume() {
        super.onResume()
        startScanWifi(true)
    }

    override fun onPause() {
        super.onPause()
        startScanWifi(false)
    }

    var scanTimer = Timer()
    private fun startScanWifi(isScan: Boolean) {
        scanTimer.cancel()
        if (isScan) {
            scanTimer = Timer()
            scanTimer.schedule(object : TimerTask() {
                override fun run() {
                    val newWifiList = wifiConnectionManager.getWifiList(wifiSign)
                    // 去重
                    newWifiList?.forEach {
                        var isHave = false
                        for (iN in 0 until wifiList.size) {
                            if (wifiList[iN].SSID.equals(it.SSID)) {
                                isHave = true
                                if (wifiList[iN].level != it.level) {
                                    wifiList[iN].level = it.level
                                    runOnUiThread {
                                        wiFiLsitAdapter.notifyItemChanged(iN)
                                    }
                                }
                                break
                            }
                        }
                        if (!isHave) {
                            wifiList.add(it)
                            runOnUiThread {
                                wiFiLsitAdapter.notifyItemChanged(wifiList.size)
                            }
                        }
                    }
                    runOnUiThread {
                        if (wifiList.size > 0) llWaiting.visibility = View.GONE
                    }
                }
            }, 0, 3000)
        }
    }

    private lateinit var nowSelectWifiInfo : ScanResult
    // wifi 列表的点击事件
    override fun onItemClick(view: View?, position: Int) {
        if (wifiList[position].SSID.equals(BaseApplication.getInstance().strNowSSID))
            return
        startScanWifi(false)
        nowSelectWifiInfo = wifiList[position]
        // 属于比亚迪热点，则自动连接
        strSelectPwd = BaseApplication.DEVICE_WIFI_PWD
        wifiConnectByAndroidQ()
    }


    /**
     * 连接指定wifi-针对Android10.0系统
     */
    fun wifiConnectByAndroidQ() {
        // 如果当前连接已经是目标wifi，则自动结束
        if (BaseApplication.getInstance().strNowSSID.equals(nowSelectWifiInfo.SSID)) {
            loadingDialog.dismiss()
            mHandler.removeCallbacks(connectTimeOutRunnable)
            EventBus.getDefault().unregister(this)
            showToast("WIFI connection success！")
            finish()
        }
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val configuration: WifiConfiguration = WIFIConnectionTest.configWifiInfo(this, wifiManager,nowSelectWifiInfo, strSelectPwd)
        var netId = configuration.networkId
        if (netId == -1)
            netId = wifiManager.addNetwork(configuration)
        val isEnable = wifiManager.enableNetwork(netId, true)
        Log.e("configWifiInfo", "isEnable:${isEnable}")
        if (!isEnable) {
            loadingDialog.dismiss()
            showDialog("Connection Failed","Network connection failed,please try again.",object : AreaAddWindowHint.PeriodListener{
                override fun refreshListener(string: String?) {
                    wifiConnectByAndroidQ()
                }
                override fun cancelListener() {
                }
            },false,"cancel","Retry")
        }
        else {
            loadingDialog.showAndMsg("connecting...")
            mHandler.postDelayed(connectTimeOutRunnable,20*1000)
        }
    }

    private val connectTimeOutRunnable = object : Runnable{
        override fun run() {
            if (loadingDialog.isShowing) {
                loadingDialog.dismiss()
                showDialog("Connection Failed","Network connection failed,please try again.",object : AreaAddWindowHint.PeriodListener{
                    override fun refreshListener(string: String?) {
                        wifiConnectByAndroidQ()
                    }
                    override fun cancelListener() {
                    }
                },false,"cancel","Retry")
            }
        }

    }



    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onReceiveMessageInfo(msg: MessageInfo) {
        when(msg.iCode) {
            // 网络状态
            MessageInfo.i_NET_WORK_STATE -> {
                val netWorkType = msg.anyInfo as NetWorkType
                if (netWorkType == NetWorkType.WIFI_DEVICE) {
                    if (!this::nowSelectWifiInfo.isInitialized) return
                    if (BaseApplication.getInstance().strNowSSID.equals(nowSelectWifiInfo.SSID) && loadingDialog.isShowing) {
                        loadingDialog.dismiss()
                        mHandler.removeCallbacks(connectTimeOutRunnable)
                        EventBus.getDefault().unregister(this)
                        showToast("WIFI connection success！")
                        finish()
                    }
                }
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        mHandler.removeCallbacks(connectTimeOutRunnable)
        EventBus.getDefault().unregister(this)
        myNetState.unRegisterEventBus()
    }



}