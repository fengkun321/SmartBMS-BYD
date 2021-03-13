package com.smart.bms_byd.data

import android.os.Handler
import android.os.Message
import android.util.Log
import com.smart.bms_byd.BaseApplication
import com.smart.bms_byd.util.BaseVolume
import com.smart.bms_byd.util.DateFormatUtils
import java.util.*

class RequeryHistoryData {

    private val TAG = "RequeryHistoryData"
    companion object {
        private lateinit var requeryHistoryData:RequeryHistoryData
        public val iQUERY_HISTORY_SUCCESS = 111
        public val iQUERY_HISTORY_FAULT = 222
        public val iQUERY_HISTORY_RUNNING = 333
        public val iQUERY_HISTORY_DATA = 444
        public val iQUERY_HISTORY_ALARM_ENTITY = 555

        public fun getInstance():RequeryHistoryData {
            if (!this::requeryHistoryData.isInitialized)
                requeryHistoryData = RequeryHistoryData()
            return requeryHistoryData
        }

    }

    private lateinit var mHandler: Handler
    // 查询区间
    private var strStartDateTime = ""
    private var strStopDateTime = ""
    // 当前最大设备数（BMU + BMS）
    private var iMaxDevNum = -1
    // 当前查询的设备序号（0：BMU,其他为BMS）
    private var iNowDevNum = -1
    // 是否正在查询
    private var isBeQuering = false
    // 是否只处理报警信息
    private var isOnlyAlarm = false
    // 当前查询的次数（每次最多查询128个字节，每次循环最多查600个字节，也就是最多5次，所以需要记录当前查询次数，以便于自动循环下一轮）
    private var iQueryCount = 0
    // 用于盛放没有处理完的不完整的数据，等待拼接
    private var dataBuffer = ""

    /** 开始查询啦！ */
    public fun startQueryHistoryData(mHandler:Handler,strStartDateTime: String,strStopDateTime: String,iMaxDevNum: Int,isOnlyAlarm:Boolean) {
        stopQuery()
        this.mHandler = mHandler
        this.strStartDateTime = strStartDateTime
        this.strStopDateTime = strStopDateTime
        this.iMaxDevNum = iMaxDevNum
        this.isOnlyAlarm = isOnlyAlarm
        iNowDevNum = 0
        isBeQuering = true
        Log.e(TAG,"开始查询历史数据啦！start:$strStartDateTime -- stop:$strStopDateTime")
        setQueryState()
        mHandler.sendEmptyMessage(iQUERY_HISTORY_RUNNING)

    }

    /** 设置为查询状态 */
    private fun setQueryState() {
        // 所有设备已经查完啦！
        if (iNowDevNum == iMaxDevNum) {
            Log.e(TAG,"所有设备都已经查询完毕啦！！！！！")
            stopQuery()
            mHandler.sendEmptyMessage(iQUERY_HISTORY_SUCCESS)
        }
        else {
            dataBuffer = ""
            Log.e(TAG,"BMS:${iNowDevNum} 先设置成‘读开始’的状态！")
            // 读对应BMS参数之前，要先设置为开始读：8100
            val strData = "${String.format("%04X",iNowDevNum)}8100"
            // 01 10 05 A0 00 02 04 00 00 81 00 F8 53
            val strSendData = CreateControlData.writeMoreByAddress(BaseVolume.CMD_READ_DEV_HISTORY_05A0,strData)
            BaseApplication.getInstance().StartSendDataByTCPTimeOut(strSendData)
        }

    }

    /** 处理过来的数据 */
    public fun analyHistory(analysisInfo: AnalysisInfo) {
        if (!isBeQuering) return
        BaseApplication.getInstance().StopSend()
        // 读取的返回
        if (analysisInfo.strType.equals(BaseVolume.CMD_TYPE_READ_DATA, ignoreCase = true)) {
            if (analysisInfo.iReadNumber == 1) {
                Log.e(TAG,"BMS:${iNowDevNum} 当前读取状态：${analysisInfo.strReadDataBuffer}")
                if (analysisInfo.strReadDataBuffer.equals("8801")) {
                    startSureState(false)
                    Log.e(TAG,"BMS:${iNowDevNum} 开始读取...")
                    iQueryCount = 0
                    queryDataByCount()
                }
            }
            // 判断是否为历史数据的包头
            else if (analysisInfo.strAllData.startsWith("0103820080")) {
                disposalData(analysisInfo.strAllData.substring(10,analysisInfo.strAllData.length - 4))
            }
        }
        // 写入的返回
        else if (analysisInfo.strType.equals(BaseVolume.CMD_TYPE_WRITE_MORE, ignoreCase = true)) {
            // 将指定BMS状态设置成‘开始读’，下一步就开始读啦！
            if (analysisInfo.strWriteMoreAddress.equals(BaseVolume.CMD_READ_DEV_HISTORY_05A0) && analysisInfo.iWriteMoreRegisterNumber == 0x0002) {
                Log.e(TAG,"BMS:${iNowDevNum} 状态设置成功，等待1s后查询BMS的读取状态")
                startSureState(true)
            }
        }
        // 写入失败的返回
        else if (analysisInfo.strType.equals(BaseVolume.CMD_TYPE_WRITE_MORE_ERROR, ignoreCase = true)) {

        }

    }

    /** 查询数据 */
    private fun queryDataByCount() {
        ++iQueryCount
        // 读取次数大于5次，则需要重新来一遍
        if (iQueryCount > 5) {
            setQueryState()
        }
        else {
            Log.e(TAG,"BMS:${iNowDevNum} 查询，第 $iQueryCount 次")
            val strSendData = CreateControlData.readInfoByAddress(BaseVolume.CMD_READ_DEV_HISTORY_ADDRESS_05A8,65)
            BaseApplication.getInstance().StartSendDataByTCPTimeOut(strSendData)
        }
    }

    /** 处理数据 */
    private fun disposalData(strData : String) {
        // 拿出有效数据，拼接，
        // 第五包，只有前面88个字节有效
        if (iQueryCount == 5)
            dataBuffer += strData.substring(0,88*2)
        else
            dataBuffer += strData

        var isTimeOut = false
        // 每30个字节为一条历史记录,一共能查20条
        while (dataBuffer.length > 30*2) {
            val strHistoryData = dataBuffer.substring(0,60)
            dataBuffer = dataBuffer.substring(60)
            val strHistoryTime = strHistoryData.substring(2,14)
            // 是否超出时间范围
            isTimeOut = isTimeOutHistory(strHistoryTime)
            if (isTimeOut) {
                dataBuffer = ""
            }
            else {
                // 解析具体历史信息
                historyEntityByData(strHistoryData)
            }

        }
        // 超出时间，则说明该设备查完了，该查下一个设备啦！
        if (isTimeOut) {
            Log.e(TAG,"BMS:${iNowDevNum} 历史数据时间超过范围啦，则开始下一个设备！")
            ++iNowDevNum
            setQueryState()
        }
        else {
            queryDataByCount()
        }


    }

    /** 判断当前数据是否超出时间范围 true:超时啦！ */
    private fun isTimeOutHistory(strItemDate : String) : Boolean {
        var strHistoryTime = "20"+String.format("%02d",strItemDate.substring(0,2).toInt(16))+"-"+
                String.format("%02d",strItemDate.substring(2,4).toInt(16))+"-"+
                String.format("%02d",strItemDate.substring(4,6).toInt(16))+" "+
                String.format("%02d",strItemDate.substring(6,8).toInt(16))+":"+
                String.format("%02d",strItemDate.substring(8,10).toInt(16))+":"+
                String.format("%02d",strItemDate.substring(10,12).toInt(16))
        Log.e(TAG,"该条数据的时间：history time:$strHistoryTime")
        val lStartTimeStamp = DateFormatUtils.str2Long(strStartDateTime,false)
        val lStopTimeStamp = if (!strStopDateTime.equals("")) DateFormatUtils.str2Long(strStopDateTime,false) else Calendar.getInstance().time.time
        val lHistoryTimeStamp = DateFormatUtils.str2Long(strHistoryTime,false)
//        if (lHistoryTimeStamp in lStopTimeStamp .. lStartTimeStamp)
//            return false
//        else
//            return true
        // 历史数据的时间不在起止时间范围内
        return lHistoryTimeStamp !in lStartTimeStamp .. lStopTimeStamp
    }

    /** 将数据转成实体类 */
    private fun historyEntityByData(strData : String) {
        if (isOnlyAlarm) {
            val strType = strData.substring(0,2)
            if (!strType.equals("02")) return // 类型是02，才是故障告警

            val strTime = strData.substring(2,14)
            var strHistoryTime = String.format("%02d",strTime.substring(0,2).toInt(16))+"-"+
                    String.format("%02d",strTime.substring(2,4).toInt(16))+"-"+
                    String.format("%02d",strTime.substring(4,6).toInt(16))+" "+
                    String.format("%02d",strTime.substring(6,8).toInt(16))+":"+
                    String.format("%02d",strTime.substring(8,10).toInt(16))+":"+
                    String.format("%02d",strTime.substring(10,12).toInt(16))
            Log.e(TAG,"处理报警数据：$strHistoryTime")
//            var diagnosticMessageInfoList = arrayListOf<DiagnosticMessageInfo>()
//            // 第一个，是BMU，日志类型区分高、低压
//            if (iNowDevNum == 0) {
//                var diagnosticMessageInfo = DiagnosticMessageInfo("BMU","",strHistoryTime,1,iNowDevNum)
//                // 高压
//                if (DeviceStateInfo.getInstance().isHighVolInfo()) {
//
//
//                }
//                // 低压
//                else {
//
//                }
//            }
//            // 其他序号，是BMS，不区分高、低压
//            else {
//                var diagnosticMessageInfo = DiagnosticMessageInfo("BMS","","",1,iNowDevNum)
//
//            }
//            if (diagnosticMessageInfoList.size > 0) {
//                var msg = Message()
//                msg.what = iQUERY_HISTORY_ALARM_ENTITY
//                msg.obj = diagnosticMessageInfoList
//                mHandler.sendMessage(msg)
//            }

        }
        else {
            var msg = Message()
            msg.what = iQUERY_HISTORY_DATA
            msg.obj = strData
            mHandler.sendMessage(msg)
        }
    }


    private var stateTimerTask = Timer()
    private var iNowSureCount = -1
    private fun startSureState(isSure : Boolean) {
        stateTimerTask.cancel()
        iNowSureCount = -1
        if (isSure) {
            iNowSureCount = 10
            stateTimerTask = Timer()
            stateTimerTask.schedule(object : TimerTask(){
                override fun run() {
                    --iNowSureCount
                    if (iNowSureCount < 0) {
                        Log.e(TAG,"查询超时啦！10秒都没变化状态！")
                        startSureState(false)
                        stopQuery()
                        var msg = Message()
                        msg.what = iQUERY_HISTORY_FAULT
                        msg.obj = "query state fault!"
                        mHandler.sendMessage(msg)
                    }
                    else {
                        // 01 03 05 A1 00 01 D5 17
                        val strSendData = CreateControlData.readInfoByAddress(BaseVolume.CMD_READ_DEV_HISTORY_STATE_05A1,1)
                        BaseApplication.getInstance().StartSendDataByTCP(strSendData)
                    }
                }
            },0,1000) // 间隔1秒，循环查询一次，直到状态改变位置
        }
    }


    public fun stopQuery() {
        isBeQuering = false
        startSureState(false)
    }

}