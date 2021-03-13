package com.smart.bms_byd.ui

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.smart.bms_byd.BaseApplication
import com.smart.bms_byd.R
import com.smart.bms_byd.adapter.SystemStatusAdapter
import com.smart.bms_byd.data.*
import com.smart.bms_byd.util.BaseVolume
import com.smartIPandeInfo.data.MessageInfo
import kotlinx.android.synthetic.main.fragment_system.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class SystemFragment : Fragment() {

    private var systemStatusArrayList = arrayListOf<SystemStatusInfo>()
    private lateinit var systemStatusAdapter: SystemStatusAdapter
    private var mHandler = Handler()
    private val TAG = "SystemFragment"
    private var isCheckData = false
    override fun onCreateView(inflater: LayoutInflater,container: ViewGroup?,savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_system, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        EventBus.getDefault().register(this);
    }

    private fun initUI() {

        systemStatusArrayList.add(SystemStatusInfo(0))
        for (iN in 0 until DeviceStateInfo.getInstance().getBMSNumberNow()) {
            systemStatusArrayList.add(SystemStatusInfo(iN+1))
        }
        systemStatusAdapter = SystemStatusAdapter(systemStatusArrayList,context)
        recyclerSystemInfo.layoutManager = LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false)
        recyclerSystemInfo.adapter = systemStatusAdapter
        recyclerSystemInfo.itemAnimator = DefaultItemAnimator()

    }


    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        // 隐藏啦
        if (hidden) {
            isCheckData = false
            mHandler.removeCallbacks(runnableReadWorkInfo)
            mHandler.removeCallbacks(runnbaleReadBMSState)
        }
        // 显示啦
        else {
            isCheckData = true
            queryBMSBMUVerInfo()
//            mHandler.post(runnableReadWorkInfo)
        }

    }

    /** 读取BMU 系统参数寄存器:  0x0000 寄存器个数：102 */
    private fun queryBMSBMUVerInfo() {
        val strSendData = CreateControlData.readInfoByAddress("0000", "0065")
        BaseApplication.getInstance().StartSendDataByTCPTimeOut(strSendData)
    }


    var iNowReadNumber = -1
    /** 开始读取电池的所有信息 */
    val runnableReadWorkInfo = object : Runnable{
        override fun run() {
            readWorkInfo()
        }
    }

    /** 查询当前bms的状态 */
    val runnbaleReadBMSState = object : Runnable{
        override fun run() {
            // 01 03 05 51 00 01 D5 17
            val strSendData = CreateControlData.readInfoByAddress(BaseVolume.CMD_READ_BMS_WORK_READ_STATE_0551,1)
            BaseApplication.getInstance().StartSendDataByTCPTimeOut(strSendData)
        }

    }

    private fun readWorkInfo() {
        BaseApplication.getInstance().StopSend()
        ++iNowReadNumber
        // 读取BMU工作参数
        if (iNowReadNumber == 0) {
            val strSendData = CreateControlData.readInfoByAddress("0500",25)
            BaseApplication.getInstance().StartSendDataByTCPTimeOut(strSendData)
        }
        // 所有参数已经全部读取完成
        else if (iNowReadNumber > DeviceStateInfo.getInstance().getBMSNumberNow()){
            showToast("全部读取完成！")
            // 5分钟查询一次
            iNowReadNumber = -1
            mHandler.postDelayed(runnableReadWorkInfo,1000*60*5)
        }
        // 读取BMS工作参数
        else {
            Log.e(TAG,"BMS:${iNowReadNumber} 先设置成‘读开始’的状态！")
            // 读对应BMS参数之前，要先设置为开始读：8100
            val strData = "${String.format("%04X",iNowReadNumber)}8100"
            // 01 10 05 50 00 02 04 00 01 81 00 F8 53
            val strSendData = CreateControlData.writeMoreByAddress(BaseVolume.CMD_READ_BMS_WORK_0550,strData)
            BaseApplication.getInstance().StartSendDataByTCPTimeOut(strSendData)

        }

    }
    private fun checkData(analysisInfo : AnalysisInfo) {
        // 读取的返回
        if (analysisInfo.strType.equals(BaseVolume.CMD_TYPE_READ_DATA, ignoreCase = true)) {
            // BMU 系统参数信息
            if (analysisInfo.iReadNumber == 102) {
                systemStatusArrayList.clear()
                systemStatusArrayList.add(SystemStatusInfo(0))
                for (iN in 0 until DeviceStateInfo.getInstance().getBMSNumberNow()) {
                    systemStatusArrayList.add(SystemStatusInfo(iN+1))
                }
                systemStatusAdapter.notifyDataSetChanged()
                iNowReadNumber = -1
                mHandler.post(runnableReadWorkInfo)
            }
            // 5分钟查询一次的BMU信息
            else if (analysisInfo.iReadNumber == 25) {
                Log.e(TAG,"更新BMU信息！")
                systemStatusArrayList[0].updateBMUInfo(DeviceStateInfo.getInstance())
                systemStatusAdapter.notifyItemChanged(0)
                // 继续读取下一个
                readWorkInfo()
            }
            else if (analysisInfo.iReadNumber == 65) {
                Log.e(TAG,"更新BMS:${iNowReadNumber} 信息！")
                val systemStatusInfo = analysisInfo.analysisBMSDataByNumber(iNowReadNumber,analysisInfo.strReadDataBuffer)
                systemStatusArrayList[iNowReadNumber].updateBMSInfo(systemStatusInfo)
                systemStatusAdapter.notifyItemChanged(iNowReadNumber)
                // 继续读取下一个
                readWorkInfo()
            }
            else if (analysisInfo.iReadNumber == 1) {
                Log.e(TAG,"BMS:${iNowReadNumber} 当前读取状态：${analysisInfo.strReadDataBuffer}")
                if (analysisInfo.strReadDataBuffer.equals("8801")) {
                    mHandler.removeCallbacks(runnbaleReadBMSState)
                    Log.e(TAG,"BMS:${iNowReadNumber} 开始读取...")
                    // 读信息 27个寄存器，一共54个字节。
                    val strSendData = CreateControlData.readInfoByAddress(BaseVolume.CMD_READ_BMS_WORK_DATA_ADDRESS_0558,65)
                    BaseApplication.getInstance().StartSendDataByTCPTimeOut(strSendData)
                }
                else {
                    Log.e(TAG,"BMS:${iNowReadNumber} 状态还未改变，等待1s后查询BMS的读取状态")
                    mHandler.postDelayed(runnbaleReadBMSState,1000)
                }

            }
        }
        else if (analysisInfo.strType.equals(BaseVolume.CMD_TYPE_WRITE_MORE, ignoreCase = true) ||
            analysisInfo.strType.equals(BaseVolume.CMD_TYPE_WRITE_MORE_ERROR, ignoreCase = true)) {
            BaseApplication.getInstance().StopSend()
            // 将指定BMS状态设置成‘开始读’，下一步就开始读啦！
            if (analysisInfo.strWriteMoreAddress.equals(BaseVolume.CMD_READ_BMS_WORK_0550) && analysisInfo.iWriteMoreRegisterNumber == 0x0002) {
                Log.e(TAG,"BMS:${iNowReadNumber} 状态设置成功，等待1s后查询BMS的读取状态")
                mHandler.postDelayed(runnbaleReadBMSState,1000)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onReceiveMessageInfo(msg: MessageInfo) {
        when(msg.iCode) {
            // 接收数据
            MessageInfo.i_RECEIVE_DATA -> {
//                if (this.isHidden) return
                if (!isCheckData) return
                val analysisInfo = msg.anyInfo as AnalysisInfo
                if (analysisInfo.iErrprCode != 0) {
                    showToast("${analysisInfo.iErrprCode},${analysisInfo.strErrorInfo}")
                    return
                }
                checkData(analysisInfo)
            }

        }

    }

    public fun updateIsCheckData() {
        isCheckData = false
        mHandler.removeCallbacks(runnableReadWorkInfo)
        mHandler.removeCallbacks(runnbaleReadBMSState)
    }


    private fun showToast(strMsg : String) {
        Toast.makeText(BaseApplication.getInstance(),strMsg,Toast.LENGTH_SHORT).show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        mHandler.removeCallbacks(runnableReadWorkInfo)
        mHandler.removeCallbacks(runnbaleReadBMSState)
        EventBus.getDefault().unregister(this)

    }



}