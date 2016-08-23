package com.ozner.cup.mycenter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ozner.cup.Device.OznerApplication;
import com.ozner.cup.R;

/*
* Created by xinde on 2015/12/11
 */
public class WebActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String URL = "URL";
    public static final String TITLE = "TITLE";
    public static final String IsHideTitle = "IsHideTitle";
    //    RelativeLayout rlay_back;
    WebView wv_webView;
    WebSettings settings;
    ProgressBar pb_progress;
    //    TextView tv_title;
    private TextView toolbar_text, toolbar_save;
    private boolean isHideTitle = false;
    //    private ImageButton toolbar_share;
    private String shareUrl;//领红包的分享链接
    private String sharetype = "lhb";
    private boolean isRedBag = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        OznerApplication.changeTextFont((ViewGroup) getWindow().getDecorView());
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            //更改状态栏颜色
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.fz_blue));
            //更改底部导航栏颜色(限有底部的手机)
            window.setNavigationBarColor(ContextCompat.getColor(this, R.color.fz_blue));
        }
        Intent dataIntent = getIntent();

        try {
            isRedBag = dataIntent.getBooleanExtra("IsRedBag", false);
            Log.e("webActivity", "isRedBag: " + isRedBag);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        String url = dataIntent.getStringExtra(URL);
        String title = "";
        try {
            title = dataIntent.getStringExtra(TITLE);
        } catch (Exception ex) {
            title = "";
            ex.printStackTrace();
        }
        try {
            isHideTitle = dataIntent.getBooleanExtra(IsHideTitle, false);
        } catch (Exception ex) {
            ex.printStackTrace();
            isHideTitle = false;
        }

        toolbar_text = (TextView) findViewById(R.id.toolbar_text);
        toolbar_save = (TextView) findViewById(R.id.toolbar_save);
        toolbar_save.setText("分享");
        toolbar_text.setText(title);
        toolbar_text.setTextColor(ContextCompat.getColor(WebActivity.this, R.color.white));
//        if ("领红包".equals(title)) {
//            toolbar_save.setVisibility(View.VISIBLE);
//            toolbar_save.setOnClickListener(this);
//            toolbar_save.setTextColor(ContextCompat.getColor(WebActivity.this, R.color.white));
//        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        toolbar_share=(ImageButton)findViewById(R.id.toolbar_share);//分享键
//        toolbar_share.setOnClickListener(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setBackgroundResource(R.color.air_background);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WebActivity.this.finish();
            }
        });
        if (isHideTitle) {
            toolbar.setVisibility(View.GONE);
        }
        wv_webView = (WebView) findViewById(R.id.wv_webView);
        pb_progress = (ProgressBar) findViewById(R.id.pb_progress);
        wv_webView.setOnClickListener(this);
        initWebView(wv_webView);
        if (null != url && "" != url) {
            wv_webView.loadUrl(url);
            wv_webView.setWebChromeClient(webChromeClient);
            wv_webView.setWebViewClient(webViewClient);
        } else {
            new AlertDialog.Builder(this).setMessage(getString(R.string.url_null))
                    .setPositiveButton(getString(R.string.ensure), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    }).show();
        }
    }

    private void initWebView(WebView web) {
        settings = web.getSettings();
//        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);//优先使用缓存
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);//优先不使用缓存
        if (isRedBag) {
            settings.setJavaScriptEnabled(false);//javascript 支持
        } else {
            settings.setJavaScriptEnabled(true);//javascript 支持
        }
        settings.setUseWideViewPort(true);//将图片调整到适合webview的大小
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);//支持页面重新布局
        settings.setLoadWithOverviewMode(true);//缩放至屏幕大小
//        settings.setBuiltInZoomControls(true);//设置支持缩放
        settings.setLoadsImagesAutomatically(true);//支持自动加载图片
        settings.setNeedInitialFocus(true);//调用requestFocus时为webview设置节点
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rlay_back:
                onBackPressed();
                break;
            case R.id.toolbar_save:
//                Toast.makeText(WebActivity.this, "分享操作", Toast.LENGTH_SHORT).show();
//                Log.e("webActivity", "分享操作 ");
                shareWeChat();
//                ShareView.showShareToDialogHb(WebActivity.this, sharetype, CenterUrlContants.getRedPacUrl);
                break;
//            case R.id.toolbar_share:
//                ShareView.showShareToDialogHb(WebActivity.this,sharetype,shareUrl);
//                break;
        }
    }

    WebChromeClient webChromeClient = new WebChromeClient() {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {

            pb_progress.setProgress(newProgress);
            super.onProgressChanged(view, newProgress);
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            if (title != null && title != "") {
                toolbar_text.setText(title);
            }
            super.onReceivedTitle(view, title);
        }
    };
    WebViewClient webViewClient = new WebViewClient() {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
            return super.shouldOverrideKeyEvent(view, event);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            settings.setBlockNetworkImage(true);
            pb_progress.setVisibility(View.VISIBLE);
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            settings.setBlockNetworkImage(false);
            pb_progress.setVisibility(View.GONE);
            super.onPageFinished(view, url);
        }
    };

    private void shareWeChat() {
    }

    @Override
    public void onBackPressed() {
        if (wv_webView.canGoBack()) {
            wv_webView.goBack();
        } else {
            finish();
        }
    }

    @Override
    public void onDetachedFromWindow() {
        wv_webView.destroy();
        super.onDetachedFromWindow();
    }
}
