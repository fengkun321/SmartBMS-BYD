package com.smart.bms_byd.data

import com.smart.bms_byd.util.BaseVolume
import com.smart.bms_byd.util.NetworkUtils

class CreateControlData {

    companion object {

        /**
         * 读取某段寄存器的数据
         */
        fun readInfoByAddress(strStartAddress : String,strEndAddress : String) : String{
            var strData = "01" // 地址
            strData += BaseVolume.CMD_TYPE_READ_DATA // 功能码
            strData += strStartAddress // 起始地址
            val iMaxNumber = Integer.parseInt(strEndAddress,16) - Integer.parseInt(strStartAddress,16) + 1
            strData += String.format("%04X",iMaxNumber)
            val strCRC = NetworkUtils.bytesToHexString(CRC16.getCrc16(strData))
            strData += strCRC
            return strData.toUpperCase()
        }



    }




}