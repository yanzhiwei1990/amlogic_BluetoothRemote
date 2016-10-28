package com.droidlogic.BluetoothRemote;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public abstract class SuotaActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		menu.findItem(R.id.restart_scan).setVisible(false);
		menu.findItem(R.id.disconnect).setVisible(false);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch (id) {
			case R.id.restart_scan:
			case R.id.disconnect:
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	protected final void showAlertDialog(String title, String message) {
		
	}
}
