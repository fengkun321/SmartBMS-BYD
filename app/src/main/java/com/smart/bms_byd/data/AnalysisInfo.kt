package com.smart.bms_byd.data

import android.util.Log
import com.smart.bms_byd.util.BaseVolume
import com.smart.bms_byd.util.NetworkUtils
import java.io.Serializable

class AnalysisInfo : Serializable {

    // 数据类型
    var strType = ""
    // 完整数据
    var strAllData = ""

    // 读寄存器的相关数据
    var iReadNumber = 0
    var strReadDataBuffer = ""

    // 写单个寄存器的数据
    var strWriteOnlyAddress = ""
    var strWriteOnlyBuffer = ""

    // 写多个寄存器的数据
    var strWriteMoreAddress = ""
    var iWriteMoreRegisterNumber = 0
//    var strWriteMoreBuffer = ""

    // 错误描述
    var iErrprCode = 0
    var strErrorInfo = ""

    val strTag = "AnalysisInfo"



    constructor(strType: String, strAllData: String) {
        this.strType = strType
        this.strAllData = strAllData
        initData()
    }

    private fun initData() {
        // 读的回复
        if (strType.equals(BaseVolume.CMD_TYPE_READ_DATA, ignoreCase = true)) {
            iReadNumber = strAllData.substring(4,6).toInt(16)/2
            strReadDataBuffer = strAllData.substring(6,strAllData.length - 4)
            // BMU 系统参数信息查询(102个寄存器，1个寄存器占2个字节，1个字节的hex占字符串2个长度)
            if (strReadDataBuffer.length == 102*2*2)
                analysisDataByReadBMUSystem(strReadDataBuffer)
            // BMU状态 25个寄存器
            else if (strReadDataBuffer.length == 25*2*2) {
                analysisDataByReadBMUState(strReadDataBuffer)
            }
            // 调试信息查询 BMS信息
//            else if (strReadDataBuffer.length == 65*2*2) {
//                analysisBMSDataByNumber(strReadDataBuffer)
//            }
        }
        // 写单个寄存器
        else if (strType.equals(BaseVolume.CMD_TYPE_WRITE_ONLY, ignoreCase = true)) {
            strWriteOnlyAddress = strAllData.substring(4,8)
            strWriteOnlyBuffer = strAllData.substring(8,12)
        }
        // 写多个寄存器
        else if (strType.equals(BaseVolume.CMD_TYPE_WRITE_MORE, ignoreCase = true)) {
            strWriteMoreAddress = strAllData.substring(4,8)
            iWriteMoreRegisterNumber = strAllData.substring(8,12).toInt(16)
        }
        // 异常
        else if (strType.equals(BaseVolume.CMD_TYPE_READ_DATA_ERROR, ignoreCase = true)
            || strType.equals(BaseVolume.CMD_TYPE_WRITE_ONLY_ERROR, ignoreCase = true)
            || strType.equals(BaseVolume.CMD_TYPE_WRITE_MORE_ERROR, ignoreCase = true)) {
            iErrprCode = strAllData.substring(4,6).toInt(16)
            checkError()
        }

    }

    /** 解析读的BMU系统参数 */
    private fun analysisDataByReadBMUSystem(strDataBuffer : String) {
        val strBCU_SN = String(NetworkUtils.hexStringToBytes(strDataBuffer.substring(0,48)))
        val strBCU_APP_A_L = Integer.parseInt(strDataBuffer.substring(48,50),16)
        val strBCU_APP_A_R = Integer.parseInt(strDataBuffer.substring(50,52),16)
        val strBCU_APP_B_L = Integer.parseInt(strDataBuffer.substring(52,54),16)
        val strBCU_APP_B_R = Integer.parseInt(strDataBuffer.substring(54,56),16)
        val strBMS_Version_L = Integer.parseInt(strDataBuffer.substring(56,58),16)
        val strBMS_Version_R = Integer.parseInt(strDataBuffer.substring(58,60),16)
        val strBCU_Qu = Integer.parseInt(strDataBuffer.substring(60,62),16)
        val strBMS_Qu = Integer.parseInt(strDataBuffer.substring(62,64),16)
        val iInverterType = Integer.parseInt(strDataBuffer.substring(64,66),16)
        val strBMSNumberHEX = strDataBuffer.substring(66,68)
        val iBMSType = Integer.parseInt(strDataBuffer.substring(68,70),16)
        val iUser_Scene = Integer.parseInt(strDataBuffer.substring(70,72),16)
        val iDan_or_San = Integer.parseInt(strDataBuffer.substring(72,74),16)
        val strNull = Integer.parseInt(strDataBuffer.substring(74,76),16)
        val strInverterTable = strDataBuffer.substring(76,388)
        val strBMUState = strDataBuffer.substring(388,392)
        val strFaultCode = strDataBuffer.substring(392,396)
        val strTimeY = Integer.parseInt(strDataBuffer.substring(396,398),16)
        val strTimeM = String.format("%02d",Integer.parseInt(strDataBuffer.substring(398,400),16))
        val strTimeD = String.format("%02d",Integer.parseInt(strDataBuffer.substring(400,402),16))
        val strTimeH = String.format("%02d",Integer.parseInt(strDataBuffer.substring(402,404),16))
        val strTimeMin = String.format("%02d",Integer.parseInt(strDataBuffer.substring(404,406),16))
        val strTimeSec = String.format("%02d",Integer.parseInt(strDataBuffer.substring(406,408),16))


        DeviceStateInfo.getInstance().BCU_SN = strBCU_SN
        DeviceStateInfo.getInstance().BCU_APP_A_Version = "$strBCU_APP_A_L.$strBCU_APP_A_R"
        DeviceStateInfo.getInstance().BCU_APP_A_Version_HEX = strDataBuffer.substring(48,50)+strDataBuffer.substring(50,52)
        DeviceStateInfo.getInstance().BCU_APP_B_Version = "$strBCU_APP_B_L.$strBCU_APP_B_R"
        DeviceStateInfo.getInstance().BCU_APP_B_Version_HEX = strDataBuffer.substring(52,54)+strDataBuffer.substring(54,56)
        DeviceStateInfo.getInstance().BMS_Version = "$strBMS_Version_L.$strBMS_Version_R"
        DeviceStateInfo.getInstance().BMS_Version_HEX = strDataBuffer.substring(56,58)+strDataBuffer.substring(58,60)
        DeviceStateInfo.getInstance().BCU_APP_Area = "$strBCU_Qu"
        DeviceStateInfo.getInstance().BMS_APP_Area = "$strBMS_Qu"
        DeviceStateInfo.getInstance().Inverter_Type = iInverterType
        DeviceStateInfo.getInstance().BMS_Number_HEX = strBMSNumberHEX
        DeviceStateInfo.getInstance().BMS_Type = iBMSType
        DeviceStateInfo.getInstance().User_Scene = iUser_Scene
        DeviceStateInfo.getInstance().Dan_or_San = iDan_or_San
        DeviceStateInfo.getInstance().Inverter_Table = strInverterTable
        DeviceStateInfo.getInstance().BMU_State = strBMUState
        DeviceStateInfo.getInstance().FaultCode = strFaultCode
        DeviceStateInfo.getInstance().BCU_Time = "$strTimeY-$strTimeM-$strTimeD $strTimeH:$strTimeMin:$strTimeSec"

        if (DeviceStateInfo.getInstance().BCU_APP_Area.equals("0"))
            DeviceStateInfo.getInstance().BCU_Now_Version = DeviceStateInfo.getInstance().BCU_APP_A_Version
        else
            DeviceStateInfo.getInstance().BCU_Now_Version = DeviceStateInfo.getInstance().BCU_APP_B_Version
        // 更新BMU状态
        DeviceStateInfo.getInstance().updateBMUState()
        Log.e(strTag,"数据,analysisDataByReadBMUSystem:"+DeviceStateInfo.getInstance().toString())


    }

    /** 解析读到的BMU状态 */
    private fun analysisDataByReadBMUState(strDataBuffer : String) {
        val iSOC = Integer.parseInt(strDataBuffer.substring(0,4),16)/100
        val fBMU_HighVoltage = Integer.parseInt(strDataBuffer.substring(4,8),16)/100.0f
        val fBMU_LowVoltage = Integer.parseInt(strDataBuffer.substring(8,12),16)/100.0f
        val iSOH = Integer.parseInt(strDataBuffer.substring(12,16),16)/100
        val fBMU_Ele = Integer.parseInt(strDataBuffer.substring(16,20),16)/10.0f
        val fBMU_SumVoltage = Integer.parseInt(strDataBuffer.substring(20,24),16)/100.0f
        val iBMU_HighTemper = Integer.parseInt(strDataBuffer.substring(24,28),16)
        val iBMU_LowTemper = Integer.parseInt(strDataBuffer.substring(28,32),16)
        val iBMU_AverageTemper = Integer.parseInt(strDataBuffer.substring(32,36),16)

        val strBCU_Ver_Null = Integer.parseInt(strDataBuffer.substring(36,40),16)
        val strBCU_Ver_L = Integer.parseInt(strDataBuffer.substring(40,42),16)
        val strBCU_Ver_R = Integer.parseInt(strDataBuffer.substring(42,44),16)

        val Now_Alarm_First = strDataBuffer.substring(44,48)
        val Now_Alarm_Second = strDataBuffer.substring(48,52)
        val Now_Alarm_Thirdly = strDataBuffer.substring(52,56)

        val strInverter_Ver_L = Integer.parseInt(strDataBuffer.substring(56,58),16)
        val strInverter_Ver_R = Integer.parseInt(strDataBuffer.substring(58,60),16)

        val strBMSType = Integer.parseInt(strDataBuffer.substring(60,64),16)
        val fPACK_Voltage = Integer.parseInt(strDataBuffer.substring(64,68),16)/100.0f
//        val All_Energy_in = Integer.parseInt(strDataBuffer.substring(68,84),16)
//        val All_Energy_out = Integer.parseLong(strDataBuffer.substring(84,100),10);
        val All_Energy_in = strDataBuffer.substring(68,84).toLong(16)
        val All_Energy_out = strDataBuffer.substring(84,100).toLong(16)
        DeviceStateInfo.getInstance().Five_BMU_SOC = iSOC
        DeviceStateInfo.getInstance().Five_BMU_HighVoltage = fBMU_HighVoltage
        DeviceStateInfo.getInstance().Five_BMU_LowVoltage = fBMU_LowVoltage
        DeviceStateInfo.getInstance().Five_BMU_SOH = iSOH
        DeviceStateInfo.getInstance().Five_BMU_Ele = fBMU_Ele
        DeviceStateInfo.getInstance().Five_BMU_SumVoltage = fBMU_SumVoltage
        DeviceStateInfo.getInstance().Five_BMU_HighTemper = iBMU_HighTemper
        DeviceStateInfo.getInstance().Five_BMU_LowTemper = iBMU_LowTemper
        DeviceStateInfo.getInstance().Five_BMU_AverageTemper = iBMU_AverageTemper

        DeviceStateInfo.getInstance().Five_BCU_Now_Version = "$strBCU_Ver_L.$strBCU_Ver_R"
        DeviceStateInfo.getInstance().Five_BCU_Now_Version_HEX = strDataBuffer.substring(40,42)+strDataBuffer.substring(42,44)

        DeviceStateInfo.getInstance().Five_Now_Alarm_First = Now_Alarm_First
        DeviceStateInfo.getInstance().Five_Now_Alarm_Second = Now_Alarm_Second
        DeviceStateInfo.getInstance().Five_Now_Alarm_Thirdly = Now_Alarm_Thirdly

        DeviceStateInfo.getInstance().Five_Table_Version = "$strInverter_Ver_L.$strInverter_Ver_R"
        DeviceStateInfo.getInstance().Five_Table_Version_HEX = strDataBuffer.substring(56,58)+strDataBuffer.substring(58,60)

        DeviceStateInfo.getInstance().Five_BMS_Type = strBMSType
        DeviceStateInfo.getInstance().Five_PACK_Voltage = fPACK_Voltage
        DeviceStateInfo.getInstance().Five_All_Energy_in = All_Energy_in
        DeviceStateInfo.getInstance().Five_All_Energy_out = All_Energy_out
        Log.e(strTag,"数据,analysisDataByReadBMUState:"+DeviceStateInfo.getInstance().toString())

    }

    /** 解析某个BMS工作信息 */
    public fun analysisBMSDataByNumber(iNumber : Int,strDataBuffer : String) : SystemStatusInfo{

        // BMS 工作信息
        var systemStatusInfo = SystemStatusInfo(iNumber)

        val iEveryByteNum = strDataBuffer.substring(0,4).toInt(16)
        val iMaxVol = strDataBuffer.substring(4,8).toInt(16)
        val iMinVol = strDataBuffer.substring(8,12).toInt(16)
        val iMaxVolNumber = strDataBuffer.substring(12,14).toInt(16)
        val iMinVolNumber = strDataBuffer.substring(14,16).toInt(16)
        val iMaxTemp = strDataBuffer.substring(16,20).toInt(16)
        val iMinTemp = strDataBuffer.substring(20,24).toInt(16)
        val iMaxTempNumber = strDataBuffer.substring(24,26).toInt(16)
        val iMinTempNumber = strDataBuffer.substring(26,30).toInt(16)
        val iSumVol = strDataBuffer.substring(84,88).toInt(16)
        val iPackVol = strDataBuffer.substring(96,100).toInt(16)
        val iCurrent = strDataBuffer.substring(108,112).toInt(16)

        systemStatusInfo.strMaxCellVoltage = "$iMaxVol mV"
        systemStatusInfo.strMinCellVoltage = "$iMinVol mV"
        systemStatusInfo.strMaxCellVoltageNumber = "$iMaxVolNumber"
        systemStatusInfo.strMinCellVoltageNumber = "$iMinVolNumber"
        systemStatusInfo.strMaxCellTemperature = "$iMaxTemp ℃"
        systemStatusInfo.strMinCellTemperature = "$iMinTemp ℃"
        systemStatusInfo.strMaxCellTemperatureNumber = "$iMaxTempNumber"
        systemStatusInfo.strMinCellTemperatureNumber = "$iMinTempNumber"
        systemStatusInfo.strBatteryVoltage = "${String.format("%.1f",iSumVol/10.0f)} V"
        systemStatusInfo.strOutputVoltage = "${String.format("%.1f",iPackVol/10.0f)} V"
        systemStatusInfo.strCurrent = "${String.format("%.1f",iCurrent/10.0f)} A"

        return systemStatusInfo
    }

    /** 解析BMU的异常信息 */
    public fun analysisBMUErrorInfo(strDataBuffer : String) : List<DiagnosticMessageInfo> {
        var listAll = arrayListOf<DiagnosticMessageInfo>()
        var firstList = arrayListOf<DiagnosticMessageInfo>()
        var secondList = arrayListOf<DiagnosticMessageInfo>()
        var thirdlyList = arrayListOf<DiagnosticMessageInfo>()
        val Now_Alarm_First_Binary = NetworkUtils.hexToBinary(strDataBuffer.substring(44,48))
        val Now_Alarm_Second_Binary = NetworkUtils.hexToBinary(strDataBuffer.substring(48,52))
        val Now_Alarm_Thirdly_Binary = NetworkUtils.hexToBinary(strDataBuffer.substring(52,56))
        val iFirstAlarmLength = Now_Alarm_First_Binary.length
        val iSecondAlarmLength = Now_Alarm_Second_Binary.length
        val iThirdlyAlarmLength = Now_Alarm_Thirdly_Binary.length
//        for (iN in 0 until iFirstAlarmLength) {
//            val strErrorInfo = getErrorInfoByBit(Now_Alarm_First_Binary.substring(iFirstAlarmLength - iN - 1,iFirstAlarmLength - iN),iN)
//            if (!strErrorInfo.equals("")) {
//                firstList.add(DiagnosticMessageInfo("BMU",strErrorInfo,"",1))
//            }
//        }
//        for (iN in 0 until iSecondAlarmLength) {
//            val strErrorInfo = getErrorInfoByBit(Now_Alarm_Second_Binary.substring(iSecondAlarmLength - iN - 1,iSecondAlarmLength - iN),iN)
//            if (!strErrorInfo.equals("")) {
//                secondList.add(DiagnosticMessageInfo("BMU",strErrorInfo,"",2))
//            }
//        }
        for (iN in 0 until iThirdlyAlarmLength) {
            val strErrorInfo = getErrorInfoByBit(Now_Alarm_Thirdly_Binary.substring(iThirdlyAlarmLength - iN - 1,iThirdlyAlarmLength - iN),iN)
            if (!strErrorInfo.equals("")) {
                thirdlyList.add(DiagnosticMessageInfo("BMU",strErrorInfo,"",3))
            }
        }
        listAll.addAll(firstList)
        listAll.addAll(secondList)
        listAll.addAll(thirdlyList)
        return listAll
    }

    /** 解析BMS的异常信息 */
    public fun analysisBMSErrorInfo(iBMSNumber: Int,strDataBuffer : String) : List<DiagnosticMessageInfo> {

        var listAll = arrayListOf<DiagnosticMessageInfo>()

        var firstList = arrayListOf<DiagnosticMessageInfo>()
        var secondList = arrayListOf<DiagnosticMessageInfo>()
        var thirdlyList = arrayListOf<DiagnosticMessageInfo>()
        val Now_Alarm_First_Binary = NetworkUtils.hexToBinary(strDataBuffer.substring(112,116))
        val Now_Alarm_Second_Binary = NetworkUtils.hexToBinary(strDataBuffer.substring(116,120))
        val Now_Alarm_Thirdly_Binary = NetworkUtils.hexToBinary(strDataBuffer.substring(120,124))
        val iFirstAlarmLength = Now_Alarm_First_Binary.length
        val iSecondAlarmLength = Now_Alarm_Second_Binary.length
        val iThirdlyAlarmLength = Now_Alarm_Thirdly_Binary.length
//        for (iN in 0 until iFirstAlarmLength) {
//            val strErrorInfo = getErrorInfoByBit(Now_Alarm_First_Binary.substring(iFirstAlarmLength - iN - 1,iFirstAlarmLength - iN),iN)
//            if (!strErrorInfo.equals("")) {
//                firstList.add(DiagnosticMessageInfo("BMS",strErrorInfo,"",1,iBMSNumber))
//            }
//        }
//        for (iN in 0 until iSecondAlarmLength) {
//            val strErrorInfo = getErrorInfoByBit(Now_Alarm_Second_Binary.substring(iSecondAlarmLength - iN - 1,iSecondAlarmLength - iN),iN)
//            if (!strErrorInfo.equals("")) {
//                secondList.add(DiagnosticMessageInfo("BMS",strErrorInfo,"",2,iBMSNumber))
//            }
//        }
        for (iN in 0 until iThirdlyAlarmLength) {
            val strErrorInfo = getErrorInfoByBit(Now_Alarm_Thirdly_Binary.substring(iThirdlyAlarmLength - iN - 1,iThirdlyAlarmLength - iN),iN)
            if (!strErrorInfo.equals("")) {
                thirdlyList.add(DiagnosticMessageInfo("BMS",strErrorInfo,"",3,iBMSNumber))
            }
        }
        listAll.addAll(firstList)
        listAll.addAll(secondList)
        listAll.addAll(thirdlyList)
        return listAll
    }

    /** 根据bit序号获取对应的异常信息 */
    private fun getErrorInfoByBit(strBit : String,iPosition : Int) : String{
        var strError = ""
        if (strBit.equals("0")) return strError
        when(iPosition) {
            0 -> strError = "总压过压"
            1 -> strError = "总压欠压"
            2 -> strError = "单体过压"
            3 -> strError = "单体欠压"
            4 -> strError = "电压传感器故障"
            5 -> strError = "温度传感器故障"
            6 -> strError = "单体放电温度高"
            7 -> strError = "单体放电温度低"
            8 -> strError = "单体充电温度高"
            9 -> strError = "单体充电温度低"
            10 -> strError = "电池充电过流"
            11 -> strError = "电池充电过流"
            12 -> strError = "主回路故障"
            13 -> strError = "短路告警"
            14 -> strError = "电池不均衡(压差)"
            15 -> strError = "电流传感器故障"

        }

        return strError
    }




    private fun checkError() {
        when(iErrprCode) {
            0x01 -> strErrorInfo = "非法的功能代码"
            0x02 -> strErrorInfo = "错误的寄存器地址"
            0x03 -> strErrorInfo = "错误的寄存器数量"
            0x04 -> strErrorInfo = "处理过程错误"
        }
    }



}