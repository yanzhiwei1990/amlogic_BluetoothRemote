package com.droidlogic.BluetoothRemote.bluetooth;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import com.droidlogic.BluetoothRemote.DeviceActivity;
import com.droidlogic.BluetoothRemote.data.Statics;

/**
 * Created by wouter on 6-11-14.
 */
public class SuotaManager extends BluetoothManager {
    public static final int TYPE = 1;

	public static final int MEMORY_TYPE_EXTERNAL_I2C = 0x12;
	public static final int MEMORY_TYPE_EXTERNAL_SPI = 0x13;

    static final String TAG = "SuotaManager";

    public SuotaManager(Context context) {
        super(context);
        activity = DeviceActivity.getInstance();
        type = SuotaManager.TYPE;
    }

    public void processStep(Intent intent) {
        int newStep = intent.getIntExtra("step", -1);
        int error = intent.getIntExtra("error", -1);
		int memDevValue = intent.getIntExtra("memDevValue", -1);
        if (error >= 0) {
            onError(error);
        }

		else if(memDevValue >= 0) {
			processMemDevValue(memDevValue);
		}
        // If a step is set, change the global step to this value
        if (newStep >= 0) {
            this.step = newStep;
        }
        // If no step is set, check if Bluetooth characteristic information is set
        else {
            int index = intent.getIntExtra("characteristic", -1);
            String value = intent.getStringExtra("value");
		Log.d(TAG, "characteristic index:  " + index+"  value: "+value);
            activity.setItemValue(index, value);
            readNextCharacteristic();
        }
        Log.d(TAG, "step " + this.step);
        switch (this.step) {
            case 0:
                activity.initMainScreen();
                this.step = -1;
//                        initFileList();
                break;
            // Enable notifications
            case 1:
                enableNotifications();
                break;
            // Init mem type
            case 2:
                activity.progressText.setText("Uploading " + fileName + " to " + device.getName() + ".\n" +
                        "Please wait until the progress is\n" +
                        "completed.");
                setSpotaMemDev();
                activity.fileListView.setVisibility(View.GONE);
                activity.progressBar.setVisibility(View.VISIBLE);
                break;
            // Set mem_type for SPOTA_GPIO_MAP_UUID
            case 3:
                setSpotaGpioMap();
                break;
            // Set SPOTA_PATCH_LEN_UUID
            case 4:
                setPatchLength();
                break;
            // Send a block containing blocks of 20 bytes until the patch length (default 240) has been reached
            // Wait for response and repeat this action
            case 5:
                if (!lastBlock) {
                    sendBlock();
                } else {
                    if (!preparedForLastBlock) {
                        setPatchLength();
                    } else if (!lastBlockSent) {
                        sendBlock();
                    } else if (!endSignalSent) {
                        sendEndSignal();
                    } else {
                        onSuccess();
                    }
                }
                break;
        }
    }

	@Override
	protected int getSpotaMemDev() {
		int memTypeBase = -1;
		switch (memoryType) {
			case Statics.MEMORY_TYPE_SPI:
				memTypeBase = MEMORY_TYPE_EXTERNAL_SPI;
				break;
			case Statics.MEMORY_TYPE_I2C:
				memTypeBase = MEMORY_TYPE_EXTERNAL_I2C;
				break;
		}
		int memType = (memTypeBase << 24) | imageBank;
		return memType;
	}

	private void processMemDevValue(int memDevValue) {
		String stringValue = String.format("%#10x", memDevValue);
		Log.d(TAG, "processMemDevValue() step: " + step + ", value: " + stringValue);
		switch (step) {
			case 2:
				if(memDevValue == 0x1) {
					activity.log("Set SPOTA_MEM_DEV: 0x1");
					goToStep(3);
				}
				else {
					onError(0);
				}
				break;
		}
	}
}
