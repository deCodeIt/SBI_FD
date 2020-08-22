package com.upbringo.sbifd;

import android.content.Context;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class SBIWebViewClient extends WebViewClient {
    private static final String TAG = MainActivity.TAG;
    private Context mContext;

    SBIWebViewClient( Context c ) {
        Log.v( TAG, "Initialize SBIWebViewClient with context" );
        mContext = c;
    }

    private void loadJS( WebView view, String js ) {
        Log.i( TAG, "Load JS: " + js );
        view.loadUrl( "javascript:" + js );
    }

    private void doClickOnLogin( WebView view ) {
        Log.d( TAG, "doClickOnLogin" );
        String script = "document.querySelector(\"a[class='login_button']\").click()";
        loadJS( view, script );
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        String title = view.getTitle();
        switch( url ) {
            case "https://retail.onlinesbi.com/retail/login.htm": {
                if( title.equals( "State Bank of India - Personal Banking" ) ) {
                    doClickOnLogin( view );
                }
                break;
            }
        }
    }
}
