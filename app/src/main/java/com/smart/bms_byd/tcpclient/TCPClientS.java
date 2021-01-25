package com.smart.bms_byd.tcpclient;

import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.smart.bms_byd.util.NetworkUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class TCPClientS {

    /**
     * single instance TcpClient
     * */
    private static TCPClientS mSocketClient = null;

    private TCPClientS(OnDataReceiveListener onDataReceiveListener){
        this.onDataReceiveListener = onDataReceiveListener;
    }
    public static TCPClientS getInstance(OnDataReceiveListener onDataReceiveListener){
        if(mSocketClient == null){
            synchronized (TCPClientS.class) {
                mSocketClient = new TCPClientS(onDataReceiveListener);
            }
        }
        return mSocketClient;
    }

    // 连接已断开
    public static final int TCP_CONNECT_STATE_DISCONNECT = -1;
    // 连接成功
    public static final int TCP_CONNECT_STATE_CONNECTED = 1;
    // 连接中
    public static final int TCP_CONNECT_STATE_CONNECTTING = 0;
    private int iConnectionState = TCP_CONNECT_STATE_DISCONNECT;

    public int getConnectionState() {
        return iConnectionState;
    }


    String TAG_log = "Socket";
    private Socket mSocket;

    private OutputStream mOutputStream;
    private InputStream mInputStream;

    private SocketThread mSocketThread;
    private boolean isStop = false;//thread flag


    /**
     * 128 - 数据按照最长接收，一次性
     * */
    private class SocketThread extends Thread {

        private String ip;
        private int port;
        public SocketThread(String ip, int port){
            this.ip = ip;
            this.port = port;
        }

        @Override
        public void run() {
            Log.e(TAG_log,"SocketThread start ");
            super.run();

            //connect ...
            try {
                if (mSocket != null) {
                    mSocket.close();
                    mSocket = null;
                }

                InetAddress ipAddress = InetAddress.getByName(ip);
                mSocket = new Socket(ipAddress, port);

                //设置不延时发送
                //mSocket.setTcpNoDelay(true);
                //设置输入输出缓冲流大小
                //mSocket.setSendBufferSize(8*1024);
                //mSocket.setReceiveBufferSize(8*1024);

                if(isConnect()){
                    mOutputStream = mSocket.getOutputStream();
                    mInputStream = mSocket.getInputStream();

                    isStop = false;

                    uiHandler.sendEmptyMessage(1);
                }
                /* 此处这样做没什么意义不大，真正的socket未连接还是靠心跳发送，等待服务端回应比较好，一段时间内未回应，则socket未连接成功 */
                else{
                    Message msg = new Message();
                    msg.what = -1;
                    Bundle bundle = new Bundle();
                    bundle.putString("error","连接异常");
                    msg.setData(bundle);
                    uiHandler.sendMessage(msg);
                    Log.e(TAG_log,"SocketThread connect fail");
                    return;
                }

            }
            catch (IOException e) {
                Message msg = new Message();
                msg.what = -1;
                Bundle bundle = new Bundle();
                bundle.putString("error",e.getMessage());
                msg.setData(bundle);
                uiHandler.sendMessage(msg);
                Log.e(TAG_log,"SocketThread connect io exception = "+e.getMessage());
                e.printStackTrace();
                return;
            }
            Log.d(TAG_log,"SocketThread connect over ");

            //read ...
            while (isConnect() && !isStop && !isInterrupted()) {

                int size;
                try {
                    byte[] buffer = new byte[1024];
                    if (mInputStream == null) return;
                    size = mInputStream.read(buffer);//null data -1 , zrd serial rule size default 10
                    if (size > 0) {
                        Message msg = new Message();
                        msg.what = 100;
                        Bundle bundle = new Bundle();
                        bundle.putByteArray("data",buffer);
                        bundle.putInt("size",size);
                        msg.setData(bundle);
                        uiHandler.sendMessage(msg);
                    }
                    Log.i(TAG_log, "SocketThread read listening");

                }
                catch (IOException e) {
                    Message msg = new Message();
                    msg.what = -1;
                    Bundle bundle = new Bundle();
                    bundle.putString("error",e.getMessage());
                    msg.setData(bundle);
                    uiHandler.sendMessage(msg);
                    Log.e(TAG_log,"SocketThread read io exception = "+e.getMessage());
                    e.printStackTrace();
                    return;
                }
            }
        }
    }



    //==============================socket connect============================
    /**
     * connect socket in thread
     * Exception : android.os.NetworkOnMainThreadException
     * */
    public void connect(String ip, int port){
        iConnectionState = TCP_CONNECT_STATE_CONNECTTING;
        mSocketThread = new SocketThread(ip, port);
        mSocketThread.start();
    }

    /**
     * socket is connect
     * */
    private boolean isConnect(){
        boolean flag = false;
        if (mSocket != null) {
            flag = mSocket.isConnected();
        }
        return flag;
    }

    /**
     * socket disconnect
     * */
    public void disconnect() {
        isStop = true;
        iConnectionState = TCP_CONNECT_STATE_DISCONNECT;
        try {
            if (mOutputStream != null) {
                mOutputStream.close();
            }

            if (mInputStream != null) {
                mInputStream.close();
            }

            if (mSocket != null) {
                mSocket.close();
                mSocket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (mSocketThread != null) {
            mSocketThread.interrupt();//not intime destory thread,so need a flag
        }
    }


    /**
     * 发送16进制数据
     * @param strHexCmd
     */
    public void sendHexCmd(final String strHexCmd) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mOutputStream != null) {
                        byte[] mBuffer = NetworkUtils.hexStringToBytes(strHexCmd);
                        mOutputStream.write(mBuffer);
                        mOutputStream.flush();
                        if (onDataReceiveListener != null) {
                            onDataReceiveListener.onDataResultInfo(true,"");
                        }
                    }
                    else {
                        onDataReceiveListener.onDataResultInfo(false,"发送异常");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    if (onDataReceiveListener != null) {
                        onDataReceiveListener.onDataResultInfo(false,e.getMessage());
                    }
                }
            }
        }).start();

    }

    /**
     * send byte[] cmd
     * Exception : android.os.NetworkOnMainThreadException
     * */
    public void sendByteCmd(final byte[] mBuffer) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mOutputStream != null) {
                        mOutputStream.write(mBuffer);
                        mOutputStream.flush();
                        if (onDataReceiveListener != null) {
                            onDataReceiveListener.onDataResultInfo(true,"");
                        }
                    }
                    else {
                        onDataReceiveListener.onDataResultInfo(false,"发送异常");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    if (onDataReceiveListener != null) {
                        onDataReceiveListener.onDataResultInfo(false,e.getMessage());
                    }
                }
            }
        }).start();

    }


    Handler uiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                //connect error
                case -1:
                    iConnectionState = TCP_CONNECT_STATE_DISCONNECT;
                    Bundle bundle = msg.getData();
                    String strMsg = bundle.getString("error");
                    if (null != onDataReceiveListener) {
                        onDataReceiveListener.onConnectFail(strMsg);
                        disconnect();
                    }
                    break;

                //connect success
                case 1:
                    iConnectionState = TCP_CONNECT_STATE_CONNECTED;
                    if (null != onDataReceiveListener) {
                        onDataReceiveListener.onConnectSuccess();
                    }
                    break;

                //receive data
                case 100:
                    Bundle bundle0 = msg.getData();
                    byte[] buffer = bundle0.getByteArray("data");
                    int size = bundle0.getInt("size");
                    byte[] data = new byte[size];
                    System.arraycopy(buffer, 0, data, 0, size);
                    if (null != onDataReceiveListener) {
                        onDataReceiveListener.onDataReceive(data);
                    }
                    break;
            }
        }
    };




    /**
     * socket response data listener
     * */
    private OnDataReceiveListener onDataReceiveListener = null;

    public interface OnDataReceiveListener {
        public void onConnectSuccess();
        public void onConnectFail(String strFailMsg);
        public void onDataReceive(byte[] receiveData);
        public void onDataResultInfo(boolean isOK,String strErrorInfo);
    }
    public void setOnDataReceiveListener(OnDataReceiveListener dataReceiveListener) {
        onDataReceiveListener = dataReceiveListener;
    }

}
