package com.ds.IrDA;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileNotFoundException;



import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
	
/**
 * 蓝牙通讯线程
 * @author GuoDong
 *
 */
public class BluetoothCommunThread extends Thread {

	private Handler serviceHandler;		//与Service通信的Handler
	private BluetoothSocket socket;
	private InputStream inStream;		//对象输入流
	private OutputStream outStream;	//对象输出流
	public volatile boolean isRun = true;	//运行标志位
	private byte[]nec_start={(byte)9000,(byte)(9000>>8),(byte)(4500),(byte)(4500>>8)};
	private byte[]command={(byte)0xAA,0x02,(byte)132,0,38};
	private int []nec_addr={0,0,0,0,1,1,1,0};
	private int []nec_vup ={0,1,0,0,0,1,0,0};
	private int []nec_vdn ={0,1,0,0,0,0,1,1};
	private int []nec_mute={0,0,0,0,1,1,1,0};
	private int []nec_next={0,0,0,1,0,1,1,0};
	private int []nec_play={0,0,0,0,1,1,0,1};
	private int []nec_prev={0,0,0,1,0,1,1,1};
	private int []cli1={0,0,0,0,0,0,0,1};
	private int []cli2={0,0,0,0,0,0,1,0};
	private int []cli3={0,0,0,0,0,0,1,1};
	private int []cli4={0,0,0,0,0,1,0,0};
	private int []cli5={0,0,0,0,0,1,0,1};
	private int []cli6={0,0,0,0,0,1,1,0};
	private int []cli7={0,0,0,0,0,1,1,1};
	private int []cli8={0,0,0,0,1,0,0,0};
	private int []cli9={0,0,0,0,1,0,0,1};
	public byte[]buffer1=new byte[128];
	private int obj;
	
	/**
	 * 构造函数
	 * @param handler 用于接收消息
	 * @param socket
	 */
	public BluetoothCommunThread(Handler handler, BluetoothSocket socket) {
		this.serviceHandler = handler;
		this.socket = socket;
		try {
			this.outStream = socket.getOutputStream();
			this.inStream = socket.getInputStream();
		} catch (Exception e) {
			try {
				socket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			//发送连接失败消息
			serviceHandler.obtainMessage(BluetoothTools.MESSAGE_CONNECT_ERROR).sendToTarget();
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		while (true) {
			byte[] buffer = new byte[1024];
			int bytes;
			if (!isRun) {
				break;
			}
			try {
				bytes =inStream.read(buffer);
				//发送成功读取到对象的消息，消息的obj参数为读取到的对象
				Message msg = serviceHandler.obtainMessage(2,bytes,-1,buffer);
				msg.what = BluetoothTools.MESSAGE_READ_OBJECT;
				msg.sendToTarget();
			} catch (Exception ex) {
				//发送连接失败消息
				serviceHandler.obtainMessage(BluetoothTools.MESSAGE_CONNECT_ERROR).sendToTarget();
				ex.printStackTrace();
				return;
			}
		}
		
		//关闭流
		if (inStream != null) {
			try {
				inStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (outStream != null) {
			try {
				outStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 写入一个可序列化的对象
	 * @param obj
	 */
	public void converse(int[]data)
	{
		for(int j=0;j<8;j++)
		{
			if(nec_addr[j]==1)
			{
			 buffer1[4*j]=(byte)560;
			buffer1[4*j+1]=(byte)(560>>8);
			buffer1[4*j+2]=(byte)1690;
			buffer1[4*j+3]=(byte)(1690>>8);
			buffer1[32+4*j]=(byte)560;
			buffer1[32+4*j+1]=(byte)(560>>8);
			buffer1[32+4*j+2]=(byte)560;
			buffer1[32+4*j+3]=(byte)(560>>8);
			}
			else if(nec_addr[j]==0)
			{
				 buffer1[4*j]=(byte)560;
					buffer1[4*j+1]=(byte)(560>>8);
					buffer1[4*j+2]=(byte)560;
					buffer1[4*j+3]=(byte)(560>>8);
					buffer1[32+4*j]=(byte)560;
					buffer1[32+4*j+1]=(byte)(560>>8);
					buffer1[32+4*j+2]=(byte)1690;
					buffer1[32+4*j+3]=(byte)(1690>>8);
			}
		}
		for(int i=0;i<8;i++)
		{
			if(data[i]==1)
			{
				 buffer1[64+4*i]=(byte)560;
				 buffer1[64+4*i+1]=(byte)(560>>8);
				 buffer1[64+4*i+2]=(byte)1690;
				 buffer1[64+4*i+3]=(byte)(1690>>8);
				 buffer1[96+4*i]=(byte)560;
				 buffer1[96+4*i+1]=(byte)(560>>8);
				 buffer1[96+4*i+2]=(byte)560;
				 buffer1[96+4*i+3]=(byte)(560>>8);
		    }
			else if(data[i]==0)
			{
				 buffer1[64+4*i]=(byte)560;
				 buffer1[64+4*i+1]=(byte)(560>>8);
				 buffer1[64+4*i+2]=(byte)560;
				 buffer1[64+4*i+3]=(byte)(560>>8);
				 buffer1[96+4*i]=(byte)560;
				 buffer1[96+4*i+1]=(byte)(560>>8);
				 buffer1[96+4*i+2]=(byte)1690;
				 buffer1[96+4*i+3]=(byte)(1690>>8);
			}
		}
	}
	public void write(byte[]buffer_get) {
        obj=ClientActivity.obj;
        int sort=ClientActivity.sort;
        byte[]buffer3=buffer_get;
        if(ClientActivity.flag==3)
        {
        	byte[] buffer2={(byte)0xAA,(byte)0x04};
        	try {
				outStream.write(buffer2);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        else{
        	if(sort==0)
        	{
		if(obj==0)
		{
			
			converse(nec_vup);
		}
		else if(obj==1)
		{
			converse(nec_vdn);
		}
		else if(obj==2)
		{
			converse(nec_mute);
		}
		else if(obj==3)
		{
			converse(nec_next);
		}
		else if(obj==4)
		{
			converse(nec_prev);
		}
		else if(obj==5)
		{
			converse(nec_play);
		}
		else if(obj==6)
		{
			converse(cli1);
		}
		else if(obj==7)
		{
			converse(cli2);
		}
		else if(obj==8)
		{
			converse(cli3);
		}
		else if(obj==9)
		{
			converse(cli4);
		}
		else if(obj==10)
		{
			converse(cli5);
		}
		else if(obj==11)
		{
			converse(cli6);
		}
		else if(obj==12)
		{
			converse(cli7);
		}
		else if(obj==13)
		{
			converse(cli8);
		}
		else if(obj==14)
		{
			converse(cli9);
		}
        try {
        	outStream.write(command);
        	try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	outStream.write(nec_start);
        	try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            outStream.write(buffer1);
          //  ClientActivity.flag=1;
        } catch (IOException e) {
            ;
        }
	}
		else
		{
			try {
				outStream.write(buffer3);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		}
	}
}
