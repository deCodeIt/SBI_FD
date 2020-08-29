package com.upbringo.sbifd;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

public class SBIWebViewClient extends WebViewClient {
    private static final String TAG = MainActivity.TAG;
    private Context mContext;
    private Activity mActivity;
    private WebviewActivity mWebviewActivity;
    private WebView webview;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor sharedPrefEditor;
    private ProgressDialog progressDialog;
    private Dialog msgDialog;
    private Timer t;

    SBIWebViewClient(Context c, Activity a, WebviewActivity w, ProgressDialog p ) {
        Log.v( TAG, "Initialize SBIWebViewClient with context" );
        mContext = c;
        mActivity = a;
        mWebviewActivity = w;
        sharedPref = c.getSharedPreferences( MainActivity.PREFERENCES, Context.MODE_PRIVATE );
        progressDialog = p;
        msgDialog = new Dialog( mContext );
        msgDialog.setContentView( R.layout.dialog_message );
        msgDialog.setCanceledOnTouchOutside(false);
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
//        progressDialog.setMessage( "Executing js: " + js );
        dialogMessage( "Executing Javascript", js );
        view.loadUrl( "javascript:(function() {" + js + "})()" );
    }

    private void dialogMessage( String title, String msg ) {
        TextView textViewTitle = msgDialog.findViewById( R.id.text_dialog_title );
        TextView textViewMessage = msgDialog.findViewById( R.id.text_dialog_message );
        if( title != null && title.length() != 0 ) {
            textViewTitle.setText( title );
        }
        if( msg != null && msg.length() != 0 ) {
            textViewMessage.setText( msg );
        }
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
        int currentFdCount = sharedPref.getInt( MainActivity.CUR_FD_BROKEN, 0 );
        int totalFdToBreak = sharedPref.getInt( MainActivity.NUM_FD_TO_BREAK, 0 );
        if( currentFdCount >= totalFdToBreak ) {
            doLogout( view );
            return;
        }
        String script = "document.getElementById( 'dr0').click();";
        loadJS( view, script );
        script = "document.querySelector(\"input[type='submit'][name='Button'][value='Proceed']\").click();";
        loadJS( view, script );
    }

    private void doAddRemark( WebView view ) {
        String script = "document.getElementById('remarks').value = 'SBI APP';";
        loadJS( view, script );
        script = "document.querySelector(\"input[type='button'][name='Confirm'][value='Confirm']\").click();";
        loadJS( view, script );
    }

    private void doReturnToBreakFDPage( WebView view ) {

        // increment broken fd count
        int currentFdCount = sharedPref.getInt( MainActivity.CUR_FD_BROKEN, 0 );
        sharedPrefEditor = sharedPref.edit();
        sharedPrefEditor.putInt( MainActivity.CUR_FD_BROKEN, currentFdCount + 1 );
        sharedPrefEditor.commit();

        // return to break fd page
        String script = "callURL('/retail/fixeddepositpreclosureinitial.htm');";
        loadJS( view, script );
    }

    private void doProcessOTP( WebView view ) {
        // Fetch otp from app
        sharedPrefEditor = sharedPref.edit();
        sharedPrefEditor.putString( MainActivity.OTP, "" ); // set otp to null before fetch
        sharedPrefEditor.commit();

        // open sbi otp app and copy otp in shared pref
        MainActivity.startNewActivity( mContext, mContext.getResources().getString( R.string.sbi_app_name ), Intent.FLAG_ACTIVITY_SINGLE_TOP );
        waitForOTP( view );
    }

    private void waitForOTP( final WebView view ) {
        // wait until we have the OTP
        if( t != null ) {
            t.cancel();
        }
        t = new Timer();
        t.scheduleAtFixedRate( new TimerTask() {
            @Override
            public void run() {
                final String OTP = sharedPref.getString( MainActivity.OTP, "" );
                if( OTP.length() != 0 ) {
                    // OTP found
                    if( !mWebviewActivity.visible ) {
                        Log.v( TAG, "App is in backgruond" );
                        return;
                    }
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.v( TAG, "OTP: " + OTP );
                            String script = "document.querySelector(\"input[name='securityPassword']\").value=" + OTP + ";";
                            loadJS( view, script );

                            script = "document.getElementById('confirmButton').click();";
                            loadJS( view, script );

                            sharedPrefEditor = sharedPref.edit();
                            sharedPrefEditor.putString( MainActivity.OTP, "" ); // set otp to null after fetch
                            sharedPrefEditor.commit();
                        }
                    });
                    t.cancel();
                    Log.d( TAG, "OTP Entered" );
                } else {
                    Log.v( TAG, "Waiting for OTP" );
                }
            }
        }, 0, 1000 );
    }

    private  void doLogout( WebView view ) {
        String script = "cifPopup('Later')";
        loadJS( view, script );
    }

    private void doFinishActivity( WebView view ) {
        mActivity.finish(); // terminate activity to go back to main activity.
    }

    @Nullable
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
//        Log.v(TAG, "shouldInterceptRequest: " + request.getUrl().toString());
        return super.shouldInterceptRequest(view, request);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if( t != null ) {
            t.cancel();
        }
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
            case "https://retail.onlinesbi.com/retail/login.htm":
            case "https://retail.onlinesbi.com/retail/loginsubmit.htm": {
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
            case "https://retail.onlinesbi.com/retail/fixeddepositpreclosureinterim.htm": {
                if (title.equals("State Bank of India")) {
                    doAddRemark(view);
                }
                break;
            }
            case "https://retail.onlinesbi.com/retail/smsenablehighsecurity.htm": {
                if (title.equals("State Bank of India")) {
                    doProcessOTP(view);
                }
                break;
            }
            case "https://retail.onlinesbi.com/retail/smsenablehighsecurityconfirm.htm": {
                if (title.equals("State Bank of India")) {
                    doReturnToBreakFDPage(view);
                }
                break;
            }
            case "https://retail.onlinesbi.com/retail/logout.htm": {
                if (title.equals("State Bank of India")) {
                    doFinishActivity(view);
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
