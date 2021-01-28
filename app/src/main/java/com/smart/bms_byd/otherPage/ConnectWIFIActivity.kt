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
import com.smart.bms_byd.util.NetWorkType
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
    private var nowSelectSSID = BaseApplication.getInstance().strNowSSID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connectwifi)

        EventBus.getDefault().register(this);


        myNetState.initView(this, true, null);


//        wifiList.add(ScanWiFiInfo("123","11"))
//        wifiList.add(ScanWiFiInfo("456","22"))
//        wifiList.add(ScanWiFiInfo("789","33"))

        wiFiLsitAdapter = WiFiLsitAdapter(wifiList, mContext)
        wiFiLsitAdapter.onItemClickListener = this
        //设置布局管理器
        recyclerDevice.layoutManager = LinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false
        )
        //设置adapter
        recyclerDevice.adapter = wiFiLsitAdapter
        //设置Item增加、移除动画
        recyclerDevice.itemAnimator = DefaultItemAnimator()
        //添加分割线
//        recyclerDevice.addItemDecoration(SpaceItemDecoration(1, mContext))

        WIFIConnectionManager.getInstance(this)?.openWifi()
        startScanWifi(true)

    }

    override fun onResume() {
        super.onResume()

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
                    val newWifiList = WIFIConnectionManager.getInstance(mContext)?.allWifiList!!
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
                }
            }, 0, 2000)

        }

    }


    /**
     * 连接指定wifi-针对Android10.0系统
     */
    fun wifiConnectByAndroidQ(scanResult: ScanResult) {

        nowSelectSSID = scanResult.SSID
        val strConnectPwd = "BYDB-Box"
//        val strConnectPwd = "fk12345678"
        // Android10.0以上用这个 // 想使用这种方式，需要将api提升到29以上
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            val specifier: NetworkSpecifier = WifiNetworkSpecifier.Builder()
//                .setSsidPattern(PatternMatcher(nowSelectSSID, PatternMatcher.PATTERN_PREFIX))
//                .setWpa2Passphrase(strConnectPwd)
//                .build()
//            val request = NetworkRequest.Builder()
//                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
//                .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
//                .setNetworkSpecifier(specifier)
//                .build()
//            val connectivityManager = mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//            val networkCallback: ConnectivityManager.NetworkCallback = object : ConnectivityManager.NetworkCallback() {
//                override fun onAvailable(network: Network) {
//                    // do success processing here..
//                    Log.e("NetworkCallback", "onAvailable!")
//                    loadingDialog.dismiss()
//                    showToast("Connection Success！")
////                    finish()
//                    val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
//                    val wifiInfo: WifiInfo = wifiManager.getConnectionInfo()
//                    BaseApplication.getInstance().checkSSIDTYPE(wifiInfo.ssid)
//
//
//                }
//
//                override fun onUnavailable() {
//                    Log.e("NetworkCallback", "onUnavailable:")
//                    loadingDialog.dismiss()
//                    showToast("Connection Fail！")
//                }
//            }
//            connectivityManager.requestNetwork(request, networkCallback)
//        }
//        // Android10.0 以下用这个
//        else {
//            val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
//            val configuration: WifiConfiguration = WIFIConnectionTest.configWifiInfo(this, wifiManager,scanResult, strConnectPwd)
//            var netId = configuration.networkId
//            if (netId == -1) {
//                netId = wifiManager.addNetwork(configuration)
//            }
//            val isEnable = wifiManager.enableNetwork(netId, true)
//            Log.e("configWifiInfo", "isEnable:${isEnable}")
//            if (!isEnable) {
//                loadingDialog.dismiss()
//                showToast("connect fail!")
//            }
//        }

        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val configuration: WifiConfiguration = WIFIConnectionTest.configWifiInfo(this, wifiManager,scanResult, strConnectPwd)
        var netId = configuration.networkId
        if (netId == -1) {
            netId = wifiManager.addNetwork(configuration)
        }
        val isEnable = wifiManager.enableNetwork(netId, true)
        Log.e("configWifiInfo", "isEnable:${isEnable}")
        if (!isEnable) {
            loadingDialog.dismiss()
            showToast("connect fail!")
        }
        else {
            loadingDialog.showAndMsg("connecting...")
            mHandler.postDelayed(connectTimeOutRunnable,40*1000)
        }


    }

    private val connectTimeOutRunnable = object : Runnable{
        override fun run() {
            if (loadingDialog.isShowing) {
                loadingDialog.dismiss()
                showToast("connect time out!")
            }
        }

    }


    // wifi 列表的点击事件
    override fun onItemClick(view: View?, position: Int) {
        if (wifiList[position].SSID.equals(BaseApplication.getInstance().strNowSSID))
            return
        startScanWifi(false)
        wifiConnectByAndroidQ(wifiList[position])

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onReceiveMessageInfo(msg: MessageInfo) {
        when(msg.iCode) {
            // 接收数据
            MessageInfo.i_NET_WORK_STATE -> {
                val netWorkType = msg.anyInfo as NetWorkType
                myNetState.updateNetInfo(netWorkType)
                if (netWorkType == NetWorkType.WIFI_DEVICE) {
                    if (BaseApplication.getInstance().strNowSSID.equals(nowSelectSSID) && loadingDialog.isShowing) {
                        mHandler.removeCallbacks(connectTimeOutRunnable)
                        loadingDialog.dismiss()
                        showToast("Connection Success！")
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

    }



}