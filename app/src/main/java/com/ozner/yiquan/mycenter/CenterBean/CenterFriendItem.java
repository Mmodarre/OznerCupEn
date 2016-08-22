package com.ozner.yiquan.mycenter.CenterBean;

/**
 * Created by xinde on 2015/12/22.
 */
public class CenterFriendItem {
    private int Id;
    private String Mobile;
    private String FriendMobile;
    private String RequestContent;
    private int Status;
    private int Disabled;
    private String CreateBy;
    private long CreateTime;
    private String ModifyBy;
    private long ModifyTime;
    private String Nickname;
    private String Icon;
    private int Score;
    private int MessageCount;

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        this.Id = id;
    }

    public String getMobile() {
        return Mobile;
    }

    public void setMobile(String mobile) {
        this.Mobile = mobile;
    }

    public String getFriendMobile() {
        return FriendMobile;
    }

    public void setFriendMobile(String friendMobile) {
        this.FriendMobile = friendMobile;
    }

    public String getRequestContent() {
        return RequestContent;
    }

    public void setRequestContent(String content) {
        this.RequestContent = content;
    }

    public int getStatus() {
        return Status;
    }

    public void setStatus(int status) {
        this.Status = status;
    }

    public int getDisabled() {
        return Disabled;
    }

    public void setDisabled(int disabled) {
        this.Disabled = disabled;
    }

    public String getCreateBy() {
        return CreateBy;
    }

    public void setCreateBy(String userid) {
        this.CreateBy = userid;
    }

    public long getCreateTime() {
        return CreateTime;
    }

    public void setCreateTime(String createTime) {
        this.CreateTime = Long.parseLong(createTime.replace("/Date(", "").replace(")/", ""));
    }

    public String getModifyBy() {
        return ModifyBy;
    }

    public void setModifyBy(String userid) {
        this.ModifyBy = userid;
    }

    public long getModifyTime() {
        return ModifyTime;
    }

    public void setModifyTime(String modifyTime) {
        this.ModifyTime = Long.parseLong(modifyTime.replace("/Date(", "").replace(")/", ""));
    }

    public String getNickname() {
        return Nickname;
    }

    public void setNickname(String nickname) {
        this.Nickname = nickname;
    }

    public String getIcon() {
        return Icon;
    }

    public void setIcon(String imgpath) {
        this.Icon = imgpath;
    }

    public int getScore() {
        return Score;
    }

    public void setScore(int score) {
        this.Score = score;
    }

    public int getMessageCount() {
        return this.MessageCount;
    }

    public void setMessageCount(int count) {
        this.MessageCount = count;
    }
}
