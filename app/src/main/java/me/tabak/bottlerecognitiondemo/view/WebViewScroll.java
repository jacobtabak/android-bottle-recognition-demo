package me.tabak.bottlerecognitiondemo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class WebViewScroll extends WebView {
    private OnScrollChangedListener mScrollListener;

    public WebViewScroll(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WebViewScroll(Context context) {
        this(context, null, 0);
    }

    public WebViewScroll(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (!isInEditMode()) {
            setWebChromeClient(new CustomWebChromeClient());
        }
    }

    public void setOnScrollChangedListener(OnScrollChangedListener listener) {
        mScrollListener = listener;
    }

    public interface OnScrollChangedListener {
        public void onScrollChanged(int l, int t, int oldl, int oldt);
    }

    public class CustomWebChromeClient extends WebChromeClient {
        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            Log.d(WebViewScroll.class.getName(), consoleMessage.message());
            return super.onConsoleMessage(consoleMessage);
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (mScrollListener != null) {
            mScrollListener.onScrollChanged(l, t, oldl, oldt);
        }
        Log.d(WebViewScroll.class.getName(), "t: " + t);
    }
}
