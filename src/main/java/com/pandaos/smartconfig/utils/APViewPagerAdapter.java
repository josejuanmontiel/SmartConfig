//
//  Copyright (c) 2014 Texas Instruments. All rights reserved.
//

package com.pandaos.smartconfig.utils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.pandaos.smartconfig.APModePage1;
import com.pandaos.smartconfig.APModePage1_;
import com.pandaos.smartconfig.APModePage2;
import com.pandaos.smartconfig.APModePage2_;
import com.pandaos.smartconfig.APModePage3;
import com.pandaos.smartconfig.APModePage3_;
import com.pandaos.smartconfig.APModePage4;
import com.pandaos.smartconfig.APModePage4_;
import com.pandaos.smartconfig.APModePage5;
import com.pandaos.smartconfig.APModePage5_;
import com.pandaos.smartconfig.APModePage6;
import com.pandaos.smartconfig.APModePage6_;

public class APViewPagerAdapter extends FragmentPagerAdapter {

	// Declare the number of ViewPager pages
	final int PAGE_COUNT = 6;
	
	APModePage1 apModePage1 = new APModePage1_();
	APModePage2 apModePage2 = new APModePage2_();
	APModePage3 apModePage3 = new APModePage3_();
	APModePage4 apModePage4 = new APModePage4_();
	APModePage5 apModePage5 = new APModePage5_();
	APModePage6 apModePage6 = new APModePage6_();
	
	public APViewPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int position) {
		switch (position) {
		case 0:
			return apModePage1;
		case 1:
			return apModePage2;
		case 2:
			return apModePage3;
		case 3:
			return apModePage4;
		case 4:
			return apModePage5;
		case 5:
			return apModePage6;
		}
		return apModePage1;
	}

	@Override
	public int getCount() {
		return PAGE_COUNT;
	}
}
