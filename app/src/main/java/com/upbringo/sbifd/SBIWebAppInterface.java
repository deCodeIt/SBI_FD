package com.upbringo.sbifd;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.upbringo.sbifd.R;

public class SBIWebAppInterface {
    private static final String TAG = MainActivity.TAG;
    private Context mContext;
    private WebView webview;

    SBIWebAppInterface(Context c, WebView w) {
        mContext = c;
        webview = w;
    }

    @JavascriptInterface
    public void captchaImageLoad( final int[] imageData, final int width, final int height ) {
        Log.d( TAG, "captchaImageLoad" );

        // Decode image
        Bitmap captcha = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int i = ( x + y * width ) * 4; // As image in canvas is kept row-wise.
                captcha.setPixel(x, y, Color.argb( imageData[ i+3 ], imageData[ i ], imageData[ i + 1 ], imageData[ i + 2 ] ) );
            }
        }

        final Dialog dialog = new Dialog( mContext );
        dialog.setContentView( R.layout.dialog_captcha );
        dialog.setCanceledOnTouchOutside(false);

        // show image in native dialog
        ImageView captchaImage = dialog.findViewById( R.id.captcha_image );
        captchaImage.setImageBitmap( captcha );

        dialog.show();

        final EditText editTextCaptcha = dialog.findViewById( R.id.captcha_text );

        Button buttonSubmit = dialog.findViewById( R.id.captcha_submit_button );
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String captcha = editTextCaptcha.getText().toString();
                if( captcha.length() != 0 ) {
                    // Enter captcha in web-page.
                    webview.post( new Runnable() {
                        @Override
                        public void run() {
                            webview.loadUrl("javascript:(function(){" +
                                    "document.getElementById('loginCaptchaValue').value='" + captcha + "';" +
                                    "document.getElementById('Button2').click();" +
                                    "})()");
                        }
                    } );
                    dialog.dismiss();
                } else {
                    Toast.makeText( mContext, "Captcha cannot be empty", Toast.LENGTH_SHORT ).show();
                }
            }
        });
    }
}
