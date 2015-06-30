//
//  Copyright (c) 2014 Texas Instruments. All rights reserved.
//

package com.pandaos.smartconfig;

import net.sourceforge.zbar.Symbol;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.LightingColorFilter;
import android.support.v4.app.Fragment;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;
import com.integrity_project.smartconfiglib.SmartConfig;
import com.integrity_project.smartconfiglib.SmartConfigListener;
import com.pandaos.smartconfig.utils.MDnsCallbackInterface;
import com.pandaos.smartconfig.utils.MDnsHelper;
import com.pandaos.smartconfig.utils.NetworkUtil;
import com.pandaos.smartconfig.utils.SharedPreferencesInterface_;
import com.pandaos.smartconfig.utils.SmartConfigConstants;
import com.pandaos.smartconfig.utils.ZBarConstants;

@EFragment(R.layout.tab_smartconfig_view)
public class SmartConfigFragment extends Fragment {
	
	int runTime;
	boolean isPasswordShown = false;
	boolean isStartClicked = false;
	boolean waitForScanFinish = false;
	boolean foundNewDevice = false;
	
	@Bean
	MDnsHelper mDnsHelper;
	
	MDnsCallbackInterface mDnsCallback;
	JSONArray devicesArray;
	JSONArray recentDevicesArray;
	
	byte[] freeData;
	SmartConfig smartConfig;
	SmartConfigListener smartConfigListener;
	
	@Pref
	SharedPreferencesInterface_ prefs;
	
	@ViewById
	TextView title_smartconfig;
	
	@ViewById
	EditText smartconfig_network_name_field;
	
	@ViewById
	EditText smartconfig_network_pass_field;
	
	@ViewById
	EditText smartconfig_device_name_field;
	
	@ViewById
	EditText smartconfig_key_field;
	
	@ViewById
	ImageView smartconfig_network_pass_eye;
	
	@ViewById
	RelativeLayout tab_smartconfig_view;
	
	@ViewById
	RelativeLayout smartconfig_device_name_layout;
	
	@ViewById
	RelativeLayout smartconfig_device_name_explanation_layout;
	
	@ViewById
	RelativeLayout smartconfig_key_layout;
	
	@ViewById
	ImageView smartconfig_key_button;
	
	@ViewById
	TextView smartconfig_start;
	
	@ViewById
	ProgressBar smartconfig_progressbar;
	
	BroadcastReceiver networkChangeReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			smartconfig_network_name_field.setText(NetworkUtil.getWifiName(getActivity()));
		}
	};
	
	BroadcastReceiver scanFinishedReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (waitForScanFinish || prefs.isSmartConfigActive().get()) {
				waitForScanFinish = false;
				lookForNewDevice();
			}
		}
	};
	
	@AfterViews
	void afterViews() {
		smartconfig_progressbar.getProgressDrawable().setColorFilter(new LightingColorFilter(0xFFCC0000, 0x00000000)); //color the progress bar TI red
	}
	
	@Override
	public void onResume() {
		super.onResume();
		getActivity().registerReceiver(networkChangeReceiver, new IntentFilter(SmartConfigConstants.NETWORK_CHANGE_BROADCAST_ACTION));
		getActivity().registerReceiver(scanFinishedReceiver, new IntentFilter(SmartConfigConstants.SCAN_FINISHED_BROADCAST_ACTION));
		if (prefs.showDeviceName().get()) {
			smartconfig_device_name_layout.setVisibility(View.VISIBLE);
			smartconfig_device_name_explanation_layout.setVisibility(View.VISIBLE);
		} else {
			smartconfig_device_name_layout.setVisibility(View.INVISIBLE);
			smartconfig_device_name_explanation_layout.setVisibility(View.INVISIBLE);
		}
		if (prefs.showSmartConfigPass().get()) {
			smartconfig_key_layout.setVisibility(View.VISIBLE);
		} else {
			smartconfig_key_layout.setVisibility(View.INVISIBLE);
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
		getActivity().unregisterReceiver(networkChangeReceiver);
		getActivity().unregisterReceiver(scanFinishedReceiver);
	}
	
	@Click
	void tab_smartconfig_view() { // hide the soft keyboard when touching anything that's not an EditText field.
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
	}

	@Click
	void smartconfig_start() {
		if (isStartClicked) {
			foundNewDevice = true;
			runTime = SmartConfigConstants.SC_RUNTIME; // stop SmartConfig and reset the progress bar
		} else {
			int networkState = NetworkUtil.getConnectionStatus(getActivity());
			if (networkState != NetworkUtil.WIFI) { //if user isn't connected to wifi
				((MainActivity)getActivity()).showWifiDialog(getActivity());
			} else {
				// show "no password" dialog if user didn't enter a password
				if (smartconfig_network_pass_field.getText().toString().equals("")) {
					showPasswordDialog();
				} else {
					startSmartConfig();
				}
			}
		}
	}
	
	@Click
	void smartconfig_network_pass_eye() { // toggle if the password it shown or hidden
		isPasswordShown = !isPasswordShown;
		if (isPasswordShown) {
			smartconfig_network_pass_field.setTransformationMethod(null);
//			smartconfig_key_field.setTransformationMethod(null);
		} else {
			smartconfig_network_pass_field.setTransformationMethod(new PasswordTransformationMethod());
//			smartconfig_key_field.setTransformationMethod(new PasswordTransformationMethod());
		}
	}
	
	@Click
	void smartconfig_key_button() { // start QR scanner
		Intent intent = new Intent(getActivity(), QRScannerActivity_.class);
		intent.putExtra(ZBarConstants.SCAN_MODES, new int[]{Symbol.QRCODE});
		startActivityForResult(intent, ZBarConstants.ZBAR_SCANNER_REQUEST);
	}
	
	@OnActivityResult(ZBarConstants.ZBAR_SCANNER_REQUEST)
	void onResult(int resultCode, Intent returnIntent) { // returned from QR scanner activity 
		try {
			if (resultCode == Activity.RESULT_OK) { // scan succeeded
				JSONObject scannedDevice = new JSONObject(returnIntent.getStringExtra(ZBarConstants.SCAN_RESULT));
				smartconfig_device_name_field.setText(scannedDevice.getString("name"));
				smartconfig_key_field.setText(scannedDevice.getJSONObject("keys").getString("0"));
			} else if (resultCode == Activity.RESULT_CANCELED) { // we didn't scan for some reason
				Toast.makeText(getActivity(), "Camera unavailable or scan canceled by user", Toast.LENGTH_SHORT).show();
			}
		}catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	@Background
	void runProgressBar() {
		isStartClicked = true;
		runTime = 0;
		hideTabs();
		try {
			while (runTime < SmartConfigConstants.SC_RUNTIME) {
				if ((runTime > 0) && ((runTime % SmartConfigConstants.SC_MDNS_INTERVAL) == 0)) {
					System.out.println("Pausing MDNS...");
					pauseMDNS();
				}
				Thread.sleep(SmartConfigConstants.SC_PROGRESSBAR_INTERVAL);
				runTime += SmartConfigConstants.SC_PROGRESSBAR_INTERVAL;
				updateProgressBar(runTime);
			}
		} catch (InterruptedException e) {
		} finally {
			isStartClicked = false;
			waitForScanFinish = false;
			if (!foundNewDevice) {
				notifyNotFoundNewDevice(); // haven't found new device
			}
			resetProgressBar();
			stopSmartConfig();
		}
	}
	
	@UiThread
	void updateProgressBar(int runTime) {
		if (smartconfig_progressbar != null)
			smartconfig_progressbar.setProgress(smartconfig_progressbar.getMax() * runTime / SmartConfigConstants.SC_RUNTIME);
	}
	
	@UiThread
	void resetProgressBar() {
		updateProgressBar(0);
		smartconfig_start.setText(R.string.smartconfig_start);
		smartconfig_progressbar.setVisibility(View.INVISIBLE);
		((MainActivity)getActivity()).initTabs(0);
	}
	
	@UiThread
	void hideTabs() {
		smartconfig_start.setText(R.string.smartconfig_cancel);
		smartconfig_progressbar.setVisibility(View.VISIBLE);
		((MainActivity)getActivity()).tabhost.clearAllTabs();
	}
	
	public void showPasswordDialog() {
		AlertDialog noPasswordDialog = new AlertDialog.Builder(getActivity()). //create a dialog
				setTitle("No Password Entered").
				setMessage("You did not enter a password. Would you like to continue?").
				setPositiveButton("Yes", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) { //the user clicked yes
						startSmartConfig();
					}
				}).
				setNegativeButton("No", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// the user clicked no. do nothing...
					}
				}).setCancelable(false).
				create();
		noPasswordDialog.show(); //show the dialog
	}
	
	public void startSmartConfig() {
		runProgressBar();
		foundNewDevice = false;
		String passwordKey = smartconfig_network_pass_field.getText().toString().trim();
		byte[] paddedEncryptionKey;
		String SSID = smartconfig_network_name_field.getText().toString().trim();
		String gateway = NetworkUtil.getGateway(getActivity());
		if (smartconfig_key_field.getText().length() > 0) {
			paddedEncryptionKey = (smartconfig_key_field.getText().toString() + SmartConfigConstants.ZERO_PADDING_16).substring(0, 16).trim().getBytes();
		} else {
			paddedEncryptionKey = null;
		}
		if (smartconfig_device_name_field.getText().length() > 0) { // device name isn't empty
			byte[] freeDataChars = new byte[smartconfig_device_name_field.getText().length() + 2];
			freeDataChars[0] = 0x03;
			freeDataChars[1] = (byte) smartconfig_device_name_field.getText().length();
			for (int i=0; i<smartconfig_device_name_field.getText().length(); i++) {
				freeDataChars[i+2] = (byte) smartconfig_device_name_field.getText().charAt(i);
			}
			freeData = freeDataChars;
		} else {
			freeData = new byte[1];
			freeData[0] = 0x03;
		}
		smartConfig = null;
		smartConfigListener = new SmartConfigListener() {
			@Override
			public void onSmartConfigEvent(SmtCfgEvent event, Exception e) {}
		};
		try {
			smartConfig = new SmartConfig(smartConfigListener, freeData, passwordKey, paddedEncryptionKey, gateway, SSID, (byte) 0, "");
			smartConfig.transmitSettings();
			lookForNewDevice();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void stopSmartConfig() {
		try {
			smartConfig.stopTransmitting();
			runTime = SmartConfigConstants.SC_RUNTIME;
			mDnsHelper.stopDiscovery();
			Thread.sleep(SmartConfigConstants.JMDNS_CLOSE_TIME);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			prefs.isScanning().put(false);
			prefs.isSmartConfigActive().put(false);
			Intent intent = new Intent();
			intent.setAction(SmartConfigConstants.SCAN_FINISHED_BROADCAST_ACTION);
			getActivity().sendBroadcast(intent);
		}
	}
	
	@Background
	public void pauseMDNS() {
		try {
			mDnsHelper.stopDiscovery();
			Thread.sleep(SmartConfigConstants.JMDNS_CLOSE_TIME);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			prefs.isScanning().put(false);
			Intent intent = new Intent();
			intent.setAction(SmartConfigConstants.SCAN_FINISHED_BROADCAST_ACTION);
			getActivity().sendBroadcast(intent);
		}
	}
	
	@Background
	void lookForNewDevice() {
		try {
			devicesArray = new JSONArray(prefs.devicesArray().get()); //save whatever devices we found so far
			recentDevicesArray = new JSONArray(prefs.recentDevicesArray().get());
			if (prefs.isScanning().get()) { // if main activity is still scanning
				((MainActivity)getActivity()).stopScanning();
				waitForScanFinish = true; // flag to indicate we are waiting for the main activity's scan to finish
				System.out.println("stopping scan on the main activity...");
			} else if (!waitForScanFinish) { // main activity is done scanning and we're not waiting for scan finish
				prefs.isScanning().put(true);
				prefs.isSmartConfigActive().put(true);
				mDnsCallback = new MDnsCallbackInterface() {

					@Override
					public void onDeviceResolved(JSONObject deviceJSON) {
						if (isNewDevice(deviceJSON)) { // if this is a device we haven't already discovered
							foundNewDevice = true;
							recentDevicesArray.put(deviceJSON);
							prefs.recentDevicesArray().put(recentDevicesArray.toString());// add the device to the new devices list and array
							notifyFoundNewDevice(); // notify the user
						}
					}
				};
				mDnsHelper.init(getActivity(), mDnsCallback);
				mDnsHelper.startDiscovery();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public boolean isNewDevice(JSONObject deviceJSON) {
		try {
			for (int i=0; i<devicesArray.length(); i++) {
				if (devicesArray.getJSONObject(i).getString("host").equals(deviceJSON.getString("host"))) {
					if (devicesArray.getJSONObject(i).getString("name").equals(deviceJSON.getString("name"))){
						return false;
					} else {
						devicesArray = removeFromJSONArray(devicesArray, i);
						prefs.devicesArray().put(devicesArray.toString());
						return true;
					}
				}
			}
			for (int i=0; i<recentDevicesArray.length(); i++) {
				if (recentDevicesArray.getJSONObject(i).getString("host").equals(deviceJSON.getString("host"))) {
					if (recentDevicesArray.getJSONObject(i).getString("name").equals(deviceJSON.getString("name"))){
						return false;
					} else {
						recentDevicesArray = removeFromJSONArray(recentDevicesArray, i);
						prefs.recentDevicesArray().put(recentDevicesArray.toString());
						return true;
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	@UiThread
	public void notifyFoundNewDevice() {
		runTime = SmartConfigConstants.SC_RUNTIME; // stop progressbar and smartconfig
		AlertDialog foundNewDeviceDialog = new AlertDialog.Builder(getActivity()). //create a dialog
				setTitle("New Device Found!").
				setMessage("New device found! You can view it at the \"Devices\" tab").
				setPositiveButton("OK", new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {}
				}).setCancelable(false).create();
		foundNewDeviceDialog.show(); //show the dialog to notify the user
	}
	
	@UiThread
	public void notifyNotFoundNewDevice() {
		AlertDialog wifiDialog = new AlertDialog.Builder(getActivity()). //create a dialog
				setTitle("No New Device Found!").
				setMessage("No new device has been found. What would you like to do?").
				setPositiveButton("Start AP Mode", new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						((MainActivity)getActivity()).startAPMode();
					}
				}).
				setNegativeButton("Nothing", new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
					}
				}).setCancelable(false).create();
		wifiDialog.show(); //show the dialog
	}
	
	public JSONArray removeFromJSONArray(JSONArray array, int index) {
		JSONArray result = new JSONArray();
		try {
		for (int i=0; i<array.length(); i++) {
			if (i != index) {
				result.put(array.getJSONObject(i));
			}
		}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}
	
}