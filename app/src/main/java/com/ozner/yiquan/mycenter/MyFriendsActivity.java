package com.ozner.yiquan.mycenter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;

import com.ozner.yiquan.BaiduPush.OznerBroadcastAction;
import com.ozner.yiquan.Command.OznerCommand;
import com.ozner.yiquan.Command.OznerPreference;
import com.ozner.yiquan.Command.UserDataPreference;
import com.ozner.yiquan.Device.OznerApplication;
import com.ozner.yiquan.HttpHelper.NetJsonObject;
import com.ozner.yiquan.HttpHelper.NetUserVfMessage;
import com.ozner.yiquan.HttpHelper.OznerDataHttp;
import com.ozner.yiquan.R;
import com.ozner.yiquan.mycenter.CenterBean.CenterFriendInfo;
import com.ozner.yiquan.mycenter.CenterBean.CenterFriendItem;
import com.ozner.yiquan.mycenter.CenterBean.CenterNotification;
import com.ozner.yiquan.mycenter.CenterBean.ClassifiedRankInfo2;
import com.ozner.yiquan.mycenter.CenterBean.LeaveMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

/**
 * Created by xinde on 2015/12/08
 */

public class MyFriendsActivity extends AppCompatActivity implements ExpandableListView.OnGroupClickListener
        , View.OnClickListener, ExpandableListView.OnChildClickListener {
    private static final int VERFIY_MSG = 2;
    private static final int FRIEND_LOADED = 3;
    private static final int FRIEND_LOAD_FAIL = 4;
    private static final int RANK_LOADED = 5;
    private static final int NEW_MESSAGE = 6;
    MyFriendHandler mhandler;
    String userid, mMobile;
    List<NetUserVfMessage> vfMsglist;
    CenterFriendInfo friendinfo;
    List<ClassifiedRankInfo2> ranklist2;
    MyFriendListAdapter friendAdapter;
    ClassifiedRankListAdapter rankAdapter;
    ExpandableListView friendListView;
    ListView rankListView;
    TextView tv_ranklistnone;
    ImageView iv_addFriend, iv_newMsg, iv_verifyTips;
    RelativeLayout rlay_back, rlay_btnGroup, rlay_leaveMsg, rlay_hasFriend, rlay_loadingFriend;
    LinearLayout llay_NoFriends;

    ArrayList<CenterFriendItem> friendList;
    HashMap<String, List<LeaveMessage>> childMsgMap;

    Button btn_leMsgSend;
    EditText et_leaveMsg;
    int curGroupPos = -1;
    String curFriendUserid;
    Display ds;
    DisplayMetrics metrics;
    private MyFriendRecevie friendRecevie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_friends);
        OznerApplication.changeTextFont((ViewGroup) getWindow().getDecorView());
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            //更改状态栏颜色
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.fz_blue));
            //更改底部导航栏颜色(限有底部的手机)
            window.setNavigationBarColor(ContextCompat.getColor(this, R.color.fz_blue));
        }
        mhandler = new MyFriendHandler();
        friendList = new ArrayList<>();
        childMsgMap = new HashMap<>();
        vfMsglist = new ArrayList<NetUserVfMessage>();
        friendinfo = new CenterFriendInfo();
        ranklist2 = new ArrayList<>();
        friendRecevie = new MyFriendRecevie();
        IntentFilter actionFilter = new IntentFilter();
        actionFilter.addAction(OznerBroadcastAction.NewFriend);
        actionFilter.addAction(OznerBroadcastAction.NewFriendVF);
        actionFilter.addAction(OznerBroadcastAction.NewRank);
        actionFilter.addAction(OznerBroadcastAction.NewMessage);
        registerReceiver(friendRecevie, actionFilter);

        Log.e("tag", "usertoken:" + OznerPreference.UserToken(MyFriendsActivity.this));
        View viewleft = getLayoutInflater().inflate(R.layout.my_center_tab_wiget, null);
        ((TextView) viewleft.findViewById(R.id.tv_text)).setText(getString(R.string.Center_MyShort));
        View viewright = getLayoutInflater().inflate(R.layout.my_center_tab_wiget, null);
        ((TextView) viewright.findViewById(R.id.tv_text)).setText(getString(R.string.Center_MyFriend));
        TabHost tabHost = (TabHost) findViewById(R.id.tabHost);
        tabHost.setup();
        tabHost.addTab(tabHost.newTabSpec("tab01").setIndicator(viewleft).setContent(R.id.rlay_myShort));
        tabHost.addTab(tabHost.newTabSpec("tab02").setIndicator(viewright).setContent(R.id.rlay_myFriends));
        WindowManager wm = getWindowManager();
        ds = wm.getDefaultDisplay();
        metrics = new DisplayMetrics();
        ds.getMetrics(metrics);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        rlay_btnGroup = (RelativeLayout) findViewById(R.id.rlay_btnGroup);
        rlay_back = (RelativeLayout) findViewById(R.id.rlay_back);
        iv_addFriend = (ImageView) findViewById(R.id.iv_addFriend);
        rankListView = (ListView) findViewById(R.id.lv_ranklist);
        friendListView = (ExpandableListView) findViewById(R.id.elv_myFirendsList);
        iv_newMsg = (ImageView) findViewById(R.id.iv_newMsg);
        iv_verifyTips = (ImageView) findViewById(R.id.iv_verifyTips);
        llay_NoFriends = (LinearLayout) findViewById(R.id.llay_NoFriends);
        tv_ranklistnone = (TextView) findViewById(R.id.tv_ranklistnone);
        rlay_leaveMsg = (RelativeLayout) findViewById(R.id.rlay_leaveMsg);
        btn_leMsgSend = (Button) findViewById(R.id.btn_leaveMsgSend);
        et_leaveMsg = (EditText) findViewById(R.id.et_leaveMsg);
        rlay_hasFriend = (RelativeLayout) findViewById(R.id.rlay_hasFriend);
        rlay_loadingFriend = (RelativeLayout) findViewById(R.id.rlay_loadingFriend);
        rlay_back.setOnClickListener(this);
        iv_addFriend.setOnClickListener(this);
        iv_newMsg.setOnClickListener(this);
        btn_leMsgSend.setOnClickListener(this);

        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                if ("tab01" == tabId) {//我的排名页
                    rlay_btnGroup.setVisibility(View.GONE);
                } else {//我的好友页
                    CenterNotification.resetCenterNotify(getBaseContext(), CenterNotification.DealNewMessage);
                    rlay_btnGroup.setVisibility(View.VISIBLE);
                    if (friendList != null) {
                        friendAdapter.reloadGroupData(friendList);
                    } else {
                        asyncLoadFriendList(MyFriendsActivity.this);
                    }
                }
            }
        });

        et_leaveMsg.addTextChangedListener(new MyTextWatch());

        rankAdapter = new ClassifiedRankListAdapter(MyFriendsActivity.this);
        rankAdapter.setOnDeleteItemLinster(new ClassifiedRankListAdapter.onDeleteItemLinster() {
            @Override
            public void onDeleteItem(List<ClassifiedRankInfo2> reslist) {
                UserDataPreference.SetUserData(MyFriendsActivity.this, UserDataPreference.ClassifiedRankList, JSON.toJSONString(reslist));
            }
        });
        rankListView.setAdapter(rankAdapter);

        friendAdapter = new MyFriendListAdapter(this);
        friendListView.setAdapter(friendAdapter);
        friendListView.setOnGroupClickListener(this);
        friendListView.setOnChildClickListener(this);
        friendListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                showLeaveMsgDialog();
            }
        });
        friendListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int groupPosition) {
                hideLeaveMsgDialog();
            }
        });
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            friendListView.setChildDivider(getDrawable(R.drawable.center_leavemsg_child_divider));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        userid = UserDataPreference.GetUserData(getBaseContext(), UserDataPreference.UserId, "");
        Log.e("tag", "userid:" + userid);
        mMobile = UserDataPreference.GetUserData(MyFriendsActivity.this, UserDataPreference.Mobile, "");
        if (userid != null && userid.length() > 0) {
            initVerifyMsg();
            loadRankInfo(MyFriendsActivity.this);
            asyncLoadFriendList(MyFriendsActivity.this);
        }

    }

    private void setFriLoadingLayout() {
        rlay_hasFriend.setVisibility(View.GONE);
        llay_NoFriends.setVisibility(View.GONE);
        rlay_loadingFriend.setVisibility(View.VISIBLE);
    }

    private void setFriLaodedFail() {
        rlay_loadingFriend.setVisibility(View.GONE);
        rlay_hasFriend.setVisibility(View.GONE);
        llay_NoFriends.setVisibility(View.VISIBLE);
    }

    private void setFriLoadedSuccess() {
        rlay_loadingFriend.setVisibility(View.GONE);
        llay_NoFriends.setVisibility(View.GONE);
        rlay_hasFriend.setVisibility(View.VISIBLE);
    }

    class MyTextWatch implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() > 0) {
                btn_leMsgSend.setEnabled(true);
            } else {
                btn_leMsgSend.setEnabled(false);
            }
        }
    }

    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
        curGroupPos = groupPosition;
        int groupcount = friendAdapter.getGroupCount();
        for (int i = 0; i < groupcount; i++) {
            if (i != groupPosition)
                parent.collapseGroup(i);
        }
        if (!parent.isGroupExpanded(groupPosition)) {
            List<LeaveMessage> childMsgList = new ArrayList<LeaveMessage>();
            if (friendList.get(groupPosition).getCreateBy().equals(userid)) {
                curFriendUserid = friendList.get(groupPosition).getModifyBy();
            } else if (friendList.get(groupPosition).getModifyBy().equals(userid)) {
                curFriendUserid = friendList.get(groupPosition).getCreateBy();
            }
            if (!childMsgMap.containsKey(curFriendUserid)) {
                loadHistoryLeaveMsg(v, groupPosition, curFriendUserid);
            } else {
                childMsgList = childMsgMap.get(curFriendUserid);
                friendAdapter.reloadChildData(groupPosition, childMsgList);
                parent.expandGroup(groupPosition);
            }
        } else {
            parent.collapseGroup(groupPosition);
        }
        return true;
    }

    public void hideSoftInputView() {
        InputMethodManager manager = ((InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE));
        if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getCurrentFocus() != null)
                manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void showLeaveMsgDialog() {
        rlay_leaveMsg.setVisibility(View.VISIBLE);
        et_leaveMsg.setFocusable(true);
        et_leaveMsg.requestFocus();

        if (et_leaveMsg != null) {
            et_leaveMsg.setText("");
        }
        if (curGroupPos > -1 && mMobile != "") {
            CenterFriendItem friendInfo = friendList.get(curGroupPos);
            String hint = "";
            if (friendInfo.getMobile().equals(mMobile)) {
                hint = friendInfo.getNickname() != null && friendInfo.getNickname() != "" ? friendInfo.getNickname() : friendInfo.getFriendMobile();
            } else if (friendInfo.getFriendMobile().equals(mMobile)) {
                hint = friendInfo.getNickname() != null && friendInfo.getNickname() != "" ? friendInfo.getNickname() : friendInfo.getMobile();
            }
            if (hint != "")
                et_leaveMsg.setHint(getString(R.string.Center_Repeat) + hint);
        }
    }

    private void hideLeaveMsgDialog() {
        rlay_leaveMsg.setVisibility(View.GONE);
        hideSoftInputView();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_addFriend:
                if (userid != null && userid != "") {
                    Intent addFriendintent = new Intent(MyFriendsActivity.this, AddFriendActivity.class);
                    startActivity(addFriendintent);
                }
                break;
            case R.id.iv_newMsg:
                if (userid != null && userid != "") {
                    Intent dealVerifyIntent = new Intent(MyFriendsActivity.this, DealVerifyActivity.class);
                    startActivity(dealVerifyIntent);
                }
                break;
            case R.id.btn_leaveMsgSend:
                String msg = String.valueOf(et_leaveMsg.getText());
                if (null != msg && !"".equals(msg)) {
                    sendLeaveMsg(curGroupPos, curFriendUserid, msg);
                }
                break;
            case R.id.rlay_back:
                onBackPressed();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (110 == requestCode && RESULT_OK == resultCode) {
            initVerifyMsg();
            asyncLoadFriendList(MyFriendsActivity.this);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initVerifyMsg() {
        byte vernotify = CenterNotification.getCenterNotifyState(getBaseContext());
        vernotify &= CenterNotification.NewFriendVF;
        if (vernotify > 0) {
            iv_verifyTips.setVisibility(View.VISIBLE);
        } else {
            iv_verifyTips.setVisibility(View.GONE);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<NetUserVfMessage> vfInfoList = OznerCommand.GetUserVerifMessage(MyFriendsActivity.this);
                Message message = new Message();
                message.what = VERFIY_MSG;
                message.obj = vfInfoList;
                mhandler.sendMessage(message);
            }
        }).start();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        showLeaveMsgDialog();
        return true;
    }


    class MyFriendHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case VERFIY_MSG:
                    try {
                        vfMsglist = (List<NetUserVfMessage>) msg.obj;
                        int waitNum = 0;
                        for (NetUserVfMessage vfmsg : vfMsglist) {
                            if (vfmsg.Status != 2) {
                                waitNum++;
                            }
                        }
                        if (waitNum > 0) {
                            CenterNotification.setCenterNotify(getBaseContext(), CenterNotification.NewFriendVF);
                            iv_verifyTips.setVisibility(View.VISIBLE);
                        } else {
                            CenterNotification.resetCenterNotify(getBaseContext(), CenterNotification.DealNewFriendVF);
                            iv_verifyTips.setVisibility(View.GONE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case FRIEND_LOADED:

                    String res = (String) msg.obj;
                    friendinfo = JSON.parseObject(res, CenterFriendInfo.class);
                    try {
                        if (friendinfo.getState() > 0) {
                            setFriLoadedSuccess();
                            friendList = friendinfo.getFriendList();
                            friendAdapter = new MyFriendListAdapter(getBaseContext());
                            friendAdapter.reloadGroupData(friendList);
                            friendListView.setAdapter(friendAdapter);
                        } else {
                            if (friendList != null && friendList.size() > 0) {
                                friendAdapter.reloadGroupData(friendList);
                            } else {
                                setFriLaodedFail();
                            }
//                            llay_NoFriends.setVisibility(View.VISIBLE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        setFriLaodedFail();
                        Log.e("Center", "NewFriendEx:" + e.getMessage());
                    }
                    break;
                case FRIEND_LOAD_FAIL:
                    setFriLaodedFail();
                    break;
                case RANK_LOADED:
                    if (ranklist2 != null && ranklist2.size() > 0)
                        rankAdapter.reloadData(ranklist2);
                    break;
                case NEW_MESSAGE:
                    CenterNotification.resetCenterNotify(getBaseContext(), CenterNotification.DealNewMessage);
                    break;
            }
            super.handleMessage(msg);
        }
    }


    private void asyncLoadFriendList(final Activity activity) {
        setFriLoadingLayout();
        CenterNotification.resetCenterNotify(getBaseContext(), CenterNotification.DealNewFriend);
        final String loadFriendUrl = OznerPreference.ServerAddress(activity) + "/OznerServer/GetFriendList";
        loadFriendFromPerference(activity, loadFriendUrl);
        loadFriendlistFromNet(activity, loadFriendUrl);
    }

    private void loadFriendlistFromNet(final Activity activity, final String loadFriendUrl) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("usertoken", OznerPreference.UserToken(activity)));
                NetJsonObject netJsonObject = OznerDataHttp.OznerWebServer(activity, loadFriendUrl, params);
                if (netJsonObject.state >= 0) {
                    UserDataPreference.SetUserData(activity, loadFriendUrl, netJsonObject.value);
                    Message message = new Message();
                    message.what = FRIEND_LOADED;
                    message.obj = (Serializable) netJsonObject.value;
                    mhandler.sendMessage(message);
                } else {
                    mhandler.sendEmptyMessage(FRIEND_LOAD_FAIL);
                }
            }
        }).start();
    }

    private void loadFriendFromPerference(Activity activity, String loadFriendUrl) {
        String friendInfoStr = UserDataPreference.GetUserData(activity, loadFriendUrl, "");
        if (friendInfoStr != null && friendInfoStr != "") {
            Message message = new Message();
            message.what = FRIEND_LOADED;
            message.obj = friendInfoStr;
            mhandler.sendMessage(message);
        }
    }

    //我的排名方法
    private void loadRankInfo(final Activity activity) {
        CenterNotification.resetCenterNotify(MyFriendsActivity.this, CenterNotification.DealNewRank);
        new LoadClassifiedRankTask(activity).execute();
    }

    private class LoadClassifiedRankTask extends AsyncTask<String, Void, List<ClassifiedRankInfo2>> {
        private Context mContext;
        private String rankUrl;

        public LoadClassifiedRankTask(Context context) {
            this.mContext = context;
            rankUrl = OznerPreference.ServerAddress(context) + "/OznerDevice/GetRankNotify";
        }

        @Override
        protected void onPreExecute() {
            String rankStr = UserDataPreference.GetUserData(mContext, UserDataPreference.ClassifiedRankList, "");
//            Log.e("tag", "oldRandList:" + rankStr);
            try {
                if (rankStr != null && "" != rankStr) {
                    ranklist2 = JSON.parseArray(rankStr, ClassifiedRankInfo2.class);
                    rankAdapter.reloadData(ranklist2);
                }
                if (ranklist2.size() == 0) {
                    rankListView.setVisibility(View.GONE);
                    tv_ranklistnone.setVisibility(View.VISIBLE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                Log.e("tag", "MyFriendsActivity_loadRankInfo_Pre_Ex:" + ex.getMessage());
            }
        }

        @Override
        protected List<ClassifiedRankInfo2> doInBackground(String... params) {
            List<NameValuePair> parms = new ArrayList<NameValuePair>();
            parms.add(new BasicNameValuePair("usertoken", OznerPreference.UserToken(mContext)));
            NetJsonObject result = OznerDataHttp.OznerWebServer(mContext, rankUrl, parms);
            if (result != null) {
//                Log.e("tag", "LoadCenterRankRes:" + result.value);
                if (result.state > 0) {
                    JSONObject jsonObject = result.getJSONObject();
                    List<ClassifiedRankInfo2> rankInfo2s = new ArrayList<ClassifiedRankInfo2>();
                    try {
                        String rankNew = jsonObject.getString("data");
                        rankInfo2s = JSON.parseArray(rankNew, ClassifiedRankInfo2.class);
                        if (rankInfo2s.size() > 0) {
                            ranklist2.clear();
                            ranklist2.addAll(rankInfo2s);
//                            Log.e("tag", "newRankList:" + JSON.toJSONString(ranklist2));
                            UserDataPreference.SetUserData(mContext, UserDataPreference.ClassifiedRankList, JSON.toJSONString(ranklist2));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e("tag", "loadRankInfo_msg:" + result.value);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<ClassifiedRankInfo2> classifiedRankInfo2s) {
            if (ranklist2.size() > 0 && rankAdapter != null) {
                tv_ranklistnone.setVisibility(View.GONE);
                rankListView.setVisibility(View.VISIBLE);
                rankAdapter.reloadData(ranklist2);
            }
//            rankAdapter.reloadData(ranklist2);
        }
    }


    private void loadHistoryLeaveMsg(View v, int groupPos, String frienduserid) {
        new LoadLeaveMsgAsyncTask(v, groupPos, LoadLeaveMsgAsyncTask.TaskType.HISTORY_MSG).execute(frienduserid);
    }

    private void sendLeaveMsg(int groupPos, String frienduserid, String msg) {
        new LoadLeaveMsgAsyncTask(friendListView.getChildAt(groupPos), groupPos, LoadLeaveMsgAsyncTask.TaskType.LEAVE_MSG).execute(frienduserid, msg);
    }


    //加载留言
    private class LoadLeaveMsgAsyncTask extends AsyncTask<String, Integer, NetJsonObject> {
        private String taskType = TaskType.HISTORY_MSG;
        private ProgressDialog dialog;
        private String sendMsg;
        private int groupPos;
        private View rootView;

        public LoadLeaveMsgAsyncTask(View v, int groupPos, String taskType) {
            this.rootView = v;
            this.groupPos = groupPos;
            this.taskType = taskType;
        }

        @Override
        protected NetJsonObject doInBackground(String... params) {
            NetJsonObject netJsonObject = new NetJsonObject();
            if (params.length > 0) {
                String otheruserid = params[0];
                List<NameValuePair> requestParms = new ArrayList<>();
                requestParms.add(new BasicNameValuePair("usertoken", OznerPreference.UserToken(MyFriendsActivity.this)));
                requestParms.add(new BasicNameValuePair("otheruserid", otheruserid));
                if (TaskType.LEAVE_MSG == taskType) {//发送留言
                    if (params.length == 2) {
                        sendMsg = params[1];
                        requestParms.add(new BasicNameValuePair("message", sendMsg));
                        String leaveMsgUrl = OznerPreference.ServerAddress(MyFriendsActivity.this) + "/OznerDevice/LeaveMessage";
                        netJsonObject = OznerDataHttp.OznerWebServer(MyFriendsActivity.this, leaveMsgUrl, requestParms);
                    }
                } else if (TaskType.HISTORY_MSG == taskType) {//历史留言
                    String historyUrl = OznerPreference.ServerAddress(MyFriendsActivity.this) + "/OznerDevice/GetHistoryMessage";
                    netJsonObject = OznerDataHttp.OznerWebServer(MyFriendsActivity.this, historyUrl, requestParms);
                }
            }
            return netJsonObject;
        }

        @Override
        protected void onPreExecute() {
            if (TaskType.HISTORY_MSG == taskType) {
                dialog = ProgressDialog.show(MyFriendsActivity.this, "", getString(R.string.Center_Loading));
            } else {
                dialog = ProgressDialog.show(MyFriendsActivity.this, "", getString(R.string.Center_LeavingMsg));
            }
        }

        @Override
        protected void onPostExecute(NetJsonObject netJsonObject) {
            if (dialog != null) {
                dialog.dismiss();
            }
            if (netJsonObject != null) {
                Log.e("tag", "leveMsg_Result:" + netJsonObject.value);
                List<LeaveMessage> msglist = new ArrayList<>();
                CenterFriendItem friendItem = friendList.get(groupPos);
                if (netJsonObject.state > 0) {

                    if (TaskType.LEAVE_MSG == taskType) {//处理留言
                        if (sendMsg != null) {
                            if (childMsgMap.containsKey(curFriendUserid)) {
                                msglist = childMsgMap.get(curFriendUserid);
                            }
                            LeaveMessage leMsg = new LeaveMessage();
                            leMsg.setIcon(friendItem.getIcon());
                            leMsg.setMessage(sendMsg);
                            String friendmobile = "";
                            if (friendItem.getMobile().equals(mMobile)) {
                                friendmobile = friendItem.getFriendMobile();
                            } else if (friendItem.getFriendMobile().equals(mMobile)) {
                                friendmobile = friendItem.getMobile();
                            }
                            leMsg.setMobile(friendmobile);
                            leMsg.setNickname(friendItem.getNickname());
                            leMsg.setRecvuserid(curFriendUserid);
                            leMsg.setSenduserid(userid);
                            leMsg.setStime(String.valueOf(System.currentTimeMillis()));
                            msglist.add(leMsg);
                            childMsgMap.put(curFriendUserid, msglist);
                            friendList.get(groupPos).setMessageCount(friendList.get(groupPos).getMessageCount() + 1);
                            try {
                                TextView tvNum = (TextView) rootView.findViewById(R.id.tv_msgNum);
                                if (tvNum != null) {
                                    tvNum.setText(String.valueOf(friendList.get(groupPos).getMessageCount()));
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            friendAdapter.reloadChildData(groupPos, msglist);
                            friendListView.collapseGroup(groupPos);
                            if (!friendListView.isGroupExpanded(groupPos)) {
                                friendListView.expandGroup(groupPos);
                            }
                            hideLeaveMsgDialog();
                        }
                    } else if (TaskType.HISTORY_MSG == taskType) {//处理历史留言
                        JSONObject jsonObject = netJsonObject.getJSONObject();
                        try {
                            String arryStr = jsonObject.getString("data");
                            msglist = JSON.parseArray(arryStr, LeaveMessage.class);
                            childMsgMap.put(curFriendUserid, msglist);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        friendAdapter.reloadChildData(groupPos, msglist);
                        friendListView.collapseGroup(groupPos);
                        if (!friendListView.isGroupExpanded(groupPos)) {
                            friendListView.expandGroup(groupPos);
                        }
                    }
                } else {
                    msglist.clear();
                    LeaveMessage msgItem = new LeaveMessage();
                    msgItem.setMessage(getString(R.string.Center_None));
                    msglist.add(msgItem);
                    friendAdapter.reloadChildData(groupPos, msglist);
                    friendListView.collapseGroup(groupPos);
                    if (!friendListView.isGroupExpanded(groupPos)) {
                        friendListView.expandGroup(groupPos);
                    }
                }
            } else {
                Toast.makeText(MyFriendsActivity.this, getString(R.string.Center_LoadFail) + netJsonObject.state, Toast.LENGTH_SHORT).show();
            }
        }

        public class TaskType {
            public static final String LEAVE_MSG = "LEAVE_MSG";
            public static final String HISTORY_MSG = "HISTORY_MSG";
        }
    }

    class MyFriendRecevie extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case OznerBroadcastAction.NewFriend:
                    final String loadFriendUrl = OznerPreference.ServerAddress(MyFriendsActivity.this) + "/OznerServer/GetFriendList";
                    loadFriendlistFromNet(MyFriendsActivity.this, loadFriendUrl);
                    break;
                case OznerBroadcastAction.NewRank:
                    loadRankInfo(MyFriendsActivity.this);
                    break;
                case OznerBroadcastAction.NewFriendVF:
                    initVerifyMsg();
                    break;
                case OznerBroadcastAction.NewMessage:
                    mhandler.sendEmptyMessageDelayed(NEW_MESSAGE, 500);
                    break;
            }
        }
    }

    @Override
    public void finish() {
        if (vfMsglist != null) {
            vfMsglist.clear();
        }
        if (ranklist2 != null) {
            ranklist2.clear();
        }
        if (friendList != null) {
            friendList.clear();
        }
        if (childMsgMap != null) {
            childMsgMap.clear();
        }
        if (rankAdapter != null) {
            rankAdapter = null;
        }
        if (friendAdapter != null) {
            friendAdapter = null;
        }
        friendListView.destroyDrawingCache();
        super.finish();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(friendRecevie);
        releaseObject();
        super.onDestroy();
    }

    /**
     * 释放资源
     */
    private void releaseObject() {
        vfMsglist.clear();
        vfMsglist = null;
        mhandler = null;
        friendinfo = null;
        ranklist2.clear();
        ranklist2 = null;
        friendAdapter = null;
        friendList.clear();
        friendList = null;
        childMsgMap.clear();
        childMsgMap = null;
        friendRecevie = null;
        metrics = null;
        ds = null;
        System.gc();
    }


}

