package com.ozner.cup.Command;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

/**
 * Created by C-sir@hotmail.com  on 2015/12/7.
 */
public class MsgHandler  extends Handler {
        private Activity activity;
        public MsgHandler(Activity activity){
            this.activity=activity;
        }
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case 1:
                    showInfo("登录成功！");
                    break;
                case 2:
                    showInfo("用户名称或者密码错误，请重新输入！");
                    break;
                case 3:
                    showInfo("如果登录成功,以后账号和密码会自动输入!");
                    break;
                case 4:
                    showInfo("网络未连接!");
                    break;
                case 5:
                    showInfo("用户账户是必填项！");
                    break;
                case 6:
                  //  showInfo("Delete From NetWork Wrong");
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }

        /**
         * 显示提示信息
         * @param info
         */
        public void showInfo(String info)
        {
            Toast.makeText(activity.getApplicationContext(), info, Toast.LENGTH_SHORT).show();
        }


}
