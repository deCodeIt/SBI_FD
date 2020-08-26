package com.upbringo.sbifd;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class SBIWebChromeClient extends WebChromeClient {
    private static final String TAG = MainActivity.TAG;
    private Context mContext;
    private ProgressDialog progressDialog;

    SBIWebChromeClient(Context c, ProgressDialog p ) {
        mContext = c;
        progressDialog = p;
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        super.onProgressChanged(view, newProgress);
        Log.v( TAG, "Progress " + newProgress );
        if( newProgress != 100 && !progressDialog.isShowing() ) {
            progressDialog.show();
        }
        progressDialog.setProgress( newProgress );
    }
}
