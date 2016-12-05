package com.ozner.cup.UIView;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by taoran on 2016/3/23.
 */
public class NetHelper {
    //判断网络状况
    public static final int NO_NETWORK = 0;//没有网络
    public static final int TYPE_WIFI = 1;//WIFI
    public static final int TYPE_MOBILE = 2;//手机网络
    public static final int TYPE = 3;//手机网络

    /**
     * 判断当前网络状态
     *
     * @param context
     * @return
     */
    public static int checkNetwork(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = cm.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                    return TYPE_WIFI;
                } else {
                    return TYPE_MOBILE;
                }
            } else {
                return NO_NETWORK;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return NO_NETWORK;
        }
    }
}
