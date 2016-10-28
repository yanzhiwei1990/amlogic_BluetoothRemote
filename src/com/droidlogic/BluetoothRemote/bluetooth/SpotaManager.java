package com.droidlogic.BluetoothRemote.bluetooth;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import com.droidlogic.BluetoothRemote.DeviceActivity;
import com.droidlogic.BluetoothRemote.data.Statics;

/**
 * Created by wouter on 6-11-14.
 */
public class SpotaManager extends BluetoothManager {
    public static final int TYPE = 2;

	public static final int MEMORY_TYPE_SYSTEM_RAM = 0x00;
	public static final int MEMORY_TYPE_RETENTION_RAM = 0x01;
	public static final int MEMORY_TYPE_EXTERNAL_I2C = 0x02;
	public static final int MEMORY_TYPE_EXTERNAL_SPI = 0x03;

    public SpotaManager(Context context) {
        super(context);
        activity = DeviceActivity.getInstance();
        type = SpotaManager.TYPE;
    }

    @Override
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
        else if (newStep >= 0) {
            this.step = newStep;
        }
        // If no step is set, check if Bluetooth characteristic information is set
        else {
            int index = intent.getIntExtra("characteristic", -1);
            String value = intent.getStringExtra("value");
            activity.setItemValue(index, value);
            readNextCharacteristic();
        }
        Log.d(TAG, "step " + this.step);
        switch (this.step) {
            case 0:
                activity.initMainScreen();
                this.step = -1;
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
                // TODO: 8. If mem_type = I2C or SPI then
//                setSpotaGpioMap();
				goToStep(4);
                break;
            // Read SPOTA_MEM_INFO_UUID
            case 4:
                readMemInfo();
                break;
            // Parse mem info and log
            case 5:
                int memInfoValue = intent.getIntExtra("value", -1);
				String stringValue = String.format("%#10x", memInfoValue);
				Log.d(TAG, "mem info: " + stringValue);
                if(!lastBlockSent && memInfoValue == 0) {
                    // Go to step 6
                    goToStep(6);
                }
                // TODO: 17. Parse mem_info and log the number and the entire size of existing patches in memory device.
				else if(lastBlockSent) {
					activity.logMemInfoValue(memInfoValue);
					goToStep(9);
                }
                break;
            // Set SPOTA_PATCH_LEN_UUID
            // Check if the blocks are sent
            // After all the blocks are sent, read the mem info again
            case 6:
                if(!lastBlockSent) {
                    setPatchLength();
                }
                else {
                    goToStep(8);
                }
                break;
            // Send all the blocks
            case 7:
                sendBlock();
                break;
			case 8:
				readMemInfo();
				break;
            // Send end signal code and request 0x03 response
            case 9:
				sendEndSignal();
                break;
            // Finished, disconnect
            case 10:
				if(!finished) {
					onSuccess();
					disconnect();
				}
                break;
        }
    }

	@Override
	protected int getSpotaMemDev() {
		int memTypeBase = MEMORY_TYPE_SYSTEM_RAM;
		// For system and retention RAM, the patch base address is 0x00, for SPI and I2C, it's custom.
		boolean customPatchBaseAddress = false;
		switch (memoryType) {
			case Statics.MEMORY_TYPE_RETENTION_RAM:
				memTypeBase = MEMORY_TYPE_RETENTION_RAM;
				break;
			case Statics.MEMORY_TYPE_SPI:
				memTypeBase = MEMORY_TYPE_EXTERNAL_SPI;
				customPatchBaseAddress = true;
				break;
			case Statics.MEMORY_TYPE_I2C:
				memTypeBase = MEMORY_TYPE_EXTERNAL_I2C;
				customPatchBaseAddress = true;
				break;
		}
		// Use 0xFFFFFF to strip off the characters that don't fit
		int patchBaseAddress = customPatchBaseAddress ? this.patchBaseAddress & 0xFFFFFF : 0x00;
		int memType = (memTypeBase << 24) | patchBaseAddress;
		return memType;
	}

	private void readMemInfo() {
        Log.d(TAG, "readMemInfo");
        if (BluetoothGattSingleton.getSpotaMemInfoCharacteristic() != null) {
            BluetoothGattSingleton.getGatt().readCharacteristic(BluetoothGattSingleton.getSpotaMemInfoCharacteristic());
        }
        else {
            Log.e(TAG, "spotaMemInfoCharacteristic not set");
        }
    }

    @Override
    public void sendEndSignal() {
        BluetoothGattCharacteristic characteristic = BluetoothGattSingleton.getGatt().getService(Statics.SPOTA_SERVICE_UUID)
                .getCharacteristic(Statics.SPOTA_MEM_DEV_UUID);
        characteristic.setValue(0xff000000, BluetoothGattCharacteristic.FORMAT_UINT32, 0);
        BluetoothGattSingleton.getGatt().writeCharacteristic(characteristic);
		endSignalSent = true;
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
            case 9:
				if(memDevValue != 0x03) {
					activity.log("Set SPOTA_MEM_DEV: 0x03");
					onError(0);
				}
				else {
					goToStep(10);
				}
                break;
        }
    }
}
