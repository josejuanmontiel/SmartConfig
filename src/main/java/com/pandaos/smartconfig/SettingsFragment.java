//
//  Copyright (c) 2014 Texas Instruments. All rights reserved.
//

package com.pandaos.smartconfig;

import android.support.v4.app.Fragment;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;
import com.pandaos.smartconfig.utils.SharedPreferencesInterface_;

@EFragment(R.layout.tab_settings_view)
public class SettingsFragment extends Fragment implements OnCheckedChangeListener{
	
	@Pref
	SharedPreferencesInterface_ prefs;
	
	@ViewById
	Switch settings_show_name_toggle;
	
	@ViewById
	Switch settings_open_devices_tab_toggle;
	
	@ViewById
	Switch settings_show_password_toggle;
	
	@ViewById
	TextView settings_ap_mode_button;
	
	@ViewById
	TextView settings_privacy_button;

	
	@AfterViews
	void afterViews() {
		// initialize switches
		if (prefs.showDeviceName().get())
			settings_show_name_toggle.setChecked(true);
		else
			settings_show_name_toggle.setChecked(false);
		if (prefs.openInDevicesList().get())
			settings_open_devices_tab_toggle.setChecked(true);
		else
			settings_open_devices_tab_toggle.setChecked(false);
		if (prefs.showSmartConfigPass().get())
			settings_show_password_toggle.setChecked(true);
		else
			settings_show_password_toggle.setChecked(false);
		// initialize listeners
		settings_show_name_toggle.setOnCheckedChangeListener(this);
		settings_open_devices_tab_toggle.setOnCheckedChangeListener(this);
		settings_show_password_toggle.setOnCheckedChangeListener(this);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
		case R.id.settings_show_name_toggle:
			prefs.showDeviceName().put(settings_show_name_toggle.isChecked());
			break;
		case R.id.settings_open_devices_tab_toggle:
			if (settings_open_devices_tab_toggle.isChecked())
				prefs.startTab().put(1);
			else
				prefs.startTab().put(0);
			prefs.openInDevicesList().put(settings_open_devices_tab_toggle.isChecked());
			break;
		case R.id.settings_show_password_toggle:
			prefs.showSmartConfigPass().put(settings_show_password_toggle.isChecked());
			break;
		}
	}
	
	@Click
	void settings_ap_mode_button() {
		((MainActivity)getActivity()).startAPMode();
	}
	
	@Click
	void settings_privacy_button() {
		((MainActivity)getActivity()).startPrivacyPolicy();
	}
}
