package com.ozner.cup.mycenter.CenterBean;

/**
 * Created by xinde on 2016/1/2.
 */
public class LikeMeInfo {
    private int id;
    private String userid;
    private String likeuserid;
    private String devicetype;
    private long liketime;
    private String Mobile;
    private String Nickname;
    private String Icon;
    private int Score;

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserid() {
        return this.userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getLikeuserid() {
        return this.likeuserid;
    }

    public void setLikeuserid(String likeuserid) {
        this.likeuserid = likeuserid;
    }

    public String getDevicetype() {
        return this.devicetype;
    }

    public void setDevicetype(String type) {
        this.devicetype = type;
    }

    public long getLiketime() {
        return this.liketime;
    }

    public void setLiketime(String liketime) {
        this.liketime = Long.parseLong(liketime.replace("/Date(", "").replace(")/", ""));
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

    public int getScore() {
        return this.Score;
    }

    public void setScore(int score) {
        this.Score = score;
    }
}
