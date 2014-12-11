package com.ds.IrDA;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import com.ds.IrDA.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

//import java.io.IOException;



public class ClientActivity extends Activity {
    private static final int REQUEST_CONNECT_DEVICE = 1;
	//private double mainnum=1;
	private TextView serversText;
	//private TextView protoltext;//协议类型说明
	//private EditText chatEditText;
	private Button startSearchBtn;
	private Button studyover;
	private EditText mOutEditText;
	private Button addButton;
	public static String message;
	public static int [] study_num=new int[6];//学习码的码长记录
	public static int [] study_order=new int[6];//学习码的码位置记录
	public static int[]  out_num=new int[6];//输出码长记录
	public static int[] out_order=new int[6];//输出码位置记录
	private StringBuffer mOutStringBuffer;
	private BluetoothDevice device;
	private Button study;
	private Button volup;
	private Button voldn;
	private Button play;
	private Button prev;
	private Button next;
	private Button mute;
	private Button cli1;
	private Button cli2;
	private Button cli3;
	private Button cli4;
	private Button cli5;
	private Button cli6;
	private Button cli7;
	private Button cli8;
	private Button cli9;
	private Spinner protol;//显示各类码
	public static int obj;//按键识别变量
	public  int category=0; //学习码类型数统计
	public static String filename;//码存储文件名
	public static int flag=0;//工作标识
	//private byte[]buffer={(byte)0xAA,0x02};
	private String[] plist={"NEC"};//存储码类型的数组
	public static int  sort=0;//码类型识别变量
	private int study_count=0;//已学习按键个数
	public static int b=0;
	public  ArrayAdapter<String> padapter;
	private List<String>all;
	//广播接收器
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		
		@SuppressLint("NewApi") @Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			 if (BluetoothTools.ACTION_CONNECT_SUCCESS.equals(action)) {
				//连接成功
				serversText.setText("连接成功");
				
			} else if (BluetoothTools.ACTION_DATA_TO_GAME.equals(action)) {
				//接收数据
				String msg =message;
	            	   msg = device.getName()+": "+msg+"\r\n";
			//	chatEditText.append(msg);
			} 
			else if(BluetoothTools.ACTION_STUDY_PROTOL.equals(action))
			{
				study_count++;
				if(obj==0)
				{
					flag=2;
					Toast.makeText(ClientActivity.this,"音量+键学习完毕", Toast.LENGTH_LONG).show();
				}
				else if(obj==1)
				{
					flag=2;
					Toast.makeText(ClientActivity.this,"音量-键学习完毕", Toast.LENGTH_LONG).show();
				}
				else if(obj==2)
				{
					flag=2;
					Toast.makeText(ClientActivity.this,"静音键学习完毕", Toast.LENGTH_LONG).show();
				}
				else if(obj==3)
				{
					flag=2;
					Toast.makeText(ClientActivity.this,"频道+键学习完毕", Toast.LENGTH_LONG).show();
				}
				else if(obj==4)
				{
					flag=2;
					Toast.makeText(ClientActivity.this,"频道-键学习完毕", Toast.LENGTH_LONG).show();
				}
				else if(obj==5)
				{
					flag=2;
					Toast.makeText(ClientActivity.this,"待机键学习完毕", Toast.LENGTH_LONG).show();
					
				}
				if(study_count==6)
				{
					flag=0;
					study_count=0;
					b=0;
					SharedPreferences get_in = getSharedPreferences("data", MODE_PRIVATE);
					Editor meditor=get_in.edit();
					for(int i=0;i<6;i++)
					{
					String order_na=(category+"order"+i);
					meditor.putInt(order_na,study_order[i]);
					meditor.putInt("category",category);
					String num_na=(category+"num"+i);
					meditor.putInt(num_na,study_num[i]);
					meditor.commit();
				 }					
					serversText.setText("学习完毕");
				    Toast.makeText(ClientActivity.this,"学习完毕", Toast.LENGTH_LONG).show();
			}
		}
	}
	};
	
	
	@Override
	protected void onStart() {
		//清空设备列表
		
		//开启后台service
		Intent startService = new Intent(ClientActivity.this, BluetoothClientService.class);
		startService(startService);
		
		//注册BoradcasrReceiver
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothTools.ACTION_DATA_TO_GAME);
		intentFilter.addAction(BluetoothTools.ACTION_CONNECT_SUCCESS);
		intentFilter.addAction(BluetoothTools.ACTION_STUDY_PROTOL);
		registerReceiver(broadcastReceiver, intentFilter);
		
		super.onStart();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.client);
		mOutStringBuffer = new StringBuffer("");
        all = new ArrayList<String>();  
        for (int i = 0; i < plist.length; i++)  
        {  
            all.add(plist[i]);  
        }
		serversText = (TextView)findViewById(R.id.clientServersText);
	//	chatEditText = (EditText)findViewById(R.id.clientChatEditText);
		startSearchBtn = (Button)findViewById(R.id.startSearchBtn);
		studyover = (Button)findViewById(R.id.selectDeviceBtn);
        mOutEditText = (EditText) findViewById(R.id.edit_text_out);
        study=(Button)findViewById(R.id.study);
        mOutEditText.setOnEditorActionListener(mWriteListener);
        // Initialize the send button with a listener that for click events
       addButton = (Button) findViewById(R.id.button_send);
        volup = (Button) findViewById(R.id.apple_volup);
        voldn = (Button) findViewById(R.id.apple_voldn);
        mute = (Button) findViewById(R.id.apple_menu);
        next = (Button) findViewById(R.id.apple_next);
        prev = (Button) findViewById(R.id.apple_prev);
        play = (Button) findViewById(R.id.apple_play);
        protol=(Spinner)findViewById(R.id.protol);
        cli1=(Button)findViewById(R.id.cli1);
        cli2=(Button)findViewById(R.id.cli2);
        cli3=(Button)findViewById(R.id.cli3);
        cli4=(Button)findViewById(R.id.cli4);
        cli5=(Button)findViewById(R.id.cli5);
        cli6=(Button)findViewById(R.id.cli6);
        cli7=(Button)findViewById(R.id.cli7);
        cli8=(Button)findViewById(R.id.cli8);
        cli9=(Button)findViewById(R.id.cli9);
        //识别码型选择
        padapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,all);//设置下拉列表的适配器
        int pid=android.R.layout.simple_spinner_dropdown_item;
        padapter.setDropDownViewResource(pid);
        protol.setAdapter(padapter);
        protol.setOnItemSelectedListener(new OnItemSelectedListener(){
        	public void onItemSelected(AdapterView<?> arg0,View arg1,int arg2,long arg3){
        		sort=arg2;
        		if(sort!=0)
        			{
        			SharedPreferences get_out = getSharedPreferences("data", MODE_PRIVATE);
        			for(int i=0;i<6;i++)
        			{
        			String flie_num=sort+"num"+i;
        			out_num[i]=get_out.getInt(flie_num, 0);
        			String flie_order=sort+"order"+i;
        			out_order[i]=get_out.getInt(flie_order,0);
        			}
        			}
        		arg0.setVisibility(View.VISIBLE);
        	}
        	
        	public void onNothingSelected(AdapterView<?> arg0){}
        });
        SharedPreferences get_category = getSharedPreferences("data", MODE_PRIVATE);//取出代码类型个数
        category=get_category.getInt("category",0);
        for(int i=1;i<(category+1);i++)
        {
        	String justnow="学习型"+i; 
            SharedPreferences get_name = getSharedPreferences("data", MODE_PRIVATE);//取出代码类型个数
            String now=get_name.getString(justnow, ("学习型"+i));
    		padapter.add(now) ;
        }
       //学习键按键监听 
        study.setOnClickListener(new OnClickListener()
        {
        	public void onClick(View v){
        		category++;
        		flag=2;
        		 filename="study_model"+category+".txt";
        		// padapter.add(("学习型"+category)) ;
        		Toast.makeText(ClientActivity.this, "开始学习，请添加编码名", Toast.LENGTH_LONG).show();
        		serversText.setText("开始学习");
        	}
        });
        //各个按键的监听
        volup.setOnClickListener(new OnClickListener(){
        	public void onClick( View v){
        		obj=0;
        		if(flag==0)
        		{
        		sendMessage("volup");
        		}
        		if(flag==2)
        		{
        		 flag=3;
        		 sendMessage("学习...");
        		 Toast.makeText(ClientActivity.this, "开始学习音量+键", Toast.LENGTH_LONG).show();
        		}
        	}
        });
        voldn.setOnClickListener(new OnClickListener(){
        	public void onClick(View v){
        		obj=1;	
        		if(flag==0)
        		{
        		sendMessage("voldn");
        		}
        		else if(flag==2)
        		{
        		flag=3;
        		sendMessage("学习...");
        		Toast.makeText(ClientActivity.this, "开始学习音量-键", Toast.LENGTH_LONG).show();
        		}
        	}
        });
        mute.setOnClickListener(new OnClickListener(){
        	public void onClick(View v){
        		obj=2;
        		if(flag==0)
        		{
        		sendMessage("mute");
        		}
        		else if(flag==2)
        		{
        			 flag=3;
             		sendMessage("学习...");
       			 Toast.makeText(ClientActivity.this, "开始学习静音键", Toast.LENGTH_LONG).show();
        		}
        	}
        });
        next.setOnClickListener(new OnClickListener(){
        	public void onClick(View v){
        		obj=3;
        		if(flag==0)
        		{
        		sendMessage("next");
        		}
        		else if(flag==2)
        		{
        			 flag=3;
             		sendMessage("学习...");
       			 Toast.makeText(ClientActivity.this, "开始学习频道+键", Toast.LENGTH_LONG).show();
        		}
        	}
        });
        prev.setOnClickListener(new OnClickListener(){
        	public void onClick(View v){
            	obj=4;
        		if(flag==0)
        		{
        	sendMessage("prev");
        		}
        		else if(flag==2)
        		{
        			 flag=3;
             		sendMessage("学习...");
       			 Toast.makeText(ClientActivity.this, "开始学习频道-键", Toast.LENGTH_LONG).show();
        		}
        	}
        });
        play.setOnClickListener(new OnClickListener(){
        	public void onClick(View v){
        		obj=5;
        		if(flag==0)
        		{
        		sendMessage("play");
        		}
        		else if(flag==2)
        		{
        			 flag=3;
             		sendMessage("学习...");
       			 Toast.makeText(ClientActivity.this, "开始学习待机键", Toast.LENGTH_LONG).show();
        		}
        	}
        });
        cli1.setOnClickListener(new OnClickListener(){
        	public void onClick(View v){
        		if(flag==0)
        		{
        			obj=6;
        			sendMessage("1");
        		}
        	}
        });
        cli2.setOnClickListener(new OnClickListener(){
        	public void onClick(View v){
        		if(flag==0)
        		{
        			obj=7;
        			sendMessage("2");
        		}
        	}
        });
        cli3.setOnClickListener(new OnClickListener(){
        	public void onClick(View v){
        		if(flag==0)
        		{
        			obj=8;
        			sendMessage("3");
        		}
        	}
        });
        cli4.setOnClickListener(new OnClickListener(){
        	public void onClick(View v){
        		if(flag==0)
        		{
        			obj=9;
        			sendMessage("4");
        		}
        	}
        });
        cli5.setOnClickListener(new OnClickListener(){
        	public void onClick(View v){
        		if(flag==0)
        		{
        			obj=10;
        			sendMessage("5");
        		}
        	}
        });
        cli6.setOnClickListener(new OnClickListener(){
        	public void onClick(View v){
        		if(flag==0)
        		{
        			obj=11;
        			sendMessage("6");
        		}
        	}
        });
        cli7.setOnClickListener(new OnClickListener(){
        	public void onClick(View v){
        		if(flag==0)
        		{
        			obj=12;
        			sendMessage("7");
        		}
        	}
        });
        cli8.setOnClickListener(new OnClickListener(){
        	public void onClick(View v){
        		if(flag==0)
        		{
        			obj=13;
        			sendMessage("8");
        		}
        	}
        });
        cli9.setOnClickListener(new OnClickListener(){
        	public void onClick(View v){
        		if(flag==0)
        		{
        			obj=14;
        			sendMessage("9");
        		}
        	}
        });
        //添加编码名
        addButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
            	if(flag==2)
            	{
                TextView view = (TextView) findViewById(R.id.edit_text_out);
                String message = view.getText().toString();
				SharedPreferences get_class = getSharedPreferences("data", MODE_PRIVATE);
				Editor meditor=get_class.edit();
				String num_n="学习型"+category;
				meditor.putString(num_n,message);
				meditor.commit();
	    		padapter.add(message) ;
	            mOutStringBuffer.setLength(0);
	            mOutEditText.setText(mOutStringBuffer);
	    		Toast.makeText(ClientActivity.this, "编码名添加完毕", Toast.LENGTH_LONG).show();
            	}
                else
                {
                	 Toast.makeText(ClientActivity.this, "请先点击学习", Toast.LENGTH_LONG).show();
                }
            }
        });
        
		startSearchBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//开始搜索
	            Intent serverIntent = new Intent(ClientActivity.this, DeviceListActivity.class);
	            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
			}
		});
		
		studyover.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				flag=0;
				study_count=0;
				b=0;
				SharedPreferences get_in = getSharedPreferences("data", MODE_PRIVATE);
				Editor meditor=get_in.edit();
				for(int i=0;i<6;i++)
				{
				String order_na=(category+"order"+i);
				meditor.putInt(order_na,study_order[i]);
				meditor.putInt("category",category);
				String num_na=(category+"num"+i);
				meditor.putInt(num_na,study_num[i]);
				meditor.commit();
			 }	
						serversText.setText("学习完毕");
			}
		});
	}

	@Override
	protected void onStop() {
		//关闭后台Service
		Intent startService = new Intent(BluetoothTools.ACTION_STOP_SERVICE);
		sendBroadcast(startService);
		
		unregisterReceiver(broadcastReceiver);
		super.onStop();
	}
    private TextView.OnEditorActionListener mWriteListener =
            new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                // If the action is a key-up event on the return key, send the message
                if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                    String message = view.getText().toString();
                    sendMessage(message);
                }
                return true;
            }
        };
        private void sendMessage(String message) {
            // Check that there's actually something to send
            if (message.length() > 0) {
                // Get the message bytes and tell the BluetoothChatService to write
				Intent sendDataIntent = new Intent(BluetoothTools.ACTION_DATA_TO_SERVICE);
				//sendDataIntent.putExtra(BluetoothTools.DATA, message);
				sendBroadcast(sendDataIntent);
				message="我: "+message+"\r\n";
             //   chatEditText.append(message);
            }
        }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                // Get the device MAC address
                String address = data.getExtras()
                                     .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                // Get the BLuetoothDevice object
                device =BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
				Intent selectDeviceIntent = new Intent(BluetoothTools.ACTION_SELECTED_DEVICE);//选择动作
				selectDeviceIntent.putExtra(BluetoothTools.DEVICE, device);
				sendBroadcast(selectDeviceIntent);
            }
            break;
        }
    }   
}
