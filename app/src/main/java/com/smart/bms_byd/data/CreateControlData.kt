package com.smart.bms_byd.data

import com.smart.bms_byd.util.BaseVolume
import com.smart.bms_byd.util.NetworkUtils

class CreateControlData {

    companion object {

        /**
         * 读取某段寄存器的数据
         * strStartAddress 起始地址
         * iMaxNumber 读取长度
         */
        fun readInfoByAddress(strStartAddress : String,iMaxNumber : Int) : String{
            var strData = "01" // 地址
            strData += BaseVolume.CMD_TYPE_READ_DATA // 功能码
            strData += strStartAddress // 起始地址
            strData += String.format("%04X",iMaxNumber)
            val strCRC = NetworkUtils.bytesToHexString(CRC16.getCrc16(strData))
            strData += strCRC
            return strData.toUpperCase()
        }

        /**
         * 读取某段寄存器的数据
         * strStartAddress 起始地址
         * strEndAddress 结束地址
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

        /**
         * 写单个寄存器
         */
        fun writeOnlyByAddress(strAddress : String,strData0 : String) : String{
            var strData = "01" // 地址
            strData += BaseVolume.CMD_TYPE_WRITE_ONLY // 功能码
            strData += strAddress // 地址 2个hex
            strData += strData0 // 数据 2个hex

            val strCRC = NetworkUtils.bytesToHexString(CRC16.getCrc16(strData))
            strData += strCRC
            return strData.toUpperCase()
        }

        /**
         * 写多个寄存器
         */
        fun writeMoreByAddress(strAddress : String,strData0 : String) : String{
            var strData = "01" // 地址
            strData += BaseVolume.CMD_TYPE_WRITE_MORE // 功能码
            strData += strAddress // 地址 2个hex
            strData += String.format("%04X",strData0.length/4) // 寄存器数量
            strData += String.format("%02X",strData0.length/2) // 字节长度
            strData += strData0 // 数据

            val strCRC = NetworkUtils.bytesToHexString(CRC16.getCrc16(strData))
            strData += strCRC
            return strData.toUpperCase()
        }







    }




}