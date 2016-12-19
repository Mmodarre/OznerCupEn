package com.ozner.qianye.mycenter.CenterBean;

/**
 * Created by xinde on 2016/1/3.
 */
public class LeaveMessage {
    private int id;
    private String senduserid;
    private String recvuserid;
    private String message;
    private long stime;
    private String Mobile;
    private String Nickname;
    private String Icon;

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSenduserid() {
        return this.senduserid;
    }

    public void setSenduserid(String senduserid) {
        this.senduserid = senduserid;
    }

    public String getRecvuserid() {
        return this.recvuserid;
    }

    public void setRecvuserid(String recvuserid) {
        this.recvuserid = recvuserid;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getStime() {
        return this.stime;
    }

    public void setStime(String stime) {
        this.stime = Long.parseLong(stime.replace("/Date(", "").replace(")/", ""));
    }

    public String getMobile() {
        return this.Mobile;
    }

    public void setMobile(String mobile) {
        this.Mobile = mobile;
    }

    public String getNickname() {
        return this.Nickname;
    }

    public void setNickname(String nickname) {
        this.Nickname = nickname;
    }

    public String getIcon() {
        return this.Icon;
    }

    public void setIcon(String icon) {
        this.Icon = icon;
    }
}
