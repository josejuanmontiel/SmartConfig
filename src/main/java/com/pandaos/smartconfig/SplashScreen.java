//
//  Copyright (c) 2014 Texas Instruments. All rights reserved.
//

package com.pandaos.smartconfig;

import android.app.Activity;
import android.content.Intent;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import com.pandaos.smartconfig.utils.SmartConfigConstants;

@EActivity(R.layout.activity_splash_screen)
public class SplashScreen extends Activity {
	
	@AfterViews
	void afterViews() {
		splash();
	}
	
	@Background
	void splash() {
		try {
			Thread.sleep(SmartConfigConstants.SPLASH_SCREEN_TIME);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			Intent intent = new Intent(this, MainActivity_.class);
			startActivity(intent);
			// close this activity
			finish();
		}
	}
	
	public void onBackPressed() {
		// ignore the back button
	}
}
