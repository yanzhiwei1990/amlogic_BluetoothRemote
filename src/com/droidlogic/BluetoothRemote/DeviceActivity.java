package com.droidlogic.BluetoothRemote;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.droidlogic.BluetoothRemote.async.DeviceConnectTask;
import com.droidlogic.BluetoothRemote.bluetooth.BluetoothGattSingleton;
import com.droidlogic.BluetoothRemote.bluetooth.Callback;
import com.droidlogic.BluetoothRemote.bluetooth.SpotaManager;
import com.droidlogic.BluetoothRemote.bluetooth.SuotaManager;
import com.droidlogic.BluetoothRemote.data.FileData;
import com.droidlogic.BluetoothRemote.data.Statics;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DeviceActivity extends SuotaActivity implements AdapterView.OnItemClickListener, View.OnClickListener, AdapterView.OnItemSelectedListener {
	final static String TAG = "DeviceActivity";
	DeviceConnectTask connectTask;
	BroadcastReceiver bluetoothGattReceiver, progressUpdateReceiver, connectionStateReceiver,VersionInformationReceiver;
	String[] viewTitles;
	Map filesMap;
	ArrayList filesList = new ArrayList<Integer>();
	LayoutInflater inflater;
	public ListView fileListView;
	ArrayAdapter<String> mArrayAdapter;
	Context mcontext;

	ProgressDialog dialog;
	BluetoothDevice rBluetoothDevice;
	// Container which holds all the views
	ViewFlipper deviceContainer;
	// All layout views used in this activity
	View deviceFileListView, deviceMain, deviceParameterSettings, progressLayout;

	// Progress layout attributes
	public ProgressBar progressBar;
	public TextView progressText;
	EditText logWindow;

	// Layout attributes for device main view
	LinearLayout mainItemsView;
	Button patchDevice, updateDevice;
	RelativeLayout manufacturerItem, modelNumberItem, firmWareVersionItem, softwareRevisionItem;

	// Layout attributes for parameter settings view
	RadioButton memoryTypeSPI, memoryTypeI2C, memoryTypeSystemRam, memoryTypeRetentionRam;
	LinearLayout imageBankContainer, patchBaseAddressContainer, blockSizeContainer;
	View parameterI2cView, parameterSpiView;
	Spinner sclGpioSpinner, sdaGpioSpinner, misoGpioSpinner, mosiGpioSpinner, csGpioSpinner, sckGpioSpinner, imageBankSpinner;
	EditText patchBaseAddress, I2CDeviceAddress, blockSize;
	Button sendToDeviceButton, closeButton;

	int memoryType;

	public com.droidlogic.BluetoothRemote.bluetooth.BluetoothManager bluetoothManager;

	static DeviceActivity instance;

	Map<String, String> previousSettings;

	//    private Menu menu;
    private MenuItem menuItemDisconnect;
    
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		instance = this;
		mcontext = this;
		previousSettings = (Map<String, String>) Statics.getAllPreviousInput(this);
		bluetoothManager = new SuotaManager(this);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.device_container);
		viewTitles = getResources().getStringArray(R.array.app_device_titles);

		this.bluetoothGattReceiver = new BluetoothGattReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				super.onReceive(context, intent);
				bluetoothManager.processStep(intent);
				Log.d(TAG, "bluetoothGattReceiver ");
			}
		};

		this.progressUpdateReceiver = new BluetoothGattReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				super.onReceive(context, intent);
				int progress = intent.getIntExtra("progess", 0);
				progressBar.setProgress(progress);
				Log.d(TAG, "progressUpdateReceiver ");
			}
		};

		this.connectionStateReceiver = new BluetoothGattReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				super.onReceive(context, intent);
				int connectionState = intent.getIntExtra("state", 0);
				DeviceActivity.this.connectionStateChanged(connectionState);
				Log.d(TAG, "connectionStateReceiver ");
			}
		};
		registerReceiver(
				this.bluetoothGattReceiver,
				new IntentFilter(Statics.BLUETOOTH_GATT_UPDATE));

		registerReceiver(
				this.progressUpdateReceiver,
				new IntentFilter(Statics.PROGRESS_UPDATE));

		registerReceiver(
				this.connectionStateReceiver,
				new IntentFilter(Statics.CONNECTION_STATE_UPDATE));
		//BluetoothDevice device1= getIntent().getExtras().get("device");
		Set<BluetoothDevice> mBondedDevices = null;
		BluetoothDevice device000;
		final String RemoteName = "RemoteB008";
		mBondedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        if ((mBondedDevices != null) && (mBondedDevices.size() > 0)) {
            Log.i(TAG,"Got bonded devices "+mBondedDevices.size());
            for (BluetoothDevice device0 : mBondedDevices) {     
            	Log.d(TAG, "startDeviceScan:device name: "+device0.getName()+ 
            		"  device  Address: "+device0.getAddress());
            	if (device0.getName().equals(RemoteName)) {
            		rBluetoothDevice = device0;
            	}
            }
        }
        BluetoothDevice device= rBluetoothDevice;
		bluetoothManager.setDevice(device);   
		/*
		connectTask = new DeviceConnectTask(DeviceActivity.this,bluetoothManager.getDevice()) {
			@Override
			protected void onProgressUpdate(BluetoothGatt... gatt) {
				BluetoothGattSingleton.setGatt(gatt[0]);
			}
		};
		connectTask.execute();
		*/
		connectTask = new DeviceConnectTask(this, device);
		Callback callback = new Callback(connectTask);
		BluetoothGatt gatt = device.connectGatt(this, false, callback);	
		gatt.connect();
	
		dialog = new ProgressDialog(this);
		dialog.setMessage("Connecting, please wait...");
		dialog.setCancelable(false);
		dialog.show();

		inflater = LayoutInflater.from(this);
		deviceContainer = (ViewFlipper) findViewById(R.id.deviceLayoutContainer);
		deviceMain = inflater.inflate(R.layout.device_main, deviceContainer, true);
		deviceFileListView = inflater.inflate(R.layout.device_file_list, deviceContainer, true);
		deviceParameterSettings = inflater.inflate(R.layout.device_parameter_settings, deviceContainer, true);
		progressLayout = inflater.inflate(R.layout.progress, deviceContainer, true);
		switchView(0);
		progressText = (TextView) progressLayout.findViewById(R.id.progress_text);
		progressBar = (ProgressBar) progressLayout.findViewById(R.id.progress_bar);
		logWindow = (EditText) progressLayout.findViewById(R.id.logWindow);
		progressBar.setProgress(0);
		progressBar.setMax(100);

	}

	public static DeviceActivity getInstance() {
		return DeviceActivity.instance;
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "ondestroy");
		bluetoothManager.disconnect();
		try {
			unregisterReceiver(this.bluetoothGattReceiver);
			unregisterReceiver(this.progressUpdateReceiver);
			unregisterReceiver(this.connectionStateReceiver);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		if (this.deviceContainer.getDisplayedChild() >= 1) {
			switchView(this.deviceContainer.getDisplayedChild() - 1);
		} else {
			bluetoothManager.disconnect();
			super.onBackPressed();
		}
	}

	public void initMainScreen() {
		Log.d(TAG, "initMainScreen");
		mainItemsView = (LinearLayout) deviceMain.findViewById(R.id.mainItemsList);
		manufacturerItem = (RelativeLayout) inflater.inflate(R.layout.device_info_item, null);
		modelNumberItem = (RelativeLayout) inflater.inflate(R.layout.device_info_item, null);
		firmWareVersionItem = (RelativeLayout) inflater.inflate(R.layout.device_info_item, null);
		softwareRevisionItem = (RelativeLayout) inflater.inflate(R.layout.device_info_item, null);

		TextView itemName = (TextView) manufacturerItem.findViewById(R.id.itemName);
		itemName.setText("Battery level");
		itemName = (TextView) modelNumberItem.findViewById(R.id.itemName);
		itemName.setText("Model number");
		itemName = (TextView) firmWareVersionItem.findViewById(R.id.itemName);
		itemName.setText("Firmware revision");
		itemName = (TextView) softwareRevisionItem.findViewById(R.id.itemName);
		itemName.setText("Software revision");

		if(mainItemsView.getChildCount() == 0) {
			mainItemsView.addView(manufacturerItem);
			mainItemsView.addView(modelNumberItem);
			mainItemsView.addView(firmWareVersionItem);
			mainItemsView.addView(softwareRevisionItem);
		}
		initMainScreenItems();

		updateDevice = (Button) deviceMain.findViewById(R.id.updateButton);
		patchDevice = (Button) deviceMain.findViewById(R.id.patchButton);
		updateDevice.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				BluetoothDevice device = bluetoothManager.getDevice();
				bluetoothManager = new SuotaManager(DeviceActivity.this);
				bluetoothManager.setDevice(device);
				initFileList();
				switchView(1);
			}
		});
		patchDevice.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO: Dirty fix to switch to the SpotaManager
				BluetoothDevice device = bluetoothManager.getDevice();
				bluetoothManager = new SpotaManager(DeviceActivity.this);
				bluetoothManager.setDevice(device);
				initFileList();
				switchView(1);
			}
		});

		if (this.dialog.isShowing()) {
			this.dialog.dismiss();
		}

	}

	private void initMainScreenItems() {
		Log.d(TAG, "initMainScreenItems");
		List<BluetoothGattService> services = BluetoothGattSingleton.getGatt().getServices();
		for (BluetoothGattService service : services) {
			List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
			for (BluetoothGattCharacteristic characteristic : characteristics) {
				Log.d(TAG, characteristic.getUuid().toString());
				if (characteristic.getUuid().equals(Statics.ORG_BLUETOOTH_CHARACTERISTIC_BATTERY_LEVEL_STRING)) {
					bluetoothManager.characteristicsQueue.add(characteristic);
				} else if (characteristic.getUuid().equals(Statics.ORG_BLUETOOTH_CHARACTERISTIC_MODEL_NUMBER_STRING)) {
					bluetoothManager.characteristicsQueue.add(characteristic);
				} else if (characteristic.getUuid().equals(Statics.ORG_BLUETOOTH_CHARACTERISTIC_FIRMWARE_REVISION_STRING)) {
					bluetoothManager.characteristicsQueue.add(characteristic);
				} else if (characteristic.getUuid().equals(Statics.ORG_BLUETOOTH_CHARACTERISTIC_SOFTWARE_REVISION_STRING)) {
					bluetoothManager.characteristicsQueue.add(characteristic);
				} else if (characteristic.getUuid().equals(Statics.SPOTA_MEM_INFO_UUID)) {
					BluetoothGattSingleton.setSpotaMemInfoCharacteristic(characteristic);
				}
			}
		}
		bluetoothManager.readNextCharacteristic();
	}

	public void setItemValue(int index, String value) {
		if (index >= 0) {
			Log.d("test", value);
			RelativeLayout item = (RelativeLayout) mainItemsView.getChildAt(index);
			TextView itemValue = (TextView) item.findViewById(R.id.itemValue);
			itemValue.setText(value);
		}
	}

	private void initFileList() {
		fileListView = (ListView) deviceFileListView.findViewById(R.id.file_list);

		mArrayAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1);
		
		filesMap = list();
		Iterator it = filesMap.entrySet().iterator();
		fileListView.setAdapter(mArrayAdapter);
		fileListView.setOnItemClickListener(this);
		while (it.hasNext()) {
			Map.Entry filesMap = (Map.Entry) it.next();
			Log.d("File", filesMap.getKey().toString());
			filesList.add(filesMap.getKey());
			mArrayAdapter.add((String) filesMap.getValue());
		}
	}
	public  Map list() {
		Map map = new HashMap<Integer, String>();
		String string1 = getResources().getString(R.string.latest_version)
				+" "+getResources().getString(R.string.firmware_version);
		String string2 = getResources().getString(R.string.default_version);
		map.put(string2,string2);
		map.put(string1,string1);
		return map;
	}
	private void initParameterSettings() {
		int gpioValuesId = R.array.gpio_values;

		memoryTypeSystemRam = (RadioButton) deviceParameterSettings.findViewById(R.id.memoryTypeSystemRam);
		memoryTypeSystemRam.setOnClickListener(this);
		memoryTypeRetentionRam = (RadioButton) deviceParameterSettings.findViewById(R.id.memoryTypeRetentionRam);
		memoryTypeRetentionRam.setOnClickListener(this);
		memoryTypeSPI = (RadioButton) deviceParameterSettings.findViewById(R.id.memoryTypeSPI);
		memoryTypeSPI.setOnClickListener(this);
		memoryTypeI2C = (RadioButton) deviceParameterSettings.findViewById(R.id.memoryTypeI2C);
		memoryTypeI2C.setOnClickListener(this);

		closeButton = (Button) deviceParameterSettings.findViewById(R.id.buttonClose);
		closeButton.setOnClickListener(this);

		// SUOTA ONLY
		imageBankContainer = (LinearLayout) deviceParameterSettings.findViewById(R.id.imageBankContainer);
		blockSizeContainer = (LinearLayout) deviceParameterSettings.findViewById(R.id.blockSizeContainer);
		blockSize = (EditText) deviceParameterSettings.findViewById(R.id.blockSize);

		// SPOTA ONLY
		patchBaseAddressContainer = (LinearLayout) deviceParameterSettings.findViewById(R.id.patchBaseAddressContainer);

		if (bluetoothManager.type == SpotaManager.TYPE) {
			patchBaseAddressContainer.setVisibility(View.VISIBLE);
			imageBankContainer.setVisibility(View.GONE);
			memoryTypeSystemRam.setVisibility(View.VISIBLE);
			memoryTypeRetentionRam.setVisibility(View.VISIBLE);
			blockSizeContainer.setVisibility(View.GONE);
		} else if (bluetoothManager.type == SuotaManager.TYPE) {
			patchBaseAddressContainer.setVisibility(View.GONE);
			imageBankContainer.setVisibility(View.VISIBLE);
			memoryTypeSystemRam.setVisibility(View.GONE);
			memoryTypeRetentionRam.setVisibility(View.GONE);
			blockSizeContainer.setVisibility(View.VISIBLE);
			String previousText = previousSettings.get(String.valueOf(R.id.blockSize));
			if(previousText == null || previousText.equals("")) {
				previousText = Statics.DEFAULT_BLOCK_SIZE_VALUE;
			}
			blockSize.setText(previousText);
		}

		// Different views for memory types
		parameterI2cView = deviceParameterSettings.findViewById(R.id.pI2cContainer);
		parameterSpiView = deviceParameterSettings.findViewById(R.id.pSpiContainer);

		// SPOTA patch base address
		patchBaseAddress = (EditText) deviceParameterSettings.findViewById(R.id.patchBaseAddress);
		patchBaseAddress.setText(previousSettings.get(String.valueOf(R.id.patchBaseAddress)));

		// SUOTA image bank
		imageBankSpinner = (Spinner) deviceParameterSettings.findViewById(R.id.imageBank);
		ArrayAdapter<CharSequence> imageBankAdapter = ArrayAdapter.createFromResource(this,
				R.array.image_bank_addresses, android.R.layout.simple_spinner_item);
		imageBankAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		imageBankSpinner.setAdapter(imageBankAdapter);
		imageBankSpinner.setOnItemSelectedListener(this);
		int position = imageBankAdapter.getPosition(previousSettings.get(String.valueOf(R.id.imageBank)));
		imageBankSpinner.setSelection(position);

		// I2C Device address
		I2CDeviceAddress = (EditText) deviceParameterSettings.findViewById(R.id.I2CDeviceAddress);
		I2CDeviceAddress.setText(previousSettings.get(String.valueOf(R.id.I2CDeviceAddress)));

		// Spinners for 12C
		sclGpioSpinner = (Spinner) deviceParameterSettings.findViewById(R.id.sclGpioSpinner);
		sdaGpioSpinner = (Spinner) deviceParameterSettings.findViewById(R.id.sdaGpioSpinner);

		// Spinners for SPI
		misoGpioSpinner = (Spinner) deviceParameterSettings.findViewById(R.id.misoGpioSpinner);
		mosiGpioSpinner = (Spinner) deviceParameterSettings.findViewById(R.id.mosiGpioSpinner);
		csGpioSpinner = (Spinner) deviceParameterSettings.findViewById(R.id.csGpioSpinner);
		sckGpioSpinner = (Spinner) deviceParameterSettings.findViewById(R.id.sckGpioSpinner);

		ArrayAdapter<CharSequence> gpioAdapter = ArrayAdapter.createFromResource(this,
				gpioValuesId, android.R.layout.simple_spinner_item);
		gpioAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		sclGpioSpinner.setAdapter(gpioAdapter);
		sclGpioSpinner.setOnItemSelectedListener(this);
		position = gpioAdapter.getPosition(previousSettings.get(String.valueOf(R.id.sclGpioSpinner)));
		sclGpioSpinner.setSelection(position);

		sdaGpioSpinner.setAdapter(gpioAdapter);
		sdaGpioSpinner.setOnItemSelectedListener(this);
		position = gpioAdapter.getPosition(previousSettings.get(String.valueOf(R.id.sdaGpioSpinner)));
		sdaGpioSpinner.setSelection(position);

		misoGpioSpinner.setAdapter(gpioAdapter);
		misoGpioSpinner.setOnItemSelectedListener(this);
		position = gpioAdapter.getPosition(previousSettings.get(String.valueOf(R.id.misoGpioSpinner)));
		if (position <= 0) {
			position = Statics.DEFAULT_MISO_VALUE;
		}
		Log.d("position", "MISO: " + position);
		misoGpioSpinner.setSelection(position);

		mosiGpioSpinner.setAdapter(gpioAdapter);
		mosiGpioSpinner.setOnItemSelectedListener(this);
		position = gpioAdapter.getPosition(previousSettings.get(String.valueOf(R.id.mosiGpioSpinner)));
		if (position <= 0) {
			position = Statics.DEFAULT_MOSI_VALUE;
		}
		Log.d("position", "MOSI: " + position);
		mosiGpioSpinner.setSelection(position);

		csGpioSpinner.setAdapter(gpioAdapter);
		csGpioSpinner.setOnItemSelectedListener(this);
		position = gpioAdapter.getPosition(previousSettings.get(String.valueOf(R.id.csGpioSpinner)));
		if (position <= 0) {
			position = Statics.DEFAULT_CS_VALUE;
		}
		Log.d("position", "CS: " + position);
		csGpioSpinner.setSelection(position);

		sckGpioSpinner.setAdapter(gpioAdapter);
		sckGpioSpinner.setOnItemSelectedListener(this);
		position = gpioAdapter.getPosition(previousSettings.get(String.valueOf(R.id.sckGpioSpinner)));
		if (position <= 0) {
			position = Statics.DEFAULT_SCK_VALUE;
		}
		Log.d("position", "SCK: " + position);
		sckGpioSpinner.setSelection(position);

		sendToDeviceButton = (Button) deviceParameterSettings.findViewById(R.id.sendToDeviceButton);
		sendToDeviceButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startUpdate();
			}
		});
		// Set default memory type to SPI
		setMemoryType(Statics.MEMORY_TYPE_SPI);
	}

	private void startUpdate() {
		Intent intent = new Intent();

		if (bluetoothManager.type == SpotaManager.TYPE) {
			int patchBaseAddressValue = Integer.decode(patchBaseAddress.getText().toString());
			Statics.setPreviousInput(this, R.id.patchBaseAddress, String.format("%#10x", patchBaseAddressValue));
			bluetoothManager.setPatchBaseAddress(patchBaseAddressValue);
		} else if (bluetoothManager.type == SuotaManager.TYPE) {
			Statics.setPreviousInput(this, R.id.blockSize, blockSize.getText().toString());
		}

		if (memoryType == Statics.MEMORY_TYPE_I2C) {
			int I2CDeviceAddressValue = Integer.decode(I2CDeviceAddress.getText().toString());
			Statics.setPreviousInput(this, R.id.I2CDeviceAddress, String.format("%#10x", I2CDeviceAddressValue));
			bluetoothManager.setI2CDeviceAddress(I2CDeviceAddressValue);
		}

		// Set default block size to 1 for SPOTA, this will not be used in this case
		int fileBlockSize = 1;
		if (bluetoothManager.type == SuotaManager.TYPE) {
			fileBlockSize = Integer.parseInt(blockSize.getText().toString());
		}
		bluetoothManager.getFile().setFileBlockSize(fileBlockSize);

		intent.setAction(Statics.BLUETOOTH_GATT_UPDATE);
		intent.putExtra("step", 1);
		sendBroadcast(intent);
		switchView(3);
	}

	private void setMemoryType(int memoryType) {
		this.clearMemoryTypeChecked();
		this.memoryType = memoryType;
		bluetoothManager.setMemoryType(memoryType);
		parameterI2cView.setVisibility(View.GONE);
		parameterSpiView.setVisibility(View.GONE);
		switch (memoryType) {
			case Statics.MEMORY_TYPE_SYSTEM_RAM:
				patchBaseAddressContainer.setVisibility(View.GONE);
				memoryTypeSystemRam.setChecked(true);
				break;
			case Statics.MEMORY_TYPE_RETENTION_RAM:
				patchBaseAddressContainer.setVisibility(View.GONE);
				memoryTypeRetentionRam.setChecked(true);
				break;
			case Statics.MEMORY_TYPE_SPI:
				if (bluetoothManager.type == SpotaManager.TYPE) {
					patchBaseAddressContainer.setVisibility(View.VISIBLE);
				}
				parameterSpiView.setVisibility(View.VISIBLE);
				memoryTypeSPI.setChecked(true);
				break;

			case Statics.MEMORY_TYPE_I2C:
				if (bluetoothManager.type == SpotaManager.TYPE) {
					patchBaseAddressContainer.setVisibility(View.VISIBLE);
				}
				parameterI2cView.setVisibility(View.VISIBLE);
				memoryTypeI2C.setChecked(true);
				break;
		}
	}

	private void switchView(int viewIndex) {
		this.deviceContainer.setDisplayedChild(viewIndex);
		setTitle(viewTitles[viewIndex]);
		if (viewIndex == 0) {
			setTitle(bluetoothManager.getDevice().getName());
		}
	}

	private void clearMemoryTypeChecked() {
		memoryTypeSystemRam.setChecked(false);
		memoryTypeRetentionRam.setChecked(false);
		memoryTypeI2C.setChecked(false);
		memoryTypeSPI.setChecked(false);
	}

	public void enableCloseButton() {
		closeButton.setVisibility(View.VISIBLE);
	}

	private void connectionStateChanged(int connectionState) {
		if(connectionState == BluetoothProfile.STATE_DISCONNECTED) {
			Toast.makeText(DeviceActivity.this, this.bluetoothManager.getDevice().getName() + " disconnected.", Toast.LENGTH_LONG).show();
			BluetoothGattSingleton.getGatt().close();
			if(!bluetoothManager.isFinished()) {
				finish();
			}
		}
	}

	public void logMemInfoValue(int memInfoValue) {
		String message = "Patch Memory Info:\n";
		int numberOfPatches = (memInfoValue >> 16) & 0xff;
		int numberOfBytes = memInfoValue & 0xff;
		int sizeOfPatches = (int) Math.ceil((double)numberOfBytes / (double)4);
		message += "\tNumber of patches = " + numberOfPatches + "\n" +
				"\tSize of patches = " + sizeOfPatches + " words (" + numberOfBytes + " bytes)";
		this.log(message);
	}

	public void log(String message) {
		this.logWindow.getText().append(message + "\n");
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
							long id) {
		// Get the resourceID necessary for retrieving the file
		String filename = filesList.get(position).toString();
		bluetoothManager.setFileName(filename);
		Log.d(TAG, "Clicked: " + filename);
		try {
			//bluetoothManager.setFile(File.getByFileName(filename));			
			bluetoothManager.setFile(getByFileName(filename));
			initParameterSettings();
			switchView(2);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public FileData getByFileName(String filename) throws IOException {
		// Get the file and store it in fileStream
		InputStream is = null;
		
		if (filename.endsWith(getResources().getString(R.string.firmware_version))) {
			is = this.getResources().openRawResource(R.raw.amlogic012);
		}
		else {
			is = this.getResources().openRawResource(R.raw.stableversion);
		}
		return new FileData(is);
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.memoryTypeSystemRam:
				setMemoryType(Statics.MEMORY_TYPE_SYSTEM_RAM);
				break;
			case R.id.memoryTypeRetentionRam:
				setMemoryType(Statics.MEMORY_TYPE_RETENTION_RAM);
				break;
			case R.id.memoryTypeSPI:
				setMemoryType(Statics.MEMORY_TYPE_SPI);
				break;
			case R.id.memoryTypeI2C:
				setMemoryType(Statics.MEMORY_TYPE_I2C);
				break;
			case R.id.buttonClose:
				finish();
				break;
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		String stringValue = parent.getSelectedItem().toString();
		int value = Statics.gpioStringToInt(stringValue);
		Statics.setPreviousInput(this, parent.getId(), stringValue);
		switch (parent.getId()) {
			// SPI
			case R.id.misoGpioSpinner:
				bluetoothManager.setMISO_GPIO(value);
				break;
			case R.id.mosiGpioSpinner:
				bluetoothManager.setMOSI_GPIO(value);
				break;
			case R.id.csGpioSpinner:
				bluetoothManager.setCS_GPIO(value);
				break;
			case R.id.sckGpioSpinner:
				bluetoothManager.setSCK_GPIO(value);
				break;

			// I2C
			case R.id.sclGpioSpinner:
				bluetoothManager.setSCL_GPIO(value);
				break;
			case R.id.sdaGpioSpinner:
				bluetoothManager.setSDA_GPIO(value);
				break;

			// SUOTA
			case R.id.imageBank:
				bluetoothManager.setImageBank(value);
				break;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean status = super.onCreateOptionsMenu(menu);
		menuItemDisconnect = menu.findItem(R.id.disconnect);
		menuItemDisconnect.setVisible(true);
		menuItemDisconnect.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				bluetoothManager.disconnect();
				finish();
				return false;
			}
		});
		return status;
	}
}