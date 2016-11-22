package com.droidlogic.BluetoothRemote;


import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import com.droidlogic.BluetoothRemote.async.DeviceConnectTask;
import com.droidlogic.BluetoothRemote.bluetooth.BluetoothGattSingleton;
import com.droidlogic.BluetoothRemote.bluetooth.Callback;
import com.droidlogic.BluetoothRemote.data.Statics;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.BluetoothGattCharacteristic;


public class ReadVersionService extends Service {
	private static final String TAG = "ReadVersionService";
	private static final String RemoteName = "RemoteB008";
	private BluetoothAdapter mBluetoothAdapter;
    private DeviceConnectTask connectTask;
    private String firmwareversion;
    private BluetoothDevice mBluetoothDevice;
    BroadcastReceiver bluetoothGattReceiver_service;
    public Queue<BluetoothGattCharacteristic> characteristicsQueue;
    public ProgressBar progressBar;
    BluetoothGattCharacteristic characteristics_READ;
    private boolean readservice_flag = true;
    BluetoothGatt gatt;
    Callback callback;
    Context mContext;
    private boolean HasRemote = false;
    private boolean registerReceiver_flag;
    private boolean connectTask_flag = false;
    private String READ_VERSION = "";
    private String LATEST_VERSION = "";
    
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "ReadVersionService-onStartCommand");
		firmwareversion = this.getResources().getString(R.string.firmware_version);
		characteristicsQueue = new ArrayDeque<BluetoothGattCharacteristic>();
		readservice_flag = true;
		bluetoothGattReceiver_service = new BluetoothGattReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				mContext = context;
				if (readservice_flag) {
					processStep(intent);
				}
			}
		};
		new Handler().postDelayed(new Runnable()
		{  
		    public void run()
		    {  	
		    	stopSelf();
		    	Log.d(TAG, "Handler().postDelayed");
		    }  
		 }, 12000); 
		startDeviceScan();
		return super.onStartCommand(intent, flags, startId);
	}  
	private void CallForDialog(){
		AlertDialog d = new AlertDialog.Builder(this).setTitle(mContext.getResources().getString(R.string.firmwareupdate)
				+":"+mContext.getResources().getString(R.string.firmware_version))  
		        .setPositiveButton(R.string.update_now, new DialogInterface.OnClickListener() {  
		            public void onClick(DialogInterface dialog, int whichButton) { 
		            	Log.d(TAG, "OK is Clicked! "); 
		                Intent i = new Intent(getApplicationContext(), DeviceActivity.class);
		                //i.putExtra("device",mBluetoothDevice);
		                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK ); 
		                getApplicationContext().startActivity(i);
		            	//stopSelf();
		            }  
		        }).setNegativeButton(R.string.ignore, new DialogInterface.OnClickListener() {  
		            public void onClick(DialogInterface dialog, int whichButton) {  
		            	Log.d(TAG, "Cancel is Clicked! "); 
		            	//stopSelf();
		            }  
		        }).create();
		d.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		d.show();
	}
	private void delay(int ms){  
        try {  
            Thread.currentThread();  
            Thread.sleep(ms);  
        } catch (InterruptedException e) {  
            e.printStackTrace();  
        }   
     } 
	Set<BluetoothDevice> mBondedDevices = null;
	private void startDeviceScan() {
		int WaitCount = 2;
		HasRemote = false;
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter != null ) {
			while (BluetoothAdapter.STATE_ON != mBluetoothAdapter.getState()) {
			//!mBluetoothAdapter.isEnabled()
			//while (WaitCount > 0) {
				Log.d(TAG, "wait for bluetooth turn on: "+mBluetoothAdapter.getState());
				delay(2000);
				WaitCount--;
				if(WaitCount == 0) {
					break;
				}
			}
			Log.d(TAG, "bluetooth is turned on ");
		}
		if (mBluetoothAdapter != null && BluetoothAdapter.STATE_ON == mBluetoothAdapter.getState()) {
			mBondedDevices = mBluetoothAdapter.getBondedDevices();
			if (mBondedDevices != null) {
				if ( mBondedDevices.size() > 0) {
					Log.i(TAG,"Got bonded devices "+mBondedDevices.size());
					for (BluetoothDevice device : mBondedDevices) {     
						Log.d(TAG, "startDeviceScan:device name: "+device.getName()+ 
			            		"  device  Address: "+device.getAddress());
						if (device.getName().equals(RemoteName)) {
							mBluetoothDevice = device;
							HasRemote = true;
							WaitCount = 2;
							while (BluetoothDevice.BOND_BONDED != mBluetoothDevice.getBondState()) {
								Log.d(TAG, "not BOND: "+mBluetoothDevice.getBondState());
								delay(2000);
								WaitCount--;
								if (WaitCount == 0) {
									break;
								}
							}
							delay(2000);
							registerReceiver(bluetoothGattReceiver_service,
									new IntentFilter(Statics.BLUETOOTH_GATT_UPDATE));
							connectTask = new DeviceConnectTask(this, mBluetoothDevice);
							callback = new Callback(connectTask);
							gatt = mBluetoothDevice.connectGatt(this, false, callback);	
							WaitCount = 2;
							while (!gatt.connect()) {	
								delay(2000);
								Log.d(TAG, "WAIT FOR CONNECT!");
								WaitCount--;
								if (WaitCount == 0) {
									Log.d(TAG, "CONNECT FAIL");
									break;
								}
							}
							connectTask_flag = true;
						}
					}
				}
			}			
		}
		if (!HasRemote) {
			Log.d(TAG, "NOT FOUND REMOTE");
		}	
	}
	public void processStep(Intent intent) {
		int newStep = intent.getIntExtra("step", -1);
		if (newStep >= 0) {
			Log.d(TAG, "is not version information");	
		}
		else {
			int index = intent.getIntExtra("characteristic", -1);
			String value = intent.getStringExtra("value");
			READ_VERSION = value;
			int temp1 = Integer.parseInt(value.substring(7), 10);
			int temp2 = Integer.parseInt(firmwareversion.substring(7), 10);
			Log.d(TAG, "READ VERSION: "+temp1+"  CURRENT VERSION: "+temp2);
			if(index == 2) {
				readservice_flag = false;				
			}
			if(index == 2 && temp1 < temp2) {
				Log.d(TAG, "get right index:  " + index+"  value: "+value);
				CallForDialog();
			}
			readNextCharacteristic(); 
		}
		if(newStep == 0) {
			List<BluetoothGattService> services = BluetoothGattSingleton.getGatt().getServices();
			for (BluetoothGattService service : services) {
				List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
				for (BluetoothGattCharacteristic characteristic : characteristics) {
					if (characteristic.getUuid().equals(Statics.ORG_BLUETOOTH_CHARACTERISTIC_FIRMWARE_REVISION_STRING)) {
						characteristics_READ = characteristic;
						readNextCharacteristic();
						break;
					} 							
				}
			}					
		}
	}
	public void readNextCharacteristic() {
		Log.d(TAG, "readNextCharacteristic start");
		if (characteristics_READ != null) {
			BluetoothGattSingleton.getGatt().readCharacteristic(characteristics_READ);
			characteristics_READ = null;
		}
	}
	@Override 
	public void onCreate(){
		Log.i(TAG, "ReadVersionService-onCreate");
		super.onCreate();
		//startForeground(2, new Notification());
	}
	@Override  
    public void onStart(Intent intent, int startId) {  
        Log.i(TAG, "ReadVersionService-onStart");  
        super.onStart(intent, startId);  
    } 
	@Override  
    public IBinder onBind(Intent intent) {  
        Log.i(TAG, "ReadVersionService-onBind");  
        return null;  
    }  
      
    @Override  
    public void onDestroy() {  
        Log.i(TAG, "ReadVersionService-onDestroy");
        if (connectTask_flag) {
			connectTask_flag = false;
			gatt.disconnect();
			gatt.close();;
		}
        if (registerReceiver_flag) {
			registerReceiver_flag = false;
			unregisterReceiver(bluetoothGattReceiver_service);
		}
        super.onDestroy();  
    } 
}
