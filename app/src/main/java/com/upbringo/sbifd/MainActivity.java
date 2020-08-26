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
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "SBI_FD";
    public static boolean isAccessibilityEnabled = false;

    public static final String PREFERENCES = "SBI_FD_PREF";
    public static final String BREAK_FD_IN_PROGRESS = "BREAK_FD_IN_PROGRESS";
    public static final String OTP = "OTP";
    public static final String PIN = "PIN";
    public static final String NUM_FD_TO_BREAK = "NUM_FD_TO_BREAK";
    public static final String CUR_FD_BROKEN = "CUR_FD_BROKEN";
    public static final String USERNAME = "USERNAME";
    public static final String PASSWORD = "PASSWORD";

    private SharedPreferences sharedPref;
    private SharedPreferences.Editor sharedPrefEditor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d( TAG, "onCreate");

        sharedPref = getSharedPreferences( PREFERENCES, Context.MODE_PRIVATE );

        final EditText editTextUsername = findViewById( R.id.text_username );
        final EditText editTextPassword = findViewById( R.id.text_password );
        final EditText editTextPin = findViewById( R.id.text_pin );
        final EditText editTextNumFds = findViewById( R.id.text_num_fds );

        editTextUsername.setText( sharedPref.getString( USERNAME, "" ) );
        editTextPassword.setText( sharedPref.getString( PASSWORD, "" ) );
        editTextPin.setText( sharedPref.getString( PIN, "" ) );

        Button logIn = findViewById( R.id.log_in );
        logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = editTextUsername.getText().toString();
                String password = editTextPassword.getText().toString();
                String pin = editTextPin.getText().toString();
                String numFdsStr = editTextNumFds.getText().toString();
                Integer numFds = Integer.parseInt( numFdsStr );
                // Validation
                if( username.length() == 0 ) {
                    Log.e( TAG, "EmptyUsernameError" );
                    Toast.makeText( v.getContext(), "Username cannot be empty", Toast.LENGTH_SHORT );
                    return;
                } else if( password.length() == 0 ) {
                    Log.e( TAG, "EmptyPasswordError" );
                    Toast.makeText( v.getContext(), "Password cannot be empty", Toast.LENGTH_SHORT );
                    return;
                } else if( pin.length() == 0 ) {
                    Log.e( TAG, "EmptyPinError" );
                    Toast.makeText( v.getContext(), "Pin cannot be empty", Toast.LENGTH_SHORT );
                    return;
                }

                sharedPrefEditor = sharedPref.edit();
                sharedPrefEditor.putInt( CUR_FD_BROKEN, 0 );
                sharedPrefEditor.putInt( NUM_FD_TO_BREAK, numFds );
                sharedPrefEditor.putBoolean( BREAK_FD_IN_PROGRESS, true );
                sharedPrefEditor.putString( OTP, "" );

                sharedPrefEditor.putString( USERNAME, username );
                sharedPrefEditor.putString( PASSWORD, password );
                sharedPrefEditor.putString( PIN, pin );
                sharedPrefEditor.commit();
                // Redirect to web view to start login process.
                Intent intent = new Intent( v.getContext(), WebviewActivity.class );
                startActivity( intent );
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        // TODO: Enable the following once webview navigation is fixed.
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
                        intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
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

    public static void startNewActivity(Context context, String packageName) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage( packageName );
        if (intent == null) {
            Log.d( TAG, "Null Intent");
            // Bring user to the market or let them choose an app?
            intent = new Intent( Intent.ACTION_VIEW );
            intent.setData( Uri.parse("market://details?id=" + packageName ) );
        }
        intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP );
        context.startActivity( intent );
    }

}
