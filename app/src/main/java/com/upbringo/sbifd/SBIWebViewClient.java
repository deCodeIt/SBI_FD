package com.upbringo.sbifd;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.InputStream;

public class SBIWebViewClient extends WebViewClient {
    private static final String TAG = MainActivity.TAG;
    private Context mContext;
    private WebView webview;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor sharedPrefEditor;

    SBIWebViewClient( Context c ) {
        Log.v( TAG, "Initialize SBIWebViewClient with context" );
        mContext = c;
        sharedPref = c.getSharedPreferences( MainActivity.PREFERENCES, Context.MODE_PRIVATE );
    }

    private void injectJS(String script) {
        try {
            InputStream inputStream = mContext.getAssets().open(script);
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();
            String encoded = Base64.encodeToString(buffer, Base64.NO_WRAP);
            webview.loadUrl("javascript:(function() {" +
                    "var alreadyExist = false;" +
                    "var m_scr = document.getElementsByTagName('script');" +
                    "for(i=0;i<m_scr.length;i++) { if( m_scr[i].getAttribute(\"data-scriptname\") == \"" + script + "\") { alreadyExist = true; console.log('script: " + script + " exists!'); break; } }" +
                    "if(!alreadyExist) { "+
                    "console.log('script: " + script + " Injected!');" +
                    "var parent = document.getElementsByTagName('head').item(0);" +
                    "var script = document.createElement('script');" +
                    "script.type = 'text/javascript';" +
                    "script.setAttribute(\"data-scriptname\",\"" + script + "\");" +
                    "script.innerHTML = window.atob('" + encoded + "');" +
                    "parent.appendChild(script)" +
                    "} })()");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadJS( WebView view, String js ) {
        Log.i( TAG, "Load JS: " + js );
        view.loadUrl( "javascript:(function() {" + js + "})()" );
    }

    private void doClickOnLogin( WebView view ) {
        Log.d( TAG, "doClickOnLogin" );
        String username = sharedPref.getString( MainActivity.USERNAME, "" );
        String password = sharedPref.getString( MainActivity.PASSWORD, "" );
        String script = "document.querySelector(\"a[class='login_button']\").click();";
        loadJS( view, script );
        script = "document.getElementById('username').value='" + username + "';";
        loadJS( view, script );
        script = "document.getElementById('label2').value='" + password + "';";
        loadJS( view, script );
        getCaptchaImage( view );
    }

    private void getCaptchaImage( WebView view ) {
        String script = "var c = document.createElement('canvas');" +
        "c.width  = 300;" +
        "c.height = 100;" +
        "var ctx=c.getContext(\"2d\");" +
        "ctx.beginPath();" +
        "ctx.rect(0, 0, c.width, c.height);" +
        "ctx.fillStyle = \"white\";" +
        "ctx.fill();" +
        "var img=document.getElementById(\"refreshImgCaptcha\");" +
        "ctx.drawImage(img,0,0);" +
        "var data = ctx.getImageData(0, 0, c.width, c.height);" +
        "var arrayData = Array.prototype.slice.call(data.data);" +
        "Android.captchaImageLoad(arrayData,c.width,c.height);";
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
