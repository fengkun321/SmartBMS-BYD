package com.smart.bms_byd.data

class SystemStatusInfo {

    var iNumber = -1
    var strType = "System"
    var strSOC = "0"
    var strBatteryVoltage = "0"
    var strOutputVoltage = "0"
    var strCurrent = "0"
    var strParrellalConnection = "0"
    var strModulesPerTower = "0"
    var strMaxCellVoltage = "0"
    var strMaxCellVoltageNumber = "0"
    var strMinCellVoltage = "0"
    var strMinCellVoltageNumber = "0"
    var strMaxCellTemperature = "0"
    var strMaxCellTemperatureNumber = "0"
    var strMinCellTemperature = "0"
    var strMinCellTemperatureNumber = "0"

    constructor(iNumber : Int) {
        this.iNumber = iNumber
        if (iNumber == 0)
            this.strType = "System"
        else
            this.strType = "Sub-system $iNumber"
    }


    /** 更新BMU信息 */
    public fun updateBMUInfo(deviceStateInfo: DeviceStateInfo) {
        strSOC = "${deviceStateInfo.Five_BMU_SOC}%"
        if (deviceStateInfo.isHighVolInfo()) {
            // 并联数
            strParrellalConnection = "${deviceStateInfo.BMS_Number_HEX.substring(0,1).toInt(16)}%"
            // 塔内模组数
            strModulesPerTower = "${deviceStateInfo.BMS_Number_HEX.substring(1).toInt(16)}"
        }
        else {
            strParrellalConnection = "0"
            strModulesPerTower = "0"
        }

        strMaxCellVoltage = "${deviceStateInfo.Five_BMU_HighVoltage} mV"
        strMinCellVoltage = "${deviceStateInfo.Five_BMU_LowVoltage} mV"
//        strMaxCellVoltageNumber = "${deviceStateInfo.Five_BMU_SOC}%"
//        strMinCellVoltageNumber = "${deviceStateInfo.Five_BMU_SOC}%"
        strMaxCellTemperature = "${deviceStateInfo.Five_BMU_HighTemper} ℃"
        strMinCellTemperature = "${deviceStateInfo.Five_BMU_LowTemper} ℃"
//        strMaxCellTemperatureNumber = "${deviceStateInfo.Five_BMU_SOC}%"
//        strMinCellTemperatureNumber = "${deviceStateInfo.Five_BMU_SOC}%"
        strBatteryVoltage = "${deviceStateInfo.Five_BMU_SumVoltage} V"
        strOutputVoltage = "${deviceStateInfo.Five_PACK_Voltage} V"
        strCurrent = "${deviceStateInfo.Five_BMU_Ele} A"


    }

    /** 更新BMS信息 */
    public fun updateBMSInfo(systemStatusInfo: SystemStatusInfo) {
        strMaxCellVoltage = systemStatusInfo.strMaxCellVoltage
        strMinCellVoltage = systemStatusInfo.strMinCellVoltage
        strMaxCellVoltageNumber = systemStatusInfo.strMaxCellVoltageNumber
        strMinCellVoltageNumber = systemStatusInfo.strMinCellVoltageNumber
        strMaxCellTemperature = systemStatusInfo.strMaxCellTemperature
        strMinCellTemperature = systemStatusInfo.strMinCellTemperature
        strMaxCellTemperatureNumber = systemStatusInfo.strMaxCellTemperatureNumber
        strMinCellTemperatureNumber = systemStatusInfo.strMinCellTemperatureNumber
        strBatteryVoltage = systemStatusInfo.strBatteryVoltage
        strOutputVoltage = systemStatusInfo.strOutputVoltage
        strCurrent = systemStatusInfo.strCurrent

    }




}