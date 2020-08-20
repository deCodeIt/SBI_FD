package com.upbringo.sbifd;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "SBI_FD";
    public static boolean isAccessibilityEnabled = false;
    public static final String PREFERENCES = "SBI_FD_PREF";
    public static final String BreakFdInProcess = "BREAK_FD_IN_PROGRESS";

    // Used with SBI OTP APP
    public static final String PIN = "PIN";
    public static final String NUM_FD_TO_BREAK = "PIN";
    public static final String CUR_FD_BROKEN = "PIN";

    private SharedPreferences sharedPref;
    private SharedPreferences.Editor sharedPrefEditor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d( TAG, "onCreate");

        sharedPref = getSharedPreferences( PREFERENCES, Context.MODE_PRIVATE );
        sharedPrefEditor = sharedPref.edit();

        Button logIn = findViewById( R.id.log_in );
        logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPrefEditor.putString( PIN, "1234" );
                sharedPrefEditor.putInt( CUR_FD_BROKEN, 0 );
                sharedPrefEditor.putInt( NUM_FD_TO_BREAK, 2 );
                sharedPrefEditor.putBoolean( BreakFdInProcess, true );
                sharedPrefEditor.commit();
                // startNewActivity( getApplicationContext(), "com.sbi.SBISecure" );
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        checkForAccessibility(); // check if accessibility is enabled, otherwise enable it
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public static void logInstalledAccessiblityServices(Context context) {

        AccessibilityManager am = (AccessibilityManager) context
                .getSystemService(Context.ACCESSIBILITY_SERVICE);

        List<AccessibilityServiceInfo> runningServices = am
                .getInstalledAccessibilityServiceList();
        for (AccessibilityServiceInfo service : runningServices) {
            Log.i(TAG, service.getId());
        }
    }

    public static boolean isAccessibilityServiceEnabled(Context context ) {
        String id = "com.upbringo.sbifd/.SbiFdService";

        AccessibilityManager am = (AccessibilityManager) context
                .getSystemService(Context.ACCESSIBILITY_SERVICE);

        List<AccessibilityServiceInfo> runningServices = am
                .getEnabledAccessibilityServiceList(AccessibilityEvent.TYPES_ALL_MASK);
//        Log.d( TAG, runningServices.size() + "" );
        for (AccessibilityServiceInfo service : runningServices) {
//            Log.d( TAG, "SERVICE: " + service.toString());
            if (id.equals(service.getId())) {
                return true;
            }
        }

        return false;
    }

    public void checkForAccessibility() {
        Log.d( TAG, "In checkForAccessibility()");

        if( !isAccessibilityServiceEnabled( this ) ) {
            Log.d( TAG, "Accessibility service is not enabled, asking the user to enable it now" );
            createDialog( this );
        } else {
            Log.d( TAG, "Accessibility service is already enabled" );
            isAccessibilityEnabled = true;
        }
    }

    void createDialog(final Activity activity ) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder( activity );
        builder.setMessage( getString( R.string.accessibility_dialog_disabled_msg ) )
                .setPositiveButton( getString( R.string.accessibility_dialog_submit ), new DialogInterface.OnClickListener() {
                    public void onClick( DialogInterface dialog, int id ) {
                        Intent intent = new Intent( Settings.ACTION_ACCESSIBILITY_SETTINGS );
                        startActivity( intent );
                    }
                })
                .setNegativeButton(getString( R.string.accessibility_dialog_exit ), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id ) {
                        activity.finishAffinity();
                    }
                });
        // Create the AlertDialog object and return it
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
    }

    public void startNewActivity(Context context, String packageName) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage( packageName );
        if (intent == null) {
            Log.d( TAG, "Null Intent");
            // Bring user to the market or let them choose an app?
            intent = new Intent( Intent.ACTION_VIEW );
            intent.setData( Uri.parse("market://details?id=" + packageName ) );
        }
        intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
        context.startActivity( intent );
    }

}
