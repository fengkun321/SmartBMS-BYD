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
    var iWriteOnlyAddress = 0
    var strWriteOnlyBuffer = ""

    // 写多个寄存器的数据
    var iWriteMoreAddress = 0
    var iWriteMoreNumber = 0
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
            // 调试信息查询 73个寄存器
            else if (strReadDataBuffer.length == 73*2*2) {

            }
            // 历史记录查询 73个寄存器
            else if (strReadDataBuffer.length == 73*2*2) {

            }
        }
        // 写单个寄存器
        else if (strType.equals(BaseVolume.CMD_TYPE_WRITE_ONLY, ignoreCase = true)) {
            iWriteOnlyAddress = strAllData.substring(4,8).toInt(16)
            strWriteOnlyBuffer = strAllData.substring(8,12)
        }
        // 写多个寄存器
        else if (strType.equals(BaseVolume.CMD_TYPE_WRITE_MORE, ignoreCase = true)) {
            iWriteMoreAddress = strAllData.substring(4,8).toInt(16)
            iWriteMoreNumber = strAllData.substring(8,12).toInt(16)/2
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
        val strInverterType = Integer.parseInt(strDataBuffer.substring(64,66),16)
        val strBMSNumber = Integer.parseInt(strDataBuffer.substring(66,68),16)
        val strBMSType = Integer.parseInt(strDataBuffer.substring(68,70),16)
        val strUser_Scene = strDataBuffer.substring(70,72)
        val strDan_or_San = Integer.parseInt(strDataBuffer.substring(72,74),16)
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
        DeviceStateInfo.getInstance().BCU_APP_B_Version = "$strBCU_APP_B_L.$strBCU_APP_B_R"
        DeviceStateInfo.getInstance().BMS_Version = "$strBMS_Version_L.$strBMS_Version_R"
        DeviceStateInfo.getInstance().BCU_APP_Area = "$strBCU_Qu"
        DeviceStateInfo.getInstance().BMS_APP_Area = "$strBMS_Qu"
        DeviceStateInfo.getInstance().Inverter_Type = "$strInverterType"
        DeviceStateInfo.getInstance().BMS_Number = strBMSNumber
        DeviceStateInfo.getInstance().BMS_Type = strBMSType
        DeviceStateInfo.getInstance().User_Scene = strUser_Scene
        DeviceStateInfo.getInstance().Dan_or_San = strDan_or_San
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
        DeviceStateInfo.getInstance().BMU_SOC = iSOC
        DeviceStateInfo.getInstance().BMU_HighVoltage = fBMU_HighVoltage
        DeviceStateInfo.getInstance().BMU_LowVoltage = fBMU_LowVoltage
        DeviceStateInfo.getInstance().BMU_SOH = iSOH
        DeviceStateInfo.getInstance().BMU_Ele = fBMU_Ele
        DeviceStateInfo.getInstance().BMU_SumVoltage = fBMU_SumVoltage
        DeviceStateInfo.getInstance().BMU_HighTemper = iBMU_HighTemper
        DeviceStateInfo.getInstance().BMU_LowTemper = iBMU_LowTemper
        DeviceStateInfo.getInstance().BMU_AverageTemper = iBMU_AverageTemper

        DeviceStateInfo.getInstance().BCU_Now_Version = "$strBCU_Ver_L.$strBCU_Ver_R"
        if (DeviceStateInfo.getInstance().BCU_APP_Area.equals("0"))
            DeviceStateInfo.getInstance().BCU_APP_A_Version = "$$strBCU_Ver_L.$strBCU_Ver_R"
        else
            DeviceStateInfo.getInstance().BCU_APP_B_Version = "$$strBCU_Ver_L.$strBCU_Ver_R"

        DeviceStateInfo.getInstance().Now_Alarm_First = Now_Alarm_First
        DeviceStateInfo.getInstance().Now_Alarm_Second = Now_Alarm_Second
        DeviceStateInfo.getInstance().Now_Alarm_Thirdly = Now_Alarm_Thirdly

        DeviceStateInfo.getInstance().VPT_Table_Version = "$strInverter_Ver_L.$strInverter_Ver_R"

        DeviceStateInfo.getInstance().BMS_Type = strBMSType
        DeviceStateInfo.getInstance().PACK_Voltage = fPACK_Voltage
        DeviceStateInfo.getInstance().All_Energy_in = All_Energy_in
        DeviceStateInfo.getInstance().All_Energy_out = All_Energy_out
        Log.e(strTag,"数据,analysisDataByReadBMUState:"+DeviceStateInfo.getInstance().toString())

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