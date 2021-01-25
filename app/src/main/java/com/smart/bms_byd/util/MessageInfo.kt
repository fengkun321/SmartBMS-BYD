package com.smartIPandeInfo.data

class MessageInfo {

    public companion object {

        const val i_NET_WORK_STATE = 0 // 网络状态
        const val i_TCP_CONNECT_SUCCESS = 1 // TCP连接成功
        const val i_TCP_CONNECT_FAIL = 2 // TCP连接失败
        const val i_RECEIVE_DATA = 3 // 接收TCP过来的数据 消息体为：{"DATA_TYPE":"03","DATA_VALUE":"112233"}

    }


    var iCode:Int = -1
    var anyInfo:Any? = null

    constructor(iCode:Int,anyInfo:Any) {
        this.iCode = iCode
        this.anyInfo = anyInfo
    }

}