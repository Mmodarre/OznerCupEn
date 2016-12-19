package com.ozner.qianye.Device;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.ozner.qianye.Command.CenterUrlContants;
import com.ozner.qianye.Command.OznerPreference;
import com.ozner.qianye.Command.UserDataPreference;
import com.ozner.qianye.R;

/**
 * Created by mengdongya on 2015/12/17.
 */
public class OznerMallFragment extends Fragment implements View.OnClickListener{
//    ImageView imageView= null;
    WebView webview;
    WebSettings webSettings;
//    TextView toolbarText;
    ProgressBar mprogressBar;
    public OznerMallFragment() {}

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ozner_mall,container,false);
//        toolbarText = ((TextView)view.findViewById(R.id.tv_tool_text));
//        imageView = (ImageView)view.findViewById(R.id.iv_gouwuche);
//        imageView.setOnClickListener(this);
        String mobile = UserDataPreference.GetUserData(getContext(), UserDataPreference.Mobile, null);
        String usertoken = OznerPreference.UserToken(getActivity());
    //    Log.e("tag",usertoken);
        mprogressBar = (ProgressBar)view.findViewById(R.id.pb_progress_mall);
        webview = (WebView)view.findViewById(R.id.wv_webView_oznermall);
        webview.loadUrl(CenterUrlContants.getMallUrl(mobile,usertoken,"zh","zh"));
        Log.e("tag2", CenterUrlContants.getMallUrl(mobile,usertoken,"zh","zh")+"---------------");
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
        return view;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case KeyEvent.KEYCODE_BACK:
                if (webview.canGoBack()) {
                    webview.goBack();
                } else {
//                    getActivity().finish();
                }
                break;
//            case R.id.iv_gouwuche:
//                Toast.makeText(getActivity(),"sjdkfdhakfh",Toast.LENGTH_SHORT).show();
//                break;
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
}
