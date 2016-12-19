package com.ozner.qianye.UIView;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mob.tools.utils.UIHandler;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.ozner.cup.CupRecord;
import com.ozner.qianye.Command.ImageHelper;
import com.ozner.qianye.Command.OznerCommand;
import com.ozner.qianye.HttpHelper.NetUserHeadImg;
import com.ozner.qianye.R;

import java.util.HashMap;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;

/**
 * Created by taoran on 2015/12/2.  分享框
 */
public class ShareView implements PlatformActionListener,Handler.Callback {
    private static Dialog shareDialog,dialog;
    private static final int MSG_ACTION_CCALLBACK = 2;
    private static Activity activity;
    private  static String path;
    private  static MyLoadImgListener imageLoadListener = new MyLoadImgListener();
    public ShareView() {

    }

    public static void showShareToDialogHb(final Context context,String type, final String url) {
        activity = (Activity) context;
        dialog = new Dialog(context, R.style.dialog_style);
        dialog.setContentView(R.layout.share_dialog_layout);
        dialog.getWindow().setGravity(Gravity.BOTTOM);

        WindowManager windowManager = activity.getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.width = (int) (display.getWidth()); // 设置宽度
        Window window = dialog.getWindow();
        window.setAttributes(lp);
        window.setWindowAnimations(R.style.dialogstyle);
        dialog.findViewById(R.id.share_wx_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(context, "微信分享", Toast.LENGTH_SHORT).show();
                Platform.ShareParams wechat = new Platform.ShareParams();
                wechat.setTitle("疯了疯了，注册浩泽会员就送微信红包");
                wechat.setText("新会员注册即有98元的浩泽净水探头和微信红包");
//                wechat.setImageUrl("http://ytqmp.qiniudn.com/biaoqing/bairen12_qmp.gif");
                wechat.setUrl(url);
                wechat.setShareType(Platform.SHARE_WEBPAGE);
                Platform weixin = ShareSDK.getPlatform(Wechat.NAME);
                weixin.setPlatformActionListener(new ShareView());
                weixin.share(wechat);
            }
        });
        dialog.findViewById(R.id.share_wxfriend_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(context, "朋友圈分享", Toast.LENGTH_SHORT).show();
                Platform.ShareParams wechatMoments = new Platform.ShareParams();
                wechatMoments.setTitle("疯了疯了，注册浩泽会员就送微信红包");
                wechatMoments.setText("新会员注册即有98元的浩泽净水探头和微信红包");
                Log.e("tags", url);
//                wechatMoments.setImagePath(path);
                wechatMoments.setUrl(url);
                wechatMoments.setShareType(Platform.SHARE_WEBPAGE);
                Platform wxpyq = ShareSDK.getPlatform(WechatMoments.NAME);
                wxpyq.setPlatformActionListener(new ShareView());
                wxpyq.share(wechatMoments);
            }
        });
//        shareDialog.findViewById(R.id.share_more_layout).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Toast.makeText(context, "更多", Toast.LENGTH_SHORT).show();
////                shareDialog.findViewById(R.id.share_more_layout).setVisibility(View.GONE);
//                shareDialog.findViewById(R.id.share_sina_layout).setVisibility(View.VISIBLE);
//                shareDialog.findViewById(R.id.share_message_layout).setVisibility(View.VISIBLE);
//                shareDialog.findViewById(R.id.share_qq_layout).setVisibility(View.VISIBLE);
//                shareDialog.findViewById(R.id.share_mail_layout).setVisibility(View.VISIBLE);
////                shareDialog.findViewById(R.id.share_url_layout).setVisibility(View.VISIBLE);
//
//            }
//        });
//        shareDialog.findViewById(R.id.share_sina_layout).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Toast.makeText(context, "sina分享", Toast.LENGTH_SHORT).show();
//                Platform.ShareParams sp = new Platform.ShareParams();
//                sp.setImagePath(path);
//                Platform weibo = ShareSDK.getPlatform(SinaWeibo.NAME);
//                weibo.SSOSetting(true);
//                weibo.setPlatformActionListener(new ShareView()); // 设置分享事件回调
//                weibo.share(sp);
//            }
//        });


//        shareDialog.findViewById(R.id.share_qq_layout).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Toast.makeText(context, "qq分享", Toast.LENGTH_SHORT).show();
//                Platform.ShareParams qq = new Platform.ShareParams();
//                qq.setTitle("分享标题");
////                qq.setTitleUrl("http://mob.com");//分享后点击跳转链接
//                qq.setText("分享文本");
//                qq.setImagePath(path);
////                qq.setImageUrl("http://f1.sharesdk.cn/imgs/2014/02/26/owWpLZo_638x960.jpg");
//                Platform qqq = ShareSDK.getPlatform(QQ.NAME);
//                qqq.setPlatformActionListener(new ShareView());
//                qqq.share(qq);
//            }
//        });

        dialog.show();

    }







    public static void showShareToDialog(final Context context,String url) {
        activity = (Activity) context;
        path=url;
        shareDialog = new Dialog(context, R.style.dialog_style);
        shareDialog.setContentView(R.layout.share_dialog_layout);
        shareDialog.getWindow().setGravity(Gravity.BOTTOM);

        WindowManager windowManager = activity.getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = shareDialog.getWindow().getAttributes();
        lp.width = (int) (display.getWidth()); // 设置宽度
        Window window = shareDialog.getWindow();
        window.setAttributes(lp);
        window.setWindowAnimations(R.style.dialogstyle);
        shareDialog.findViewById(R.id.share_wx_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(context, "微信分享", Toast.LENGTH_SHORT).show();
                Platform.ShareParams wechat = new Platform.ShareParams();
//                wechat.setTitle("分享标题");
//                wechat.setText("分享文本");
//                wechat.setImageUrl("http://ytqmp.qiniudn.com/biaoqing/bairen12_qmp.gif");
//                wechat.setUrl("http://www.17maoxian.com/");
                wechat.setImagePath(path);
                wechat.setShareType(Platform.SHARE_EMOJI);
                Platform weixin = ShareSDK.getPlatform(Wechat.NAME);
                weixin.setPlatformActionListener(new ShareView());
                weixin.share(wechat);
            }
        });
        shareDialog.findViewById(R.id.share_wxfriend_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(context, "朋友圈分享", Toast.LENGTH_SHORT).show();
                Platform.ShareParams wechatMoments = new Platform.ShareParams();
                wechatMoments.setTitle("Oener 分享");
                wechatMoments.setText("分享喝水量");
                Log.e("tags", path);
                wechatMoments.setImagePath(path);
//                wechatMoments.setUrl("http://mob.com");
//                wechatMoments.setImageUrl("http://f1.sharesdk.cn/imgs/2014/02/26/owWpLZo_638x960.jpg");
//                wechatMoments.setMusicUrl("http://www.zhlongyin.com/UploadFiles/xrxz/2011/5/201105051307513619.mp3");
                wechatMoments.setShareType(Platform.SHARE_MUSIC);
                Platform wxpyq = ShareSDK.getPlatform(WechatMoments.NAME);
                wxpyq.setPlatformActionListener(new ShareView());
                wxpyq.share(wechatMoments);
            }
        });
//        shareDialog.findViewById(R.id.share_more_layout).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Toast.makeText(context, "更多", Toast.LENGTH_SHORT).show();
////                shareDialog.findViewById(R.id.share_more_layout).setVisibility(View.GONE);
//                shareDialog.findViewById(R.id.share_sina_layout).setVisibility(View.VISIBLE);
//                shareDialog.findViewById(R.id.share_message_layout).setVisibility(View.VISIBLE);
//                shareDialog.findViewById(R.id.share_qq_layout).setVisibility(View.VISIBLE);
//                shareDialog.findViewById(R.id.share_mail_layout).setVisibility(View.VISIBLE);
////                shareDialog.findViewById(R.id.share_url_layout).setVisibility(View.VISIBLE);
//
//            }
//        });
        shareDialog.findViewById(R.id.share_sina_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Platform.ShareParams sp = new Platform.ShareParams();
                sp.setImagePath(path);
                Platform weibo = ShareSDK.getPlatform(SinaWeibo.NAME);
                weibo.SSOSetting(true);
                weibo.setPlatformActionListener(new ShareView()); // 设置分享事件回调
                weibo.share(sp);
            }
        });


        shareDialog.findViewById(R.id.share_qq_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toast.makeText(context, "qq分享", Toast.LENGTH_SHORT).show();
                Platform.ShareParams qq = new Platform.ShareParams();
                qq.setTitle("分享标题");
//                qq.setTitleUrl("http://mob.com");//分享后点击跳转链接
                qq.setText("分享文本");
                qq.setImagePath(path);
//                qq.setImageUrl("http://f1.sharesdk.cn/imgs/2014/02/26/owWpLZo_638x960.jpg");
                Platform qqq = ShareSDK.getPlatform(QQ.NAME);
                qqq.setPlatformActionListener(new ShareView());
                qqq.share(qq);
            }
        });
        shareDialog.show();

    }
    @Override
    public void onCancel(Platform platform, int action) {
        // 取消
        Message msg = new Message();
        msg.what = MSG_ACTION_CCALLBACK;
        msg.arg1 = 3;
        msg.arg2 = action;
        msg.obj = platform;
        UIHandler.sendMessage(msg, this);

    }

    @Override
    public void onComplete(Platform platform, int action, HashMap<String, Object> arg2) {
        // 成功
        Message msg = new Message();
        msg.what = MSG_ACTION_CCALLBACK;
        msg.arg1 = 1;
        msg.arg2 = action;
        msg.obj = platform;
        UIHandler.sendMessage(msg, this);

    }

    @Override
    public void onError(Platform platform, int action, Throwable t) {
        // 失敗
        //打印错误信息,print the error msg
        t.printStackTrace();
        //错误监听,handle the error msg
        Message msg = new Message();
        msg.what = MSG_ACTION_CCALLBACK;
        msg.arg1 = 2;
        msg.arg2 = action;
        msg.obj = t;
        UIHandler.sendMessage(msg, this);

    }

    public boolean handleMessage(Message msg) {
        switch (msg.arg1) {
            case 1: {
                // 成功
//                Toast.makeText(activity, "分享成功", Toast.LENGTH_SHORT).show();
//                System.out.println("分享回调成功------------");
            }
            break;
            case 2: {
                // 失败
//                Toast.makeText(activity, "分享失败", Toast.LENGTH_SHORT).show();
            }
            break;
            case 3: {
                // 取消
//                Toast.makeText(activity, "分享取消", Toast.LENGTH_SHORT).show();
            }
            break;
        }
        return false;

    }

    public static Bitmap createViewBitmap(Activity activity,String type,int value,int rank,int tdsRank) {
        LayoutInflater inflate = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View shareBitmap = inflate.inflate(R.layout.share_bitmap_layout, null);
        //好友排名
        LinearLayout lay_tdsShort=(LinearLayout)shareBitmap.findViewById(R.id.lay_tdsShort);
        TextView pm = (TextView) shareBitmap.findViewById(R.id.tv_share_ranking_value);
        TextView tdspm = (TextView) shareBitmap.findViewById(R.id.tv_tdsShort);
        TextView tv_share_dw = (TextView) shareBitmap.findViewById(R.id.tv_share_dw);
        TextView tv_sharecontent_value = (TextView) shareBitmap.findViewById(R.id.tv_sharecontent_value);
        TextView tv_sharecontent = (TextView) shareBitmap.findViewById(R.id.tv_sharecontent);
        tv_sharecontent_value.setText(value+"");
        pm.setText(rank + "");
        tdspm.setText(tdsRank+"");
        //表情图片
        ImageView bqIv = (ImageView) shareBitmap.findViewById(R.id.iv_share_pm);
        ImageView iv_waterValum = (ImageView) shareBitmap.findViewById(R.id.iv_waterValum);
        if ("TDS".equals(type)) {
            tv_sharecontent.setText(activity.getResources().getString(R.string.share_rankTds));
            lay_tdsShort.setVisibility(View.VISIBLE);
            iv_waterValum.setVisibility(View.GONE);
            tv_share_dw.setVisibility(View.GONE);
            if(value>=0&&value<= CupRecord.TDS_Good_Value){
                bqIv.setImageResource(R.drawable.share_tdsgood);
            }else if(value> CupRecord.TDS_Good_Value&&value<= CupRecord.TDS_Bad_Value){
                bqIv.setImageResource(R.drawable.share_tdsmid);
            }else{
                bqIv.setImageResource(R.drawable.share_tdsbad);
            }
        } else if ("WATER".equals(type)) {
            tv_sharecontent.setText(activity.getResources().getString(R.string.share_rankWater));
            bqIv.setImageResource(R.drawable.share_volummid);
            lay_tdsShort.setVisibility(View.GONE);
            iv_waterValum.setVisibility(View.VISIBLE);
            tv_share_dw.setVisibility(View.VISIBLE);

            if (value <= tdsRank) {
                int volumDrink = (int) ((value * 100) / tdsRank);
                if (volumDrink <= 30) {
                    bqIv.setImageResource(R.drawable.share_volumless);
                } else if (volumDrink > 30 && volumDrink <= 60) {
                    bqIv.setImageResource(R.drawable.share_volummid);
                } else if (volumDrink > 60 && volumDrink <= 100) {
                    bqIv.setImageResource(R.drawable.share_volumheight);
                }
            }
        }
        //头像和用户名
        TextView tv_share_username=(TextView) shareBitmap.findViewById(R.id.tv_share_username);
        ImageView iv_share_headimage=(ImageView)shareBitmap.findViewById(R.id.iv_share_headimage);
        NetUserHeadImg netUserHeadImg = new NetUserHeadImg();
        netUserHeadImg.fromPreference(activity);
        if (netUserHeadImg != null) {
            tv_share_username.setText(netUserHeadImg.nickname+"");
        }
        Bitmap userImg= OznerCommand.GetUserHeadImage(activity);
        iv_share_headimage.setImageBitmap(userImg);
        //view转化成bitmap
        shareBitmap.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0,
                    View.MeasureSpec.UNSPECIFIED));
        shareBitmap.layout(0, 0, shareBitmap.getMeasuredWidth(), shareBitmap.getMeasuredHeight());
        shareBitmap.buildDrawingCache();
        Bitmap bitmap = shareBitmap.getDrawingCache();
        return bitmap;
        }

    public static class MyLoadImgListener extends SimpleImageLoadingListener {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                ((ImageView) view).setImageBitmap(ImageHelper.toRoundBitmap(activity,loadedImage));
                super.onLoadingComplete(imageUri, view, loadedImage);
            }
        }

}
