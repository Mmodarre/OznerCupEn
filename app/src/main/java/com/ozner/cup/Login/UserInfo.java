package com.ozner.cup.Login;

import java.io.Serializable;

public class UserInfo implements Serializable {
    private String userIcon;
    private String userName;
    private Gender userGender;
    private String userId;

    public String getUserIcon() {
        return userIcon;
    }

    public void setUserIcon(String userIcon) {
        this.userIcon = userIcon;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Gender getUserGender() {
        return userGender;
    }

    public void setUserGender(Gender userGender) {
        this.userGender = userGender;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userNote) {
        this.userId = userNote;
    }

    public static enum Gender {MALE, FEMALE}

}
