package com.upbringo.sbifd;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
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
    private static final String TAG = MainActivity.TAG;;

    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d( TAG, "onAccessibiltiyEvent" );
        if( !sharedPref.getBoolean( MainActivity.BREAK_FD_IN_PROGRESS, false ) ) {
            Log.d( TAG, "Nothing to do" );
            return;
        }
        try {
            // clickPerform(getRootInActiveWindow(), 0);
            Log.d(TAG, "==============================================");
            Log.i(TAG, "ACC::onAccessibilityEvent: " + event.getEventType());
            if (event.getContentChangeTypes() > 0) {
                //TYPE_WINDOW_STATE_CHANGED == 32
                //TYPE_WINDOW_CONTENT_CHANGED == 2048
                switch (event.getEventType()) {
                    case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED: {
                        AccessibilityNodeInfo nodeInfo = event.getSource();
                        Log.d(TAG, "" + event);
                        Log.d(TAG, "" + event.getContentChangeTypes());

                        Log.i(TAG, "ACC::onAccessibilityEvent: STATE_CHANGED nodeInfo=" + nodeInfo);
                        if (nodeInfo == null) {
                            return;
                        }
                        Log.d(TAG, "" + nodeInfo.getText());
                        Log.d(TAG, "" + nodeInfo.getContentDescription());
                        Log.d(TAG, "" + nodeInfo.getViewIdResourceName());
                        Log.d(TAG, "" + nodeInfo.getActionList());
                        Log.d(TAG, "" + nodeInfo.getInputType());
                        Log.d(TAG, "" + nodeInfo.getLabelFor());
                        Log.d(TAG, "" + nodeInfo.getParent());
                        Log.d(TAG, "" + nodeInfo.getWindow());
                    }
                    break;
                    case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED: {
                        AccessibilityNodeInfo nodeInfo = event.getSource();
                        // Log.d(TAG, "" + event);
                        // Log.d(TAG, "" + event.getContentChangeTypes());

                        Log.i(TAG, "ACC::onAccessibilityEvent: CONTENT_CHANGED nodeInfo=" + nodeInfo);
                        if (nodeInfo == null) {
                            return;
                        }
                        Log.d(TAG, "" + nodeInfo.getText());
                        Log.d(TAG, "" + nodeInfo.getContentDescription());
                        Log.d(TAG, "" + nodeInfo.getViewIdResourceName());
                        Log.d(TAG, "" + nodeInfo.getActionList());
                        Log.d(TAG, "" + nodeInfo.getInputType());
                        Log.d(TAG, "" + nodeInfo.getLabelFor());
                        Log.d(TAG, "" + nodeInfo.getParent());
                        Log.d(TAG, "" + nodeInfo.getWindow());
                        Log.d(TAG, "" + nodeInfo.getWindowId());

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
                        Log.d(TAG, "" + event);
                        Log.d(TAG, "" + event.getContentChangeTypes());

                        Log.i(TAG, "ACC::onAccessibilityEvent: CLICKED nodeInfo=" + nodeInfo);
                        if (nodeInfo == null) {
                            return;
                        }
                        Log.d(TAG, "" + nodeInfo.getText());
                        Log.d(TAG, "" + nodeInfo.getContentDescription());
                        Log.d(TAG, "" + nodeInfo.getViewIdResourceName());
                        Log.d(TAG, "" + nodeInfo.getActionList());
                        Log.d(TAG, "" + nodeInfo.getInputType());
                        Log.d(TAG, "" + nodeInfo.getLabelFor());
                        Log.d(TAG, "" + nodeInfo.getParent());
                        Log.d(TAG, "" + nodeInfo.getWindow());
                    }
                    break;
                    case AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED: {
                        AccessibilityNodeInfo nodeInfo = event.getSource();
                        Log.d(TAG, "" + event);
                        Log.d(TAG, "" + event.getContentChangeTypes());

                        Log.i(TAG, "ACC::onAccessibilityEvent: FOCUSED nodeInfo=" + nodeInfo);
                        if (nodeInfo == null) {
                            return;
                        }
                        Log.d(TAG, "" + nodeInfo.getText());
                        Log.d(TAG, "" + nodeInfo.getContentDescription());
                        Log.d(TAG, "" + nodeInfo.getViewIdResourceName());
                        Log.d(TAG, "" + nodeInfo.getActionList());
                        Log.d(TAG, "" + nodeInfo.getInputType());
                        Log.d(TAG, "" + nodeInfo.getLabelFor());
                        Log.d(TAG, "" + nodeInfo.getParent());
                        Log.d(TAG, "" + nodeInfo.getWindow());
                    }
                    break;
                    default: {
                        Log.d(TAG, "Unhandled event");
                    }
                }
            }
            logViewHierarchy(getRootInActiveWindow(), 0);
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

    public static void doPerformSteps( AccessibilityNodeInfo rootView ) {

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

            // TRY LOGIN PAGE
            nodes = rootView.findAccessibilityNodeInfosByText( "Login" );
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

            // Get Online OTP PAGE
            nodes = rootView.findAccessibilityNodeInfosByText( "Get Online OTP" );
            if( nodes.size() > 0 ) {
                // options page
                Log.d( TAG, "ON: Get Online OTP page" );
                node = nodes.get( 0 );
                node.performAction( AccessibilityNodeInfo.ACTION_CLICK );
                return;
            }

            // OTP displayed, copy it
            // sharedPrefEditor = sharedPref.edit();

        } catch( Exception e ) {
            Log.e( TAG, "Some exception occurred: " + e.getMessage() + e.getLocalizedMessage() );
            e.printStackTrace();
        }
    }
}
