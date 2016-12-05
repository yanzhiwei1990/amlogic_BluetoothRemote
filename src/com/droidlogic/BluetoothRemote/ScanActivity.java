package com.droidlogic.BluetoothRemote;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

//import com.crashlytics.android.Crashlytics;
import com.droidlogic.BluetoothRemote.data.FileData;
import com.droidlogic.BluetoothRemote.data.Statics;
import com.droidlogic.BluetoothRemote.data.Uuid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

//import io.fabric.sdk.android.Fabric;

public class ScanActivity extends SuotaActivity implements OnItemClickListener {
    private final static String TAG = "ScanActivity";
    private final static int REQUEST_ENABLE_BT = 1;
    private static final String RemoteName = "RemoteB008";
    private boolean isScanning = false;

    private BluetoothAdapter mBluetoothAdapter;
    private HashMap<String, BluetoothDevice> scannedDevices;

    private ArrayList<BluetoothDevice> bluetoothDeviceList;
    private ArrayAdapter<String> mArrayAdapter;

    // Layout varbiables;
    private ListView deviceListView;
//    private Menu menu;
    private MenuItem menuItemRefesh;

    private Handler handler;

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi,
                             final byte[] scanRecord) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    List<UUID> uuids = Uuid.parseFromAdvertisementData(scanRecord);
                    for (UUID uuid : uuids) {
                        // if (uuid.equals(Statics.SPOTA_SERVICE_UUID) && !scannedDevices.containsKey(device.getAddress())) {
                        if (!scannedDevices.containsKey(device.getAddress())) {
                            scannedDevices.put(device.getAddress(), device);
                            bluetoothDeviceList.add(device);
                            mArrayAdapter.add(device.getName() + "\n"
                                    + device.getAddress());
                        }
                    }
                }
            });
        }
    };

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                bluetoothDeviceList.add(device);
                // Add the name and address to an array adapter to show in a
                // ListView
                mArrayAdapter
                        .add(device.getName() + "\n" + device.getAddress());
                Log.d(TAG,
                        "Found device: " + device.getName() + " "
                                + device.getAddress());
//                String name = device.getName();
//                if (name == null || name.equals("null")) {
//                    name = "unknown";
//                }
//                String address = device.getAddress();
                // deviceArrayList.add(name + "\n" + address);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Fabric.with(this, new Crashlytics());
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_scan);
        this.initialize();
		if(!Statics.fileDirectoriesCreated(this) || true) {
			FileData.createFileDirectories(this);
			Statics.setFileDirectoriesCreated(this);
		}
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Check which request we're responding to
		if (requestCode == REQUEST_ENABLE_BT) {
			// Make sure the request was successful
			if (resultCode == Activity.RESULT_OK) {
				this.startDeviceScan();
			}
		}
	}

    @Override
    protected void onDestroy() {
        // Disable discovery on close
        mBluetoothAdapter.cancelDiscovery();
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    private void initialize() {
        // Initialize layout variables
        setTitle(getResources().getString(R.string.app_devices_title));
        deviceListView = (ListView) findViewById(R.id.device_list);
        scannedDevices = new HashMap<String, BluetoothDevice>();
        bluetoothDeviceList = new ArrayList<BluetoothDevice>();
        mArrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1);

        // Initialize Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Log.e(TAG, "Bluetooth not supported.");
            super.showAlertDialog("Error",
                    "Bluetooth is not supported on this device");
        }
        // If the bluetooth adapter is not enabled, request to enable it
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT); 
        }
        // Otherwise, start scanning right away
        else {
            this.startDeviceScan();
        }
        
        deviceListView.setAdapter(mArrayAdapter);
        deviceListView.setOnItemClickListener(this);

    }
    Set<BluetoothDevice> mBondedDevices = null;
    private void startDeviceScan() {
        isScanning = true;
        mArrayAdapter.clear();
        bluetoothDeviceList.clear();
        scannedDevices.clear();
        if(menuItemRefesh != null) {
            menuItemRefesh.setVisible(false);
        }
        Log.d(TAG, "Start scanning");
        setProgressBarIndeterminateVisibility(true);

        mBondedDevices = mBluetoothAdapter.getBondedDevices();
        if ((mBondedDevices != null) && (mBondedDevices.size() > 0))
        {
            Log.i(TAG,"Got bonded devices "+mBondedDevices.size());
            // Loop through paired devices
            for (BluetoothDevice device : mBondedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                scannedDevices.put(device.getAddress(), device);
                bluetoothDeviceList.add(device);
                mArrayAdapter.add(device.getName() + "\n"
                        + device.getAddress());
		  Log.d(TAG, "device name: "+device.getName()+ "  device  Address: "+device.getAddress());
            }
        }
        mBluetoothAdapter.startLeScan(mLeScanCallback);
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopDeviceScan();
            }
        }, 7000);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
    }

    private void stopDeviceScan() {
        if(isScanning) {
            isScanning = false;
            Log.d(TAG, "Stop scanning");
            setProgressBarIndeterminateVisibility(false);
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            if (menuItemRefesh != null) {
                menuItemRefesh.setVisible(true);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean status = super.onCreateOptionsMenu(menu);
        menuItemRefesh = menu.findItem(R.id.restart_scan);
        if(isScanning) {
            menuItemRefesh.setVisible(false);
        }
        return status;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.restart_scan) {
            this.startDeviceScan();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * On click listener for scanned devices
     *
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        stopDeviceScan();
        BluetoothDevice device = bluetoothDeviceList.get(position);
        if (RemoteName.equals(device.getName())) {
            Intent i = new Intent(ScanActivity.this, DeviceActivity.class);
            i.putExtra("device", device);
            Log.d(TAG, " device  position: "+position);
            startActivity(i);
        }
    }
}
