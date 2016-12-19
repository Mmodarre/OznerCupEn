package com.ozner.qianye.Device;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ozner.AirPurifier.AirPurifier_MXChip;
import com.ozner.WaterPurifier.WaterPurifier;
import com.ozner.WaterReplenishmentMeter.WaterReplenishmentMeter;
import com.ozner.device.OznerDevice;
import com.ozner.device.OznerDeviceManager;
import com.ozner.tap.Tap;

import com.ozner.qianye.R;

/**
 * Created by mengdongya on 2015/12/10.
 */
public class AboutDeviceActivity extends AppCompatActivity {
    TextView toolbar_text;
    Toolbar toolbar;
    String Mac;
    boolean isdesk = false;//url用来标识柜式/台式净水器，尚未给出区分方式
    OznerDevice device;
    WebView webView;
    WebSettings mwebSettings;
//    String urlCup = "http://cup.ozner.net/app/gyznb/gyznb.html";//http://cup.ozner.net/app/us/gyznb_us.html英文版
    String urlTap = "http://app.joyro.com.cn:8282/SmartWaterTester.html";//水探头
    String urlWRM = "http://app.joyro.com.cn:8282/BeautyInstrument.html";//补水仪
    String urlWPDesktop = "http://app.joyro.com.cn:8282/DesktopWaterPurifier.html";//台式净水器
    String urlWPVertical = "http://app.joyro.com.cn:8282/VerticalWaterPurifier.html";//柜式净水器
    String urlAirVer = "http://app.joyro.com.cn:8282/AirPurifier.html";//立式空净
//    String urlAirTai = "http://app.joyro.com.cn:8282/DesktopWaterPurifier.html";
    String urlTdsPen="file:///android_asset/hz_tdspen.html";
    String flag="";
    ProgressBar mprogressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mac = getIntent().getStringExtra("MAC");
        device = OznerDeviceManager.Instance().getDevice(Mac);
        flag=getIntent().getStringExtra("Flag");
        setContentView(R.layout.activity_about_device);
        try {
            isdesk = getIntent().getBooleanExtra("Desk",false);
        } catch (NullPointerException e) {
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        webView = (WebView) findViewById(R.id.wv_about_device);
        mwebSettings = webView.getSettings();
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        toolbar_text = (TextView) findViewById(R.id.toolbar_text);
        mprogressBar = (ProgressBar) findViewById(R.id.pb_progress);
        initView();
        OznerApplication.changeTextFont((ViewGroup) getWindow().getDecorView());

    }

    private void initView() {
//        if (device instanceof Cup) {
//            toolbar_text.setText(getString(R.string.about_smart_glass));
//            webView.loadUrl(urlCup);
//        } else
        if (device instanceof Tap) {
            if("tdspen".equals(flag)){
                toolbar_text.setText(getString(R.string.about_water_tdspen));
                webView.loadUrl(urlTdsPen);
            }else{
                toolbar_text.setText(getString(R.string.about_water_probe));
                webView.loadUrl(urlTap);
            }

        } else if (device instanceof WaterPurifier) {

            if (isdesk) {
                toolbar_text.setText(getString(R.string.about_water_purifier));
                webView.loadUrl(urlWPDesktop);
            } else {
                toolbar_text.setText(getString(R.string.about_water_purifier_ver));
                webView.loadUrl(urlWPVertical);
            }
        } else if (device instanceof AirPurifier_MXChip) {
            toolbar_text.setText(getString(R.string.my_air_purifier_ver));
            webView.loadUrl(urlAirVer);
        } else if (device instanceof WaterReplenishmentMeter) {
            toolbar_text.setText(getString(R.string.water_replen_meter));
            webView.loadUrl(urlWRM);

        }

        mwebSettings.setJavaScriptEnabled(true);
        mwebSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        mwebSettings.setSupportZoom(true);
        mwebSettings.setBuiltInZoomControls(true);
        mwebSettings.setDisplayZoomControls(false);
        mwebSettings.setUseWideViewPort(true);
        mwebSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        mwebSettings.setLoadWithOverviewMode(true);
        webView.setWebViewClient(new MyWebViewClient());
        webView.setWebChromeClient(new MyWebChromeClient());

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
            mwebSettings.setBlockNetworkImage(true);
            mprogressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            mwebSettings.setBlockNetworkImage(false);
            mprogressBar.setVisibility(View.GONE);
            webView.loadUrl("javascript:setWebType(1)");
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
        }
    }

}
