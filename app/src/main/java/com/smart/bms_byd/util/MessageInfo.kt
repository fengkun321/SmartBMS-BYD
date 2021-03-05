package com.smartIPandeInfo.data

class MessageInfo {

    public companion object {

        const val i_NET_WORK_STATE = 0 // 网络状态
        const val i_TCP_CONNECT_SUCCESS = 1 // TCP连接成功
        const val i_TCP_CONNECT_FAIL = 2 // TCP连接失败
        const val i_RECEIVE_DATA = 3 // 接收TCP过来的数据
        const val i_ERROR_INFO = 4 // 设备错误码
        const val i_MESSAGE_INFO = 5 // 信息栏
        const val i_SEND_DATA_ERROR = 6 // 数据发送失败

    }


    var iCode:Int = -1
    var anyInfo:Any? = null

    constructor(iCode:Int,anyInfo:Any) {
        this.iCode = iCode
        this.anyInfo = anyInfo
    }

}