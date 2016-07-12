package com.ozner.cup.Login;

import cn.sharesdk.framework.authorize.AuthorizeAdapter;

/**
 * Created by ozner_67 on 2016/7/4.
 */
public class ShareSDKAdapter extends AuthorizeAdapter {
    @Override
    public void onCreate() {
        //super.onCreate();
        hideShareSDKLogo();
    }
}
