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

import java.util.List;

public class WebviewActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.TAG;

    private SharedPreferences sharedPref;
    private SharedPreferences.Editor sharedPrefEditor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        Log.d( TAG, "WebviewActivity.onCreate" );
        sharedPref = getSharedPreferences( MainActivity.PREFERENCES, Context.MODE_PRIVATE );
    }
}
