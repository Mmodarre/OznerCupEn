package com.ozner.cup.HttpHelper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

import com.ozner.cup.Main.BaseMainActivity;
import com.ozner.cup.MainActivity;

public class NetBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == ConnectivityManager.CONNECTIVITY_ACTION){
            context.sendBroadcast(new Intent(BaseMainActivity.ACTION_NetChenge));
        }
    }
}
