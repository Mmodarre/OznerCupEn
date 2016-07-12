package com.ozner.cup.Command;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

import com.ozner.cup.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xinde on 2016/1/13.
 */
public class NetErrDecode {
    public static Map<Integer, String> ErrMap = new HashMap<>();

    static {
        ErrMap.put(-10011, "没有发现对应请求");
        ErrMap.put(-10010, "已经发送过请求");
        ErrMap.put(-10007, "Token验证失败");
        ErrMap.put(-10002, "验证码过期");
        ErrMap.put(-10003, "验证码错误");
        ErrMap.put(-10015, "设备没有发现");
        ErrMap.put(-10004, "异常错误");
        ErrMap.put(0, "失败");
        ErrMap.put(-10013, "和目标用户没有关系");
        ErrMap.put(-10014, "对方不是浩泽用户");
        ErrMap.put(-10008, "参数不能为空");
        ErrMap.put(-10009, "参数不能为空");
        ErrMap.put(-10012, "参数错误");
        ErrMap.put(1, "Success");
        ErrMap.put(-10005, "添加用户异常");
        ErrMap.put(-10006, "Token写入数据库失败");
        ErrMap.put(-10001, "用户名错误");
        ErrMap.put(-10016, "用户没有绑定百度设备id或手机类型");
        ErrMap.put(-10017, "没有此用户");
        ErrMap.put(-10018, "已经点过赞");
        ErrMap.put(-10019, "二维码无效");
        ErrMap.put(-10020, "二维码已经使用过了");
        ErrMap.put(-10021, "设备未绑定");
    }

    public static String getErrMsg(int errCode) {
        if (ErrMap.containsKey(errCode)) {
            return ErrMap.get(errCode);
        } else {
            return "";
        }
    }

    public static void ShowErrMsgDialog(Context context, int errCode, String defMsg) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        if (ErrMap.containsKey(errCode)) {
            alertDialog.setMessage(ErrMap.get(errCode));
        } else {
            alertDialog.setMessage(defMsg);
        }

        alertDialog.setButton(Dialog.BUTTON_POSITIVE, context.getString(R.string.ensure), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }

}
