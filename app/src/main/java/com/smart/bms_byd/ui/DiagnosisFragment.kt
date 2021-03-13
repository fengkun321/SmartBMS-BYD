package com.smart.bms_byd.ui

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.smart.bms_byd.BaseApplication
import com.smart.bms_byd.MainActivityTest
import com.smart.bms_byd.R
import com.smart.bms_byd.adapter.DiagnosticListAdapter
import com.smart.bms_byd.data.*
import com.smart.bms_byd.util.BaseVolume
import com.smartIPandeInfo.data.MessageInfo
import kotlinx.android.synthetic.main.fragment_diagnosis.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.SimpleDateFormat
import java.util.*

class DiagnosisFragment : Fragment(),View.OnClickListener{

    var diagnosisList = arrayListOf<DiagnosticMessageInfo>()
    var diagnosisHistoryList = arrayListOf<DiagnosticMessageInfo>()
    lateinit var diagnosticListAdapter : DiagnosticListAdapter
    lateinit var diagnosticHistoryListAdapter : DiagnosticListAdapter
    var iNowReadNumber = -1
    val TAG = "DiagnosisFragment"
    private var mHandler = Handler()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_diagnosis, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        EventBus.getDefault().register(this)
        isCheckData = true
        iNowReadNumber = -1
        diagnosisList.clear()
        readErrorInfo()
    }

    private fun initUI() {

//        diagnosisList.add(DiagnosticMessageInfo("2021-01-29 12:30:25","BMS","xxxx event type xxxx event type xxxx"))
//        diagnosisList.add(DiagnosticMessageInfo("2021-01-29 12:30:25","BMS","xxxx event type xxxx event type xxxx"))
//        diagnosisList.add(DiagnosticMessageInfo("2021-01-29 12:30:25","BMS","xxxx event type xxxx event type xxxx"))
//        diagnosisList.add(DiagnosticMessageInfo("2021-01-29 12:30:25","BMS","xxxx event type xxxx event type xxxx"))
//        diagnosisList.add(DiagnosticMessageInfo("2021-01-29 12:30:25","BMS","xxxx event type xxxx event type xxxx"))
//        diagnosisList.add(DiagnosticMessageInfo("2021-01-29 12:30:25","BMS","xxxx event type xxxx event type xxxx"))
//        diagnosisList.add(DiagnosticMessageInfo("2021-01-29 12:30:25","BMS","xxxx event type xxxx event type xxxx"))
//        diagnosisList.add(DiagnosticMessageInfo("2021-01-29 12:30:25","BMS","xxxx event type xxxx event type xxxx"))
//        diagnosisList.add(DiagnosticMessageInfo("2021-01-29 12:30:25","BMS","xxxx event type xxxx event type xxxx"))

        diagnosticListAdapter = DiagnosticListAdapter(diagnosisList, context)
        recyclerNowError.adapter = diagnosticListAdapter
        recyclerNowError.itemAnimator = DefaultItemAnimator()
        recyclerNowError.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.VERTICAL,
            false
        )

        diagnosticHistoryListAdapter = DiagnosticListAdapter(diagnosisHistoryList, context)
        diagnosticHistoryListAdapter.changeHistory(true)
        recyclerHistoryError.adapter = diagnosticHistoryListAdapter
        recyclerHistoryError.itemAnimator = DefaultItemAnimator()
        recyclerHistoryError.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.VERTICAL,
            false
        )


        tvRealTime.setOnClickListener(this)
        tvHistory.setOnClickListener(this)

    }

    private var isCheckData = false
    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        // 隐藏啦
        if (hidden) {
            isCheckData = false
//            RequeryHistoryData.getInstance().stopQuery()
            mHandler.removeCallbacks(runnbaleReadBMSState)
        }
        // 显示啦
        else {
            isCheckData = true
            iNowReadNumber = -1
            diagnosisList.clear()
            readErrorInfo()
        }

    }

    /** 查询当前bms的状态 */
    val runnbaleReadBMSState = object : Runnable{
        override fun run() {
            // 01 03 05 51 00 01 D5 17
            val strSendData = CreateControlData.readInfoByAddress(
                BaseVolume.CMD_READ_BMS_WORK_READ_STATE_0551,
                1
            )
            BaseApplication.getInstance().StartSendDataByTCPTimeOut(strSendData)
        }

    }


    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.tvRealTime -> {
                if (tvRealTime.tag.toString().toBoolean()) return
                tvRealTime.tag = true
                tvRealTime.setTextColor(resources.getColor(R.color.white))
                tvRealTime.setBackgroundResource(R.drawable.error_red_bg_border)
                tvHistory.tag = false
                tvHistory.setTextColor(resources.getColor(R.color.black))
                tvHistory.setBackgroundResource(0)
                llErrorTitle.visibility = View.GONE

                recyclerNowError.visibility = View.VISIBLE
                recyclerHistoryError.visibility = View.GONE
                isCheckData = true
                iNowReadNumber = -1
                diagnosisList.clear()
                readErrorInfo()

            }
            R.id.tvHistory -> {
                if (tvHistory.tag.toString().toBoolean()) return
                tvRealTime.tag = false
                tvRealTime.setTextColor(resources.getColor(R.color.black))
                tvRealTime.setBackgroundResource(0)
                tvHistory.tag = true
                tvHistory.setTextColor(resources.getColor(R.color.white))
                tvHistory.setBackgroundResource(R.drawable.error_red_bg_border)
                llErrorTitle.visibility = View.VISIBLE

                recyclerNowError.visibility = View.GONE
                recyclerHistoryError.visibility = View.VISIBLE
                val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                var cal = Calendar.getInstance()
                cal[Calendar.DAY_OF_YEAR] = cal[Calendar.DAY_OF_YEAR] - 14
                val strStartTime = simpleDateFormat.format(cal.time)
                val strStopTime = simpleDateFormat.format(Date())
                RequeryHistoryData.getInstance().startQueryHistoryData(
                    queryHistoryHandler, strStartTime, "",
                    DeviceStateInfo.getInstance().getBMSNumberNow() + 1, true)

            }
        }

    }

    /** 读取当前的异常告警 */
    private fun readErrorInfo() {
        MainActivityTest.getInstance().loadingDialog.showAndMsg("waiting...")
        BaseApplication.getInstance().StopSend()
        ++iNowReadNumber
        // 5分钟查询一次的BMU信息
        if (iNowReadNumber == 0) {
            val strSendData = CreateControlData.readInfoByAddress("0500", 25)
            BaseApplication.getInstance().StartSendDataByTCPTimeOut(strSendData)
        }
        // 所有参数已经全部读取完成
        else if (iNowReadNumber > DeviceStateInfo.getInstance().getBMSNumberNow()){
            showToast("全部读取完成！")
            MainActivityTest.getInstance().loadingDialog.dismiss()
        }
        // 读取BMS工作参数
        else {
            Log.e(TAG, "BMS:${iNowReadNumber} 先设置成‘读开始’的状态！")
            // 读对应BMS参数之前，要先设置为开始读：8100
            val strData = "${String.format("%04X", iNowReadNumber)}8100"
            // 01 10 05 50 00 02 04 00 01 81 00 F8 53
            val strSendData = CreateControlData.writeMoreByAddress(
                BaseVolume.CMD_READ_BMS_WORK_0550,
                strData
            )
            BaseApplication.getInstance().StartSendDataByTCPTimeOut(strSendData)

        }

    }

    private fun checkData(analysisInfo: AnalysisInfo) {
        // 读取的返回
        if (analysisInfo.strType.equals(BaseVolume.CMD_TYPE_READ_DATA, ignoreCase = true)) {
            // 5分钟查询一次的BMU信息
            if (analysisInfo.iReadNumber == 25) {
                Log.e(TAG, "BMU 信息读取完毕！")
                val errorList = analysisInfo.analysisBMUErrorInfo(analysisInfo.strReadDataBuffer)
                diagnosisList.addAll(errorList)
                diagnosticListAdapter.notifyDataSetChanged()
                // 继续读取下一个
                readErrorInfo()
            }
            else if (analysisInfo.iReadNumber == 65) {
                Log.e(TAG, "BMS:${iNowReadNumber} 信息读取完毕！")
                val errorList = analysisInfo.analysisBMSErrorInfo(
                    iNowReadNumber,
                    analysisInfo.strReadDataBuffer
                )
                diagnosisList.addAll(errorList)
                diagnosticListAdapter.notifyDataSetChanged()
                // 继续读取下一个
                readErrorInfo()
            }
            else if (analysisInfo.iReadNumber == 1) {
                Log.e(TAG, "BMS:${iNowReadNumber} 当前读取状态：${analysisInfo.strReadDataBuffer}")
                if (analysisInfo.strReadDataBuffer.equals("8801")) {
                    mHandler.removeCallbacks(runnbaleReadBMSState)
                    Log.e(TAG, "BMS:${iNowReadNumber} 开始读取...")
                    // 读信息 27个寄存器，一共54个字节。
                    val strSendData = CreateControlData.readInfoByAddress(
                        BaseVolume.CMD_READ_BMS_WORK_DATA_ADDRESS_0558,
                        65
                    )
                    BaseApplication.getInstance().StartSendDataByTCPTimeOut(strSendData)
                }
                else {
                    Log.e(TAG, "BMS:${iNowReadNumber} 状态还未改变，等待1s后查询BMS的读取状态")
                    mHandler.postDelayed(runnbaleReadBMSState, 1000)
                }

            }
        }
        else if (analysisInfo.strType.equals(BaseVolume.CMD_TYPE_WRITE_MORE, ignoreCase = true)) {
            BaseApplication.getInstance().StopSend()
            // 将指定BMS状态设置成‘开始读’，下一步就开始读啦！
            if (analysisInfo.strWriteMoreAddress.equals(BaseVolume.CMD_READ_BMS_WORK_0550) && analysisInfo.iWriteMoreRegisterNumber == 0x0002) {
                Log.e(TAG, "BMS:${iNowReadNumber} 状态设置成功，等待1s后查询BMS的读取状态")
                mHandler.postDelayed(runnbaleReadBMSState, 1000)
            }
        }

    }

    public fun updateIsCheckData() {
        MainActivityTest.getInstance().loadingDialog.dismiss()
        RequeryHistoryData.getInstance().stopQuery()
        isCheckData = false
        mHandler.removeCallbacks(runnbaleReadBMSState)
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
                    RequeryHistoryData.getInstance().stopQuery()
                    MainActivityTest.getInstance().loadingDialog.dismiss()
                    showToast("${analysisInfo.iErrprCode},${analysisInfo.strErrorInfo}")
                }
                // 只处理实时数据
                if (recyclerNowError.visibility == View.VISIBLE)
                    checkData(analysisInfo)
                // 处理历史数据
                else
                    RequeryHistoryData.getInstance().analyHistory(analysisInfo)

            }
        }

    }

    val queryHistoryHandler = object : Handler(){
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            when(msg?.what) {
                RequeryHistoryData.iQUERY_HISTORY_RUNNING -> {
                    diagnosisHistoryList.clear()
                    diagnosticHistoryListAdapter.notifyDataSetChanged()
                    MainActivityTest.getInstance().loadingDialog.showAndMsg("waiting...")
                }
                RequeryHistoryData.iQUERY_HISTORY_ALARM_ENTITY -> {
                    val list = msg.obj as ArrayList<DiagnosticMessageInfo>
                    diagnosisHistoryList.addAll(list)
                    diagnosticHistoryListAdapter.notifyDataSetChanged()
                }
                RequeryHistoryData.iQUERY_HISTORY_SUCCESS -> {
                    showToast("查询完成！")
                    MainActivityTest.getInstance().loadingDialog.dismiss()
                }
                RequeryHistoryData.iQUERY_HISTORY_FAULT -> {
                    val strMsg = msg.obj as String
                    showToast(strMsg)
                    MainActivityTest.getInstance().loadingDialog.dismiss()
                }
            }

        }
    }

    private fun showToast(strMsg: String) {
        Toast.makeText(BaseApplication.getInstance(), strMsg, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isCheckData = false
        mHandler.removeCallbacks(runnbaleReadBMSState)
        RequeryHistoryData.getInstance().stopQuery()
        EventBus.getDefault().unregister(this)
    }




}