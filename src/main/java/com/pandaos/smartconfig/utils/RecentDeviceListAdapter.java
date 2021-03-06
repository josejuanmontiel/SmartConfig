//
//  Copyright (c) 2014 Texas Instruments. All rights reserved.
//

package com.pandaos.smartconfig.utils;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.sharedpreferences.Pref;

@EBean
public class RecentDeviceListAdapter extends BaseAdapter {
	
	List<Device> recentDevices;
	
	@Pref
	SharedPreferencesInterface_ prefs;
	
	@RootContext
	Context context;

	@AfterInject
	void initAdapter() {
		recentDevices = new ArrayList<Device>(); // initialize new list of devices
		try {
			JSONArray recentDevicesArray = new JSONArray(prefs.recentDevicesArray().get()); // get the JSON array of devices from the shared preferences
			for (int i=0; i<recentDevicesArray.length(); i++) { // populate the list
				recentDevices.add(new Device(recentDevicesArray.getJSONObject(i).getString("name"), recentDevicesArray.getJSONObject(i).getString("host")));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		initAdapter();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		DeviceItemView deviceItemView;
		if (convertView == null) {
			deviceItemView = DeviceItemView_.build(context);
		} else {
			deviceItemView = (DeviceItemView) convertView;
		}
		deviceItemView.bind(getItem(position));
		return deviceItemView;
	}
	
	@Override
	public int getCount() {
		return recentDevices.size();
	}

	@Override
	public Device getItem(int position) {
		return recentDevices.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

}
