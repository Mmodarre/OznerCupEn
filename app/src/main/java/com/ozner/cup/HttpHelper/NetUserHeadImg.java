package com.ozner.cup.HttpHelper;

import android.app.Activity;

import com.ozner.cup.Command.UserDataPreference;

import org.json.JSONObject;

/**
 * Created by C-sir@hotmail.com  on 2015/12/14.
 */
public class NetUserHeadImg {
    public String mobile;
    public String nickname;
    public String headimg;
    public String gradename;
    public int Score;
    public int Status;

    public NetUserHeadImg() {
    }

    public void fromJSONobject(JSONObject jsonObject) {
        if (jsonObject != null) {
            try {
                mobile = jsonObject.get("mobile").toString();
            } catch (Exception ex) {
                ex.printStackTrace();
                mobile = "";
            }
            try {
                nickname = jsonObject.get("nickname").toString();
            } catch (Exception ex) {
                ex.printStackTrace();
                nickname = "";
            }
            try {
                headimg = jsonObject.get("headimg").toString();
            } catch (Exception ex) {
                ex.printStackTrace();
                headimg = "";
            }
            try {
                gradename = jsonObject.get("GradeName").toString();
            } catch (Exception ex) {
                ex.printStackTrace();
                gradename = "";
            }
            try {
                Score = Integer.parseInt(jsonObject.get("Score").toString());
            } catch (Exception ex) {
                ex.printStackTrace();
                Score = 0;
            }
            try {
                Status = Integer.parseInt(jsonObject.get("Status").toString());
            } catch (Exception ex) {
                ex.printStackTrace();
                Status = 0;
            }
        }
    }

    public void fromPreference(Activity activity) {
        mobile = UserDataPreference.GetUserData(activity, "mobile", "");
        nickname = UserDataPreference.GetUserData(activity, "nickname", "");
        headimg = UserDataPreference.GetUserData(activity, "headimg", "");
        gradename = UserDataPreference.GetUserData(activity, "GradeName", "");
        try {
            Score = Integer.parseInt(UserDataPreference.GetUserData(activity, "Score", "0"));
        } catch (Exception ex) {
            ex.printStackTrace();
            Score = 0;
        }
        try {
            Status = Integer.parseInt(UserDataPreference.GetUserData(activity, "Status", "0"));
        } catch (Exception ex) {
            ex.printStackTrace();
            Status = 0;
        }
    }
}
