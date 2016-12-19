package com.ozner.qianye.CChat.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.nineoldandroids.view.ViewHelper;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.ozner.qianye.CChat.HtmlImageGetter.CURLImageParser;
import com.ozner.qianye.CChat.bean.ChatInfo;
import com.ozner.qianye.CChat.command;
import com.ozner.qianye.CChat.gif.AnimatedGifDrawable;
import com.ozner.qianye.CChat.gif.AnimatedImageSpan;
import com.ozner.qianye.Command.ImageHelper;
import com.ozner.qianye.Command.OznerCommand;
import com.ozner.qianye.HttpHelper.NetUserHeadImg;
import com.ozner.qianye.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressLint("NewApi")
public class ChatLVAdapter extends RecyclerView.Adapter<ChatViewHolder> {
    private Context mContext;
    private List<ChatInfo> list;
    WindowManager wm;
    DisplayMetrics metrics;
    ImageHelper imageHelper;
    Calendar calendar;
    String chatTime = "";
    //    Map<Integer, ChatViewHolder> holderMap = new HashMap<>();
//    Bitmap headBit;
    /**
     * 弹出的更多选择框
     */
    private PopupWindow popupWindow;
    /**
     * 复制，删除
     */
    private TextView copy, delete;
    private LayoutInflater inflater;
    /**
     * 执行动画的时间
     */
    protected long mAnimationTime = 150;
    private NetUserHeadImg headImg = null;

    public ChatLVAdapter(Context mContext, List<ChatInfo> list) {
        super();
        calendar = Calendar.getInstance();
        this.mContext = mContext;
        if (this.list != null) {
            this.list.clear();
        }
        this.list = list;
        inflater = LayoutInflater.from(mContext);
        initPopWindow();
        wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
//        if (headBit != null && !headBit.isRecycled()) {
//            headBit.recycle();
//        }
        headImg = new NetUserHeadImg();
        headImg.fromPreference((Activity) mContext);
        imageHelper = new ImageHelper(mContext);
        imageHelper.setImageLoadingListener(new MySimpleImageLoading());
    }

//    public void setHeadBit(Bitmap bitmap) {
////        this.headBit = bitmap;
//    }

    public void setList(List<ChatInfo> list) {
        this.list = list;
    }

    @Override
    public int getItemCount() {
        // TODO Auto-generated method stub
        return list.size();
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public void onBindViewHolder(final ChatViewHolder hodler, final int position) {
        // TODO Auto-generated method stub
        CURLImageParser imageGetter;
        String firstStr = "<div style=\"font-size:14px;font-family:微软雅黑\">";
        int last = list.get(position).content.lastIndexOf("</div>");
        try {
//            Log.e("tag", "ChatLvAdatper_time:" + list.get(position).time + " , position:" + position);
            if (position == 0) {
                hodler.chat_time.setVisibility(View.VISIBLE);
                hodler.chat_time.setText(list.get(position).time);
            } else if (!chatTime.equals(list.get(position).time)) {
                hodler.chat_time.setVisibility(View.VISIBLE);
                hodler.chat_time.setText(list.get(position).time);

            } else {
                hodler.chat_time.setVisibility(View.GONE);
                hodler.chat_time.setText("");
            }
            chatTime = list.get(position).time;

        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("tag", "ChatLvAdatper_time_Ex:" + ex.getMessage());
        }
        String msgTemp = list.get(position).content;
//        Log.e("CChat", "msgTemp:" + msgTemp);
        if (list.get(position).content.contains(firstStr) && last > -1)
            msgTemp = msgTemp.substring(firstStr.length(), last);
//        if (msgTemp.indexOf("<") > 0) {
//            msgTemp = msgTemp.substring(msgTemp.indexOf("<"));
//        }
        msgTemp = msgTemp.replace("<div><br></div>", "\n");

        msgTemp = msgTemp.replace("<br>", "");
        msgTemp = msgTemp.replace("<div>", "");
        msgTemp = msgTemp.replace("</div>", "");
        msgTemp = msgTemp.replace("&nbsp;", " ");
        msgTemp = msgTemp.trim();
        if (list.get(position).fromOrTo == 2) {
            // 收到消息 from显示
            hodler.fromContent.setMaxWidth(metrics.widthPixels - OznerCommand.dip2px(mContext, 130));
            hodler.toContainer.setVisibility(View.GONE);
            hodler.fromContainer.setVisibility(View.VISIBLE);
            imageGetter = new CURLImageParser(hodler.fromContent);

            if (list.get(position).content.contains("data:image/png;base64,")
                    || list.get(position).content.contains("data:image/jpg;base64,")
                    || list.get(position).content.contains("data:image/jpeg;base64,")) {
                msgTemp = "";
            }
            if (command.HtmlImage.ImageFormInternet(msgTemp) >= 0) {
                command.HtmlImage.TextViewDrawBitmap(hodler.fromContent, msgTemp);
            } else {
                SpannableStringBuilder sb = handler(hodler.fromContent,
                        msgTemp);
                hodler.fromContent.setText(sb);
            }

        } else

        {
            // 发送消息 to显示
            hodler.toContent.setMaxWidth(metrics.widthPixels - OznerCommand.dip2px(mContext, 130));
            hodler.toContainer.setVisibility(View.VISIBLE);
            hodler.fromContainer.setVisibility(View.GONE);
//            if (headBit != null)
//                hodler.iv_right.setImageBitmap(headBit);
            if (headImg != null && headImg.headimg != null && headImg.headimg.length() > 0) {
                imageHelper.loadImage(hodler.iv_right, headImg.headimg);
            }
            if (list.get(position).isSendSuc == 0) {
                hodler.iv_sendfail.setVisibility(View.VISIBLE);
            } else {
                hodler.iv_sendfail.setVisibility(View.GONE);
            }
            // 对内容做处理
            if (command.HtmlImage.ImageFormInternet(msgTemp) >= 0) {
                command.HtmlImage.TextViewDrawBitmap(hodler.toContent, msgTemp);
            } else {
                SpannableStringBuilder sb = handler(hodler.toContent,
                        msgTemp);
                hodler.toContent.setText(sb);
            }

//            if (command.HtmlImage.ImageFormInternet(list.get(position).content) >= 0) {
//                command.HtmlImage.TextViewDrawBitmap(hodler.toContent, list.get(position).content);
//            } else {
//                SpannableStringBuilder sb = handler(hodler.toContent,
//                        list.get(position).content);
//                hodler.toContent.setText(sb);
//            }
        }

        hodler.fromContent.setOnClickListener(new View.OnClickListener() {
                                                  @Override
                                                  public void onClick(View v) {
                                                      // TODO Auto-generated method stub
//                                                      showImg(list.get(position).content);
                                                  }
                                              }

        );
        hodler.toContent.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
//                                                    showImg(list.get(position).content);
                                                    // TODO Auto-generated method stub
                                                }
                                            }

        );

//        // 设置+按钮点击效果
//        hodler.fromContent.setOnLongClickListener(new
//                popAction(hodler.RootView,
//                position, list.get(position).fromOrTo));
//        hodler.toContent.setOnLongClickListener(new
//                popAction(hodler.RootView,
//                position, list.get(position).fromOrTo));

    }

    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup viewGroup, int arg1) {
        // TODO Auto-generated method stub
        View itemLayout = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chat_lv_item, null);
        ((ImageView) itemLayout.findViewById(R.id.chatfrom_icon)).setImageBitmap(ImageHelper.loadResBitmap(mContext,R.drawable.ozner));
        ((ImageView) itemLayout.findViewById(R.id.chatto_icon)).setImageBitmap(ImageHelper.loadResBitmap(mContext,R.drawable.customer));
        return new ChatViewHolder(itemLayout);
    }

    private SpannableStringBuilder handler(final TextView gifTextView, String content) {
        SpannableStringBuilder sb = new SpannableStringBuilder(content);
        String regex = "(\\#\\[face/png/f_static_)\\d{3}(.png\\]\\#)";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(content);
        while (m.find()) {
            String tempText = m.group();
            try {
                String num = tempText.substring("#[face/png/f_static_".length(), tempText.length() - ".png]#".length());
                String gif = "face/gif/f" + num + ".gif";
                /**
                 * 如果open这里不抛异常说明存在gif，则显示对应的gif
                 * 否则说明gif找不到，则显示png
                 * */
                InputStream is = mContext.getAssets().open(gif);
                sb.setSpan(new AnimatedImageSpan(new AnimatedGifDrawable(is, new AnimatedGifDrawable.UpdateListener() {
                            @Override
                            public void update() {
                                gifTextView.postInvalidate();
                            }
                        })), m.start(), m.end(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                is.close();
            } catch (Exception e) {
                String png = tempText.substring("#[".length(), tempText.length() - "]#".length());
                try {
                    sb.setSpan(new ImageSpan(mContext, BitmapFactory.decodeStream(mContext.getAssets().open(png))), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                e.printStackTrace();
            }
        }

        return sb;
    }

    public void showImg(String path) {
        Log.e("tag", "showImg:" + path);
        final AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
        View rootview = inflater.inflate(R.layout.img_show_dialog, null);
        ImageView iv_show = (ImageView) rootview.findViewById(R.id.iv_showImg);
        if (path.contains("http:") && path.contains(".jpg") || path.contains(".jpeg")) {//是网络图片

        } else {//本地图片

        }
        iv_show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        alertDialog.setView(rootview);
        alertDialog.show();
    }


    /**
     * 屏蔽listitem的所有事件
     */
//    @Override
//    public boolean areAllItemsEnabled() {
//        return false;
//    }
//
//    @Override
//    public boolean isEnabled(int position) {
//        return false;
//    }

    /**
     * 初始化弹出的pop
     */
    private void initPopWindow() {
        View popView = inflater.inflate(R.layout.chat_item_copy_delete_menu,
                null);
        copy = (TextView) popView.findViewById(R.id.chat_copy_menu);
        delete = (TextView) popView.findViewById(R.id.chat_delete_menu);
        popupWindow = new PopupWindow(popView, LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        popupWindow.setBackgroundDrawable(new ColorDrawable(0));
        // 设置popwindow出现和消失动画
        // popupWindow.setAnimationStyle(R.style.PopMenuAnimation);
    }

    /**
     * 显示popWindow
     */
    public void showPop(View parent, int x, int y, final View view,
                        final int position, final int fromOrTo) {
        // 设置popwindow显示位置
        popupWindow.showAtLocation(parent, 0, x, y);
        // 获取popwindow焦点
        popupWindow.setFocusable(true);
        // 设置popwindow如果点击外面区域，便关闭。
        popupWindow.setOutsideTouchable(true);
        // 为按钮绑定事件
        // 复制
        copy.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (popupWindow.isShowing()) {
                    popupWindow.dismiss();
                }
                // 获取剪贴板管理服务
                ClipboardManager cm = (ClipboardManager) mContext
                        .getSystemService(Context.CLIPBOARD_SERVICE);
                // 将文本数据复制到剪贴板
                cm.setText(list.get(position).content);
            }
        });
        // 删除
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (popupWindow.isShowing()) {
                    popupWindow.dismiss();
                }
                if (fromOrTo == 0) {
                    // from
                    //   leftRemoveAnimation(view, position);
                    ChatLVAdapter.this.notifyItemRemoved(position);

                } else if (fromOrTo == 1) {
                    // to
                    //    rightRemoveAnimation(view, position);
                    ChatLVAdapter.this.notifyItemRemoved(position);
                }
                // list.remove(position);
                // notifyDataSetChanged();
            }
        });
        popupWindow.update();
        if (popupWindow.isShowing()) {

        }
    }

    /**
     * 每个ITEM中more按钮对应的点击动作
     */
    public class popAction implements OnLongClickListener {
        int position;
        View view;
        int fromOrTo;

        public popAction(View view, int position, int fromOrTo) {
            this.position = position;
            this.view = view;
            this.fromOrTo = fromOrTo;
        }

        @Override
        public boolean onLongClick(View v) {
            // TODO Auto-generated method stub
            int[] arrayOfInt = new int[2];
            // 获取点击按钮的坐标
            v.getLocationOnScreen(arrayOfInt);
            int x = arrayOfInt[0];
            int y = arrayOfInt[1];
            // System.out.println("x: " + x + " y:" + y + " w: " +
            // v.getMeasuredWidth() + " h: " + v.getMeasuredHeight() );
            showPop(v, x, y, view, position, fromOrTo);
            return true;
        }

    }

    /**
     * item删除动画
     */
    private void rightRemoveAnimation(final View view, final int position) {
        final Animation animation = (Animation) AnimationUtils.loadAnimation(
                mContext, R.anim.chatto_remove_anim);
        animation.setAnimationListener(new AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                view.setAlpha(0);
                performDismiss(view, position);
                animation.cancel();
            }
        });

        view.startAnimation(animation);
    }

    /**
     * item删除动画
     */
    private void leftRemoveAnimation(final View view, final int position) {
        final Animation animation = (Animation) AnimationUtils.loadAnimation(mContext, R.anim.chatfrom_remove_anim);
        animation.setAnimationListener(new AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                view.setAlpha(0);
                performDismiss(view, position);
                animation.cancel();
            }
        });

        view.startAnimation(animation);
    }

    /**
     * 在此方法中执行item删除之后，其他的item向上或者向下滚动的动画，并且将position回调到方法onDismiss()中
     *
     * @param dismissView
     * @param dismissPosition
     */
    private void performDismiss(final View dismissView,
                                final int dismissPosition) {
        final ViewGroup.LayoutParams lp = dismissView.getLayoutParams();// 获取item的布局参数
        final int originalHeight = dismissView.getHeight();// item的高度

        ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 0)
                .setDuration(mAnimationTime);
        animator.start();

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                list.remove(dismissPosition);
                notifyDataSetChanged();
                // 这段代码很重要，因为我们并没有将item从ListView中移除，而是将item的高度设置为0
                // 所以我们在动画执行完毕之后将item设置回来
                ViewHelper.setAlpha(dismissView, 1f);
                ViewHelper.setTranslationX(dismissView, 0);
                ViewGroup.LayoutParams lp = dismissView.getLayoutParams();
                lp.height = originalHeight;
                dismissView.setLayoutParams(lp);
            }
        });

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                // 这段代码的效果是ListView删除某item之后，其他的item向上滑动的效果
                lp.height = (Integer) valueAnimator.getAnimatedValue();
                dismissView.setLayoutParams(lp);
            }
        });

    }

    protected float dpToPx(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, mContext.getResources().getDisplayMetrics());
    }

    class MySimpleImageLoading extends SimpleImageLoadingListener {
        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            super.onLoadingComplete(imageUri, view, loadedImage);
            ((ImageView) view).setImageBitmap(ImageHelper.toRoundBitmap(mContext, loadedImage));
        }
    }
}
