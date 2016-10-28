package com.droidlogic.BluetoothRemote;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootCompletedReceiver extends BroadcastReceiver {
	private static final String TAG = "BootCompletedReceiver";
	public void onReceive(Context context, Intent intent) { 
		Log.d(TAG, "BootCompletedReceiver in read version ");
		Intent it = new Intent(context, ReadVersionService.class);
        context.startService(it);   
	}
}
