package com.smart.bms_byd.data

class RunDataInfo {

    var Start_Address = "" // 开始地址
    var Raw_Data = "" // 原始数据

    // 查询 调试信息 、 历史记录
    var BMS_Address = "" // BMS地址
    var Read_State = "" // 读状态
    var Read_MaxLength = -1 // 读的总字节长度
    var Net_Write_Read_Data_Num = 0 // 以太网模块写的读到的数据块数量
    var BMU_Write_Send_Data_Num = 0 // BMU写的发送数据块数量
    var Every_Data_Num = 0 // 每个数据块中的数据字节数
    var More_DATA = "" // 更多的DATA

    // 更新BMU 、BMS软件
    var APP_Version = "" // App版本号
    var Update_State = "" // 更新状态
    var Update_MaxLength = "" // 更新字节总数
    var BMU_Write_Receive_Data_Num = 0 // BMU写的接收数据块数量
    var Net_Write_Send_Data_Num = 0 // 以太网模块写的发送数据块数量

    constructor(strData : String) {
        this.Raw_Data = strData
        initData()
    }

    private fun initData() {


    }


}