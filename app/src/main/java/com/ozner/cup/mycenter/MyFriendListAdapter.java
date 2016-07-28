package com.ozner.cup.mycenter;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.ozner.cup.Command.ImageHelper;
import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.Device.OznerApplication;
import com.ozner.cup.R;
import com.ozner.cup.mycenter.CenterBean.CenterFriendItem;
import com.ozner.cup.mycenter.CenterBean.LeaveMessage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by xinde on 2015/12/8.
 */
public class MyFriendListAdapter extends BaseExpandableListAdapter {
    private List<CenterFriendItem> friendInfoList;
    private List<LeaveMessage> leMsgList;
    private String mUserid, mMobile, mNickname;

    //    private Map<Integer, ArrayList<FriendInfo.FriendMessage>> childMsgMap = new HashMap<>();
    private LayoutInflater mInflater;
    private GroupViewHolder groupViewHolder;
    private ChildViewHolder childViewHolder;
    private Context mContext;
    private Calendar todayCal;

    public MyFriendListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
//        UserDataPreference.Init(mContext);
        mUserid = UserDataPreference.GetUserData(context, UserDataPreference.UserId, "");
        mMobile = UserDataPreference.GetUserData(context, UserDataPreference.Mobile, "");
        mNickname = UserDataPreference.GetUserData(context, UserDataPreference.NickName, "");
//        Log.e("tag", "mUserid:" + mUserid);
        this.mContext = context;
        friendInfoList = new ArrayList<CenterFriendItem>();
        leMsgList = new ArrayList<>();
        todayCal = Calendar.getInstance();
        todayCal.setTime(new Date(System.currentTimeMillis()));
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {

    }

    public void reloadGroupData(ArrayList<CenterFriendItem> infoList) {
//        this.friendInfoList.clear();
        this.friendInfoList = infoList;
        Log.e("FriendAdatper", "friendInfoList_size:" + friendInfoList.size());
        this.notifyDataSetInvalidated();
        Log.e("FriendAdatper", "friendInfoList_size:" + friendInfoList.size());
        this.notifyDataSetChanged();
    }

    public void reloadChildData(int groupPos, List<LeaveMessage> childDatalist) {

        this.leMsgList = childDatalist;
        Log.e("FriendAdatper", "leMsgList_size:" + leMsgList.size());
        this.notifyDataSetInvalidated();
    }

    @Override
    public int getGroupCount() {
        return friendInfoList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return leMsgList.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return friendInfoList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return leMsgList.get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        Log.e("FriendAdatper", "getGroupView:" + groupPosition);
        ImageHelper imageHelper = new ImageHelper(mContext);
        imageHelper.setImageLoadingListener(new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {

            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {

            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                ((ImageView) view).setImageBitmap(ImageHelper.toRoundBitmap(mContext, bitmap));
            }

            @Override
            public void onLoadingCancelled(String s, View view) {

            }
        });
        if (convertView == null) {
            groupViewHolder = new GroupViewHolder();
            convertView = mInflater.inflate(R.layout.friend_list_item, null);
            OznerApplication.changeTextFont((ViewGroup) convertView);
            groupViewHolder.iv_headImg = (ImageView) convertView.findViewById(R.id.iv_headImg);
            groupViewHolder.tv_newMsgNum = (TextView) convertView.findViewById(R.id.tv_newMsgNum);
            groupViewHolder.tv_friendName = (TextView) convertView.findViewById(R.id.tv_friendName);
            groupViewHolder.tv_msgNum = (TextView) convertView.findViewById(R.id.tv_msgNum);
            convertView.setTag(groupViewHolder);
        } else {
            groupViewHolder = (GroupViewHolder) convertView.getTag();
        }

        if (friendInfoList.get(groupPosition).getIcon() != null) {
            imageHelper.loadImage(groupViewHolder.iv_headImg, friendInfoList.get(groupPosition).getIcon());
        } else {
            groupViewHolder.iv_headImg.setImageResource(R.mipmap.icon_default_headimage);
        }
//        groupViewHolder.iv_headImg.setImageResource(friendInfoList.get(groupPosition).headImage);

//        if (friendInfoList.get(groupPosition).newMsgNum > 0) {
//            groupViewHolder.tv_newMsgNum.setVisibility(View.VISIBLE);
//            groupViewHolder.tv_newMsgNum.setText(friendInfoList.get(groupPosition).newMsgNum + "");
//        } else {
//            groupViewHolder.tv_newMsgNum.setVisibility(View.GONE);
//        }
        String nickname = friendInfoList.get(groupPosition).getNickname();
        String showName = "";
        if (nickname != null && !nickname.equals("")) {
            showName = nickname;
        } else {
            if (friendInfoList.get(groupPosition).getMobile().equals(mMobile)) {
                showName = friendInfoList.get(groupPosition).getFriendMobile();
            } else if (friendInfoList.get(groupPosition).getFriendMobile().equals(mMobile)) {
                showName = friendInfoList.get(groupPosition).getMobile();
//                showName = "我";
            }
        }
//        Log.e("tag","FriendMobile:"+friendInfoList.get(groupPosition).getFriendMobile());
//        Log.e("tag","Mobile:"+friendInfoList.get(groupPosition).getMobile());
        groupViewHolder.tv_friendName.setText(showName);
        groupViewHolder.tv_msgNum.setText(String.valueOf(friendInfoList.get(groupPosition).getMessageCount()));
//        groupViewHolder.tv_msgNum.setText(friendInfoList.get(groupPosition).MsgNum + "");

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            childViewHolder = new ChildViewHolder();
            convertView = mInflater.inflate(R.layout.friend_list_child_item, null);
            OznerApplication.changeTextFont((ViewGroup) convertView);
            childViewHolder.tv_msgDesc = (TextView) convertView.findViewById(R.id.tv_msgDesc);
            childViewHolder.tv_msgTime = (TextView) convertView.findViewById(R.id.tv_msgTime);
            convertView.setTag(childViewHolder);
        } else {
            childViewHolder = (ChildViewHolder) convertView.getTag();
        }

        SpannableStringBuilder span_Desc = new SpannableStringBuilder();
        String from = "";
        String to = "";
        if (leMsgList.get(childPosition).getSenduserid() != null
                && !leMsgList.get(childPosition).getSenduserid().equals("")
                && leMsgList.get(childPosition).getSenduserid().equals(mUserid)) {
//            from = mNickname != "" ? mNickname : mMobile;
            from = mContext.getString(R.string.center_i);
            to = friendInfoList.get(groupPosition).getNickname() != null
                    && !friendInfoList.get(groupPosition).getNickname().equals("")
                    ? friendInfoList.get(groupPosition).getNickname() : leMsgList.get(childPosition).getMobile();
        } else if (leMsgList.get(childPosition).getRecvuserid() != null
                && !leMsgList.get(childPosition).getRecvuserid().equals("")
                && leMsgList.get(childPosition).getRecvuserid().equals(mUserid)) {
//            to = mNickname != "" ? mNickname : mMobile;
            to = mContext.getString(R.string.center_i);
            from = friendInfoList.get(groupPosition).getNickname() != null
                    && !friendInfoList.get(groupPosition).getNickname().equals("")
                    ? friendInfoList.get(groupPosition).getNickname() : leMsgList.get(childPosition).getMobile();
        }
        from = (null != from) ? from : "";
        to = (null != to) ? to : "";
        span_Desc.append(from);
        if (from != "") {
//            span_Desc.setSpan(new ForegroundColorSpan(0xff3c89f2), 0, from.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            span_Desc.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.checked)), 0, from.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        }
        if ("" != from && "" != to) {
            span_Desc.append(mContext.getString(R.string.repeat));
        }
        span_Desc.append(to);
        if (to != "") {
            span_Desc.append(":");
            int toStart = span_Desc.toString().indexOf(to);
            span_Desc.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.checked)), toStart, toStart + to.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        }
        int start = span_Desc.toString().indexOf(mContext.getString(R.string.repeat));
//        int offset = 2;
//        if (!((OznerApplication) ((Activity) mContext).getApplication()).isLanguageCN()) {
//            offset = 5;
//        }
        if (start > 0) {
//            if (((OznerApplication) ((Activity) mContext).getApplication()).isLanguageCN())
            if (span_Desc.toString().indexOf("回复") > 0)
                span_Desc.setSpan(new ForegroundColorSpan(Color.BLACK), start, start + 2, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            else
                span_Desc.setSpan(new ForegroundColorSpan(Color.BLACK), start, start + 5, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        }
        String msgText = leMsgList.get(childPosition).getMessage();
        span_Desc.append(msgText);
        int msgStart = span_Desc.toString().indexOf(msgText);
        span_Desc.setSpan(new ForegroundColorSpan(Color.BLACK), msgStart, msgStart + msgText.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        childViewHolder.tv_msgDesc.setText(span_Desc);

        if (leMsgList.get(childPosition).getStime() != 0) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date(leMsgList.get(childPosition).getStime()));
            String msgTimeText = "";
            if (cal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR)
                    && cal.get(Calendar.MONTH) == todayCal.get(Calendar.MONTH)
                    && cal.get(Calendar.DATE) == todayCal.get(Calendar.DATE)) {
                String time = "";

                if (cal.get(Calendar.HOUR) < 10) {
                    time += "0";
                }
                time += String.valueOf(cal.get(Calendar.HOUR)) + ":";
                if (cal.get(Calendar.MINUTE) < 10) {
                    time += "0";
                }
                time += String.valueOf(cal.get(Calendar.MINUTE));

                if (cal.get(Calendar.AM_PM) == 0) {
                    msgTimeText = mContext.getString(R.string.Center_AM);
                } else if (cal.get(Calendar.AM_PM) == 1) {
                    msgTimeText = mContext.getString(R.string.Center_PM);
                }
                msgTimeText += time;
            } else {
                msgTimeText = cal.get(Calendar.YEAR) + "-";// + (cal.get(Calendar.MONTH) + 1) + "-" + cal.get(Calendar.DATE);
                if (cal.get(Calendar.MONTH) < 9) {
                    msgTimeText += "0";
                }
                msgTimeText += (cal.get(Calendar.MONTH) + 1) + "-";
                if (cal.get(Calendar.DATE) < 10) {
                    msgTimeText += "0";
                }
                msgTimeText += cal.get(Calendar.DATE);
            }

            childViewHolder.tv_msgTime.setText(msgTimeText);
        }
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public void onGroupExpanded(int groupPosition) {

    }

    @Override
    public void onGroupCollapsed(int groupPosition) {

    }

    @Override
    public long getCombinedChildId(long groupId, long childId) {
        return 0;
    }

    @Override
    public long getCombinedGroupId(long groupId) {
        return 0;
    }


    class GroupViewHolder {
        public ImageView iv_headImg;
        public TextView tv_newMsgNum;
        public TextView tv_friendName;
        public TextView tv_msgNum;
    }

    class ChildViewHolder {
        public TextView tv_msgDesc;
        public TextView tv_msgTime;
    }
}
