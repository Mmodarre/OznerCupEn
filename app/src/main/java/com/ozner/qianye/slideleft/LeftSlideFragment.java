package com.ozner.qianye.slideleft;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.ozner.device.OznerDevice;
import com.ozner.device.OznerDeviceManager;
import com.ozner.qianye.Command.DeviceData;
import com.ozner.qianye.Command.FootFragmentListener;
import com.ozner.qianye.Command.ImageHelper;
import com.ozner.qianye.Command.OznerPreference;
import com.ozner.qianye.Command.PageState;
import com.ozner.qianye.Command.UserDataPreference;
import com.ozner.qianye.Device.AddDeviceActivity;
import com.ozner.qianye.Device.OznerApplication;
import com.ozner.qianye.HttpHelper.NetDeviceList;
import com.ozner.qianye.HttpHelper.NetJsonObject;
import com.ozner.qianye.HttpHelper.NetUserHeadImg;
import com.ozner.qianye.HttpHelper.OznerDataHttp;
import com.ozner.qianye.Main.BaseMainActivity;
import com.ozner.qianye.MainActivity;
import com.ozner.qianye.R;
import com.ozner.qianye.mycenter.MyFragment;
import com.ozner.qianye.slideleft.adapter.SlideAdapter;
import com.ozner.qianye.slideleft.bean.SlideBean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

/**
 * Created by gongxibo on 2015/11/26.
 * Modify by C-sir@hotmail.com
 */
public class LeftSlideFragment extends Fragment implements FootFragmentListener {
    public final String[] constate = new String[]{"未连接", "正在连接", "已连接"};
    private final static String DEVICELIST = "devicelist";
    SlideAdapter adapter;
    private String userid;
    /**
     * @author C-sir@hotmail.com
     * @myDeviceList 基础设备数据
     */
    private List<DeviceData> myDeviceList = new ArrayList<DeviceData>();
    private ArrayList<String> list;
    private FootFragmentListener myListener;
    private DragSortListView mDslv;
    private DragSortController mController;
    public int dragStartMode = DragSortController.ON_LONG_PRESS;
    public boolean removeEnabled = false;
    public int removeMode = DragSortController.FLING_REMOVE;
    public boolean sortEnabled = true;
    public boolean dragEnabled = true;
    public TextView show_text, txt_showadd, user_name;
    public LinearLayout llay_left_bg, user_info;
    public ImageView iv_left_buble, user_image;
    private final int USER_HEAD_INFO = 1;//
    MyCenterHandle uihandle = new MyCenterHandle();
    MyLoadImgListener imageLoadListener = new MyLoadImgListener();

    /**
     * 添加新设备
     */
    private View addDvice;

    public static LeftSlideFragment newInstance(Bundle bundle) {
        LeftSlideFragment fragment = new LeftSlideFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        userid = UserDataPreference.GetUserData(getContext(), UserDataPreference.UserId, null);
        View rootview = inflater.inflate(R.layout.fragment_slide_left_layout, container, false);
        OznerApplication.changeTextFont((ViewGroup) rootview);
        mDslv = (DragSortListView) rootview.findViewById(R.id.drag_listview);
        addDvice = rootview.findViewById(R.id.add_device);
        show_text = (TextView) rootview.findViewById(R.id.show_text);
        txt_showadd = (TextView) rootview.findViewById(R.id.txt_showadd);
        llay_left_bg = (LinearLayout) rootview.findViewById(R.id.llay_left_bg);
        iv_left_buble = (ImageView) rootview.findViewById(R.id.iv_left_buble);
        mController = buildController(mDslv);
        mDslv.setFloatViewManager(mController);
        mDslv.setOnTouchListener(mController);
        mDslv.setDragEnabled(dragEnabled);
        user_info = (LinearLayout) rootview.findViewById(R.id.user_info);
        user_image = (ImageView) rootview.findViewById(R.id.user_image);
        user_name = (TextView) rootview.findViewById(R.id.user_name);
        user_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().beginTransaction().replace(R.id.framen_main_con, new MyFragment()).commitAllowingStateLoss();
//                startActivity(new Intent(getContext(), MyCenterActivity.class));
//                if (((OznerApplication) getActivity().getApplication()).isLoginPhone()) {
                    ((MainActivity) getActivity()).myOverlayDrawer.toggleMenu();
//                } else {
//                    ((MainEnActivity) getActivity()).myOverlayDrawer.toggleMenu();
//                }
            }
        });
//        if (!((OznerApplication) getActivity().getApplication()).isLoginPhone() && userid != null && userid.length() > 0) {
            user_info.setVisibility(View.VISIBLE);
            rootview.findViewById(R.id.llay_holder).setVisibility(View.GONE);
//        } else {
//            user_info.setVisibility(View.GONE);
//            rootview.findViewById(R.id.llay_holder).setVisibility(View.VISIBLE);
//        }
        initImageViewBitmap(rootview);
        return rootview;
    }

    @Override
    public void onResume() {
        super.onResume();
        initHeadImg();
    }

    private void initImageViewBitmap(View initView) {
        WeakReference<Context> refContext = new WeakReference<Context>(getContext());
        if (refContext != null) {
            ((ImageView) initView.findViewById(R.id.iv_left_center)).setImageBitmap(ImageHelper.loadResBitmap(refContext.get(), R.drawable.left_draw));
            user_image.setImageBitmap(ImageHelper.loadResBitmap(refContext.get(), R.mipmap.icon_default_headimage));
            iv_left_buble.setImageBitmap(ImageHelper.loadResBitmap(refContext.get(), R.drawable.left_buble_cn));
            ((ImageView) initView.findViewById(R.id.add_device)).setImageBitmap(ImageHelper.loadResBitmap(refContext.get(),R.drawable.add));
        }
    }

    private void ShowNoDeviceView(View view) {
//        view.setBackgroundResource(R.drawable.left_backgroud);
        view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.main_bgcolor));
        iv_left_buble.setVisibility(View.VISIBLE);
//        if (((OznerApplication) getActivity().getApplication()).isLanguageCN()) {
            iv_left_buble.setImageBitmap(ImageHelper.loadResBitmap(getContext(), R.drawable.left_buble_cn));
//        } else {
//            iv_left_buble.setImageBitmap(ImageHelper.loadResBitmap(getContext(), R.drawable.left_buble));
//        }
        llay_left_bg.setVisibility(View.VISIBLE);
        show_text.setVisibility(View.INVISIBLE);
        txt_showadd.setVisibility(View.INVISIBLE);
    }

    private void ShowDeviceView(View view) {
//        view.setBackgroundResource(0);
        view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.draglist_bkg));
        iv_left_buble.setVisibility(View.GONE);
        llay_left_bg.setVisibility(View.GONE);
        show_text.setVisibility(View.VISIBLE);
        txt_showadd.setVisibility(View.VISIBLE);
    }

    /*
     * 初始化数据
     * */
    public void InitData() {
        //初始化设备列表
        try {
            NetDeviceList devicelist = (NetDeviceList) getArguments().getSerializable(DEVICELIST);
            if (devicelist.state > 0) {
                JSONArray deviclistarray = devicelist.getDevicelist();
                //获取设备列表数据
                if (devicelist.getDevicelist() != null && deviclistarray.length() > 0) {
                    try {
                        //清空当前的列表数据
                        myDeviceList.removeAll(myDeviceList);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    //遍历里表数据
                    for (int i = 0; i < deviclistarray.length(); i++) {
                        DeviceData deviceData = new DeviceData();
                        try {
                            //将JSONObject 对象转化为DeviceData
                            if (deviceData.fromJSONObject(deviclistarray.getJSONObject(i))) {
                                //开启蓝牙连接
                                try {
                                    OznerDevice oznerDevice = OznerDeviceManager.Instance().getDevice(deviceData.getMac(), deviceData.getDeviceType(), deviceData.getSettings());
                                    deviceData.setOznerDevice(oznerDevice);
                                    if (deviceData.getOznerDevice() != null) {
                                        OznerDeviceManager.Instance().save(deviceData.getOznerDevice());
                                    }
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                                myDeviceList.add(deviceData);

                                //通知他页面设备列表发生变化
                                // myFootFragmentListener.DeviceDataChange(myDeviceList);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    //结束遍历列表数据
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    //初始化个人信息
    private void initHeadImg() {
        final String url = OznerPreference.ServerAddress(getContext()) + "/OznerServer/GetUserNickImage";
        loadUserHeadImg(getActivity());
        new Thread(new Runnable() {
            @Override
            public void run() {
                NetUserHeadImg netUserHeadImg = centerInitUserHeadImg(getActivity(), url);
                Message message = new Message();
                message.what = USER_HEAD_INFO;
                message.obj = netUserHeadImg;
                uihandle.sendMessage(message);
            }
        }).start();
    }

    private void loadUserHeadImg(final Activity activity) {
        NetUserHeadImg netUserHeadImg = new NetUserHeadImg();
        netUserHeadImg.fromPreference(activity);
        if (netUserHeadImg != null) {
            Message message = new Message();
            message.what = USER_HEAD_INFO;
            message.obj = netUserHeadImg;
            uihandle.sendMessage(message);
        }
    }

    public static NetUserHeadImg centerInitUserHeadImg(final Activity activity, final String inituserHeadUrl) {
        NetUserHeadImg netUserHeadImg = new NetUserHeadImg();
        String Mobile = UserDataPreference.GetUserData(activity, UserDataPreference.Mobile, null);
        if (Mobile != null) {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("usertoken", OznerPreference.UserToken(activity)));
            params.add(new BasicNameValuePair("jsonmobile", Mobile));
            NetJsonObject netJsonObject = OznerDataHttp.OznerWebServer(activity, inituserHeadUrl, params);
            if (netJsonObject.state > 0) {
//                UserDataPreference.SetUserData(activity, inituserHeadUrl, netJsonObject.value);
                try {
                    JSONArray jarry = netJsonObject.getJSONObject().getJSONArray("data");
                    if (jarry.length() > 0) {
                        JSONObject jo = (JSONObject) jarry.get(0);
                        netUserHeadImg.fromJSONobject(jo);
                        UserDataPreference.SaveUserData(activity, jo);
                    } else {
                        netUserHeadImg.fromPreference(activity);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    netUserHeadImg.fromPreference(activity);
                }
            }
        }
        netUserHeadImg.fromPreference(activity);
        return netUserHeadImg;
    }

    class MyLoadImgListener extends SimpleImageLoadingListener {
        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            ((ImageView) view).setImageBitmap(ImageHelper.toRoundBitmap(getContext(), loadedImage));
            super.onLoadingComplete(imageUri, view, loadedImage);
        }
    }

    class MyCenterHandle extends android.os.Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case USER_HEAD_INFO:
                    ImageHelper imageHelper = new ImageHelper(getContext());
                    imageHelper.setImageLoadingListener(imageLoadListener);
                    NetUserHeadImg netUserHeadImg = (NetUserHeadImg) msg.obj;
                    user_name.setText((netUserHeadImg.nickname != null && netUserHeadImg.nickname.length() > 0) ? netUserHeadImg.nickname : netUserHeadImg.mobile);

                    if (netUserHeadImg != null) {
                        if (netUserHeadImg.headimg != null && netUserHeadImg.headimg.length() > 0) {
                            imageHelper.loadImage(user_image, netUserHeadImg.headimg);
                        } else {
                            //imageHelper.loadImage(iv_person_photo, "http://a.hiphotos.baidu.com/zhidao/wh%3D600%2C800/sign=10284cd567380cd7e64baaeb9174810c/63d9f2d3572c11df09ba0c46612762d0f703c268.jpg");
                            user_image.setImageResource(R.mipmap.icon_default_headimage);
                        }
                    } else {
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    }

    /*
     * 初始化本地数据
     * */
    private void LocalInitData() {
        if (!((OznerApplication) getActivity().getApplication()).isLoginPhone() && userid != null && userid.length() > 0) {
            String nickname = UserDataPreference.GetUserData(getContext(), "Nickname", null);
            if (nickname != null && nickname.length() > 0) {
                user_name.setText(nickname);
            } else {
                nickname = UserDataPreference.GetUserData(getContext(), "Email", null);
                if (nickname != null && nickname.length() > 0) {
                    user_name.setText(nickname);
                }
            }
        }


        if (OznerDeviceManager.Instance() != null) {
            myDeviceList.clear();
            //获取本地数据库设备列表
            OznerDevice[] list = OznerDeviceManager.Instance().getDevices();
            if (list != null && list.length > 0) {
                while (myDeviceList.size() != list.length) {
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
                                if (b == myDeviceList.size()) {
                                    myDeviceList.add(devicedata);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void InitParentData() {
        if (myDeviceList == null)
            myDeviceList = new ArrayList<DeviceData>();
        if (myDeviceList.size() > 0) {
            ShowDeviceView(getView());
        } else {
            ShowNoDeviceView(getView());
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //   InitData();
        LocalInitData();
        adapter = new SlideAdapter(getContext(), myDeviceList);
        mDslv.setAdapter(adapter);
        InitParentData();
        setListAdapter();
        onClickListener();
        mDslv.setDropListener(onDrop);
        mDslv.setRemoveListener(onRemove);
    }

    /*
     * 点击监听
     */
    private void onClickListener() {
        addDvice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if (((OznerApplication) getActivity().getApplication()).isLoginPhone()) {
                    ((MainActivity) getActivity()).myOverlayDrawer.toggleMenu();
//                } else {
//                    ((MainEnActivity) getActivity()).myOverlayDrawer.toggleMenu();
//                }
                if (OznerPreference.IsLogin(getActivity())) {
                    Intent intent = new Intent(getActivity(), AddDeviceActivity.class);
                    getActivity().startActivity(intent);
                } else {
                    Toast.makeText(getContext(), getString(R.string.ShouldLogin), Toast.LENGTH_SHORT).show();
                }
            }
        });
        ListView lv = mDslv;
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                DeviceData slilde = myDeviceList.get(arg2);
                myListener.ShowContent(PageState.DEVICECHANGE, slilde.getMac());
                adapter.notifyDataSetChanged();

            }
        });

      /*  lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long arg3) {

                SilideViewHolder slideview=(SilideViewHolder)adapter.myViewGroup.get(position).getTag();
                SilideViewHolder lastview=null;
                //获取上一个页面
                if(adapter.lastview!=null)
                {
                    try {
                        if(adapter.lastview!=null) {

                            lastview = (SilideViewHolder) adapter.lastview.getTag();
                            lastview.ll_bg.setBackgroundColor(ContextCompat.getColor(getContext(),R.color.draglist_bkg));
                        }
                    }catch (Exception ex){ex.printStackTrace();lastview=null;}
                }
                adapter.lastview=adapter.myViewGroup.get(position);
                DeviceData slilde = myDeviceList.get(position);
                String type= slilde.getDeviceType();
                {
                    if(CupManager.IsCup(type))
                    {
                        if(lastview!=null)
                            lastview.icon.setImageResource(R.drawable.slide_left_beizi);
                        if(slideview!=null)
                            slideview.icon.setImageResource(R.mipmap.icon_beizi);
                    }
                    else if(TapManager.IsTap(type))
                    {
                        if(lastview!=null)
                            lastview.icon.setImageResource(R.drawable.slide_left_tantou);
                        if(slideview!=null)
                            slideview.icon.setImageResource(R.mipmap.icon_tantou);
                    }
                }
                if(slilde.getOznerDevice()!=null)
                {
                    //更新状态
                    try {
                        BaseDeviceIO.ConnectStatus connectStatus = slilde.getOznerDevice().connectStatus();
                        slideview.desc_con.setText(constate[connectStatus.ordinal()]);
                    }catch (Exception ex){ex.printStackTrace();}
                }
                else {
                    //更新状态
                    slideview.desc_con.setText(constate[0]);
                }
                try {
                    slideview.desc_text.setText(slilde.getName());
                }catch (Exception ex){ex.printStackTrace();
                    try{slideview.desc_text.setText(slilde.getMac());}
                    catch (Exception e){e.printStackTrace();}
                }
                slideview.ll_bg.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
                myListener.ShowContent(PageState.DEVICECHANGE, slilde.getMac());
//                ((CChatFragment)getActivity()).ShowContent(PageState.DEVICECHANGE,slilde.getMac());
            }
        });*/
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
                                           long arg3) {
//                String message = String.format("Long-clicked item %d", arg2);
//                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();

                return true;
            }
        });
        class MyViewHolder {
            public TextView desc_text, desc_con;
            public ImageView icon;
        }
    }

    private void setListAdapter() {
        adapter = new SlideAdapter(getContext(), myDeviceList);
        mDslv.setAdapter(adapter);
    }

    private ArrayList<SlideBean> fillData() {
        int[] icon = {R.mipmap.icon_beizi, R.mipmap.icon_tantou, R.mipmap.icon_jingshui,
                R.mipmap.ic_launcher, R.mipmap.ic_launcher, R.mipmap.ic_launcher, R.mipmap.ic_launcher};
        String[] title = {"Cups", "Sensor", "Water purification", "Water purification", "Water purification", "hhhss", "hhssss"};
        String[] desc = {"我的家庭", "我的办公室", "我的朋友", "我的家庭", "我的家庭", "我的家庭", "我的家庭"};
        int n = icon.length;
        ArrayList<SlideBean> listBean = new ArrayList<SlideBean>();
        for (int i = 0; i < n; i++) {
            SlideBean bean = new SlideBean(icon[i], title[i], desc[i]);
            listBean.add(bean);
        }
        return listBean;
    }

    /*
     * Called in onCreateView.
     * Override this to provide a custom
     * DragSortController.
     */
    public DragSortController buildController(DragSortListView dslv) {
        // defaults are
        //   dragStartMode = onDown
        //   removeMode = flingRight
        MyDSController controller = new MyDSController(dslv);
        controller.setDragHandleId(R.id.drag_handle);
        controller.setClickRemoveId(R.id.click_remove);
        controller.setRemoveEnabled(removeEnabled);
        controller.setSortEnabled(sortEnabled);
        controller.setDragInitMode(dragStartMode);
        controller.setRemoveMode(removeMode);
        return controller;
    }

    /*
     * 监听器在手机拖动停下的时候触发
     */
    private DragSortListView.DropListener onDrop =
            new DragSortListView.DropListener() {
                @Override
                public void drop(int from, int to) {
                    if (from != to) {
                        DeviceData item = adapter.getItem(from);
                        adapter.remove(item);
                        adapter.insert(item, to);
                        List<DeviceData> beans = adapter.getSortList();
                        int a = 0;
                        for (DeviceData bean : beans) {
                            System.out.print(bean.getMac() + ", ");
                            bean.getOznerDevice().setAppdata(PageState.sortPosi, a);
                            a++;
                        }
                    }
                }
            };
    /*
     * 删除监听器，点击左边差号就触发。删除item操作。
     */
    private DragSortListView.RemoveListener onRemove =
            new DragSortListView.RemoveListener() {
                @Override
                public void remove(int which) {
                    adapter.remove(adapter.getItem(which));
                }
            };

    private class MyDSController extends DragSortController {

        DragSortListView mDslv;

        public MyDSController(DragSortListView dslv) {
            super(dslv);
//            setDragHandleId(R.id.text);
            mDslv = dslv;
        }

        @Override
        public View onCreateFloatView(int position) {
            View v = adapter.getView(position, null, mDslv);
            v.setBackgroundColor(Color.WHITE);
            //拖动背景
//            v.getBackground().setLevel(10000);
//            v.setBackgroundColor(getResources().getColor(R.color.white));
            return v;
        }

        @Override
        public void onDestroyFloatView(View floatView) {
            //do nothing; block super from crashing
        }

    }

    @Override
    public void ShowContent(int i, String mac) {

    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        try {
            myListener = (FootFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void ChangeRawRecord() {

    }

    /*
    * 接收智能水杯数据变化
    * */
    @Override
    public void CupSensorChange(String address) {
    }

    /*
    * 设备列表发生变化
    * */
    @Override
    public void DeviceDataChange() {
        myDeviceList = ((BaseMainActivity) getActivity()).myDeviceList;
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case PageState.DEVICECHANGE:
                LocalInitData();
                InitParentData();
                break;
        }
    }

    @Override
    public void ContentChange(String mac, String state) {
        InitParentData();
    }

    @Override
    public void RecvChatData(String data) {

    }
}
