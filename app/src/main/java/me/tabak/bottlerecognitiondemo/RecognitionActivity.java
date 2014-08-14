package me.tabak.bottlerecognitiondemo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import me.tabak.bottlerecognitiondemo.model.Metadata;
import me.tabak.bottlerecognitiondemo.view.WebViewScroll;

public class RecognitionActivity extends Activity implements WebViewScroll.OnScrollChangedListener {
    WebViewScroll mWebView;
    TextView mInstructionsTextView;
    private View mWebViewContainer;
    private View mTopContainer;
    private View mShadowView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);
        setContentView(R.layout.activity_recognition);
        mWebView = (WebViewScroll) findViewById(R.id.webview);
        mWebViewContainer = findViewById(R.id.webview_container);
        mWebView.setWebViewClient(new CustomWebViewClient());
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setDisplayZoomControls(false);
        mShadowView = findViewById(R.id.shadow);
        mTopContainer = findViewById(R.id.top_container);
        mInstructionsTextView = (TextView) findViewById(R.id.instructions_textview);
        if (savedInstanceState == null) {
            mTopContainer.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    mTopContainer.removeOnLayoutChangeListener(this);
                    mWebViewContainer.setTranslationY(bottom);
                }
            });
        }
    }

    public void onReset() {
        mWebViewContainer.animate().translationY(mTopContainer.getHeight()).setDuration(500).start();
        mTopContainer.animate().translationY(0).setDuration(500).start();
        mShadowView.animate().translationY(0).setDuration(500).start();
        mInstructionsTextView.setText(getString(R.string.scan_a_bottle));
        mWebView.setOnScrollChangedListener(null);
        toggleInstructions(true);
    }

    public void onWineRecognized(Metadata metadata) {
        mInstructionsTextView.setText("Wine recognized, loading...");
        mWebView.setOnScrollChangedListener(this);
        mWebView.loadUrl(metadata.getWineComUrl());
    }

    public void toggleInstructions(boolean show) {
        mInstructionsTextView.animate().alpha(show ? 1 : 0).setDuration(500).start();
    }

    @Override
    public void onScrollChanged(int l, int t, int oldl, int oldt) {
        if (mWebViewContainer.getY() > 0) {
            mWebView.setScrollY(0);
            if (mWebViewContainer.getTranslationY() - t > 0) {
                mWebViewContainer.setTranslationY(mWebViewContainer.getTranslationY() - t);
                mTopContainer.setTranslationY(-(mTopContainer.getHeight() - mWebViewContainer.getY()) / 2);
                mShadowView.setTranslationY(mTopContainer.getTranslationY());
            } else {
                mWebViewContainer.setY(0);
                mWebView.setScrollY((int) (t - mWebViewContainer.getY()));
            }
        }
    }

    public class CustomWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.d(CustomWebViewClient.class.getName(), "Loading " + url);
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            Log.d(CustomWebViewClient.class.getName(), "Done loading " + url);
            toggleInstructions(false);
            super.onPageFinished(view, url);
        }
    }
}
