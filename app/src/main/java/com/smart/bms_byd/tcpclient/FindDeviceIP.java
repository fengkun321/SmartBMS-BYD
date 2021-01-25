package com.smart.bms_byd.tcpclient;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * UDP�?域网设备IP
 * @author Administrator
 *
 */
public class FindDeviceIP {

	private Context con;
	private final int DEFAULT_PORT = 988;
	private DatagramSocket udpSocket;
	private String strUUID = "HLK-M50 AliIot";
	public static final int FindDeivceIPReuslt = 123321;

	public void sendUdpCommand(final Context context, final Handler handler) {
		 new Thread(new Runnable() {
		        @Override
		        public void run() {  
		        	DatagramPacket dataPacket = null;
		    		String udpIP = getUdpServiceIP(context);
					if (udpIP == null)
						udpIP = "255.255.255.255";
		    		try {
		    			
		    			if(udpSocket == null)
		    				udpSocket = new DatagramSocket();
		    			
		    			InetAddress broadcastAddr;
		    			broadcastAddr = InetAddress.getByName(udpIP);

		    			String order = "hlk_dd";
						Log.e("UDP搜索", "UDP发送数据："+order+"+++"+udpIP);
		    			byte[] data = order.getBytes("utf8");
		    			dataPacket = new DatagramPacket(data, data.length, broadcastAddr,
		    					DEFAULT_PORT);
		    			udpSocket.send(dataPacket);

		    			byte[] dataReceive = new byte[256];
		    			DatagramPacket packetReceive = new DatagramPacket(dataReceive,
		    					dataReceive.length);
		    			udpSocket.setSoTimeout(1000*2);
		    			udpSocket.receive(packetReceive);
		    			String udpresult = new String(packetReceive.getData(),
		    					packetReceive.getOffset(), packetReceive.getLength());
		    			String ip = packetReceive.getAddress().getHostAddress();
		    			Log.e("UDP搜索", "UDP返回数据："+udpresult+"+++"+ip);
						if(udpresult.indexOf(strUUID) < 0)
							return;
						String strName = getDeivceNameByUDPData(udpresult);

						Message msg = new Message();
						msg.what = FindDeivceIPReuslt;
						Bundle bundle = new Bundle();
						bundle.putString("IP", ip);
						bundle.putString("DeviceName", strName);
						msg.setData( bundle);
						handler.sendMessage(msg);
		    			
		    		} catch (IOException e) {
		    			e.printStackTrace();
		    		}
		        }  
		    }).start();  
		
	
	}

	/**
	 * 根据返回的数据，拿到DeviceName信息
	 * @param strUDPData
	 * @return
	 */
	private String getDeivceNameByUDPData(String strUDPData) {
		String strName = "";
		// 形如：HLK-M50 AliIot(V3.4 19120910)(40:d6:3c:20:08:b7)(DN:gicisky02_fk)
		String strNInfo = strUDPData.substring(strUDPData.lastIndexOf("DN:"));
		strName = strNInfo.substring(3,strNInfo.indexOf(")"));

		return strName;
	}

	private String getUdpServiceIP(Context context) {
		String udpServiceIP = "";
		String ip = getIP(context);
		if (ip == null)
			return ip;
		if (ip != null) {
			String[] strarray = ip.split("\\.");

			for (int i = 0; i < strarray.length - 1; i++) {
				udpServiceIP += strarray[i] + ".";
			}
			udpServiceIP += "255";
		}
		return udpServiceIP;
	}

	private String getIP(Context context) {
		@SuppressWarnings("static-access")
        WifiManager wifiService = (WifiManager) context
				.getSystemService(context.WIFI_SERVICE);
		WifiInfo wifiinfo = wifiService.getConnectionInfo();
		int wifiAd = wifiinfo.getIpAddress();
		if (wifiAd == 0)
			return null;
		return intToIp(wifiAd);
	}

	private String intToIp(int i) {
		return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF)
				+ "." + (i >> 24 & 0xFF);
	}
}
