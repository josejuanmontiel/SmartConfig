//
//  Copyright (c) 2014 Texas Instruments. All rights reserved.
//

package com.pandaos.smartconfig;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.ViewsById;
import org.androidannotations.annotations.sharedpreferences.Pref;
import com.pandaos.smartconfig.utils.Device;
import com.pandaos.smartconfig.utils.DeviceListAdapter;
import com.pandaos.smartconfig.utils.RecentDeviceListAdapter;
import com.pandaos.smartconfig.utils.SharedPreferencesInterface_;
import com.pandaos.smartconfig.utils.SmartConfigConstants;

@EFragment(R.layout.tab_devices_view)
public class DevicesFragment extends Fragment {
		
	@Pref
	SharedPreferencesInterface_ prefs;
		
	@ViewById
	ImageView devices_refresh_button;
	
	@ViewById
	ProgressBar devices_refresh_spinner;

	@ViewById
	ListView devices_list_listview;
	
	@ViewById
	ListView devices_recent_listview;
	
	@Bean
	DeviceListAdapter deviceListAdapter;
	
	@Bean
	RecentDeviceListAdapter recentDeviceListAdapter;
	
	BroadcastReceiver scanFinishedReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			hideRefreshProgress();
		}
	};
	
	BroadcastReceiver deviceFoundReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			updateDeviceList();
		}
	};
	
	@AfterViews
	void afterViews() {
		devices_list_listview.setAdapter(deviceListAdapter);
		devices_recent_listview.setAdapter(recentDeviceListAdapter);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (prefs.isScanning().get()) {
			showRefreshProgress();
		} else {
			hideRefreshProgress();
		}
		getActivity().registerReceiver(scanFinishedReceiver, new IntentFilter(SmartConfigConstants.SCAN_FINISHED_BROADCAST_ACTION));
		getActivity().registerReceiver(deviceFoundReceiver, new IntentFilter(SmartConfigConstants.DEVICE_FOUND_BROADCAST_ACTION));
		updateDeviceList();
		updateRecentDeviceList();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		getActivity().unregisterReceiver(scanFinishedReceiver);
		getActivity().unregisterReceiver(deviceFoundReceiver);
	}
	
	@Click
	void devices_refresh_button() {
		showRefreshProgress();
		prefs.devicesArray().put("[]");
		prefs.recentDevicesArray().put("[]");
		updateDeviceList();
		updateRecentDeviceList();
		((MainActivity)getActivity()).scanForDevices();
	}
	
	@ItemClick
	void devices_list_listview(Device device) {
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://" + device.host));
		startActivity(browserIntent);
	}
	
	@ItemClick
	void devices_recent_listview(Device device) {
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://" + device.host));
		startActivity(browserIntent);
	}
	
	@UiThread
	void updateDeviceList() {
		deviceListAdapter.notifyDataSetChanged();
	}
	
	@UiThread
	void updateRecentDeviceList() {
		recentDeviceListAdapter.notifyDataSetChanged();
	}
	
	@UiThread
	void showRefreshProgress() {
		devices_refresh_button.setVisibility(View.INVISIBLE);
		devices_refresh_spinner.setVisibility(View.VISIBLE);
	}
	
	@UiThread
	void hideRefreshProgress() {
		devices_refresh_spinner.setVisibility(View.INVISIBLE);
		devices_refresh_button.setVisibility(View.VISIBLE);
	}
}