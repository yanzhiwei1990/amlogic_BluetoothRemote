package com.droidlogic.BluetoothRemote.async;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.droidlogic.BluetoothRemote.bluetooth.Callback;
import com.droidlogic.BluetoothRemote.data.Statics;

import java.io.IOException;
import java.lang.reflect.Method;

public class DeviceConnectTask extends AsyncTask<Void, BluetoothGatt, Boolean > {
	public static final String TAG = "DeviceGattTask";
	public Context context;
	private final BluetoothSocket mmSocket;
	private final BluetoothDevice mmDevice;

	private final BluetoothAdapter mBluetoothAdapter;

	private final Callback callback = new Callback(this);

	public DeviceConnectTask(Context context, BluetoothDevice device) {
		Log.d(TAG, "init");
		this.context = context;
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		// Use a temporary object that is later assigned to mmSocket,
		// because mmSocket is final
		BluetoothSocket tmp = null;
		mmDevice = device;

		// Get a BluetoothSocket to connect with the given BluetoothDevice
		try {
			tmp = device.createRfcommSocketToServiceRecord(Statics.SPOTA_MEM_DEV_UUID);
		} catch (IOException e) { }
		mmSocket = tmp;

	}

	/** Will cancel an in-progress connection, and close the socket */
	public void cancel() {
		try {
			mmSocket.close();
		} catch (IOException e) { }
	}

	private boolean refreshDeviceCache(BluetoothGatt gatt){
		try {
			BluetoothGatt localBluetoothGatt = gatt;
			Method localMethod = localBluetoothGatt.getClass().getMethod("refresh", new Class[0]);
			if (localMethod != null) {
				boolean bool = ((Boolean) localMethod.invoke(localBluetoothGatt, new Object[0])).booleanValue();
				return bool;
			}
		}
		catch (Exception localException) {
			Log.e(TAG, "An exception occured while refreshing device");
		}
		return false;
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		BluetoothGatt gatt = mmDevice.connectGatt(context, false, callback);
		refreshDeviceCache(gatt);
		gatt.connect();
		return true;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected void onPostExecute(Boolean aBoolean) {
		super.onPostExecute(aBoolean);
	}

	@Override
	protected void onProgressUpdate(BluetoothGatt... values) {
		super.onProgressUpdate(values);
	}

	@Override
	protected void onCancelled(Boolean aBoolean) {
		super.onCancelled(aBoolean);
		cancel();
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
		cancel();
	}

	// Override the protected method to be called from another class
	public void publishProgess(BluetoothGatt gatt) {
		this.publishProgress(gatt);
	}
}