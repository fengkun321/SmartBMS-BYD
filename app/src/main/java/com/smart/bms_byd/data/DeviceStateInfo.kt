package com.smart.bms_byd.data

class DeviceStateInfo {

    companion object {
        private lateinit var deviceStateInfo: DeviceStateInfo

        @Synchronized
        fun getInstance() : DeviceStateInfo{
            if (!this::deviceStateInfo.isInitialized)
                deviceStateInfo = DeviceStateInfo()
            return deviceStateInfo
        }

    }

    // BMU 系统参数信息 0x0000-0x0065 102个
    var BCU_SN = "" // BCU_序列号
    var BCU_APP_A_Version = ""
    var BCU_APP_B_Version = ""
    var BMS_Version = ""
    var BCU_BMS_APP_Area = "" // 当前使用的APP区
    var Inverter_Type = "" // 逆变器类型
    var BMS_Number = "" // BMS 电池数量
    var BMS_Type = "" // BMS 类型
    var User_Scene = "" // 应用场景
    var Dan_or_San = "" // 单/三相
    var Inverter_Table = "" // 逆变器参数表
    var BCU_Time = "" // BCU时间标定

    // BMU 阈值表信息 0x0400-0x0449 74个
    var VPT_Table_Number = -1 // 阈值表编号
    var VPT_Table_Version = "" // 阈值表版本号
    var VPT_Update_State = "" // 更新状态
    var VPT_Update_MaxLength = -1 // 更新的总字节长度
    var VPT_BMU_Write_Receive_Num = -1 // BMU写的接收数据块数量
    var Net_Write_Send_Num = -1 // 以太网模块写的发送数据块数量
    var VPT_Every_Data_Num = -1 // 每个数据块中的数据字节数
    var VPT_DATA = "" // 阈值表的DATA

    // BMU 寄存器工作参数
    var BMU_SOC = -1 // SOC
    var BMU_HighVoltage = 0.00f // 电池最高电压
    var BMU_LowVoltage = 0.00f // 电池最低电压
    var BMU_SOH = 0 // SOH
    var BMU_Ele = 0.0f // 电池电流
    var BMU_SumVoltage = 0.00f // 电池总电压
    var BMU_HighTemper = 0 // 电池最高温度
    var BMU_LowTemper = 0 // 电池最低温度
    var BMU_AverageTemper = 0 // 电池平均温度
    var BCU_Version = "" // BCU版本号
    var Now_Alarm_First = "" // 当前一级告警
    var Now_Alarm_Second = "" // 当前二级告警
    var Now_Alarm_Thirdly = "" // 当前三级告警
    var PACK_Voltage = 0.00f // PACK电压
    var All_Energy_in = 0*100
    var All_Energy_out = 0*100




    /** 初始化状态 */
    public fun initState() {


    }

}