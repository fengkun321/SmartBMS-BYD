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
    var BCU_SN = "P02" // BCU_序列号开头为 P01/P02:低压； P03:高压； 其他需要用户自己选择
//    var BCU_IS_HightVol = false // 是否为高压
    var BCU_APP_A_Version = ""
    var BCU_APP_A_Version_HEX = ""
    var BCU_APP_B_Version = ""
    var BCU_APP_B_Version_HEX = ""
    var BCU_Now_Version = ""
    var BMS_Version = ""
    var BMS_Version_HEX = ""
    var BCU_APP_Area = "" // 当前使用的APP区
    var BMS_APP_Area = "" // 当前使用的APP区
    var Inverter_Type = 0 // 逆变器类型
    var BMS_Number = 0 // BMS 电池数量
    var BMS_Type = 0 // BMS 类型
    var User_Scene = 0 // 应用场景
    var Dan_or_San = 0 // 单/三相
    var Inverter_Table = "" // 逆变器参数表
    var BMU_State = "" // BMU状态
    var BMU_State_List = arrayListOf<String>() // BMU状态集合
    var FaultCode = "" // 故障码
    var BCU_Time = "" // BCU时间标定

    // BMU 阈值表信息 0x0400-0x0449 74个
    var VPT_Table_Number = -1 // 阈值表编号
    var VPT_Table_Version = "" // 阈值表版本号
    var VPT_Table_Version_HEX = "" // 阈值表版本号
    var VPT_Update_State = "" // 更新状态
    var VPT_Update_MaxLength = -1 // 更新的总字节长度
    var VPT_BMU_Write_Receive_Num = -1 // BMU写的接收数据块数量
    var Net_Write_Send_Num = -1 // 以太网模块写的发送数据块数量
    var VPT_Every_Data_Num = -1 // 每个数据块中的数据字节数
    var VPT_DATA = "" // 阈值表的DATA

    // BMU 寄存器工作参数
    var Five_BMU_SOC = 0 // SOC  1%
    var Five_BMU_HighVoltage = 0.00f // 电池最高电压 0.01 V
    var Five_BMU_LowVoltage = 0.00f // 电池最低电压 0.01 V
    var Five_BMU_SOH = 0 // SOH 1%
    var Five_BMU_Ele = 0.0f // 电池电流 0.1 A
    var Five_BMU_SumVoltage = 0.00f // 电池总电压 0.01 V
    var Five_BMU_HighTemper = 0 // 电池最高温度 ℃
    var Five_BMU_LowTemper = 0 // 电池最低温度 ℃
    var Five_BMU_AverageTemper = 0 // 电池平均温度 ℃
    var Five_BCU_Now_Version = "" // BCU版本号
    var Five_BCU_Now_Version_HEX = "" // BCU版本号
    var Five_Now_Alarm_First = "" // 当前一级告警
    var Five_Now_Alarm_Second = "" // 当前二级告警
    var Five_Now_Alarm_Thirdly = "" // 当前三级告警
    var Five_Table_Version = "" // 阈值表版本号
    var Five_Table_Version_HEX = "" // 阈值表版本号
    var Five_BMS_Type = 0 // BMS 类型
    var Five_PACK_Voltage = 0.00f // PACK电压 0.01 V
    var Five_All_Energy_in = 0*100L //输入能耗 100wh
    var Five_All_Energy_out = 0*100L //输出能耗 100wh

    // 低压类型
    val lowSystemArray = arrayListOf("LVL","LV Flex","LVS")
    // 高压类型
    val highSystemArray = arrayListOf("HVL","HVM","HVS")
    // 网络模式
    val networkArray = arrayListOf("Off Grid","On Grid","Back Up")
    // 单三相
    val phaseArray = arrayListOf("Single Phase","Three Phase")
    // 逆变器类型
    val inverterAllArray = arrayListOf("Fronius","GOODWE","GOODWE","KOSTAL","Selectronic","SMA SBS 3.7-6.0"
        ,"SMA","Victron","SUNTECH","Sungrow","Kaco","Studer","SolarEdge","Ingeteam","Sungrow","Schneider","SMA SBS 2.5")

    public fun getInverterTypeInfo() : String{
        return inverterAllArray[Inverter_Type]
    }

    public fun getNetWorkInfo() : String{
        return networkArray[User_Scene]
    }

    public fun getDanSanInfo() : String{
        return phaseArray[Dan_or_San]
    }

    public fun getBMUTypeInfo() : String {
        var systemArray = arrayListOf<String>()
        // 低压
        if ((BCU_SN.indexOf("P02") == 0) || (BCU_SN.indexOf("P01") == 0))
            return lowSystemArray[BMS_Type]
        // 高压
        else if (BCU_SN.indexOf("P03") == 0)
            return highSystemArray[BMS_Type]

        return ""
    }

    /** 更新BMU State */
    public fun updateBMUState() {
        BMU_State_List.clear()
        // 十六进制转10进制，再转2进制
        val iState = Integer.parseInt(BMU_State,16)
        var strBinaryData = Integer.toBinaryString(iState)
        val strMaxLength = strBinaryData.length
        for (iN in 0 until strMaxLength) {
            val strB = strBinaryData.substring(strMaxLength-1-iN,strMaxLength-iN)
            if (strB.equals("1")) {
                when(iN) {
                    0 -> BMU_State_List.add("地址注册失败")
                    1 -> BMU_State_List.add("阈值表加载失败")
                    2 -> BMU_State_List.add("预充电失败")
                    3 -> BMU_State_List.add("BMU固件更新失败")
                    4 -> BMU_State_List.add("BMS固件更新失败")
                    5 -> BMU_State_List.add("BMS通讯失败")
                    6 -> BMU_State_List.add("逆变器通讯失败")
                    7 -> BMU_State_List.add("BMS告警/故障")
                    8 -> BMU_State_List.add("BMU正在升级")
                    9 -> BMU_State_List.add("BMS正在升级")
                    10 -> BMU_State_List.add("电量低(P2)/空开异常(P3)")
                    11 -> BMU_State_List.add("配置加载失败(P2)/单体电压告警(P3)")
                    12 -> BMU_State_List.add("类型不匹配(P2)/温度告警(P3)")
                    13 -> BMU_State_List.add("阈值表错误(P2)/传感器故障(P3)")
                    14 -> BMU_State_List.add("Pack电压告警(P3)")
                    15 -> BMU_State_List.add("电流告警(P3)")
                }
            }
        }

    }

    override fun toString(): String {
        return "DeviceStateInfo(" + "\n" +
                "BCU_SN='$BCU_SN', " + "\n" +
                "BCU_APP_A_Version='$BCU_APP_A_Version', " + "\n" +
                "BCU_APP_A_Version_HEX='$BCU_APP_A_Version_HEX', " + "\n" +
                "BCU_APP_B_Version='$BCU_APP_B_Version', " + "\n" +
                "BCU_APP_B_Version_HEX='$BCU_APP_B_Version_HEX', " + "\n" +
                "BCU_Now_Version='$BCU_Now_Version', " + "\n" +
                "BMS_Version='$BMS_Version', " + "\n" +
                "BMS_Version_HEX='$BMS_Version_HEX', " + "\n" +
                "BCU_APP_Area='$BCU_APP_Area', " + "\n" +
                "BMS_APP_Area='$BMS_APP_Area', " + "\n" +
                "Inverter_Type='$Inverter_Type', " + "\n" +
                "BMS_Number=$BMS_Number, " + "\n" +
                "BMS_Type=$BMS_Type, " + "\n" +
                "User_Scene='$User_Scene', " + "\n" +
                "Dan_or_San=$Dan_or_San, " + "\n" +
                "Inverter_Table='$Inverter_Table', " + "\n" +
                "BMU_State='$BMU_State', " + "\n" +
                "BMU_State_List=$BMU_State_List, " + "\n" +
                "FaultCode='$FaultCode', " + "\n" +
                "BCU_Time='$BCU_Time', " + "\n" +
                "VPT_Table_Number=$VPT_Table_Number, " + "\n" +
                "VPT_Table_Version='$VPT_Table_Version', " + "\n" +
                "VPT_Table_Version_HEX='$VPT_Table_Version_HEX', " + "\n" +
                "VPT_Update_State='$VPT_Update_State', " + "\n" +
                "VPT_Update_MaxLength=$VPT_Update_MaxLength, " + "\n" +
                "VPT_BMU_Write_Receive_Num=$VPT_BMU_Write_Receive_Num, " + "\n" +
                "Net_Write_Send_Num=$Net_Write_Send_Num, " + "\n" +
                "VPT_Every_Data_Num=$VPT_Every_Data_Num, " + "\n" +
                "VPT_DATA='$VPT_DATA', " + "\n" +
                "Five_BMU_SOC=$Five_BMU_SOC, " + "\n" +
                "Five_BMU_HighVoltage=$Five_BMU_HighVoltage, " + "\n" +
                "Five_BMU_LowVoltage=$Five_BMU_LowVoltage, " + "\n" +
                "Five_BMU_SOH=$Five_BMU_SOH, " + "\n" +
                "Five_BMU_Ele=$Five_BMU_Ele, " + "\n" +
                "Five_BMU_SumVoltage=$Five_BMU_SumVoltage, " + "\n" +
                "Five_BMU_HighTemper=$Five_BMU_HighTemper, " + "\n" +
                "Five_BMU_LowTemper=$Five_BMU_LowTemper, " + "\n" +
                "Five_BMU_AverageTemper=$Five_BMU_AverageTemper, " + "\n" +
                "Five_BCU_Now_Version='$Five_BCU_Now_Version', " + "\n" +
                "Five_BCU_Now_Version_HEX='$Five_BCU_Now_Version_HEX', " + "\n" +
                "Five_Now_Alarm_First='$Five_Now_Alarm_First', " + "\n" +
                "Five_Now_Alarm_Second='$Five_Now_Alarm_Second', " + "\n" +
                "Five_Now_Alarm_Thirdly='$Five_Now_Alarm_Thirdly', " + "\n" +
                "Five_Table_Version='$Five_Table_Version', " + "\n" +
                "Five_Table_Version_HEX='$Five_Table_Version_HEX', " + "\n" +
                "Five_BMS_Type=$Five_BMS_Type, " + "\n" +
                "Five_PACK_Voltage=$Five_PACK_Voltage, " + "\n" +
                "Five_All_Energy_in=$Five_All_Energy_in, " + "\n" +
                "Five_All_Energy_out=$Five_All_Energy_out)"
    }


}