package com.ozner.cup.Main;

import android.app.NotificationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.widget.Toast;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.ozner.cup.ACSqlLite.CCacheWorking;
import com.ozner.cup.CChat.CChatFragment;
import com.ozner.cup.Command.DeviceData;
import com.ozner.cup.Command.FootFragmentListener;
import com.ozner.cup.Command.PageState;
import com.ozner.cup.FootNavFragment;
import com.ozner.cup.R;
import com.ozner.cup.mycenter.CheckForUpdate.OznerUpdateManager;
import com.ozner.device.OznerDevice;
import com.ozner.device.OznerDeviceManager;
import com.umeng.analytics.MobclickAgent;

import net.simonvt.menudrawer.OverlayDrawer;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public abstract class BaseMainActivity extends AppCompatActivity implements FootFragmentListener{
    public int pagenow;
    public final String CupType = "CP001";
    public final String TapType = "SC001";
    public final String TdsPen = "SCP001";//TDS-笔
    public final String WaterType = "MXCHIP_HAOZE_Water";
    public final String AirPurifierTypeVer = "FOG_HAOZE_AIR";
    public final String AirPurifierTypeTai = "FLT001";
    public final String WaterReplenishMeter = "BSY001";
    protected final String WaterAylaType = "AY001MAB1";
    public boolean isResume;
    public Boolean isExit = false;
    public static final String ACTION_NetChenge = "ozner.net.chenge";
    public List<DeviceData> myDeviceList = new ArrayList<DeviceData>();
    public FootNavFragment footNavFragment;
    public String userid;
    public Boolean isShouldResume = true;
    public CChatFragment chatFragment = null;
    public OverlayDrawer myOverlayDrawer;
    public FootFragmentListener myFootFragmentListener;
    /*
    * 缓存页面数据  隐藏不销毁
    * @MainConFragment  mainConFragment 设备DEBUG页面
    * @MyFragment myFragment 个人中心页面
    * */
    public Handler myHandler;
    public FragmentManager fManager;
    public String MAC;
    public String NewAddMAC;
    public CCacheWorking mCCachWorking = null;
    public NotificationManager notifyManager;
    public byte centernotify = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            //更改状态栏颜色
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.main_bgcolor));
            //更改底部导航栏颜色(限有底部的手机)
            window.setNavigationBarColor(ContextCompat.getColor(this, R.color.main_bgcolor));
        }
        InitBaiduPush();
        MobclickAgent.setDebugMode(true);
        //是否登陆
        OznerUpdateManager oznerUpdateManager = new OznerUpdateManager(BaseMainActivity.this, false);
        oznerUpdateManager.checkUpdate();
        LocalInitData();
        new OznerUpdateManager(BaseMainActivity.this, false).checkUpdate();
    }
    public void InitBaiduPush() {
        PushManager.startWork(getApplicationContext(),
                PushConstants.LOGIN_TYPE_API_KEY,
                getString(R.string.Baidu_Push_ApiKey));

    }
    /*
   * 初始化本地数据
   * */
    private void LocalInitData() {
        if (OznerDeviceManager.Instance() != null) {
            myDeviceList.clear();
            //获取本地数据库设备列表
            OznerDevice[] list = OznerDeviceManager.Instance().getDevices();
            if (list != null && list.length > 0) {
                for (OznerDevice device : list) {
                    if (device != null) {
                        DeviceData devicedata = new DeviceData();
                        devicedata.setDeviceAddress(device.Address());
                        devicedata.setName(device.getName());
                        devicedata.setDeviceType(device.Type());
                        devicedata.setMac(device.Address());
                        devicedata.setOznerDevice(device);
                        myDeviceList.add(devicedata);
                    }
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0 && pagenow == PageState.WODESHEBEI) {
            getSupportFragmentManager().popBackStack();
        } else {
            exit();
        }

    }

    //退出程序
    private void exit() {
        Timer tExit = null;
        if (isExit == false) {
            isExit = true; // 准备退出
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            tExit = new Timer();
            tExit.schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false; // 取消退出
                }
            }, 2000); // 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务

        } else {
            finish();
            System.exit(0);
        }
    }

}
