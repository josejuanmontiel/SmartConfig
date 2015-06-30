//
//  Copyright (c) 2014 Texas Instruments. All rights reserved.
//

package com.pandaos.smartconfig;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;
import com.pandaos.smartconfig.utils.CameraPreview;
import com.pandaos.smartconfig.utils.SharedPreferencesInterface_;
import com.pandaos.smartconfig.utils.ZBarConstants;

@EActivity(R.layout.activity_qrscanner)
public class QRScannerActivity extends Activity {

    private Camera mCamera;
    private CameraPreview mPreview;
    private Handler autoFocusHandler;
    private boolean previewing = true;
    
    ImageScanner scanner;
    Intent returnIntent;
    
    @Pref
    SharedPreferencesInterface_ prefs;
    
    @ViewById
    FrameLayout camera_preview;
    
    @ViewById
    TextView activity_qrscanner_next;
    
    @Click
    void activity_qrscanner_next() {
    	setResult(Activity.RESULT_OK, returnIntent);
		finish();
    }
    
    static {
        System.loadLibrary("iconv");
    }
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!isCameraAvailable()) {
            // Cancel request if there is no rear-facing camera.
            cancelRequest();
            return;
        }
    }
    
    public void onResume() {
    	super.onResume();
    	startCamera();
    }
    
    public void onPause() {
        super.onPause();
        releaseCamera();
        camera_preview.removeView(mPreview);
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e){
        }
        return c;
    }

    private void startCamera() {
    	autoFocusHandler = new Handler();
        mCamera = getCameraInstance();
        /* Instance barcode scanner */
        scanner = new ImageScanner();
        scanner.setConfig(0, Config.X_DENSITY, 3);
        scanner.setConfig(0, Config.Y_DENSITY, 3);
        
        int[] scanModes = getIntent().getIntArrayExtra(ZBarConstants.SCAN_MODES);
        if (scanModes != null) {
        	scanner.setConfig(Symbol.NONE, Config.ENABLE, 0);
        	for (int scanMode : scanModes) {
        		scanner.setConfig(scanMode, Config.ENABLE, 1);
        	}
        }
        mPreview = new CameraPreview(this, mCamera, previewCb, autoFocusCB);
        camera_preview.addView(mPreview);
    }
    
    private void releaseCamera() {
        if (mCamera != null) {
            previewing = false;
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    private Runnable doAutoFocus = new Runnable() {
            public void run() {
                if (previewing)
                    mCamera.autoFocus(autoFocusCB);
            }
        };

    PreviewCallback previewCb = new PreviewCallback() {
            public void onPreviewFrame(byte[] data, Camera camera) {
                Camera.Parameters parameters = camera.getParameters();
                Size size = parameters.getPreviewSize();

                Image barcode = new Image(size.width, size.height, "Y800");
                barcode.setData(data);

                int result = scanner.scanImage(barcode);
                
                if (result != 0) {
                    previewing = false;
                    mCamera.cancelAutoFocus();
                    mCamera.setPreviewCallback(null);
                    mCamera.stopPreview();
                    
                    SymbolSet syms = scanner.getResults();
                    for (Symbol sym : syms) {
                    	String symData = sym.getData();
                    	if (!TextUtils.isEmpty(symData)) {
                    		returnIntent = new Intent();
                    		returnIntent.putExtra(ZBarConstants.SCAN_RESULT, symData);
                    		returnIntent.putExtra(ZBarConstants.SCAN_RESULT_TYPE, sym.getType());
                    		break;
                    	}
                    }
                    if (!prefs.skipScanDisplay().get()){
                    	activity_qrscanner_next.setVisibility(View.VISIBLE);
                    } else {
                    	activity_qrscanner_next();
                    }
                }
            }
        };

    // Mimic continuous auto-focusing
    AutoFocusCallback autoFocusCB = new AutoFocusCallback() {
            public void onAutoFocus(boolean success, Camera camera) {
                autoFocusHandler.postDelayed(doAutoFocus, 1000);
            }
        };
        
        public boolean isCameraAvailable() {
            PackageManager pm = getPackageManager();
            return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);
        }
        
        public void cancelRequest() {
            returnIntent = new Intent();
            returnIntent.putExtra(ZBarConstants.ERROR_INFO, "Camera unavailable or scan canceled by user");
            setResult(Activity.RESULT_CANCELED, returnIntent);
            finish();
        }
}
