package com.droidlogic.BluetoothRemote;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by wouter on 15-10-14.
 */
public class BluetoothGattReceiver extends BroadcastReceiver {
    @Override
    public void onReceive
            (Context context, Intent intent) {
        Log.d("BluetoothGattReceiver", "onReceive£º "+intent.getAction());
    }

}
