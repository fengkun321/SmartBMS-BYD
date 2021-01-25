package com.smart.bms_byd.data;

import com.smart.bms_byd.util.NetworkUtils;

public class CRC16 {

    /**
     * java CRC16算法 x16+x12+x5+1公式计算
     *
     * C语言函数
     *  * const unsigned short crc_ta[16]={0x0000,0x1021,0x2042,0x3063,0x4084,0x50a5,0x60c6,0x70e7,0x8108,0x9129,
     *  * 0xa14a,0xb16b,0xc18c,0xd1ad,0xe1ce,0xf1ef};
     *  * unsigned short Crc16_DATAs(unsigned char *ptr, unsigned short len)
     *  * {
     *  * 	unsigned char da;
     *  * 	unsigned short  CRC_16_Data = 0;
     *  * 	while(len-- != 0)
     *  *        {
     *  * 		da = CRC_16_Data >> 12;
     *  * 		CRC_16_Data <<= 4;
     *  * 		CRC_16_Data ^= crc_ta[da^(*ptr/16)];
     *  * 		da = CRC_16_Data >> 12;
     *  * 		CRC_16_Data <<= 4;
     *  * 		CRC_16_Data ^= crc_ta[da^(*ptr&0x0f)];
     *  * 		ptr++;
     *  *    }
     *  * 	return  CRC_16_Data;
     *  * }
     * @param data_arr 数据数组
     * @param data_len 数据长度
     * @return
     */
    public static int[] getCrc16(byte[] data_arr, int data_len)
    {
        int crc16 = 0;
        int i;
        for(i =0; i < (data_len); i++)
        {
            crc16 = (char)(( crc16 >> 8) | (crc16 << 8));
            crc16 ^= data_arr[i]& 0xFF;
            crc16 ^= (char)(( crc16 & 0xFF) >> 4);
            crc16 ^= (char)(( crc16 << 8) << 4);
            crc16 ^= (char)((( crc16 & 0xFF) << 4) << 1);
        }
        int [] result=new int[2];
        result[0]= (crc16/256) ;
        result[1]= (crc16%256) ;
        return result;
    }

    /**
     * int数组转byte数组
     * @param i int数组
     * @return 返回byte数组
     */
    public static byte[] intToByte(int[] i){
        byte[] bytes = new byte[2];
        for (int i1 = 0; i1 < i.length; i1++) {
            bytes[i1]= (byte)i[i1];
        }
        return bytes;
    }

    /**
     * crc16算法 返回byte数组
     * @param bytes
     * @return 返回byte数组
     */
    public static byte[] crc16ByteArrey(byte[] bytes){

        return  intToByte(getCrc16(bytes, bytes.length));
    }

    /**
     *  crc16算法  MODBUS 串口算法
     * @param arr_buff
     * @return
     */

    public static byte[] getCrc16(byte[] arr_buff) {
        int len = arr_buff.length;
        int crc = 0xFFFF;
        int i, j;
        for (i = 0; i < len; i++) {
            crc = ((crc & 0xFF00) | (crc & 0x00FF) ^ (arr_buff[i] & 0xFF));
            for (j = 0; j < 8; j++) {
                if ((crc & 0x0001) > 0) {
                    crc = crc >> 1;
                    crc = crc ^ 0xA001;
                } else
                    crc = crc >> 1;
            }
        }
        return intToBytes(crc);
    }

    /**
     *  crc16算法  MODBUS 串口算法
     * @param strHexData
     * @return
     */

    public static byte[] getCrc16(String strHexData) {
        byte[] arr_buff = NetworkUtils.hexStringToBytes(strHexData);
        int len = arr_buff.length;
        int crc = 0xFFFF;
        int i, j;
        for (i = 0; i < len; i++) {
            crc = ((crc & 0xFF00) | (crc & 0x00FF) ^ (arr_buff[i] & 0xFF));
            for (j = 0; j < 8; j++) {
                if ((crc & 0x0001) > 0) {
                    crc = crc >> 1;
                    crc = crc ^ 0xA001;
                } else
                    crc = crc >> 1;
            }
        }
        return intToBytes(crc);
    }

    /**
     * int转byte数组
     * @param value
     * @return
     */
    public static byte[] intToBytes(int value)  {
        byte[] src = new byte[2];
//        src[0] =  (byte) ((value>>8) & 0xFF);
//        src[1] =  (byte) (value & 0xFF);
        // 高低位反转
        src[0] =  (byte) (value & 0xFF);
        src[1] =  (byte) ((value>>8) & 0xFF);
        return src;
    }


}
