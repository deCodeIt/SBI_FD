package com.upbringo.sbifd;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;

import java.io.InputStream;

public class SBIWebViewClient extends WebViewClient {
    private static final String TAG = MainActivity.TAG;
    private Context mContext;
    private WebView webview;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor sharedPrefEditor;
    private ProgressDialog progressDialog;

    SBIWebViewClient(Context c, ProgressDialog p ) {
        Log.v( TAG, "Initialize SBIWebViewClient with context" );
        mContext = c;
        sharedPref = c.getSharedPreferences( MainActivity.PREFERENCES, Context.MODE_PRIVATE );
        progressDialog = p;
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

    private void doSelectFixedDepositOption( WebView view ) {
        String script = "callURL('/retail/viewfixeddeposit.htm')";
        loadJS( view, script );
    }

    private void doSelectETdrEStdr( WebView view ) {
        String script = "document.querySelector('input[id=createType][value=createFd]').checked=true;";
        loadJS( view, script );
        script = "document.getElementById( 'Proceed' ).click();";
        loadJS( view, script );
    }

    private void doSelecCloseAcPrematurely( WebView view ) {
        String script = "callURL('/retail/fixeddepositpreclosureinitial.htm')";
        loadJS( view, script );
    }

    private void doSelectFdToBreak( WebView view ) {
        // TODO
        String script = "";
        loadJS( view, script );
    }

    private  void doLogout( WebView view ) {
        String script = "cifPopup('Later')";
        loadJS( view, script );
    }
    @Nullable
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        Log.v(TAG, "shouldInterceptRequest: " + request.getUrl().toString());
        return super.shouldInterceptRequest(view, request);
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        progressDialog.setProgress( 0 );
        progressDialog.show();
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        progressDialog.setProgress( 100 );
        progressDialog.hide();
        String title = view.getTitle();
        switch( url ) {
            case "https://retail.onlinesbi.com/retail/login.htm": {
                if (title.equals("State Bank of India - Personal Banking")) {
                    doClickOnLogin(view);
                }
                break;
            }
            case "https://retail.onlinesbi.com/retail/mypage.htm": {
                if (title.equals("State Bank of India")) {
                    doSelectFixedDepositOption(view);
                }
                break;
            }
            case "https://retail.onlinesbi.com/retail/viewfixeddeposit.htm": {
                if (title.equals("State Bank of India")) {
                    doSelectETdrEStdr(view);
                }
                break;
            }
            case "https://retail.onlinesbi.com/retail/fixeddeposit.htm": {
                if (title.equals("State Bank of India")) {
                    doSelecCloseAcPrematurely(view);
                }
                break;
            }
            case "https://retail.onlinesbi.com/retail/fixeddepositpreclosureinitial.htm": {
                if (title.equals("State Bank of India")) {
                    doSelectFdToBreak(view);
                }
                break;
            }
            default: {
                Log.w( TAG, "Unknown page " + url );
                doLogout( view );
            }
        }
    }
}
