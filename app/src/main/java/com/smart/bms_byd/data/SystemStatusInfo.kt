package com.smart.bms_byd.data

class SystemStatusInfo {

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

    constructor(strBatteryVoltage : String) {
        this.strBatteryVoltage = strBatteryVoltage

    }




}