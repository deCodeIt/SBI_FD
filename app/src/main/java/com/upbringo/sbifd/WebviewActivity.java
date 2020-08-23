package com.upbringo.sbifd;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;

import java.util.List;

public class WebviewActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.TAG;

    private SharedPreferences sharedPref;
    private SharedPreferences.Editor sharedPrefEditor;
    private WebView webview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        Log.d( TAG, "WebviewActivity.onCreate" );
        // Initialize 1st time variables.
        sharedPref = getSharedPreferences( MainActivity.PREFERENCES, Context.MODE_PRIVATE );
        webview = findViewById( R.id.webview );
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled( true );

        ProgressDialog progressDialog = new ProgressDialog( this );
        progressDialog.setCancelable( false );
        progressDialog.setMessage( "Loading..." );
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax( 100 );
        progressDialog.setCanceledOnTouchOutside( false );

        webview.setWebViewClient( new SBIWebViewClient( this, progressDialog ) );
        webview.setWebChromeClient( new SBIWebChromeClient( this, progressDialog ) );
        webview.addJavascriptInterface( new SBIWebAppInterface( this, webview ), "Android" );
        // start login process
        handleInitialNavigation();
    }

    protected void handleInitialNavigation() {
        String currentUrl = webview.getUrl();
        if( currentUrl == null || currentUrl.equals( "" ) ) {
            // first time initialization of webview
            webview.loadUrl( "https://retail.onlinesbi.com/retail/login.htm" );
        }
    }
}
