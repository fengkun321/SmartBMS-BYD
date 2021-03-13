package com.smart.bms_byd.data

import java.io.Serializable

class FileDataInfo : Serializable {

    var strFileName = ""
    lateinit var fileDataArray : ByteArray
    var strFileType = ""
    var fVersion = 0.0f
    var strArea = ""

    companion object {

        val FILE_TYPE_BMS = 111
        val FILE_TYPE_BMU = 222
        val FILE_TYPE_TAB = 333

    }

    constructor(strFileName : String,fileDataArray : ByteArray) {
        this.strFileName = strFileName
        this.fileDataArray = fileDataArray

        checkFileName()

    }



    private fun checkFileName() {

        // 形如：BMU-P2-1.16-B-A3FD.bin
        if (strFileName.indexOf("BMU") >= 0 || strFileName.indexOf("BMS") >= 0) {
            val strFileNameArray = strFileName.split("-")
            strFileType = strFileNameArray[0]+"-"+strFileNameArray[1]
            fVersion = strFileNameArray[2].toFloat()
            strArea = strFileNameArray[3]
        }
        // 阈值表校验 HVM-TAB-1-7.1.bin
        else if (((strFileName.indexOf("HV") >= 0) && (strFileName.indexOf("TAB") >= 0)) ||
            ((strFileName.indexOf("LV") >= 0) && (strFileName.indexOf("TAB") >= 0))) {
            val strFileNameArray = strFileName.split("-")
            strFileType = strFileNameArray[0]
            strArea = strFileNameArray[2]
            fVersion = strFileNameArray[3].split(".")[0].toFloat()

        }

    }

    public fun getType() : Int {
        // 形如：BMU-P2-1.16-B-A3FD.bin
        if (strFileName.indexOf("BMU-P") >= 0) {
            return FILE_TYPE_BMU
        }
        else if (strFileName.indexOf("BMS-P") >= 0) {
            return FILE_TYPE_BMS
        }

        // 阈值表校验 HVM-TAB-1-7.1.bin
        else if (((strFileName.indexOf("HV") >= 0) && (strFileName.indexOf("TAB") >= 0)) ||
            ((strFileName.indexOf("LV") >= 0) && (strFileName.indexOf("TAB") >= 0))) {
            return FILE_TYPE_TAB
        }
        return -1
    }




}