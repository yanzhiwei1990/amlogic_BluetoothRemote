package com.droidlogic.BluetoothRemote.data;

import android.content.Context;

import java.util.Map;
import java.util.UUID;

public class Statics {
	public static final String BLUETOOTH_GATT_UPDATE = "BluetoothGattUpdate";
	public static final String PROGRESS_UPDATE = "ProgressUpdate";
	public static final String CONNECTION_STATE_UPDATE = "ConnectionState";
    public static final int fileChunkSize = 20;

	public static final int MEMORY_TYPE_SYSTEM_RAM = 1;
	public static final int MEMORY_TYPE_RETENTION_RAM = 2;
	public static final int MEMORY_TYPE_SPI = 3;
	public static final int MEMORY_TYPE_I2C = 4;

	public static final UUID SPOTA_SERVICE_UUID = UUID.fromString("0000fef5-0000-1000-8000-00805f9b34fb");
	public static final UUID SPOTA_MEM_DEV_UUID = UUID.fromString("8082caa8-41a6-4021-91c6-56f9b954cc34");
	public static final UUID SPOTA_GPIO_MAP_UUID = UUID.fromString("724249f0-5eC3-4b5f-8804-42345af08651");
	public static final UUID SPOTA_MEM_INFO_UUID = UUID.fromString("6c53db25-47a1-45fe-a022-7c92fb334fd4");
	public static final UUID SPOTA_PATCH_LEN_UUID = UUID.fromString("9d84b9a3-000c-49d8-9183-855b673fda31");
	public static final UUID SPOTA_PATCH_DATA_UUID = UUID.fromString("457871e8-d516-4ca1-9116-57d0b17b9cb2");
	public static final UUID SPOTA_SERV_STATUS_UUID = UUID.fromString("5f78df94-798c-46f5-990a-b3eb6a065c88");

    public static final UUID SPOTA_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public static final UUID ORG_BLUETOOTH_SERVICE_DEVICE_INFORMATION              = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");
    public static final UUID ORG_BLUETOOTH_CHARACTERISTIC_MANUFACTURER_NAME_STRING = UUID.fromString("00002A29-0000-1000-8000-00805f9b34fb");
    public static final UUID ORG_BLUETOOTH_CHARACTERISTIC_MODEL_NUMBER_STRING      = UUID.fromString("00002A24-0000-1000-8000-00805f9b34fb");
    public static final UUID ORG_BLUETOOTH_CHARACTERISTIC_SERIAL_NUMBER_STRING     = UUID.fromString("00002A25-0000-1000-8000-00805f9b34fb");
    public static final UUID ORG_BLUETOOTH_CHARACTERISTIC_HARDWARE_REVISION_STRING = UUID.fromString("00002A27-0000-1000-8000-00805f9b34fb");
    public static final UUID ORG_BLUETOOTH_CHARACTERISTIC_FIRMWARE_REVISION_STRING = UUID.fromString("00002A26-0000-1000-8000-00805f9b34fb");
    public static final UUID ORG_BLUETOOTH_CHARACTERISTIC_SOFTWARE_REVISION_STRING = UUID.fromString("00002A28-0000-1000-8000-00805f9b34fb");
    public static final UUID ORG_BLUETOOTH_CHARACTERISTIC_SYSTEM_ID                = UUID.fromString("00002A23-0000-1000-8000-00805f9b34fb");
    public static final UUID ORG_BLUETOOTH_CHARACTERISTIC_IEEE_11073               = UUID.fromString("00002A2A-0000-1000-8000-00805f9b34fb");
    public static final UUID ORG_BLUETOOTH_CHARACTERISTIC_PNP_ID                   = UUID.fromString("00002A50-0000-1000-8000-00805f9b34fb");
    public static final UUID ORG_BLUETOOTH_CHARACTERISTIC_BATTERY_LEVEL_STRING                   = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb");

    public static final UUID ORG_BLUETOOTH_DESCRIPTOR_BATTERY_LEVEL_UUID                   = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");


	public static final int DEFAULT_MISO_VALUE = 16;
	public static final int DEFAULT_MOSI_VALUE = 15;
	public static final int DEFAULT_CS_VALUE = 17;
	public static final int DEFAULT_SCK_VALUE = 18;
	public static final String DEFAULT_BLOCK_SIZE_VALUE = "240";

//    public static final int ERROR_SPOTAR_SRV_EXIT = 3;
//    public static final int ERROR_SPOTAR_CRC_ERR = 4;
//    public static final int ERROR_SPOTAR_PATCH_LEN_ERR = 5;
//    public static final int ERROR_SPOTAR_EXT_MEM_ERR = 6;
//    public static final int ERROR_SPOTAR_INT_MEM_ERR = 7;
//    public static final int ERROR_INVALID_MEM_TYPE = 8;
//    public static final int ERROR_SPOTAR_APP_ERROR = 9;
//    public static final int ERROR_SPOTAR_IMG_STARTED = 10;
//    public static final int ERROR_SPOTAR_INVAL_IMG_BANKE = 11;
//    public static final int ERROR_SPOTAR_INVAL_IMG_HDR = 12;

	public static int gpioStringToInt(String spioValue) {
		String strippedValue = "0x" + spioValue.replace("P", "").replace("_", "");
		return Integer.decode(strippedValue);
	}

	public static String getPreviousInput(Context c, int resourceID) {
		return c.getSharedPreferences("settings", c.MODE_PRIVATE).getString(String.valueOf(resourceID), "0");
	}

	public static Map<String, ?> getAllPreviousInput(Context c) {
		return c.getSharedPreferences("settings", c.MODE_PRIVATE).getAll();
	}

	public static void setPreviousInput(Context c, int resourceID, String value) {
		c.getSharedPreferences("settings", c.MODE_PRIVATE).edit().putString(String.valueOf(resourceID), value.trim()).commit();
	}

	public static boolean fileDirectoriesCreated(Context c) {
		return c.getSharedPreferences("settings", c.MODE_PRIVATE).getBoolean("fileDirectoriesCreated", false);
	}

	public static void setFileDirectoriesCreated(Context c) {
		c.getSharedPreferences("settings", c.MODE_PRIVATE).edit().putBoolean("fileDirectoriesCreated", true).commit();
	}
}