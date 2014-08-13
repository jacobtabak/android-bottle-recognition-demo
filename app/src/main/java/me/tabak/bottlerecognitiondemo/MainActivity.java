package me.tabak.bottlerecognitiondemo;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import me.tabak.bottlerecognitiondemo.model.Metadata;

public class MainActivity extends Activity {
    WebView mWebView;
    TextView mInstructionsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.setWebViewClient(new CustomWebViewClient());
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setDisplayZoomControls(false);
        mInstructionsTextView = (TextView) findViewById(R.id.instructions_textview);
    }

    public void onReset() {
        mInstructionsTextView.setText("Scan a bottle of wine with your camera.");
        mInstructionsTextView.animate().alpha(1).setDuration(500).start();
    }

    public void onWineRecognized(Metadata metadata) {
        mInstructionsTextView.setText("Wine recognized, loading...");
        mWebView.loadUrl(metadata.getWineComUrl());
    }

    private class CustomWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            mInstructionsTextView.animate().alpha(0).setDuration(500).start();
        }
    }
}
