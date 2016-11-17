package com.ozner.cup;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.NotificationCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.ozner.AirPurifier.AirPurifier;
import com.ozner.AirPurifier.AirPurifier_MXChip;
import com.ozner.WaterPurifier.WaterPurifier;
import com.ozner.cup.ACSqlLite.CCacheWorking;
import com.ozner.cup.AirPurifier.DeskAirPurifierFragment;
import com.ozner.cup.AirPurifier.VerticalAirPurifierFragment;
import com.ozner.cup.BaiduPush.OznerBroadcastAction;
import com.ozner.cup.BaiduPush.PushBroadcastKey;
import com.ozner.cup.CChat.CChatFragment;
import com.ozner.cup.CChat.ChatMessageHelper;
import com.ozner.cup.CChat.bean.ChatMessage;
import com.ozner.cup.Command.DeviceData;
import com.ozner.cup.Command.FootFragmentListener;
import com.ozner.cup.Command.ImageLoaderInit;
import com.ozner.cup.Command.MsgHandler;
import com.ozner.cup.Command.OznerCommand;
import com.ozner.cup.Command.OznerPreference;
import com.ozner.cup.Command.PageState;
import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.Device.OznerApplication;
import com.ozner.cup.Device.OznerMallFragment;
import com.ozner.cup.HttpHelper.NetDeviceList;
import com.ozner.cup.HttpHelper.NetUserVfMessage;
import com.ozner.cup.HttpHelper.OznerDataHttp;
import com.ozner.cup.Login.LoginActivity;
import com.ozner.cup.Login.LoginEnActivity;
import com.ozner.cup.Main.BaseMainActivity;
import com.ozner.cup.Main.MainConFragment;
import com.ozner.cup.WaterProbe.ROWaterPurifierFragment;
import com.ozner.cup.WaterProbe.WaterCupFragment;
import com.ozner.cup.WaterProbe.WaterProbeFragment;
import com.ozner.cup.WaterProbe.WaterPurifierFragment;
import com.ozner.cup.WaterProbe.WaterReplenishMeterFragment;
import com.ozner.cup.WaterProbe.WaterTDSPenFragment;
import com.ozner.cup.mycenter.CenterBean.CenterNotification;
import com.ozner.cup.mycenter.MyFragment;
import com.ozner.cup.slideleft.LeftSlideFragment;
import com.ozner.device.BaseDeviceIO;
import com.ozner.device.OznerDevice;
import com.ozner.device.OznerDeviceManager;
import com.ozner.tap.Tap;
import com.ozner.tap.TapManager;
import com.ozner.tap.TapRecord;

import net.simonvt.menudrawer.MenuDrawer;
import net.simonvt.menudrawer.OverlayDrawer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.Header;

/*
* Create By by C-sir@hotmail.com
* */
public class MainActivity extends BaseMainActivity {
    Monitor mMonitor = new Monitor();
    /*
    * 缓存页面数据  隐藏不销毁
    * @MainConFragment  mainConFragment 设备DEBUG页面
    * @MyFragment myFragment 个人中心页面
    * */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("tag", "Main_onCreate");
//// 进行闹铃注册
//        Intent intent = new Intent(MainActivity.this,AlarmReceiver.class);
//       intent.putExtra("msg","你该打酱油了");
//
//        PendingIntent sender = PendingIntent.getBroadcast(MainActivity.this, 0, intent, 0);
//
//// 过10s 执行这个闹铃
//        Calendar calendar = Calendar.getInstance();
////        calendar.setTimeZone(TimeZone.getTimeZone("GMT+8"));
////        calendar.setTimeInMillis(System.currentTimeMillis());
//        calendar.add(Calendar.SECOND, 5);
//
//        AlarmManager manager = (AlarmManager)getSystemService(ALARM_SERVICE);
//      //  manager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
//        manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 5 * 1000,sender);

//        ShareSDK.initSDK(MainActivity.this);
        setContentView(R.layout.activity_main);

//        InitBaiduPush();
        notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        ImageLoaderInit.initImageLoader(getBaseContext());
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        Log.e("CsirTag", OznerCommand.getDeviceInfo(getBaseContext()));
        myOverlayDrawer = (OverlayDrawer) findViewById(R.id.activity_main_drawer);
        myOverlayDrawer.setContentView(R.layout.fragment_main_con);
        myOverlayDrawer.setMenuView(R.layout.fragment_slide_left_layout);
        myOverlayDrawer.setMenuSize(dm.widthPixels / 10 * 8);
        fManager = getSupportFragmentManager();
        myOverlayDrawer.setOnDrawerStateChangeListener(new MenuDrawer.OnDrawerStateChangeListener() {
            @Override
            public void onDrawerStateChange(int oldState, int newState) {
                switch (newState) {
                    case MenuDrawer.STATE_OPENING://正在打开
                        LocalInitData();
                        Fragment fragment = fManager.findFragmentByTag("leftslidefragment");
                        if (fragment != null) {
                            fragment.onActivityResult(0, PageState.DEVICECHANGE, null);
                        }
                        break;
                }
            }

            @Override
            public void onDrawerSlide(float openRatio, int offsetPixels) {

            }
        });
        myOverlayDrawer.setVerticalScrollBarEnabled(false);
        myOverlayDrawer.toggleMenu(false);
        userid = UserDataPreference.GetUserData(getBaseContext(), UserDataPreference.UserId, null);
        //重置客服id，以便重新匹配
        UserDataPreference.SetUserData(getBaseContext(), UserDataPreference.ChatUserKfId, "");
        //是否登陆
        if (userid != null && userid.length() > 0) {
            ((OznerApplication) getApplication()).setIsPhone();
            LocalInitDataLeft();//初始化本地数据
        }
        InitLeftFragment();//初始化侧边栏
        InitFootFragment();//初始化底部导航栏
        InitConFragment();//初始化内容区域
        RigisterDevice();
        myHandler = new MsgHandler(MainActivity.this);
    }

//    public void InitBaiduPush() {
//        PushManager.startWork(getApplicationContext(),
//                PushConstants.LOGIN_TYPE_API_KEY,
//                getString(R.string.Baidu_Push_ApiKey));
//
//    }

    //检查验证信息
    public void checkCenterVFstate() {
        centernotify = CenterNotification.getCenterNotifyState(getBaseContext());
        if (centernotify > 0) {
            footNavFragment.SetCenterNotify(centernotify);
        }
        new CenterVFCheckTask().execute();
    }

    public class CenterVFCheckTask extends AsyncTask<String, Void, List<NetUserVfMessage>> {

        @Override
        protected List<NetUserVfMessage> doInBackground(String... params) {
            List<NetUserVfMessage> vfInfoList = OznerCommand.GetUserVerifMessage(MainActivity.this);
            return vfInfoList;
        }

        @Override
        protected void onPostExecute(List<NetUserVfMessage> netUserVfMessages) {
            if (netUserVfMessages != null && netUserVfMessages.size() > 0) {
                int waitNum = 0;
                for (NetUserVfMessage vfmsg : netUserVfMessages) {
                    if (vfmsg.Status != 2) {
                        waitNum++;
                    }
                }
                if (waitNum > 0) {
                    CenterNotification.setCenterNotify(getBaseContext(), CenterNotification.NewFriendVF);
                }
            }
            centernotify = CenterNotification.getCenterNotifyState(getBaseContext());
            if (centernotify > 0) {
                footNavFragment.SetCenterNotify(centernotify);
            }
            super.onPostExecute(netUserVfMessages);
        }
    }

    /*
    * 初始化侧边栏
    * */
    private void InitLeftFragment() {
        Bundle leftbundle = new Bundle();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.framen_sliding_left, LeftSlideFragment.newInstance(leftbundle), "leftslidefragment").commitAllowingStateLoss();
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

    private void LocalInitDataLeft() {
        if (OznerDeviceManager.Instance() != null) {
            myDeviceList.clear();
            //获取本地数据库设备列表
            OznerDevice[] list = OznerDeviceManager.Instance().getDevices();
            if (list != null && list.length > 0) {
                while (myDeviceList.size() != list.length) {
                    int a = 0;
                    for (OznerDevice device : list) {
                        if (device != null) {
                            DeviceData devicedata = new DeviceData();
                            devicedata.setDeviceAddress(device.Address());
                            devicedata.setName(device.getName());
                            devicedata.setDeviceType(device.Type());
                            devicedata.setMac(device.Address());
                            devicedata.setOznerDevice(device);
                            Object object = device.getAppValue(PageState.sortPosi);
                            if (object != null) {
                                int b = (int) object;
                                if (b >= myDeviceList.size()) {
                                    myDeviceList.add(devicedata);
                                } else {
                                    myDeviceList.add(a + b, devicedata);
                                }
                            } else {
                                myDeviceList.add(0, devicedata);
                                a++;
                            }
                        }
                    }
                }
                int a = 0;
                for (DeviceData deviceData : myDeviceList) {
                    deviceData.getOznerDevice().setAppdata(PageState.sortPosi, a);
                    a++;
                }
            }
        }
    }

    /*
    * 初始化内容区域
    * */
    private void InitConFragment() {
//        mainConFragment = new MainConFragment();
//        getSupportFragmentManager().beginTransaction()
//                .replace(R.id.framen_main_con, mainConFragment).commit();
        ShowContent(PageState.WODESHEBEI, null);
    }

    /*
    * 初始化底部导航栏
    * */
    private void InitFootFragment() {
        footNavFragment = new FootNavFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("replace_id", R.id.framen_main_con);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.framen_foot_tab, footNavFragment).commitAllowingStateLoss();

        String countStr = UserDataPreference.GetUserData(MainActivity.this, UserDataPreference.NewChatmsgCount, "0");
        int chatNewCount = Integer.parseInt(countStr);
        if (chatNewCount > 0) {
//            UserDataPreference.SetUserData(MainActivity.this, UserDataPreference.NewChatmsgCount, String.valueOf(chatNewCount));
            footNavFragment.SetMessageCount(chatNewCount);
        }

    }

//    @Override
//    protected void onPostResume() {
//        super.onPostResume();
//        InitConFragment();
//    }

    @Override
    public void onBackPressed() {
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        if (drawer.isDrawerOpen(GravityCompat.START)) {
//            drawer.closeDrawer(GravityCompat.START);
//        } else {
//            super.onBackPressed();
//        }
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
            Toast.makeText(this, getString(R.string.PressToExit), Toast.LENGTH_SHORT).show();
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

    @Override
    public void onAttachFragment(Fragment fragment) {
        // TODO Auto-generated method stub
        try {
            myFootFragmentListener = (FootFragmentListener) fragment;
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        super.onAttachFragment(fragment);
    }

    @Override
    protected void onStop() {
        super.onStop();
//        myHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                Log.e("tag", "运行模式：" + OznerCommand.isAppRunBackound(getBaseContext(), getPackageName()));
//            }
//        }, 500);
        Log.e("tag", "onStop");
        UserDataPreference.SetUserData(this, "IsResume", "false");
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.e("Csir", "Activity  Resume");
        this.isResume = true;
        //每次页面展示，获取其他地方执行消除缓存的网络任务
        /**Begin                  程序网络缓存任务执行*/
        if (mCCachWorking == null) {
            Log.e("Csir", "NetCacheWork  Resume F");
            mCCachWorking = new CCacheWorking(getBaseContext());
            mCCachWorking.execute();
        } else {
            Log.e("Csir", "NetCacheWork  Resume S");
            if (mCCachWorking.getStatus() == AsyncTask.Status.FINISHED) {
                mCCachWorking = null;
                mCCachWorking = new CCacheWorking(getBaseContext());
                mCCachWorking.execute();
            }
        }

        boolean isShowChat = false;
        try {
            isShowChat = Boolean.parseBoolean(UserDataPreference.GetUserData(this, "isShowChat", "false"));
            UserDataPreference.SetUserData(this, "isShowChat", "false");
            Log.e("tag", "isShowChat_onResume:" + isShowChat);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        /**End                  程序网络缓存任务执行*/
        //是否登陆
        if (isShouldResume) {
            if (userid != null && userid.length() > 0) {
                LocalInitDataLeft();//初始化本地数据
                if (isShowChat) {
                    if (pagenow != PageState.ZIXUNYEMIAN) {
                        footNavFragment.ShowContent(PageState.ZIXUNYEMIAN, null);
                        ShowContent(PageState.ZIXUNYEMIAN, null);
                    }
                } else if (pagenow == PageState.WODESHEBEI) {
                    if (myDeviceList != null && myDeviceList.size() > 0) {
                        if (MAC == null || MAC == "" || MAC.length() == 0) {
                            ShowContent(PageState.DEVICECHANGE, myDeviceList.get(0).getMac());
                            //myOverlayDrawer.toggleMenu();
                        } else {
                            pagenow = PageState.DEVICECHANGE;
                            ShowContent(PageState.WODESHEBEI, null);
                            //   myOverlayDrawer.toggleMenu(false);
                        }
                    } else {
                        CacheFragment(PageState.WODESHEBEI);
                    }
                }

//                centernotify = Byte.decode(UserDataPreference.GetUserData(getBaseContext(), UserDataPreference.CenterNotify, "0"));
                checkCenterVFstate();
            }
        }
        UserDataPreference.SetUserData(this, "IsResume", "true");
//        myHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                Log.e("tag", "运行模式：" + OznerCommand.isAppRunBackound(getBaseContext(), getPackageName()));
//            }
//        }, 500);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*
    * 注册蓝牙广播监听消息
    * */
    public void RigisterDevice() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(OznerApplication.ACTION_ServiceInit);
        filter.addAction(BaseMainActivity.ACTION_NetChenge);
        filter.addAction(Cup.ACTION_BLUETOOTHCUP_RECORD_COMPLETE);
        filter.addAction(Cup.ACTION_BLUETOOTHCUP_SENSOR);
        filter.addAction(Tap.ACTION_BLUETOOTHTAP_SENSOR);
        filter.addAction(Tap.ACTION_BLUETOOTHTAP_RECORD_COMPLETE);
        filter.addAction(OznerDeviceManager.ACTION_OZNER_MANAGER_DEVICE_ADD);
        filter.addAction(OznerDeviceManager.ACTION_OZNER_MANAGER_DEVICE_REMOVE);
        filter.addAction(OznerDeviceManager.ACTION_OZNER_MANAGER_DEVICE_CHANGE);
        filter.addAction(PageState.OZNERLOCALDEVICEADD);
        filter.addAction(AirPurifier_MXChip.ACTION_AIR_PURIFIER_SENSOR_CHANGED);
        filter.addAction(AirPurifier_MXChip.ACTION_AIR_PURIFIER_STATUS_CHANGED);
        filter.addAction(WaterPurifier.ACTION_WATER_PURIFIER_STATUS_CHANGE);
        filter.addAction(BaseDeviceIO.ACTION_DEVICE_CONNECTED);
        filter.addAction(BaseDeviceIO.ACTION_DEVICE_CONNECTING);
        filter.addAction(BaseDeviceIO.ACTION_DEVICE_DISCONNECTED);
        filter.addAction(OznerDevice.ACTION_DEVICE_UPDATE);
        filter.addAction(Tap.ACTION_BLUETOOTHTAP_RECORD_COMPLETE);//水探头检测完成
        filter.addAction(OznerBroadcastAction.ReceiveMessage);//通知消息到达
        filter.addAction(OznerBroadcastAction.ReceiveMessageClick);//通知消息点击
        filter.addAction(OznerBroadcastAction.NewFriendVF);//好友验证信息
        filter.addAction(OznerBroadcastAction.NewMessage);//新的留言
        filter.addAction(OznerBroadcastAction.Logout);//退出登录
        filter.addAction(OznerBroadcastAction.LoginNotify);//登录通知
        this.registerReceiver(mMonitor, filter);//注册蓝牙监听
    }

    /**
     * 页面控制显示
     */
    @Override
    public void ShowContent(int index, String mac) {
        if (index != pagenow) {
            switch (index) {
                case PageState.WODESHEBEI://我的设备页面
                    BackToLastDevice();
                    pagenow = index;
                    return;
                case PageState.SHANGCHEGYEMIAN://我的商城页面
                    getSupportFragmentManager().beginTransaction().replace(R.id.framen_main_con, new OznerMallFragment()).commitAllowingStateLoss();
                    HideToggleMenu();
                    pagenow = index;
                    return;
                case PageState.ZIXUNYEMIAN://咨询页面
                    if (chatFragment == null) {
                        chatFragment = new CChatFragment();
                    }
//                    getSupportFragmentManager().beginTransaction()
//                            .replace(R.id.framen_main_con, new CChatFragment()).commitAllowingStateLoss();
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.framen_main_con, chatFragment).commitAllowingStateLoss();
                    HideToggleMenu();
                    pagenow = index;
                    return;
                case PageState.MYPAGE://我的页面
                    CacheFragment(PageState.MYPAGE);
                    HideToggleMenu();
                    pagenow = index;
                    return;
                default:
                    break;
            }
        }
        if (mac != null && mac.length() > 0) {
            switch (index) {
                case PageState.DEVICECHANGE:    //切换设备页面
                    ChangeDeviceFragment(mac);
                    HideToggleMenu();
                    pagenow = PageState.WODESHEBEI;
                    Log.e("tag", "MainShowContent:" + pagenow);
                    footNavFragment.Show(PageState.WODESHEBEI);
                    break;
            }
        }
    }

    public void ShowDevicePage() {

    }

    /*
    * 隐藏侧边栏
    * */
    public void HideToggleMenu() {
        if (myOverlayDrawer != null) {
            int state = myOverlayDrawer.getDrawerState();
            if (state != MenuDrawer.STATE_CLOSED && state != MenuDrawer.STATE_CLOSING && state != MenuDrawer.STATE_DRAGGING) {
                myOverlayDrawer.closeMenu();
            }
        }
    }

    /*
    * 控制设备页面显示上一个设备
    * */
    public void BackToLastDevice() {
        if (MAC != null && MAC.length() > 0) {
            OznerDevice[] devices = OznerDeviceManager.Instance().getDevices();
            if (devices != null && devices.length > 0) {
                for (OznerDevice oznerDevice : devices) {
                    if (oznerDevice.Address().equals(MAC)) {
                        ChangeDeviceFragment(MAC);
                        return;
                    }
                }
            }
            MAC = "";
        }
        CacheFragment(PageState.WODESHEBEI);

    }

    public void ChangeDeviceFragment(String mac) {
        if (mac != null && mac.length() > 0) {
            OznerDevice[] devices = OznerDeviceManager.Instance().getDevices();
            for (OznerDevice deviceData : devices) {
                //查找设备
                if (deviceData.Address().equals(mac)) {
                    Bundle params = new Bundle();
                    params.putString("MAC", mac);
                    String type = deviceData.Type();
                    Log.e("type", type);
                    switch (type) {
                        //智能水杯
                        case CupType:
                            WaterCupFragment waterCupFragment = new WaterCupFragment();
                            waterCupFragment.setArguments(params);

//                                getSupportFragmentManager().beginTransaction()
//                                        .replace(R.id.framen_main_con, waterCupFragment, CupType)
//                                        .commit();
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.framen_main_con, waterCupFragment, CupType)
                                    .commitAllowingStateLoss();
                            this.MAC = mac;
                            return;
                        case TapType:
                        case TdsPen:
                            String tapPen = deviceData.getAppValue(PageState.TapType).toString();
                            if (tapPen == "pen" || "pen".equals(tapPen)) {
                                WaterTDSPenFragment waterTDSPenFragment = new WaterTDSPenFragment();
                                waterTDSPenFragment.setArguments(params);
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.framen_main_con, waterTDSPenFragment, TdsPen)
                                        .commitAllowingStateLoss();
                                this.MAC = mac;
                            } else {
                                WaterProbeFragment waterProbeFragment = new WaterProbeFragment();
                                waterProbeFragment.setArguments(params);
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.framen_main_con, waterProbeFragment, TapType)
                                        .commitAllowingStateLoss();
                                this.MAC = mac;
                            }
                            Log.e("123456", tapPen);
                            return;
                        case WaterType:
                            WaterPurifierFragment waterPurifierFragment = new WaterPurifierFragment();
                            waterPurifierFragment.setArguments(params);
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.framen_main_con, waterPurifierFragment, WaterType)
                                    .commitAllowingStateLoss();
                            this.MAC = mac;
                            //Ro水机
                        case ROPurifierType:
                            ROWaterPurifierFragment roWaterPurifierFragment = new ROWaterPurifierFragment();
                            roWaterPurifierFragment.setArguments(params);
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.framen_main_con, roWaterPurifierFragment, ROPurifierType)
                                    .commitAllowingStateLoss();
                            this.MAC = mac;
                            return;
                        case WaterAylaType:
                            WaterPurifierFragment waterPurifierAylaFragment = new WaterPurifierFragment();
                            waterPurifierAylaFragment.setArguments(params);
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.framen_main_con, waterPurifierAylaFragment, WaterAylaType)
                                    .commitAllowingStateLoss();
                            this.MAC = mac;
                            return;
                        case AirPurifierTypeVer:
                            VerticalAirPurifierFragment verticalAirPurifierFragment = new VerticalAirPurifierFragment();
                            verticalAirPurifierFragment.setArguments(params);
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.framen_main_con, verticalAirPurifierFragment, AirPurifierTypeVer)
                                    .commitAllowingStateLoss();
                            this.MAC = mac;
                            return;
                        case AirPurifierTypeTai:
                            DeskAirPurifierFragment airPurifierFragment = new DeskAirPurifierFragment();
                            airPurifierFragment.setArguments(params);
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.framen_main_con, airPurifierFragment, AirPurifierTypeTai)
                                    .commitAllowingStateLoss();
                            this.MAC = mac;
                            return;
                        case WaterReplenishMeter:
                            WaterReplenishMeterFragment waterReplenishMeterFragment = new WaterReplenishMeterFragment();
                            waterReplenishMeterFragment.setArguments(params);
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.framen_main_con, waterReplenishMeterFragment, WaterReplenishMeter)
                                    .commitAllowingStateLoss();
                            this.MAC = mac;
                            return;
                        default:
                            break;

                    }
                    break;
                }

            }
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.framen_main_con, new MainConFragment())
                .commitAllowingStateLoss();
        footNavFragment.Show(PageState.WODESHEBEI);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String mac;
        //  myOverlayDrawer.toggleMenu(false);
        switch (resultCode) {
            case PageState.UpdateCupSetting:
                mac = data.getStringExtra("Mac");
                int color = data.getIntExtra("haloColor", 0);
                if (mac != null && color != 0) {
                    for (DeviceData deviceData : myDeviceList) {
                        OznerDevice oznerDevice = deviceData.getOznerDevice();
                        if (oznerDevice != null) {
                            Cup cup = (Cup) oznerDevice;
                            CupSetting setting = cup.Setting();
                            setting.haloColor(color);
                            cup.updateSettings();
                            Toast.makeText(getBaseContext(), "ChangeColor Success", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;
            case PageState.DeleteDevice:
                mac = data.getStringExtra(PageState.MAC);
                Log.e("CSIR", "Delete Device:" + mac);
                if (mac != null) {
                    OznerDevice[] localdevice = OznerDeviceManager.Instance().getDevices();
                    for (OznerDevice device : localdevice) {
                        //缓存删除
                        if (device.Address().equals(mac)) {
                            OznerDeviceManager.Instance().remove(device);
                            CheckIndexPage("delete", mac);
                            OznerCommand.CNetCacheDeleteBindDeviceTask(getBaseContext(), device);
                            OznerCommand.DeviceNetWorkAsync(getBaseContext());
                            break;
                        }
                    }
                }
                break;
            case PageState.CenterDeviceClick:
                String centerAddress = data.getStringExtra(PageState.CENTER_DEVICE_ADDRESS);
                footNavFragment.ShowContent(PageState.DEVICECHANGE, centerAddress);
                ShowContent(PageState.DEVICECHANGE, centerAddress);
//                pagenow = PageState.WODESHEBEI;
                break;
            case PageState.FilterStatusChat:
                String deaddr = data.getStringExtra(PageState.CENTER_DEVICE_ADDRESS);
                footNavFragment.ShowContent(PageState.ZIXUNYEMIAN, deaddr);
                ShowContent(PageState.ZIXUNYEMIAN, deaddr);
                break;
        }
        //   HideToggleMenu();
    }

    /*
    * 用于多线程里更新设备
    * */
    @SuppressWarnings("SynchronizeOnNonFinalField")
    private void ThreadUpdateDeviceList(String address) {
        NetDeviceList netDeviceList = OznerDataHttp.RefreshDeviceList(getBaseContext());

        //noinspection SynchronizeOnNonFinalField
        synchronized (myDeviceList) {
            //   myDeviceList.removeAll(myDeviceList);
            JSONArray jsonlist = netDeviceList.getDevicelist();

            for (int i = 0; i < jsonlist.length(); i++) {
                DeviceData deviceData = new DeviceData();
                try {

                    //将JSONObject 对象转化为DeviceData
                    if (deviceData.fromJSONObject(jsonlist.getJSONObject(i))) {
                        boolean isChange = false;
                        //  myDeviceList.add(deviceData);
                        for (DeviceData mydevic : myDeviceList)
                        //通知他页面设备列表发生变化
                        {
                            //IS CHANGE 事件
                            if (mydevic.getMac().equals(deviceData.getMac())) {
                                isChange = true;
                                mydevic.setName(deviceData.getName());
                                mydevic.setDeviceAddress(deviceData.getDeviceAddress());
                                break;
                            }
                        }
                        //IS ADD NEW DEVICE
                        if (!isChange) {
                            //开启蓝牙连接
                            try {
                                OznerDevice oznerDevice = OznerDeviceManager.Instance().getDevice(deviceData.getMac(), deviceData.getDeviceType(), deviceData.getSettings());
                                deviceData.setOznerDevice(oznerDevice);
                                if (deviceData.getOznerDevice() != null) {
                                    OznerDeviceManager.Instance().save(deviceData.getOznerDevice());
                                    myDeviceList.add(deviceData);
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

        }
    }

    /*
    * 删除设备时 用于我的设备页面更新
    * */
    public void CheckIndexPage(String action, String mac) {

        switch (action) {
            case "delete":
                if (this.MAC.equals(mac)) {
                    OznerDevice[] oznerDevices = OznerDeviceManager.Instance().getDevices();
                    if (oznerDevices != null && oznerDevices.length > 0) {
                        ShowContent(PageState.DEVICECHANGE, oznerDevices[0].Address());
                    } else {
                        ShowContent(PageState.WODESHEBEI, null);
                    }
                }
                break;
            case "add":
                if (this.MAC.equals(mac)) {

                } else {
                    ShowContent(PageState.DEVICECHANGE, mac);
                    myOverlayDrawer.toggleMenu();
                }
                break;
        }
    }

    /*
    *Fragment 缓存
    * @MainConFragment  mainConFragment 设备DEBUG页面
    * @MyFragment myFragment 个人中心页面
     */
    public void CacheFragment(int pagestate) {
//        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        switch (pagestate) {
            case PageState.WODESHEBEI:
                getSupportFragmentManager().beginTransaction().replace(R.id.framen_main_con, new MainConFragment()).commitAllowingStateLoss();
                pagenow = pagestate;
                break;
            case PageState.MYPAGE:
                getSupportFragmentManager().beginTransaction().replace(R.id.framen_main_con, new MyFragment()).commitAllowingStateLoss();
                pagenow = pagestate;
                break;
            default:
                break;
        }

    }

    /*
    *@ReloadDevice
    * 重新加载网络设备列表
    * */
    public void ReloadDevice() {
        //执行网络请求
        //Post参数对象
        String usertoken = OznerPreference.UserToken(getBaseContext());
        if (usertoken == null || usertoken.length() <= 0) {
            return;
        }
        RequestParams params = new RequestParams();
        params.put(OznerPreference.UserToken, usertoken);
        //网络请求对象
        AsyncHttpClient client = new AsyncHttpClient();
        client.setTimeout(3000);
        String url = OznerPreference.ServerAddress(getBaseContext()) + "/OznerServer/GetDeviceList";
        client.post(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(getBaseContext(), "访问网络失败", Toast.LENGTH_SHORT).show();
                //   ShowLoginPage();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, org.json.JSONObject response) {
                //解析结果
                int state = 0;
                try {
                    state = Integer.parseInt(response.get("state").toString());
                } catch (Exception ex) {
                    state = 0;
                }
                //网络操作成功
                if (state > 0) {
                    try {
                        //获取设备列表数据
                        JSONArray data = response.getJSONArray("data");
                        if (data != null && data.length() > 0) {
                            myDeviceList.removeAll(myDeviceList);
                            try {
                                //清空当前的列表数据
                                myDeviceList.removeAll(myDeviceList);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            //遍历里表数据
                            for (int i = 0; i < data.length(); i++) {
                                DeviceData deviceData = new DeviceData();
                                try {
                                    //将JSONObject 对象转化为DeviceData
                                    if (deviceData.fromJSONObject(data.getJSONObject(i))) {
                                        myDeviceList.add(deviceData);
                                        //通知他页面设备列表发生变化
                                    }
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                            //结束遍历列表数据
                            myFootFragmentListener.DeviceDataChange();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                }
            }
        });
        //结束网络请求
    }

    /*
    * 传感器发生变化
    * */
    @Override
    public void ChangeRawRecord() {

    }

    /*
    * 杯子数据发生变化
    * */
    @Override
    public void CupSensorChange(String sendaddress) {

    }

    /*
    * 设备列表发生变化
    * */
    @Override
    public void DeviceDataChange() {
//
    }

    /**
     * 连接状态发生变化
     */
    @Override
    public void ContentChange(String mac, String action) {
    }

    @Override
    public void RecvChatData(String data) {

    }

    /*
    * 删除设备线程
    * */
    class DeleteDeviceThread extends Thread {
        private String Mac;

        public DeleteDeviceThread(String mac) {
            this.Mac = mac;
        }

        @Override
        public void run() {
            int state = OznerDataHttp.DeleteDevice(getBaseContext(), Mac);
            if (state > 0) {
//                synchronized (myDeviceList)
//                {
//                    for (DeviceData deviceData:myDeviceList)
//                    {
//                        if(deviceData.getMac().equals(Mac))
//                        {
//                            //找到设备并删除
//                            OznerDevice device=deviceData.getOznerDevice();
//                            try
//                            {
//                                //关闭蓝牙连接
//                                device.IO().close();
//                            }catch (Exception ex)
//                            {
//                                ex.printStackTrace();
//                            }
//                            OznerDeviceManager.Instance().remove(device);
//                            myDeviceList.remove(deviceData);
//                            CheckIndexPage();
//                        }
//                    }
//                }
            } else {
                myHandler.sendEmptyMessage(6);
            }
        }
    }

    /*
    * 页面是否展现
    * */
    public Boolean isResume() {

        return this.isResume;
    }

    @Override
    public void onPause() {
        Log.e("Csir", "Activity Not Resume");
        super.onPause();
        this.isResume = false;
    }

    @Override
    protected void onDestroy() {
        this.unregisterReceiver(mMonitor);
        super.onDestroy();
    }

    /*
         * 蓝牙广播接收器
         * */
    class Monitor extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //检查通知地址
            String sendoraddress = intent.getStringExtra("Address");
            if (sendoraddress != null && sendoraddress.length() > 0) {
                switch (intent.getAction()) {
                    case BaseMainActivity.ACTION_NetChenge:
                        myFootFragmentListener.CupSensorChange(sendoraddress);
                        break;
                    case BaseDeviceIO.ACTION_DEVICE_CONNECTED:
                    case BaseDeviceIO.ACTION_DEVICE_CONNECTING:
                    case BaseDeviceIO.ACTION_DEVICE_DISCONNECTED:
                        myFootFragmentListener.ContentChange(sendoraddress, intent.getAction());
                        break;
                    case Tap.ACTION_BLUETOOTHTAP_SENSOR:
                    case Cup.ACTION_BLUETOOTHCUP_SENSOR:
                        myFootFragmentListener.CupSensorChange(sendoraddress);
                        break;
                    case Tap.ACTION_BLUETOOTHTAP_RECORD_COMPLETE:
                        myFootFragmentListener.CupSensorChange(sendoraddress);
                        OznerCommand.NotfiyTixing(sendoraddress);
                        break;
                    case Cup.ACTION_BLUETOOTHCUP_RECORD_COMPLETE:
                        myFootFragmentListener.CupSensorChange(sendoraddress);

                        break;
                    case OznerDevice.ACTION_DEVICE_UPDATE:
                        myFootFragmentListener.CupSensorChange(sendoraddress);
//                        myFootFragmentListener.ContentChange(sendoraddress, intent.getAction());
                        break;
                    case PageState.OZNERLOCALDEVICEADD:
                        String type = intent.getStringExtra("Type");
//                        new ChangeDeviceThread(sendoraddress, type).start();
                        break;
                    case WaterPurifier.ACTION_WATER_PURIFIER_STATUS_CHANGE:
                        myFootFragmentListener.ContentChange(sendoraddress, null);
                        break;
                    case AirPurifier.ACTION_AIR_PURIFIER_SENSOR_CHANGED:
                        myFootFragmentListener.CupSensorChange(sendoraddress);
                        break;

                    //删除设备,启动线程更新网络数据库
                    case OznerDeviceManager.ACTION_OZNER_MANAGER_DEVICE_REMOVE:
                        new DeleteDeviceThread(sendoraddress).start();
                        break;
                    //添加设备，启动线程更新
                    case OznerDeviceManager.ACTION_OZNER_MANAGER_DEVICE_CHANGE:
                    case OznerDeviceManager.ACTION_OZNER_MANAGER_DEVICE_ADD:
                        NewAddMAC = sendoraddress;
//                        new ChangeDeviceThread(sendoraddress, null).start();
                        Log.e("Csir", " New Device");
                        if (MainActivity.this.isResume()) {
                            Log.e("Csir", " Resume AddDevice");
                            ShowContent(PageState.DEVICECHANGE, sendoraddress);
                        } else {
                            Log.e("Csir", "Not Resume AddDevice");
                            MainActivity.this.MAC = sendoraddress;
                        }
                        break;
                    case OznerBroadcastAction.ReceiveMessage:
                        String msg = intent.getStringExtra(PushBroadcastKey.MSG);
                        Log.e("tag", "MainRecMSg:" + msg);
                        try {
                            if (msg != null && msg != "") {
                                JSONObject resObj = new JSONObject(msg);
                                String contentMsg;
                                if (resObj.has("custom_content")) {
                                    contentMsg = resObj.getJSONObject("custom_content").getString("data");
                                } else {
                                    contentMsg = resObj.getString("data");
                                }
                                if (userid != null && contentMsg != null && contentMsg.length() > 0) {
                                    Log.e("tag", "Main_userid:" + userid.replace("-", ""));

                                    ChatMessageHelper chatMessageHelper = ChatMessageHelper.getInstance(userid.replace("-", ""));
                                    ChatMessage chatMessage = new ChatMessage();
                                    chatMessage.setContent(contentMsg);
                                    chatMessage.setOper(2);
                                    chatMessage.setTime(Calendar.getInstance().getTimeInMillis());
                                    chatMessageHelper.InsertMessage(context, chatMessage);
                                }
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            Log.e("tag", "PushReceiver_ex:" + ex.getMessage());
                        }
                        if (PageState.ZIXUNYEMIAN == pagenow) {
                            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                            notificationManager.cancel(1);
                            myFootFragmentListener.RecvChatData(msg);
                            Log.e("tag", "IsResume:" + UserDataPreference.GetUserData(MainActivity.this, "IsResume", "2222222"));
                            final String notifymsg = msg;
                            if (UserDataPreference.GetUserData(MainActivity.this, "IsResume", "false").equals("false")) {
                                showMsgNotification(msg, 2);
                                Log.e("tag", "MainIsResume:true");
                            }
//                            }else {
//                                VibratorUtil.Vibrate(MainActivity.this, 500);
//                                NoticeUtil.notice(MainActivity.this);
//                            }
                        } else {
                            showMsgNotification(msg, 1);
                            String countStr = UserDataPreference.GetUserData(MainActivity.this, UserDataPreference.NewChatmsgCount, "0");
                            int chatNewCount = Integer.parseInt(countStr);
//                            chatNewCount++;
                            if (chatNewCount > 0) {
//                                UserDataPreference.SetUserData(MainActivity.this, UserDataPreference.NewChatmsgCount, String.valueOf(chatNewCount));
                                footNavFragment.SetMessageCount(chatNewCount);
                            }
                        }
                        break;
                    case OznerBroadcastAction.ReceiveMessageClick:
                        Log.e("tag", "RecevieMessageClick");
                        if (PageState.ZIXUNYEMIAN != pagenow) {
                            footNavFragment.ShowContent(PageState.ZIXUNYEMIAN, null);
                            ShowContent(PageState.ZIXUNYEMIAN, null);
                        }
                        break;
                    case OznerBroadcastAction.NewFriendVF:
                        CenterNotification.setCenterNotify(getBaseContext(), CenterNotification.NewFriendVF);
                        if (PageState.MYPAGE == pagenow) {
                            myFootFragmentListener.RecvChatData(OznerBroadcastAction.NewFriendVF);
                        }
                        checkCenterVFstate();
                        break;
                    case OznerBroadcastAction.NewFriend:
                        CenterNotification.setCenterNotify(getBaseContext(), CenterNotification.NewFriend);
                        if (PageState.MYPAGE == pagenow) {
                            footNavFragment.RecvChatData(OznerBroadcastAction.NewFriend);
                        }
                        checkCenterVFstate();
                        break;
                    case OznerBroadcastAction.NewRank:
                        CenterNotification.setCenterNotify(getBaseContext(), CenterNotification.NewRank);
                        if (PageState.MYPAGE == pagenow) {
                            footNavFragment.RecvChatData(OznerBroadcastAction.NewRank);
                        }
                        checkCenterVFstate();
                        break;
                    case OznerBroadcastAction.NewMessage:
                        CenterNotification.setCenterNotify(getBaseContext(), CenterNotification.NewMessage);
                        if (PageState.MYPAGE == pagenow) {
                            footNavFragment.RecvChatData(OznerBroadcastAction.NewMessage);
                        }
                        checkCenterVFstate();
                        break;
                    case OznerBroadcastAction.Logout:
                        MainActivity.this.finish();
                        break;
                    case OznerBroadcastAction.LoginNotify:
                        Log.e("BD", "LoginNotify");
                        String localMiei = OznerCommand.getImie(getBaseContext());
                        String loginToken = intent.getStringExtra(PushBroadcastKey.LoginUserToken);
                        String loginMiei = intent.getStringExtra(PushBroadcastKey.LoginMIEI);
                        String loginUserid = intent.getStringExtra(PushBroadcastKey.LoginUserid);
                        String localUserid = UserDataPreference.GetUserData(getBaseContext(), UserDataPreference.UserId, null);
//                        if (!loginToken.equals(OznerPreference.UserToken(MainActivity.this))) {
                        Log.e("BDPushSDK", "userid:" + localUserid);
                        if (localUserid != null
                                && localUserid.equals(loginUserid)
                                && !loginToken.equals(OznerPreference.UserToken(MainActivity.this))) {
                            Intent loginIntent = new Intent(getBaseContext(), LoginActivity.class);
                            if (((OznerApplication) getApplication()).isLanguageCN()) {
                                intent.setClass(getBaseContext(), LoginActivity.class);
                            } else {
                                intent.setClass(getBaseContext(), LoginEnActivity.class);
                            }
//                            (getBaseContext(), LoginActivity.class);
                            loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            loginIntent.putExtra(PushBroadcastKey.IsOtherLogin, true);
                            startActivity(loginIntent);
                            MainActivity.this.finish();
                        }
                        break;
                    default:
                        break;
                }
            }

        }
    }


    public void showMsgNotification(String recvMsg, int notifyId) {
        try {
            Log.e("tag", "Main_notify:init");
            JSONObject msgObj = new JSONObject(recvMsg);
            if (msgObj.has("title")) {
                String title = msgObj.getString("title");
                Log.e("tag", "Main_notify:title:" + title);
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(MainActivity.this);
                mBuilder.setSmallIcon(R.mipmap.ozner);
                mBuilder.setTicker(title);
                mBuilder.setContentTitle(title);
                mBuilder.setWhen(System.currentTimeMillis());
                mBuilder.setDefaults(Notification.DEFAULT_ALL);
                mBuilder.setAutoCancel(true);

                PendingIntent pendingIntent;
                if (UserDataPreference.GetUserData(this, "IsResume", "false").equals("false")) {//APP后台运行
                    Log.e("tag", "通知——后台启动");
                    Intent mainIntent = new Intent(this, MainActivity.class);
                    mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    mainIntent.putExtra("isShowChat", "true");
                    UserDataPreference.SetUserData(MainActivity.this, "isShowChat", "true");
                    pendingIntent = PendingIntent.getActivity(MainActivity.this, 0x322, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                } else {
                    Log.e("tag", "通知——前台启动");
                    Intent broadIntent = new Intent(OznerBroadcastAction.ReceiveMessageClick);
                    broadIntent.putExtra("Address", "this is a fuck parms");
                    pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0x321
                            , broadIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                }

                mBuilder.setContentIntent(pendingIntent);
                Notification notification = mBuilder.build();
                notifyManager.notify(notifyId, notification);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("tag", "Main:NotifyEx:" + e.getMessage());
        }
    }

//    private void setCenterNotify(int notify) {
//        centernotify |= notify;
//        UserDataPreference.SetUserData(getBaseContext(), UserDataPreference.CenterNotify, String.valueOf(centernotify));
//        footNavFragment.SetCenterNotify(centernotify);
//    }

    /*
    * 同步水探头数据
    * */
    public void AsyncTapRecord(String address) {
        if (address != null && address.length() > 0) {
            OznerDevice tapdevice = OznerDeviceManager.Instance().getDevice(address);
            if (tapdevice != null) {
                if (TapManager.IsTap(tapdevice.Type())) {
                    Tap async = (Tap) tapdevice;
                    TapRecord[] asyncData = null;
                    try {
                        asyncData = async.TapRecordList().getNoSyncItemDay(new Date());
                    } catch (Exception ex) {
                        asyncData = null;
                    }
                    if (asyncData != null) {

                    }
                }
            }
        }
    }
}
