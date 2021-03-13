package com.smart.bms_byd.data

class DiagnosticMessageInfo {

    // BMU 或 BMS
    var strType = ""
    var strTime = ""
    var strContent = ""
    var iLevel = -1
    var iBMSNumber = -1


    constructor(strType:String,strContent:String,strTime:String,iLevel:Int) {
        this.strType = strType
        this.strContent = strContent
        this.strTime = strTime
        this.iLevel = iLevel

    }

    constructor(strType:String,strContent:String,strTime:String,iLevel:Int,iBMSNumber:Int) {
        this.strType = strType
        this.strContent = strContent
        this.strTime = strTime
        this.iLevel = iLevel
        this.iBMSNumber = iBMSNumber

    }







}