package com.ozner.qianye.mycenter;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import com.ozner.qianye.BaiduPush.PushOperationAction;
import com.ozner.qianye.Command.CustomToast;
import com.ozner.qianye.Command.ImageHelper;
import com.ozner.qianye.Command.OznerCommand;
import com.ozner.qianye.Command.OznerPreference;
import com.ozner.qianye.Command.UserDataPreference;
import com.ozner.qianye.Device.OznerApplication;
import com.ozner.qianye.HttpHelper.NetJsonObject;
import com.ozner.qianye.HttpHelper.NetUserVfMessage;
import com.ozner.qianye.HttpHelper.OznerDataHttp;
import com.ozner.qianye.R;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

/*
* Created by xinde on 2015/12/09
 */
public class DealVerifyActivity extends AppCompatActivity implements View.OnClickListener {
    private final int VERFIY_MSG = 2;
    private final int DEALVERFIY_SUCCESS = 3;
    private final int DEALVERFIY_FAIL = 4;
    private final int DEALVERFIY_BEGIN = 5;
    List<NetUserVfMessage> vfMsglist = new ArrayList<NetUserVfMessage>();
    LinearLayout llay_verifyloading;
    ProgressBar pb_verifyloading;
    TextView tv_verifyloading;

    //    RelativeLayout rlay_back;
    ListView lv_verifyMsgList;
    private TextView toolbar_text;
    VerifyAdapter verifyAdapter;
    String muserid = "";
    DealVerifyHandler dealVerifyHandler = new DealVerifyHandler();
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal_verify);
        OznerApplication.changeTextFont((ViewGroup) getWindow().getDecorView());
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            //更改状态栏颜色
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.fz_blue));
            //更改底部导航栏颜色(限有底部的手机)
            window.setNavigationBarColor(ContextCompat.getColor(this, R.color.fz_blue));
        }
        progressDialog = new ProgressDialog(DealVerifyActivity.this);
        progressDialog.setMessage(getString(R.string.Center_sending_req));

        toolbar_text = (TextView) findViewById(R.id.toolbar_text);
        toolbar_text.setText(getString(R.string.Center_Verify_Msg));
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            toolbar.setBackgroundColor(getResources().getColor(R.color.MyCenter_ToolBar, null));
        } else {
            toolbar.setBackgroundColor(getResources().getColor(R.color.MyCenter_ToolBar));
        }
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(12);
                DealVerifyActivity.this.finish();
            }
        });

//        rlay_back = (RelativeLayout) findViewById(R.id.rlay_back);
        lv_verifyMsgList = (ListView) findViewById(R.id.lv_verifyMsgList);
        llay_verifyloading = (LinearLayout) findViewById(R.id.llay_verifyloading);
        tv_verifyloading = (TextView) findViewById(R.id.tv_verifyloading);
        pb_verifyloading = (ProgressBar) findViewById(R.id.pb_verifyloading);
//        rlay_back.setOnClickListener(this);

        verifyAdapter = new VerifyAdapter(DealVerifyActivity.this);
        lv_verifyMsgList.setAdapter(verifyAdapter);

    }

    @Override
    protected void onResume() {
        muserid = UserDataPreference.GetUserData(getBaseContext(), UserDataPreference.UserId, null);
        if (muserid != null && muserid.length() > 0) {
            reloadVerifyMsgList();
        }
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.rlay_back:
//                onBackPressed();
//                break;
        }
    }

    private void verifyLoading() {
        tv_verifyloading.setText(getString(R.string.Center_Loading));
        lv_verifyMsgList.setVisibility(View.GONE);
        pb_verifyloading.setVisibility(View.VISIBLE);
        llay_verifyloading.setVisibility(View.VISIBLE);
        tv_verifyloading.setVisibility(View.VISIBLE);
    }

    private void verifyLoadFail() {
        tv_verifyloading.setText(getString(R.string.Center_None));
        lv_verifyMsgList.setVisibility(View.GONE);
        tv_verifyloading.setVisibility(View.VISIBLE);
        pb_verifyloading.setVisibility(View.INVISIBLE);
        llay_verifyloading.setVisibility(View.VISIBLE);
    }

    private void verifyLoadSuccess() {
        llay_verifyloading.setVisibility(View.GONE);
        lv_verifyMsgList.setVisibility(View.VISIBLE);
    }

    private void reloadVerifyMsgList() {
        verifyLoading();
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<NetUserVfMessage> vfInfoList = OznerCommand.GetUserVerifMessage(DealVerifyActivity.this);
                Message message = new Message();
                message.what = VERFIY_MSG;
                message.obj = vfInfoList;
                dealVerifyHandler.sendMessage(message);
            }
        }).start();
    }

    class DealVerifyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            switch (msg.what) {
                case VERFIY_MSG:
                    vfMsglist = (List<NetUserVfMessage>) msg.obj;
                    if (vfMsglist != null && vfMsglist.size() > 0) {
                        verifyLoadSuccess();
                        verifyAdapter.relaodData(vfMsglist);
                    } else {
                        verifyLoadFail();
                    }
                    break;
                case DEALVERFIY_SUCCESS:
                    int successPos = (int) msg.obj;
                    vfMsglist.get(successPos).Status = 2;
                    verifyAdapter = new VerifyAdapter(getBaseContext());
                    verifyAdapter.relaodData(vfMsglist);
                    lv_verifyMsgList.setAdapter(verifyAdapter);
                    break;
                case DEALVERFIY_FAIL:
                    CustomToast.showToastCenter(DealVerifyActivity.this, getString(R.string.Center_add_failed));
                    break;
                case DEALVERFIY_BEGIN:
                    progressDialog.show();
                    break;
            }
            super.handleMessage(msg);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    class VerifyAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private List<NetUserVfMessage> verifyList = new ArrayList<NetUserVfMessage>();
        private Context mContext;
        //        private MyHandler myHandler;
        ImageHelper imageHelper;
        MyLoadImgListener imageLoadListener;
        ViewHolder viewHolder;
        //       LoadingDialog  loadingDialog;


        class MyLoadImgListener extends SimpleImageLoadingListener {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                ((ImageView) view).setImageBitmap(ImageHelper.toRoundBitmap(DealVerifyActivity.this, loadedImage));
                super.onLoadingComplete(imageUri, view, loadedImage);
            }
        }

        public VerifyAdapter(Context context) {
            this.mContext = context;
            mInflater = LayoutInflater.from(context);
//            myHandler = new MyHandler();
            imageHelper = new ImageHelper(context);
            imageLoadListener = new MyLoadImgListener();
            imageHelper.setImageLoadingListener(imageLoadListener);

        }

        public void relaodData(List<NetUserVfMessage> datalist) {
            this.verifyList = datalist;
            this.notifyDataSetInvalidated();
        }

        @Override
        public int getCount() {
            return verifyList.size();
        }

        @Override
        public Object getItem(int position) {
            return verifyList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            if (null == convertView) {
                viewHolder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.verify_msg_list_item, null);
                OznerApplication.changeTextFont((ViewGroup) convertView);
                viewHolder.iv_headImg = (ImageView) convertView.findViewById(R.id.iv_item_headImg);
                viewHolder.tv_friendName = (TextView) convertView.findViewById(R.id.tv_friendName);
                viewHolder.tv_verifyMsg = (TextView) convertView.findViewById(R.id.tv_verifyMsg);
                viewHolder.tv_waitVerify = (TextView) convertView.findViewById(R.id.tv_waitVerify);
                viewHolder.btn_addFriend = (Button) convertView.findViewById(R.id.btn_addFriend);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            final NetUserVfMessage netUserVfMessage = verifyList.get(position);

            if (netUserVfMessage.Icon != null && netUserVfMessage.Icon != "") {
                imageHelper.loadImage(viewHolder.iv_headImg, netUserVfMessage.Icon);
            } else {
                viewHolder.iv_headImg.setImageResource(R.mipmap.icon_default_headimage);
            }
//            viewHolder.iv_headImg.setImageResource(R.mipmap.icon_default_headimage);
            if (netUserVfMessage.Nickname != null && netUserVfMessage.Nickname != "") {
                viewHolder.tv_friendName.setText(netUserVfMessage.Nickname);
            } else {
                viewHolder.tv_friendName.setText(netUserVfMessage.OtherMobile);

            }

            StringBuilder verifyMsg = new StringBuilder(mContext.getResources().getString(R.string.Center_Verify_Msg));
            verifyMsg.append(":");
            verifyMsg.append(netUserVfMessage.RequestContent);
            viewHolder.tv_verifyMsg.setText(verifyMsg);
            if (netUserVfMessage.Status == 2) {//已经通过
                viewHolder.btn_addFriend.setVisibility(View.GONE);
                viewHolder.tv_waitVerify.setVisibility(View.VISIBLE);
                viewHolder.tv_waitVerify.setText(getResources().getString(R.string.Center_Added));
            } else if (netUserVfMessage.Status == 1) {//没有通过
                viewHolder.tv_waitVerify.setVisibility(View.GONE);
                viewHolder.btn_addFriend.setVisibility(View.VISIBLE);
                viewHolder.btn_addFriend.setTag(position);
                viewHolder.btn_addFriend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        viewHolder.btn_addFriend.setEnabled(false);
                        final int pos = (int) v.getTag();
                        final String id = String.valueOf(netUserVfMessage.ID);
                        dealVerifyHandler.sendEmptyMessage(DEALVERFIY_BEGIN);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                dealVerify(viewHolder, id, pos);
                            }
                        }).start();
                    }
                });
            }
            return convertView;
        }

        class ViewHolder {
            public ImageView iv_headImg;
            public TextView tv_verifyMsg;
            public Button btn_addFriend;
            public TextView tv_friendName;
            public TextView tv_waitVerify;
        }

        private void dealVerify(ViewHolder holder, String id, int pos) {
            if (id != null && id != "") {
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("usertoken", OznerPreference.UserToken(mContext)));
                params.add(new BasicNameValuePair("id", id));
                NetJsonObject result = OznerDataHttp.OznerWebServer(mContext, OznerPreference.ServerAddress(mContext) + "/OznerServer/AcceptUserVerif", params);
                if (result.state > 0) {
                    Message message = new Message();
//                    message.what = 0x131;
//                    message.obj = holder;
//                    myHandler.sendMessage(message);
                    message.what = DEALVERFIY_SUCCESS;
                    message.obj = pos;
                    dealVerifyHandler.sendMessage(message);
                } else {
                    Message message = new Message();
//                    message.what = 0x132;
//                    message.obj = holder;
//                    myHandler.sendMessage(message);
                    message.what = DEALVERFIY_FAIL;
                    message.obj = pos;
                    dealVerifyHandler.sendMessage(message);
                }
            }
        }

//        class MyHandler extends Handler {
//            @Override
//            public void handleMessage(Message msg) {
//
//                if (progressDialog.isShowing()) {
//                    progressDialog.dismiss();
//                }
//                switch (msg.what) {
//                    case 0x131://添加成功
//                        ViewHolder holder;
//                        holder = (ViewHolder) msg.obj;
//                        int pos = (int) (holder.btn_addFriend.getTag());
//                        holder.btn_addFriend.setVisibility(View.GONE);
//                        holder.tv_waitVerify.setVisibility(View.VISIBLE);
//                        verifyList.get(pos).Status = 2;
//                        VerifyAdapter.this.relaodData(verifyList);
////                        verifyAdapter
//                        break;
//                    case 0x132://添加失败
//                        ViewHolder holderfail;
//                        holderfail = (ViewHolder) msg.obj;
//                        holderfail.btn_addFriend.setEnabled(true);
//                        holderfail.btn_addFriend.setVisibility(View.VISIBLE);
//                        holderfail.tv_waitVerify.setVisibility(View.GONE);
//                        VerifyAdapter.this.relaodData(verifyList);
//                        Toast.makeText(mContext, "添加失败", Toast.LENGTH_SHORT).show();
//                        break;
//                }
//                super.handleMessage(msg);
//            }
//        }
    }

    class DealVerifyRecv extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(PushOperationAction.NewFriendVF)) {
                if (muserid != null && muserid.length() > 0) {
                    reloadVerifyMsgList();
                }
            }
        }
    }
}
