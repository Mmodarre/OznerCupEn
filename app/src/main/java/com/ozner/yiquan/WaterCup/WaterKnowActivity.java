package com.ozner.yiquan.WaterCup;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ozner.yiquan.Command.Contants;
import com.ozner.yiquan.Device.OznerApplication;
import com.ozner.yiquan.R;

/**
 * Created by taoran on 2016/1/12.
 */
public class WaterKnowActivity extends Activity implements View.OnClickListener{
    WebView webview;
    WebSettings webSettings;
    //    TextView toolbarText;
    ProgressBar mprogressBar;
   private Toolbar toolbar;
    private TextView waterText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_water_know);
        initView();
    }
    private void initView(){
        OznerApplication.changeTextFont((ViewGroup)getWindow().getDecorView());
        mprogressBar = (ProgressBar)findViewById(R.id.pb_progress_mall);
        webview = (WebView)findViewById(R.id.wv_webView_oznermall);
        webview.loadUrl(Contants.waterHealthUrl);
        webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(false);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setUseWideViewPort(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webSettings.setLoadWithOverviewMode(true);

        webview.setWebViewClient(new MyWebViewClient());
        webview.setWebChromeClient(new MyWebChromeClient());

        toolbar=(Toolbar)findViewById(R.id.cup_toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WaterKnowActivity.this.finish();
            }
        });
        waterText = (TextView)findViewById(R.id.cup_toolbar_text);
        waterText.setText(getResources().getString(R.string.water_know_test));
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case KeyEvent.KEYCODE_BACK:
                onBackPressed();
        }
    }
    class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            webSettings.setBlockNetworkImage(true);
            mprogressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            webSettings.setBlockNetworkImage(false);
            mprogressBar.setVisibility(View.GONE);
            webview.loadUrl("javascript:setWebType(1)");
        }
    }

    class MyWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            mprogressBar.setProgress(newProgress);
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
//            toolbarText.setText(title);
        }
    }
    @Override
    public void onBackPressed() {
        if (webview.canGoBack()) {
            webview.goBack();
        } else {
//            wv_webView.destroy();
            finish();

        }
        // super.onBackPressed();
    }
}
