package com.ozner.qianye.mycenter.CenterBean;

import java.util.ArrayList;

/**
 * Created by xinde on 2015/12/22.
 */
public class CenterFriendInfo {
    private int state;
    private ArrayList<CenterFriendItem> friendList = new ArrayList<>();

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public ArrayList<CenterFriendItem> getFriendList() {
        return friendList;
    }

    public void setFriendList(ArrayList<CenterFriendItem> friendList) {
        this.friendList = friendList;
    }
}
