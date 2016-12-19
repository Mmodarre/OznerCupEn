package com.ozner.qianye.mycenter;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.ozner.qianye.Command.ImageHelper;
import com.ozner.qianye.Command.OznerPreference;
import com.ozner.qianye.Command.UserDataPreference;
import com.ozner.qianye.Device.OznerApplication;
import com.ozner.qianye.HttpHelper.NetJsonObject;
import com.ozner.qianye.HttpHelper.OznerDataHttp;
import com.ozner.qianye.R;
import com.ozner.qianye.mycenter.CenterBean.LikeMeInfo;
import com.ozner.qianye.mycenter.CenterBean.RankType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
/*
* Created by xinde on 2015/12/10
 */

public class LikeMeActivity extends AppCompatActivity implements View.OnClickListener {
    //    RelativeLayout rlay_back;
    ListView lv_likeMeList;
    List<LikeMeInfo> likeMeInfoList = new ArrayList<>();
    LikeMeAdapter myAdapter;
    String userid = "";
    String usertoken = "";
    String ranktype = "";
    private TextView toolbar_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_like_me);
        OznerApplication.changeTextFont((ViewGroup) getWindow().getDecorView());
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            //更改状态栏颜色
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.fz_blue));
            //更改底部导航栏颜色(限有底部的手机)
            window.setNavigationBarColor(ContextCompat.getColor(this, R.color.fz_blue));
        }

        toolbar_text = (TextView) findViewById(R.id.toolbar_text);
        toolbar_text.setText(getString(R.string.Center_LikeMe));
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
                LikeMeActivity.this.finish();
            }
        });

        ranktype = getIntent().getStringExtra("rankType");
        Log.e("tag", "rankType:" + ranktype);
        if (null == ranktype || "" == ranktype) {
            ranktype = RankType.CupVolumType;
        }
        myAdapter = new LikeMeAdapter(LikeMeActivity.this);
        userid = UserDataPreference.GetUserData(LikeMeActivity.this, UserDataPreference.UserId, null);
        usertoken = OznerPreference.UserToken(LikeMeActivity.this);

//        rlay_back = (RelativeLayout) findViewById(R.id.rlay_back);
        lv_likeMeList = (ListView) findViewById(R.id.lv_likeMeList);
//        rlay_back.setOnClickListener(this);
//        reloadData();
//        lv_likeMeList.setAdapter(new SimpleAdapter(this, datalist, R.layout.center_like_me_list_item,
//                new String[]{"headImg", "friendName", "time"}, new int[]{R.id.iv_headImg, R.id.tv_name, R.id.tv_time}));
        lv_likeMeList.setAdapter(myAdapter);
    }

    @Override
    protected void onResume() {
        if (userid != null && userid != "") {
            new LikeMeAsyncTask().execute();
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    class LikeMeAsyncTask extends AsyncTask<String, Void, NetJsonObject> {
        private ProgressDialog dialog;

        @Override
        protected NetJsonObject doInBackground(String... params) {
            String wholikeMeUrl = OznerPreference.ServerAddress(LikeMeActivity.this) + "/OznerDevice/WhoLikeMe";
            List<NameValuePair> parms = new ArrayList<>();
            parms.add(new BasicNameValuePair("usertoken", usertoken));
            parms.add(new BasicNameValuePair("type", ranktype));
            NetJsonObject netJsonObject = OznerDataHttp.OznerWebServer(LikeMeActivity.this, wholikeMeUrl, parms);
            return netJsonObject;
        }

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(LikeMeActivity.this, "", getString(R.string.Center_Loading));
        }

        @Override
        protected void onPostExecute(NetJsonObject result) {
            if (dialog != null) {
                dialog.dismiss();
            }
            if (result != null && result.state > 0) {
                JSONObject jsonObject = result.getJSONObject();
                try {
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    if (jsonArray != null && jsonArray.length() > 0) {
                        likeMeInfoList = JSON.parseArray(jsonArray.toString(), LikeMeInfo.class);
                        myAdapter.reloadLikeData(likeMeInfoList);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (result != null) {
                Log.e("tag", "LikeMeActivity_result:" + result.value);
            }
        }
    }


    class LikeMeAdapter extends BaseAdapter {
        private List<LikeMeInfo> likeMeInfos;
        private Context mContext;
        private LikeMeHolder likeMeHolder;
        private LayoutInflater mInflater;
        ImageHelper imageHelper;
        MyLoadImgListener imageLoadListener;

        public LikeMeAdapter(Context context) {
            this.mContext = context;
            mInflater = LayoutInflater.from(context);
            likeMeInfos = new ArrayList<>();
            imageHelper = new ImageHelper(context);
            imageLoadListener = new MyLoadImgListener();
            imageHelper.setImageLoadingListener(imageLoadListener);
        }

        public void reloadLikeData(List<LikeMeInfo> list) {
            this.likeMeInfos = list;
            this.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return likeMeInfos.size();
        }

        @Override
        public Object getItem(int position) {
            return likeMeInfos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (null == convertView) {
                likeMeHolder = new LikeMeHolder();
                convertView = mInflater.inflate(R.layout.center_like_me_list_item, null);
                OznerApplication.changeTextFont((ViewGroup) convertView);
                likeMeHolder.iv_headImg = (ImageView) convertView.findViewById(R.id.iv_headImg);
                likeMeHolder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
                likeMeHolder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
                convertView.setTag(likeMeHolder);
            } else {
                likeMeHolder = (LikeMeHolder) convertView.getTag();
            }

            //设置头像
            if (likeMeInfos.get(position).getIcon() != null && likeMeInfos.get(position).getIcon() != ""
                    && likeMeInfos.get(position).getIcon().contains("http")) {
                imageHelper.loadImage(likeMeHolder.iv_headImg, likeMeInfos.get(position).getIcon());
            } else {
                likeMeHolder.iv_headImg.setImageResource(R.mipmap.icon_default_headimage);
            }
            //设置名字
            if (likeMeInfos.get(position).getNickname() != null && likeMeInfos.get(position).getNickname() != "") {
                likeMeHolder.tv_name.setText(likeMeInfos.get(position).getNickname());
            } else {
                likeMeHolder.tv_name.setText(likeMeInfos.get(position).getMobile() + "");
            }
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date(likeMeInfos.get(position).getLiketime()));
            String text = (cal.get(Calendar.MONTH) + 1) + mContext.getString(R.string.Center_Month)
                    + cal.get(Calendar.DAY_OF_MONTH) + mContext.getString(R.string.Center_Day);
            likeMeHolder.tv_time.setText(text);
            return convertView;
        }

        class LikeMeHolder {
            public ImageView iv_headImg;
            public TextView tv_name;
            public TextView tv_time;
        }

        class MyLoadImgListener extends SimpleImageLoadingListener {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                ((ImageView) view).setImageBitmap(ImageHelper.toRoundBitmap(LikeMeActivity.this, loadedImage));
                super.onLoadingComplete(imageUri, view, loadedImage);
            }
        }
    }
}
