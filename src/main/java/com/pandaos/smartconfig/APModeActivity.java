//
//  Copyright (c) 2014 Texas Instruments. All rights reserved.
//

package com.pandaos.smartconfig;

import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import com.pandaos.smartconfig.utils.APViewPagerAdapter;
import com.viewpagerindicator.CirclePageIndicator;

@EActivity(R.layout.activity_ap_mode_view)
public class APModeActivity extends FragmentActivity {

	@ViewById(R.id.pager)
	ViewPager mPager;
	
	@ViewById(R.id.indicator)
	CirclePageIndicator mIndicator;
	
	@AfterViews
	void initPager() {
		mPager.setAdapter(new APViewPagerAdapter(getSupportFragmentManager()));
		mIndicator.setViewPager(mPager);
	}
}
