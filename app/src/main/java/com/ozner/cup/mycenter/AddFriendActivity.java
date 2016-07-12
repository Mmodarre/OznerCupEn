package com.ozner.cup.mycenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.ozner.cup.Command.ImageHelper;
import com.ozner.cup.Command.OznerCommand;
import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.Device.OznerApplication;
import com.ozner.cup.HttpHelper.NetUserHeadImg;
import com.ozner.cup.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xinde on 2015/12/09
 */

public class AddFriendActivity extends AppCompatActivity implements View.OnClickListener {
    private final int LOCAL_FRIEND = 0x01;
    private final int SEND_VERIFY = 0x02;
    private final int LOCAL_SEND_VERIFY = 0x03;
    private final int SEARCH_SUCCESS = 0x03;//查询好友结果
    private final int SEARCH_FAIL = 0x04;
    RelativeLayout rlay_searchBtn, rlay_searchSuccess;
    LinearLayout llay_searchResult, llay_localFriends, llay_loadWait;
    ListView lv_contactFriend;
    Button btn_resultaddFriend;
    TextView tv_waitVerify, tv_ResultName, tv_searchfail;
    private TextView toolbar_text, tv_waitText;
    ProgressBar pb_waiting;
    ImageView iv_ResultHeadImg;
    EditText et_searchNum;
    ContactAdapter adapter;
    MyHandler mhandler = new MyHandler();
    MyLoadImgListener mLoadListener = new MyLoadImgListener();
    List<NetUserHeadImg> localList = new ArrayList<>();
    int curClickPos = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        OznerApplication.changeTextFont((ViewGroup) getWindow().getDecorView());
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            //更改状态栏颜色
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.fz_blue));
            //更改底部导航栏颜色(限有底部的手机)
            window.setNavigationBarColor(ContextCompat.getColor(this, R.color.fz_blue));
        }
        toolbar_text = (TextView) findViewById(R.id.toolbar_text);
        toolbar_text.setText(getString(R.string.Center_Add_Friend));
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddFriendActivity.this.finish();
            }
        });
//        rlay_back = (RelativeLayout) findViewById(R.id.rlay_back);
        llay_loadWait = (LinearLayout) findViewById(R.id.llay_loadWait);
        pb_waiting = (ProgressBar) findViewById(R.id.pb_waiting);
        tv_waitText = (TextView) findViewById(R.id.tv_waitText);
        btn_resultaddFriend = (Button) findViewById(R.id.btn_resultaddFriend);
        rlay_searchBtn = (RelativeLayout) findViewById(R.id.rlay_searchBtn);
        lv_contactFriend = (ListView) findViewById(R.id.lv_contactFriend);
        tv_waitVerify = (TextView) findViewById(R.id.tv_waitVerify);
        et_searchNum = (EditText) findViewById(R.id.et_searchNum);
        llay_localFriends = (LinearLayout) findViewById(R.id.llay_localFriends);
        llay_searchResult = (LinearLayout) findViewById(R.id.llay_searchResult);
        tv_searchfail = (TextView) findViewById(R.id.tv_searchfail);
        rlay_searchSuccess = (RelativeLayout) findViewById(R.id.rlay_searchSuccess);
        iv_ResultHeadImg = (ImageView) findViewById(R.id.iv_ResultHeadImg);
        tv_ResultName = (TextView) findViewById(R.id.tv_ResultName);

//        rlay_back.setOnClickListener(this);
        btn_resultaddFriend.setOnClickListener(this);
        rlay_searchBtn.setOnClickListener(this);

        adapter = new ContactAdapter(this);
        lv_contactFriend.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        Log.e("tag", "mobile:" + UserDataPreference.GetUserData(AddFriendActivity.this, UserDataPreference.Mobile, null));
        initLocalFriendList(AddFriendActivity.this);
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.rlay_back:
//                onBackPressed();
//                break;
            case R.id.rlay_searchBtn:
                Toast toast = Toast.makeText(AddFriendActivity.this, getString(R.string.Center_InSurePhone), Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                if (et_searchNum.getText().length() > 0) {
                    String mobile = String.valueOf(et_searchNum.getText());
                    if (mobile.length() == 11) {
                        if (!UserDataPreference.GetUserData(AddFriendActivity.this, UserDataPreference.Mobile, null).equals(mobile)) {
                            searchFriend(mobile);
                            llay_searchResult.setVisibility(View.VISIBLE);
                            rlay_searchSuccess.setVisibility(View.GONE);
                            tv_searchfail.setText(getString(R.string.Center_Searching));
                            tv_searchfail.setVisibility(View.VISIBLE);
                        } else {
                            toast.setText(getString(R.string.Center_InSureFriendMobile));
                            toast.show();
                        }
                    } else {
                        toast.show();
                    }
                } else {
                    toast.show();
                }
                break;
            case R.id.btn_resultaddFriend:
                String mobile = String.valueOf(btn_resultaddFriend.getTag());
                Intent addFriendIntent = new Intent(AddFriendActivity.this, SendVerifyActivity.class);
                addFriendIntent.putExtra("mobile", mobile);
                startActivityForResult(addFriendIntent, SEND_VERIFY);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    class ContactAdapter extends BaseAdapter implements View.OnClickListener {
        private Context mContext;
        private LayoutInflater mInflater;
        private List<NetUserHeadImg> datalist = new ArrayList<NetUserHeadImg>();
        MyLoadImgListener imageLoadListener;
        ImageHelper imageHelper;

        public ContactAdapter(Context context) {
            this.mContext = context;
            mInflater = LayoutInflater.from(context);
            imageHelper = new ImageHelper(context);
            imageLoadListener = new MyLoadImgListener();
            imageHelper.setImageLoadingListener(imageLoadListener);
        }

        public void reloadData(List<NetUserHeadImg> datalist) {
            this.datalist = datalist;
            this.notifyDataSetInvalidated();
        }

        @Override
        public int getCount() {
            return datalist.size();
        }

        @Override
        public Object getItem(int position) {
            return datalist.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = new ViewHolder();
            if (null == convertView) {
                convertView = mInflater.inflate(R.layout.my_center_contact_list_item, null);
                OznerApplication.changeTextFont((ViewGroup) convertView);
                holder.iv_headImg = (ImageView) convertView.findViewById(R.id.iv_item_headImg);
                holder.tv_name = (TextView) convertView.findViewById(R.id.tv_friendName);
                holder.btn_add = (Button) convertView.findViewById(R.id.btn_addFriend);
                holder.tv_waitVerify = (TextView) convertView.findViewById(R.id.tv_waitVerify);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            NetUserHeadImg netUserHeadImg = (NetUserHeadImg) datalist.get(position);
            if (netUserHeadImg.headimg != null && netUserHeadImg.headimg.length() > 0) {
                imageHelper.loadImage(holder.iv_headImg, netUserHeadImg.headimg);
            } else {
                holder.iv_headImg.setImageResource(R.mipmap.icon_default_headimage);
            }
            holder.tv_name.setText((netUserHeadImg.nickname != null && netUserHeadImg.nickname.length() > 0) ? netUserHeadImg.nickname : netUserHeadImg.mobile);

            holder.btn_add.setTag(position);
            holder.btn_add.setOnClickListener(this);
            switch (netUserHeadImg.Status) {
                case 0://没有关系
                    holder.btn_add.setVisibility(View.VISIBLE);
                    holder.tv_waitVerify.setVisibility(View.GONE);
                    break;
                case 1:
                    holder.btn_add.setVisibility(View.GONE);
                    holder.tv_waitVerify.setVisibility(View.VISIBLE);
                    holder.tv_waitVerify.setText(getResources().getString(R.string.Center_Wait_Verify));
                    break;
                case 2:
                    holder.btn_add.setVisibility(View.GONE);
                    holder.tv_waitVerify.setVisibility(View.VISIBLE);
                    holder.tv_waitVerify.setText(getResources().getString(R.string.Center_Added));
                    break;
            }

            return convertView;
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.btn_addFriend) {
                int pos = (int) v.getTag();
                curClickPos = pos;
                String mobile = String.valueOf(datalist.get(pos).mobile);
                Intent addFriendIntent = new Intent(AddFriendActivity.this, SendVerifyActivity.class);
                addFriendIntent.putExtra("mobile", mobile);
                addFriendIntent.putExtra("clickPos", pos);
                startActivityForResult(addFriendIntent, LOCAL_SEND_VERIFY);
            }
        }

        class ViewHolder {
            public ImageView iv_headImg;
            public TextView tv_name;
            public Button btn_add;
            public TextView tv_waitVerify;
        }

        class MyLoadImgListener extends SimpleImageLoadingListener {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                ((ImageView) view).setImageBitmap(ImageHelper.toRoundBitmap(AddFriendActivity.this, loadedImage));
                super.onLoadingComplete(imageUri, view, loadedImage);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (RESULT_OK == resultCode) {
            switch (requestCode) {
                case SEND_VERIFY:
                    tv_waitVerify.setVisibility(View.VISIBLE);
                    tv_waitVerify.setText(getResources().getString(R.string.Center_Wait_Verify));
                    btn_resultaddFriend.setVisibility(View.GONE);
                    String searchmobile = String.valueOf(btn_resultaddFriend.getTag());
                    if (searchmobile != null && searchmobile != "") {
                        for (int i = 0; i < localList.size(); i++) {
                            if (localList.get(i).mobile.equals(searchmobile)) {
                                localList.get(i).Status = 1;
                            }
                        }
                        adapter.reloadData(localList);
                    }

                    break;
                case LOCAL_SEND_VERIFY:
                    int pos = data.getIntExtra("clickPos", -1);
                    if (pos > -1) {
                        localList.get(curClickPos).Status = 1;
                        adapter.reloadData(localList);
                    }
                    break;
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void searchFriend(final String mobile) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<NetUserHeadImg> listSerchInfo = OznerCommand.InitLocalPhoneHeadImg(AddFriendActivity.this, mobile);
                Message message = new Message();
                message.what = SEARCH_SUCCESS;
                if (listSerchInfo != null && listSerchInfo.size() > 0) {
                    message.obj = listSerchInfo.get(0);
                } else {
                    message.obj = null;
                }
                mhandler.sendMessage(message);
            }
        }).start();
    }

    private void initLocalFriendList(final Activity activity) {
        tv_waitText.setText(getString(R.string.Center_Loading));
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<NetUserHeadImg> localfriendlist = OznerCommand.InitLocalPhoneHeadImg(activity, null);
//                if (localfriendlist != null) {
                Message message = new Message();
                message.what = LOCAL_FRIEND;
                message.obj = localfriendlist;
                mhandler.sendMessage(message);
//                }
            }
        }).start();
    }

    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOCAL_FRIEND:
//                    Log.e("tag","Local_friend");
                    localList.clear();
                    List<NetUserHeadImg> resData = (List<NetUserHeadImg>) msg.obj;
                    if (resData != null && resData.size() > 0) {
                        for (int i = 0; i < resData.size(); i++) {
                            NetUserHeadImg resItem = resData.get(i);
                            if (resItem.Status >= 0) {
                                Log.e("AddFriend", "localFriend:" + resItem.mobile + " , " + resItem.nickname);
                                localList.add(resItem);
                            }
                        }
                        if (localList != null && localList.size() > 0) {
//                            llay_localFriends.setVisibility(View.VISIBLE);
                            lv_contactFriend.setVisibility(View.VISIBLE);
                            llay_loadWait.setVisibility(View.GONE);
                            adapter.reloadData(localList);
                        } else {
                            lv_contactFriend.setVisibility(View.GONE);
                            llay_loadWait.setVisibility(View.VISIBLE);
                            pb_waiting.setVisibility(View.GONE);
                            tv_waitText.setText(getString(R.string.Center_no_contacts_frient));
                        }
                    } else {
                        pb_waiting.setVisibility(View.GONE);
                        tv_waitText.setText(getString(R.string.Center_no_contacts_frient));
                    }
                    break;
                case SEARCH_SUCCESS:
                    NetUserHeadImg netUserHeadImg = (NetUserHeadImg) msg.obj;
                    llay_searchResult.setVisibility(View.VISIBLE);
                    if (netUserHeadImg != null && netUserHeadImg.Status >= 0) {
                        Log.i("tag", "netUserheadImg:" + netUserHeadImg.Status);
                        ImageHelper imageHelper = new ImageHelper(AddFriendActivity.this);
                        imageHelper.setImageLoadingListener(mLoadListener);
                        if (netUserHeadImg.headimg != null && netUserHeadImg.headimg.length() > 0) {
                            imageHelper.loadImage(iv_ResultHeadImg, netUserHeadImg.headimg);
                        } else {
                            iv_ResultHeadImg.setImageResource(R.mipmap.icon_default_headimage);
                        }
                        tv_ResultName.setText((netUserHeadImg.nickname != null && netUserHeadImg.nickname.length() > 0) ? netUserHeadImg.nickname : netUserHeadImg.mobile);
                        if (netUserHeadImg.Status == 0) {//没有关系
                            btn_resultaddFriend.setVisibility(View.VISIBLE);
                            btn_resultaddFriend.setTag(netUserHeadImg.mobile);
                            tv_waitVerify.setVisibility(View.GONE);
                        } else if (netUserHeadImg.Status == 1) {//等待验证
                            btn_resultaddFriend.setVisibility(View.GONE);
                            tv_waitVerify.setVisibility(View.VISIBLE);
                            tv_waitVerify.setText(getResources().getString(R.string.Center_Wait_Verify));
                        } else if (netUserHeadImg.Status == 2) {//已经添加
                            btn_resultaddFriend.setVisibility(View.GONE);
                            tv_waitVerify.setVisibility(View.VISIBLE);
                            tv_waitVerify.setText(getResources().getString(R.string.Center_Added));
                        }
                        rlay_searchSuccess.setVisibility(View.VISIBLE);
                        tv_searchfail.setVisibility(View.GONE);
                    } else {
                        Log.i("tag", "netUserheadImg is null");
                        Toast toast = Toast.makeText(AddFriendActivity.this, getString(R.string.Center_SearchFail), Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                        rlay_searchSuccess.setVisibility(View.GONE);
                        tv_searchfail.setText(getString(R.string.Center_SearchFail));
                        tv_searchfail.setVisibility(View.VISIBLE);
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    }

    class MyLoadImgListener extends SimpleImageLoadingListener {

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            ((ImageView) view).setImageBitmap(ImageHelper.toRoundBitmap(AddFriendActivity.this, loadedImage));
            super.onLoadingComplete(imageUri, view, loadedImage);
        }
    }

//    private SearchFriendInfo netSearchMyFriend(final Activity activity, String mobile) {
//        SearchFriendInfo searchFriendInfo = new SearchFriendInfo();
//        if (mobile != null && mobile != "") {
//            List<NameValuePair> params = new ArrayList<NameValuePair>();
//            params.add(new BasicNameValuePair("usertoken", OznerPreference.UserToken(activity)));
//            params.add(new BasicNameValuePair("jsonmobile", mobile));
//            NetJsonObject netJsonObject = OznerDataHttp.OznerWebServer(activity, "/OznerServer/searchFriend", params);
//            try {
//                JSONObject jsonObject = new JSONObject(netJsonObject.value);
//                if (jsonObject != null) {
//                    JSONObject statusObj = jsonObject.getJSONObject("friend");
//                    if (statusObj != null) {
//                        searchFriendInfo.status = 0;
//                    } else {
//                        searchFriendInfo.status = statusObj.getInt("status");
//                    }
//                    JSONObject friendObj = jsonObject.getJSONObject("userinfo");
//                    searchFriendInfo.headImg = friendObj.getString("ImgPath");
//                    searchFriendInfo.nickname = friendObj.getString("Nickname");
//                    searchFriendInfo.mobile = friendObj.getString("Mobile");
//                }
//            } catch (JSONException e) {
//                e.printStackTrace();
//                return null;
//            }
//        }
//        return searchFriendInfo;
//    }
//
//    private NetUserHeadImg netSearchFriend(final Activity activity, String mobile) {
//        NetUserHeadImg netUserHeadImg = new NetUserHeadImg();
//
//
//        if (mobile != null && mobile != "") {
//            List<NameValuePair> params = new ArrayList<NameValuePair>();
//            params.add(new BasicNameValuePair("usertoken", OznerPreference.UserToken(activity)));
//            params.add(new BasicNameValuePair("jsonmobile", mobile));
//            NetJsonObject netJsonObject = OznerDataHttp.OznerWebServer(activity, "/OznerServer/searchFriend", params);
//
//
//            if (netJsonObject.state > 0) {
//                try {
//                    JSONArray jarry = netJsonObject.getJSONObject().getJSONArray("data");
//                    if (jarry.length() > 0) {
//                        JSONObject jo = (JSONObject) jarry.get(0);
//                        netUserHeadImg.fromJSONobject(jo);
//                    } else {
//                        netUserHeadImg = null;
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                    netUserHeadImg = null;
//                }
//            } else {
//                netUserHeadImg = null;
//            }
//        } else {
//            netUserHeadImg = null;
//        }
//        return netUserHeadImg;
//    }
//
//    class SearchFriendInfo {
//        public String mobile;
//        public String nickname;
//        public String headImg;
//        public int status;
//    }


}
