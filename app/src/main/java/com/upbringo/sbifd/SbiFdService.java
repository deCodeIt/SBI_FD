package com.upbringo.sbifd;

import android.accessibilityservice.AccessibilityService;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;

public class SbiFdService extends AccessibilityService {
    private static SharedPreferences sharedPref;
    private static SharedPreferences.Editor sharedPrefEditor;
    private Object mutex;
    private static final String TAG = "SBI_SERVICE";

    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.v( TAG, "onAccessibiltiyEvent" );
        if( !sharedPref.getBoolean( MainActivity.BREAK_FD_IN_PROGRESS, false ) ) {
            Log.d( TAG, "Nothing to do" );
            return;
        }
        try {
            // clickPerform(getRootInActiveWindow(), 0);
            Log.v(TAG, "==============================================");
            Log.d(TAG, "Event: " + event);
            // TODO explore why getContentChangeTypes was used?
            if ( event.getContentChangeTypes() > 0 || true ) {
                //TYPE_WINDOW_STATE_CHANGED == 32
                //TYPE_WINDOW_CONTENT_CHANGED == 2048
                switch (event.getEventType()) {
                    case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED: {
                        AccessibilityNodeInfo nodeInfo = event.getSource();
                        Log.i(TAG, "ACC::onAccessibilityEvent: STATE_CHANGED nodeInfo=" + nodeInfo);
                        if (nodeInfo == null) {
                            return;
                        }
//                        Log.d(TAG, "" + nodeInfo.getText());
//                        Log.d(TAG, "" + nodeInfo.getContentDescription());
//                        Log.d(TAG, "" + nodeInfo.getViewIdResourceName());
//                        Log.d(TAG, "" + nodeInfo.getActionList());
//                        Log.d(TAG, "" + nodeInfo.getInputType());
//                        Log.d(TAG, "" + nodeInfo.getLabelFor());
//                        Log.d(TAG, "" + nodeInfo.getParent());
//                        Log.d(TAG, "" + nodeInfo.getWindow());
                    }
                    break;
                    case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED: {
                        AccessibilityNodeInfo nodeInfo = event.getSource();

                        Log.i(TAG, "ACC::onAccessibilityEvent: CONTENT_CHANGED nodeInfo=" + nodeInfo);
                        if (nodeInfo == null) {
                            return;
                        }
//                        Log.d(TAG, "" + nodeInfo.getText());
//                        Log.d(TAG, "" + nodeInfo.getContentDescription());
//                        Log.d(TAG, "" + nodeInfo.getViewIdResourceName());
//                        Log.d(TAG, "" + nodeInfo.getActionList());
//                        Log.d(TAG, "" + nodeInfo.getInputType());
//                        Log.d(TAG, "" + nodeInfo.getLabelFor());
//                        Log.d(TAG, "" + nodeInfo.getParent());
//                        Log.d(TAG, "" + nodeInfo.getWindow());
//                        Log.d(TAG, "" + nodeInfo.getWindowId());

                        AccessibilityNodeInfo rootView = getRootInActiveWindow();
                        if (rootView != null) {
                            if (sharedPref != null) {
                                synchronized ( mutex ) {
                                    doPerformSteps(rootView);
                                }
                            } else {
                                Log.d(TAG, "SharedPreferences is not set");
                            }
                        } else {
                            Log.d(TAG, "RootView not found");
                        }
                    }
                    break;
                    case AccessibilityEvent.TYPE_VIEW_CLICKED: {
                        AccessibilityNodeInfo nodeInfo = event.getSource();

                        Log.i(TAG, "ACC::onAccessibilityEvent: CLICKED nodeInfo=" + nodeInfo);
                        if (nodeInfo == null) {
                            return;
                        }
//                        Log.d(TAG, "" + nodeInfo.getText());
//                        Log.d(TAG, "" + nodeInfo.getContentDescription());
//                        Log.d(TAG, "" + nodeInfo.getViewIdResourceName());
//                        Log.d(TAG, "" + nodeInfo.getActionList());
//                        Log.d(TAG, "" + nodeInfo.getInputType());
//                        Log.d(TAG, "" + nodeInfo.getLabelFor());
//                        Log.d(TAG, "" + nodeInfo.getParent());
//                        Log.d(TAG, "" + nodeInfo.getWindow());
                    }
                    break;
                    case AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED: {
                        AccessibilityNodeInfo nodeInfo = event.getSource();

                        Log.i(TAG, "ACC::onAccessibilityEvent: FOCUSED nodeInfo=" + nodeInfo);
                        if (nodeInfo == null) {
                            return;
                        }
//                        Log.d(TAG, "" + nodeInfo.getText());
//                        Log.d(TAG, "" + nodeInfo.getContentDescription());
//                        Log.d(TAG, "" + nodeInfo.getViewIdResourceName());
//                        Log.d(TAG, "" + nodeInfo.getActionList());
//                        Log.d(TAG, "" + nodeInfo.getInputType());
//                        Log.d(TAG, "" + nodeInfo.getLabelFor());
//                        Log.d(TAG, "" + nodeInfo.getParent());
//                        Log.d(TAG, "" + nodeInfo.getWindow());
                    }
                    break;
                    default: {
                        Log.d(TAG, "Unhandled event");
                    }
                }
            } else {
                Log.v( TAG, "No content changed" );
            }
//            logViewHierarchy(getRootInActiveWindow(), 0);
        } catch( Exception e ){
            Log.e( TAG, "UNEXPECTED ERROR OCCURRED" );
            e.printStackTrace();
        }
    }

    @Override
    public void onInterrupt() {
        Log.d( TAG, "onInterrupt" );
    }

    protected void onServiceConnected() {
        Log.d( TAG, "onServiceConnected" );
        super.onServiceConnected();
        sharedPref = getSharedPreferences(MainActivity.PREFERENCES, Context.MODE_PRIVATE);
        Toast.makeText(getApplicationContext(), "onServiceConnected", Toast.LENGTH_SHORT).show();
        mutex = new Object();
    }

    public static void logViewHierarchy(AccessibilityNodeInfo nodeInfo, final int depth) {

        if (nodeInfo == null) return;

        String spacerString = "";

        for (int i = 0; i < depth; ++i) {
            spacerString += '-';
        }
        // Log the info you care about here.
        Log.d(TAG, spacerString + nodeInfo.getClassName() + " " + nodeInfo.getViewIdResourceName() + " " + nodeInfo.getText() + " " + nodeInfo.getLabelFor() + " " +
                nodeInfo.getContentDescription() );

        for (int i = 0; i < nodeInfo.getChildCount(); ++i) {
            logViewHierarchy(nodeInfo.getChild(i), depth+1);
        }
    }

    public void doPerformSteps( AccessibilityNodeInfo rootView ) {

        // See if it's asking to select pickup points using change option ( usually when you are at known place )
        try {
            List<AccessibilityNodeInfo> nodes;
            AccessibilityNodeInfo node;

            // PLEASE WAIT LOADER
            nodes = rootView.findAccessibilityNodeInfosByText( "Please wait" );
            if( nodes.size() > 0 ) {
                // please wait loader is on
                Log.d( TAG, "On: Loading / Please wait" );
                return;
            }

            // Maybe ProgressBar
            if( rootView.getClassName() == "android.widget.FrameLayout" ) {
                if( rootView.getChildCount() == 2 && rootView.getChild( 1 ).getClassName() == "android.widget.ProgressBar" ) {
                    // Progress bar
                    Log.d( TAG, "On: Progress Bar" );
                    return;
                }
            }

            // TRY LOGIN PAGE
            nodes = rootView.findAccessibilityNodeInfosByText( "Enter 4 digit" );
            if( nodes.size() > 0 ) {
                // login page
                Log.d( TAG, "ON: Login page" );
                String PIN = sharedPref.getString( MainActivity.PIN, "" );
                if( PIN.length() != 4 ) {
                    Log.d( TAG, "NullPinError" );
                    return;
                }
                // pin exists, enter the pin
                for( int index = 0; index < PIN.length(); index++ ) {
                    String number = Character.toString( PIN.charAt( index ) );
                    nodes = rootView.findAccessibilityNodeInfosByText( number );
                    if( nodes.size() > 0 ) {
                        node = nodes.get( 0 );
                        // click the number
                        node.performAction( AccessibilityNodeInfo.ACTION_CLICK );
                    } else {
                        Log.d( TAG, "NumberNotFoundError" );
                    }
                }
                return;
            }

            // No active one time password available
            nodes = rootView.findAccessibilityNodeInfosByText( "No active One Time Password available" );
            if( nodes.size() > 0 ) {
                // options page
                Log.d( TAG, "ON: No active OTP available pop-up" );
                nodes = rootView.findAccessibilityNodeInfosByText( "OK" );
                if( nodes.size() > 0 ) {
                    node = nodes.get(0);
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                } else {
                    Log.d( TAG, "OKNotFoundError" );
                }
                return;
            }

            // Get Online OTP PAGE.
            nodes = rootView.findAccessibilityNodeInfosByText( "Get Online OTP" );
            if( nodes.size() > 0 ) {
                // Options page.
                Log.d( TAG, "ON: Get Online OTP page" );
                node = nodes.get( 0 );
                if( sharedPref.getString( MainActivity.OTP, "" ).length() == 0 ) {
                    // Empty OTP means otp has either been cleared by the app ( already used ) or yet to be fetched.
                    Log.d( MainActivity.TAG, "Fetch OTP");
                    node.getParent().getChild(1).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                } else {
                    Log.d( MainActivity.TAG, "OTP already exists" );
                    // Navigate back to app to continue writing the otp.
                }
                return;
            }

            // OTP displayed, copy it.
            nodes = rootView.findAccessibilityNodeInfosByText( "Your OTP for close" );
            if( nodes.size() > 0 ) {
                Log.d( TAG, "ON: OTP Displayed page" );
                node = nodes.get( 0 );
                String OTP = node.getParent().getChild(2).getText().toString();
                sharedPrefEditor = sharedPref.edit();
                sharedPrefEditor.putString( MainActivity.OTP, OTP );
                sharedPrefEditor.commit();
                // Go back to options page.
                node = rootView.getChild( 0 );
                node.performAction( AccessibilityNodeInfo.ACTION_CLICK );
                // go back to our app after 1 sec
//                Thread thread = new Thread() {
//                    @Override
//                    public void run() {
//                        // Block this thread for 1 seconds.
//                        try {
//                            Thread.sleep(1000);
//                        } catch ( InterruptedException e ) {
//                        }
//
//                        // After sleep finished blocking, create a Runnable to run on the UI Thread.
//                        MainActivity.mActivity.runOnUiThread( new Runnable() {
//                            @Override
//                            public void run() {
//                                Log.v( TAG, "Going back to app:: " + BuildConfig.APPLICATION_ID );
//                                MainActivity.startNewActivity( MainActivity.mContext, BuildConfig.APPLICATION_ID );
//                                Log.v( TAG, "Back to app" );
//                            }
//                        });
//                    }
//                };
//                thread.start();
//                thread.join();
                Log.v( TAG, "Going back to App: " + BuildConfig.APPLICATION_ID );
                WebviewActivity.mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.startNewActivity( WebviewActivity.mActivity, BuildConfig.APPLICATION_ID, Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT );
                    }
                });
                return;
            }

        } catch( Exception e ) {
            Log.e( TAG, "An exception occurred: " + e.getMessage() + e.getLocalizedMessage() );
            e.printStackTrace();
        }
    }
}
