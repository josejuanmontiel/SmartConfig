//
//  Copyright (c) 2014 Texas Instruments. All rights reserved.
//

package com.pandaos.smartconfig;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_privacy_policy)
public class PrivacyPolicyActivity extends Activity {
	
	String webPage = "<body style=\"background-color:transparent;\">"
			+ "<font face=\"Helvetica\" color=\"black\" style=\"font-size:20\">"
			+ "<p><br/><b>Texas Instruments (TI) Mobile Applications Privacy Policy</b><br/><br/>"
			+ "We don\u0027t collect any user data.<br/><br/>"
			+ "We don\u0027t store or share your precise location.<br/><br/>"
			+ "Use of social media services are governed by their respective terms of use and policies.<br/><br/>"
			+ "No personal data is stored or can be identified.<br/><br/>"
			+ "<center><a href=\"http://www.ti.com/privacy\">Privacy Policy</a><br><br/></center></p>"
			+ "</font></body>";
	
	@ViewById
	WebView privacy_policy_webview;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@AfterViews
	void afterViews() {
		privacy_policy_webview.loadData(webPage, "text/html", null);
	}
}
