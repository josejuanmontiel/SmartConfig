//
//  Copyright (c) 2014 Texas Instruments. All rights reserved.
//

package com.pandaos.smartconfig;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TabWidget;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;
import com.pandaos.smartconfig.utils.MDnsCallbackInterface;
import com.pandaos.smartconfig.utils.MDnsHelper;
import com.pandaos.smartconfig.utils.NetworkUtil;
import com.pandaos.smartconfig.utils.SharedPreferencesInterface_;
import com.pandaos.smartconfig.utils.SmartConfigConstants;


@EActivity(R.layout.activity_main)
@OptionsMenu(R.menu.activity_main)
public class MainActivity extends FragmentActivity {
	
	int tabDimens; //the size of the tabs in pixels, initialized in afterViews
	
	//the tabs
	SmartConfigFragment smartConfigFragment;
	DevicesFragment devicesFragment;
	SettingsFragment settingsFragment;
	
	boolean isWifiAlertEnabled = true; //flag for enabling the wifi connection dialog
	
	@Bean
	MDnsHelper mDnsHelper;
	
	MDnsCallbackInterface mDnsCallback;
	JSONArray devicesArray;
	
	@Pref
	SharedPreferencesInterface_ prefs;
	
	@ViewById
	FragmentTabHost tabhost;
	
	@ViewById
	FrameLayout realtabcontent;
	
	@ViewById
	TabWidget tabs;
	
	BroadcastReceiver networkChangeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			int networkState = NetworkUtil.getConnectionStatus(context);
			if (networkState != NetworkUtil.WIFI && isWifiAlertEnabled){ //no wifi connection and alert dialog allowed
				isWifiAlertEnabled = false;// disable alert
				showWifiDialog(context);
			}
			if (networkState == NetworkUtil.WIFI && !isWifiAlertEnabled) // wifi connected and alert disabled
				isWifiAlertEnabled = true; //re-enable alert
		}
	};
	
	@Override
	public void onResume() {
		super.onResume();
		registerReceiver(networkChangeReceiver, new IntentFilter(SmartConfigConstants.NETWORK_CHANGE_BROADCAST_ACTION));
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@AfterViews
	void afterViews() {
		smartConfigFragment = new SmartConfigFragment_();
		devicesFragment = new DevicesFragment_();
		settingsFragment = new SettingsFragment_();
		tabDimens = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, SmartConfigConstants.TAB_DIMENS, getResources().getDisplayMetrics());
		tabhost.setup(this, getSupportFragmentManager(), realtabcontent.getId());
		initTabs(prefs.startTab().get());
		prefs.devicesArray().put("[]");
		prefs.recentDevicesArray().put("[]");
		initMDns();
		scanForDevices();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver(networkChangeReceiver);
	}
	
	@OptionsItem(R.id.menu_settings)
	void settings() {
		if (prefs.isSmartConfigActive().get()) {
			Toast.makeText(this, "Please stop SmartConfig process before leaving this tab", Toast.LENGTH_SHORT).show();
		} else {
			tabhost.setCurrentTab(2);
		}
	}

	public void initTabs(int currentTab) {
		tabhost.addTab(tabhost.newTabSpec("smartconfig").setIndicator(makeTabIndicator(getResources().getDrawable(R.drawable.tab_smartconfig_selector))),
				smartConfigFragment.getClass(), null);
		tabhost.addTab(tabhost.newTabSpec("devices").setIndicator(makeTabIndicator(getResources().getDrawable(R.drawable.tab_devices_selector))),
				devicesFragment.getClass(), null);
		tabhost.addTab(tabhost.newTabSpec("settings").setIndicator(makeTabIndicator(getResources().getDrawable(R.drawable.tab_settings_selector))),
			settingsFragment.getClass(), null);
		tabhost.setCurrentTab(currentTab);
	}

	private View makeTabIndicator (Drawable drawable) {
		ImageView tabImage = new ImageView(this);
		LayoutParams LP;
		LP = new LayoutParams(tabDimens, tabDimens, 1);
		LP.setMargins(1, 0, 1, 0);
		tabImage.setLayoutParams(LP);
		tabImage.setImageDrawable(drawable);
		tabImage.setBackgroundColor(Color.TRANSPARENT);
		return tabImage;
	}
	
	@UiThread
	public void showWifiDialog(Context context) {
		AlertDialog wifiDialog = new AlertDialog.Builder(context). //create a dialog
				setTitle("No Wifi Connection").
				setMessage("No Wifi connection detected. What would you like to do?").
				setPositiveButton("Connect to Wifi", new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) { //the user clicked yes
						activateWifi(); //activate the device's Wifi and bring up the Wifi settings screen
					}
				}).setNeutralButton("Start AP Mode", new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						startAPMode();
					}
				}).
				setNegativeButton("Nothing", new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
					}
				}).setCancelable(false).create();
		wifiDialog.show(); //show the dialog
	}
	
	public void activateWifi() {
		WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE); 
		if (!wifiManager.isWifiEnabled())
			wifiManager.setWifiEnabled(true);
		startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
	}
	
	public void startAPMode() {
		Intent intent = new Intent(this, APModeActivity_.class);
		startActivity(intent);
	}
	
	public void startPrivacyPolicy() {
		Intent intent = new Intent(this, PrivacyPolicyActivity_.class);
		startActivity(intent);
	}
	
	public void initMDns() {
		mDnsCallback = new MDnsCallbackInterface() {
			@Override
			public void onDeviceResolved(JSONObject deviceJSON) {
				devicesArray.put(deviceJSON);
				prefs.devicesArray().put(devicesArray.toString());
				Intent intent = new Intent();
				intent.setAction(SmartConfigConstants.DEVICE_FOUND_BROADCAST_ACTION);
				sendBroadcast(intent);
			}
		};
		mDnsHelper.init(this, mDnsCallback);
	}
	
	@Background
	public void scanForDevices() {
		prefs.isScanning().put(true);
		devicesArray = new JSONArray();
		try {
			mDnsHelper.startDiscovery();
			Thread.sleep(SmartConfigConstants.MAIN_SCAN_TIME);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			if (prefs.isScanning().get())
				stopScanning();
		}
	}
	
	@Background
	public void stopScanning() {
		try {
			mDnsHelper.stopDiscovery();
			Thread.sleep(SmartConfigConstants.JMDNS_CLOSE_TIME);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			Intent intent = new Intent();
			intent.setAction(SmartConfigConstants.SCAN_FINISHED_BROADCAST_ACTION);
			sendBroadcast(intent);
			prefs.isScanning().put(false);
		}
	}
}