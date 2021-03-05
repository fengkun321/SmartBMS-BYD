package com.smart.bms_byd.data

import java.io.Serializable

class NotificationMessageInfo : Serializable{

    var strTime = ""
    var strTitle = ""
    var strContent = ""
    var strType = "TXT" // TXT 文本显示 URL 链接显示

    constructor(strTime:String,strTitle:String,strContent:String) {
        this.strTime = strTime
        this.strTitle = strTitle
        this.strContent = strContent
    }

}