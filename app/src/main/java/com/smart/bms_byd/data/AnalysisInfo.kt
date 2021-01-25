package com.smart.bms_byd.data

import com.smart.bms_byd.util.BaseVolume
import java.io.Serializable

class AnalysisInfo : Serializable {

    // 数据类型
    var strType = ""
    // 完整数据
    var strAllData = ""

    // 读寄存器的相关数据
    var iReadNumber = 0
    var strReadDataBuffer = ""

    // 写单个寄存器的数据
    var iWriteOnlyAddress = 0
    var strWriteOnlyBuffer = ""

    // 写多个寄存器的数据
    var iWriteMoreAddress = 0
    var iWriteMoreNumber = 0
//    var strWriteMoreBuffer = ""

    // 错误描述
    var iErrprCode = 0
    var strErrorInfo = ""

    constructor(strType: String, strAllData: String) {
        this.strType = strType
        this.strAllData = strAllData
        initData()
    }

    private fun initData() {
        // 读的回复
        if (strType.equals(BaseVolume.CMD_TYPE_READ_DATA, ignoreCase = true)) {
            iReadNumber = strAllData.substring(4,6).toInt(16)/2
            strReadDataBuffer = strAllData.substring(6,strAllData.length - 4)
        }
        // 写单个寄存器
        else if (strType.equals(BaseVolume.CMD_TYPE_WRITE_ONLY, ignoreCase = true)) {
            iWriteOnlyAddress = strAllData.substring(4,8).toInt(16)
            strWriteOnlyBuffer = strAllData.substring(8,12)
        }
        // 写多个寄存器
        else if (strType.equals(BaseVolume.CMD_TYPE_WRITE_MORE, ignoreCase = true)) {
            iWriteMoreAddress = strAllData.substring(4,8).toInt(16)
            iWriteMoreNumber = strAllData.substring(8,12).toInt(16)/2
        }
        // 异常
        else if (strType.equals(BaseVolume.CMD_TYPE_READ_DATA_ERROR, ignoreCase = true)
            || strType.equals(BaseVolume.CMD_TYPE_WRITE_ONLY_ERROR, ignoreCase = true)
            || strType.equals(BaseVolume.CMD_TYPE_WRITE_MORE_ERROR, ignoreCase = true)) {
            iErrprCode = strAllData.substring(4,6).toInt(16)
            checkError()
        }

    }

    private fun checkError() {
        when(iErrprCode) {
            0x01 -> strErrorInfo = "非法的功能代码"
            0x02 -> strErrorInfo = "错误的寄存器地址"
            0x03 -> strErrorInfo = "错误的寄存器数量"
            0x04 -> strErrorInfo = "处理过程错误"
        }
    }



}