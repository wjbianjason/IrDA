package com.ds.IrDA;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

/**
 * 蓝牙模块客户端主控制Service
 * @author GuoDong
 *
 */
public class BluetoothClientService extends Service {
	
	//搜索到的远程设备集合
	private List<BluetoothDevice> discoveredDevices = new ArrayList<BluetoothDevice>();
		//蓝牙适配器
	private final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	//蓝牙通讯线程
	private BluetoothCommunThread communThread;
	//控制信息广播的接收器
	private BroadcastReceiver controlReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			
			if (BluetoothTools.ACTION_START_DISCOVERY.equals(action)) {
				//开始搜索
				discoveredDevices.clear();	//清空存放设备的集合
				bluetoothAdapter.enable();	//打开蓝牙
				bluetoothAdapter.startDiscovery();	//开始搜索
				
			} else if (BluetoothTools.ACTION_SELECTED_DEVICE.equals(action)) {
				//选择了连接的服务器设备
				BluetoothDevice device = (BluetoothDevice)intent.getExtras().get(BluetoothTools.DEVICE);
				
				//开启设备连接线程
				new BluetoothClientConnThread(handler, device).start();
				
			} else if (BluetoothTools.ACTION_STOP_SERVICE.equals(action)) {
				//停止后台服务
				if (communThread != null) {
					communThread.isRun = false;
				}
				stopSelf();
				
			}			
			else if (BluetoothTools.ACTION_DATA_TO_SERVICE.equals(action)) {
				//获取数据
				if (communThread != null) {
					if(ClientActivity.sort==0)
					{	byte[] nothing={1};
						communThread.write(nothing);
					}
					else
					{	int obj=ClientActivity.obj;
				        int []out_num=ClientActivity.out_num;
				        int []out_order=ClientActivity.out_order;
				        byte[]buffer_in=new byte[out_num[obj]];
				        String	filename_in="study_model"+ClientActivity.sort+".txt";
				        try {
							FileInputStream in=openFileInput(filename_in);
							try {
								in.skip(out_order[obj]);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							try {
								in.read(buffer_in, 0, out_num[obj]);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							try {
								in.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				        communThread.write(buffer_in);
					}
				}
				
			}
		}
	};
	
	//蓝牙搜索广播的接收器
	private BroadcastReceiver discoveryReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			//获取广播的Action
			String action = intent.getAction();

			if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
				//开始搜索
			} else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				//发现远程蓝牙设备
				//获取设备
				BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);//将EXTRA_DEVICE理解为变量
				discoveredDevices.add(bluetoothDevice);

				//发送发现设备广播
				Intent deviceListIntent = new Intent(BluetoothTools.ACTION_FOUND_DEVICE);
				deviceListIntent.putExtra(BluetoothTools.DEVICE, bluetoothDevice);
				sendBroadcast(deviceListIntent);
				
			} 
		}
	};
	
	//接收其他线程消息的Handler
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			//处理消息
			switch (msg.what) {
			case BluetoothTools.MESSAGE_CONNECT_ERROR:
				//连接错误
				//发送连接错误广播
				Intent errorIntent = new Intent(BluetoothTools.ACTION_CONNECT_ERROR);
				sendBroadcast(errorIntent);
				break;
			case BluetoothTools.MESSAGE_CONNECT_SUCCESS:
				//连接成功
				
				//开启通讯线程
				communThread = new BluetoothCommunThread(handler, (BluetoothSocket)msg.obj);
				communThread.start();
				
				//发送连接成功广播
				Intent succIntent = new Intent(BluetoothTools.ACTION_CONNECT_SUCCESS);
				sendBroadcast(succIntent);
				break;
			case BluetoothTools.MESSAGE_READ_OBJECT:
				//读取到对象
				//发送数据广播（包含数据对象）
				
				Intent dataIntent = new Intent(BluetoothTools.ACTION_DATA_TO_GAME);
                byte[] readBuf = (byte[]) msg.obj;//读取得到的比特流
                int a=msg.arg1;//读取比特数
                String readmessage="";//测试用的显示变量
                int temp;
                for(int i=0;i<a;i++)
                {
                	if(readBuf[i]<0)
                	{
                		temp=256+readBuf[i];
                	}
                	else
                	temp=readBuf[i];
                	readmessage=readmessage+String.valueOf(temp)+" ";
                }
                //判定是否进入学习状态
                if(ClientActivity.flag==3)
                {
                if((readBuf[0]==(byte)0xAA)&(readBuf[1]==(byte)0x08))
                {	
                	int obj=ClientActivity.obj;//获得按键类型值
                	ClientActivity.study_num[obj]=a;//存储长度信息
                	ClientActivity.study_order[obj]=ClientActivity.b;//存储位置信息
                	ClientActivity.b +=a;
                	byte[]study_get =new byte[a];
                    System.arraycopy(readBuf,0,study_get,0,a);
                    study_get[1]=0x02;
            		String filename=ClientActivity.filename;
            		try {
    					FileOutputStream outstream=openFileOutput(filename,MODE_APPEND);//打开文件流
    					try {
    						outstream.write(study_get);
    					} catch (IOException e) {
    						// TODO Auto-generated catch block
    						e.printStackTrace();
    					}
    					try {
    						outstream.close();
    					} catch (IOException e) {
    						// TODO Auto-generated catch block
    						e.printStackTrace();
    					}
    				} catch (FileNotFoundException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				} 
    				Intent study_protol = new Intent(BluetoothTools.ACTION_STUDY_PROTOL);
    				//sendDataIntent.putExtra(BluetoothTools.DATA, message);
    				sendBroadcast(study_protol);
                }
                }
                if(ClientActivity.flag==1)
                {
                if((readBuf[0]==(byte)0xAA)&(readBuf[1]==(byte)0xF5))
                {
                	ClientActivity.flag=0;
                }
                }
               // String readMessage = new String(readBuf, 0, msg.arg1);
                ClientActivity.message= readmessage;
				sendBroadcast(dataIntent);
				break;
			}
			super.handleMessage(msg);
		}
		
	};
	
	/**
	 * 获取通讯线程
	 * @return
	 */
	public BluetoothCommunThread getBluetoothCommunThread() {
		return communThread;
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		
		super.onStart(intent, startId);
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	/**
	 * Service创建时的回调函数
	 */
	@Override
	public void onCreate() {
		//discoveryReceiver的IntentFilter
		IntentFilter discoveryFilter = new IntentFilter();
		discoveryFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		discoveryFilter.addAction(BluetoothDevice.ACTION_FOUND);//这三个是系统发送的
		
		//controlReceiver的IntentFilter
		IntentFilter controlFilter = new IntentFilter();
		controlFilter.addAction(BluetoothTools.ACTION_START_DISCOVERY);
		controlFilter.addAction(BluetoothTools.ACTION_SELECTED_DEVICE);
		controlFilter.addAction(BluetoothTools.ACTION_STOP_SERVICE);
		controlFilter.addAction(BluetoothTools.ACTION_DATA_TO_SERVICE);
		
		//注册BroadcastReceiver
		registerReceiver(discoveryReceiver, discoveryFilter);
		registerReceiver(controlReceiver, controlFilter);
		super.onCreate();
	}
	
	/**
	 * Service销毁时的回调函数
	 */
	@Override
	public void onDestroy() {
		if (communThread != null) {
			communThread.isRun = false;
		}
		//解除绑定
		unregisterReceiver(discoveryReceiver);
		super.onDestroy();
	}

}
