//
//  Copyright (c) 2014 Texas Instruments. All rights reserved.
//

package com.pandaos.smartconfig.utils;

public interface SmartConfigConstants {
	
	public static final String NETWORK_CHANGE_BROADCAST_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
	public static final String SCAN_FINISHED_BROADCAST_ACTION = "com.pandaos.smartconfig.utils.SCAN_FINISHED";
	public static final String DEVICE_FOUND_BROADCAST_ACTION = "com.pandaos.smartconfig.utils.DEVICE_FOUND";
	public static final int SPLASH_SCREEN_TIME = 2000; //2 seconds
	public static final int MAIN_SCAN_TIME = 15000; //15 seconds
	public static final int JMDNS_CLOSE_TIME = 6000; //6 seconds
	public static final int TAB_DIMENS = 80; // the size of the tabs in dp
	public static final int SC_RUNTIME = 60000; // one minute
	public static final int SC_PROGRESSBAR_INTERVAL = 1000; // one second
	public static final int SC_MDNS_INTERVAL = 10000; //10 seconds
	public static final String ZERO_PADDING_16="0000000000000000";
	
}
