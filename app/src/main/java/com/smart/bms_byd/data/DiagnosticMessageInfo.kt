package com.smart.bms_byd.data

class DiagnosticMessageInfo {

    var strTime = ""
    var strTitle = ""
    var strContent = ""

    constructor(strTime:String,strTitle:String,strContent:String) {
        this.strTime = strTime
        this.strTitle = strTitle
        this.strContent = strContent
    }

}