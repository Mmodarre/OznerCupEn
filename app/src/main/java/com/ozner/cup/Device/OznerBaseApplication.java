package com.ozner.cup.Device;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.ozner.application.OznerBLEService;
import com.ozner.application.OznerBLEService.OznerBLEBinder;
import com.ozner.cup.BaiduPush.OznerBroadcastAction;
import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.Guide.LoginService;
import com.tencent.bugly.crashreport.CrashReport;

/**
 * Created by mengdongya on 2015/11/26.
 */
public abstract class OznerBaseApplication extends Application {
    OznerBLEBinder localService = null;
    ServiceConnection mServiceConnection = null;
    OznerBPOnBindBroRece bpOnBindBroRece = new OznerBPOnBindBroRece();
//    static int taskCount = 0;
//    final int MaxCount = 6;

    public OznerBLEBinder getService() {
        return localService;
    }

    /**
     * 服务初始化完成时调用的方法
     */
    protected abstract void onBindService();

    @Override
    public void onCreate() {
        super.onCreate();
        CrashReport.initCrashReport(getApplicationContext(), "900033413", false);
        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                try {
//                    if(service instanceof OznerBLEService)
                    localService = (OznerBLEBinder) service;
                    onBindService();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                localService = null;
            }
        };

        Intent intent = new Intent(this, OznerBLEService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        //初始化更新用户信息标志
        UserDataPreference.SetUserData(getApplicationContext(), UserDataPreference.hasUpdateUserInfo, "false");
        IntentFilter bindFilter = new IntentFilter();
        bindFilter.addAction(OznerBroadcastAction.UpdateUserInfo);
        registerReceiver(bpOnBindBroRece, bindFilter);

    }

    class OznerBPOnBindBroRece extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == OznerBroadcastAction.UpdateUserInfo) {
                Log.w("BaiduPush", "baiduPush:onBind broadcast");
                String userid = UserDataPreference.GetUserData(context, UserDataPreference.UserId, null);
                String deviceid = UserDataPreference.GetUserData(context, UserDataPreference.BaiduDeviceId, null);
                if (userid != null && deviceid != null) {
                    context.startService(new Intent(context,LoginService.class));
                }
            }
        }
    }

    @Override
    public void onTerminate() {
        unbindService(mServiceConnection);
        super.onTerminate();
    }
}
