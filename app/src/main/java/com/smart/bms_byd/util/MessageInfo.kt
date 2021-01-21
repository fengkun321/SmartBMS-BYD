package com.smartIPandeInfo.data

class MessageInfo {

    public companion object {

        const val i_NET_WORK_STATE = 0 // 网络状态

    }


    var iCode:Int = -1
    var anyInfo:Any? = null

    constructor(iCode:Int,anyInfo:Any) {
        this.iCode = iCode
        this.anyInfo = anyInfo
    }

}