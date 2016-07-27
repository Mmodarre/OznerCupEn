package com.ozner.cup.mycenter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nineoldandroids.view.ViewHelper;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import com.ozner.cup.Command.ImageHelper;
import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.Device.OznerApplication;
import com.ozner.cup.R;
import com.ozner.cup.mycenter.CenterBean.ClassifiedRankInfo2;
import com.ozner.cup.mycenter.CenterBean.RankType;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by xinde on 2015/12/9.
 */
public class ClassifiedRankListAdapter extends BaseAdapter implements View.OnClickListener {
    private Context mContext;
    private LayoutInflater mInflater;
    private String userid = "";
    ImageHelper imageHelper;
    MyLoadImgListener imageLoadListener;
    RankViewHolder rankViewHolder;
    // private List<ClassifiedRankInfo> dataList = new ArrayList<ClassifiedRankInfo>();
    private List<ClassifiedRankInfo2> dataList;
    private onDeleteItemLinster deleteLisenter;
    private final String[] monthsStr = {"Jan.", "Feb.", "Mar.", "Apr.", "May", "Jun.", "Jul.", "Aug.", "Sep.", "Oct.", "Nov.", "Dec."};
    /**
     * 执行动画的时间
     */
    protected long mAnimationTime = 100;

    public interface onDeleteItemLinster {
        void onDeleteItem(List<ClassifiedRankInfo2> resList);
    }

    public void setOnDeleteItemLinster(onDeleteItemLinster lisenter) {
        this.deleteLisenter = lisenter;
    }

    public ClassifiedRankListAdapter(Context context) {
        this.mContext = context;
        mInflater = LayoutInflater.from(context);
        //this.dataList = dataList;
        dataList = new ArrayList<>();
        imageHelper = new ImageHelper(context);
        imageLoadListener = new MyLoadImgListener();
        imageHelper.setImageLoadingListener(imageLoadListener);
        userid = UserDataPreference.GetUserData(context, UserDataPreference.UserId, null);
    }

    public void reloadData(List<ClassifiedRankInfo2> data) {
        this.dataList = data;
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public Object getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (null == convertView) {
            rankViewHolder = new RankViewHolder();
            convertView = mInflater.inflate(R.layout.my_center_classifier_rank_item, null);
            OznerApplication.changeTextFont((ViewGroup) convertView);
            rankViewHolder.llay_root = (LinearLayout) convertView.findViewById(R.id.llay_root);
            rankViewHolder.iv_deviceImg = (ImageView) convertView.findViewById(R.id.iv_deviceImg);
            rankViewHolder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
            rankViewHolder.tv_rank = (TextView) convertView.findViewById(R.id.tv_short);
            rankViewHolder.tv_best = (TextView) convertView.findViewById(R.id.tv_best);
            rankViewHolder.tv_likeNum = (TextView) convertView.findViewById(R.id.tv_likeNum);
            rankViewHolder.iv_firstHeadImg = (ImageView) convertView.findViewById(R.id.iv_firstHeadImg);
            rankViewHolder.tv_firstText = (TextView) convertView.findViewById(R.id.tv_firstText);
            rankViewHolder.llay_bestTds = (LinearLayout) convertView.findViewById(R.id.llay_bestTds);
            rankViewHolder.rlay_Rank = (RelativeLayout) convertView.findViewById(R.id.rlay_Rank);
            rankViewHolder.llay_LikeMe = (LinearLayout) convertView.findViewById(R.id.llay_LikeMe);
            convertView.setTag(rankViewHolder);
        } else {
            rankViewHolder = (RankViewHolder) convertView.getTag();
        }

        rankViewHolder.llay_root.setOnLongClickListener(new LongClickActoin(convertView, position));
        int deviceImgid = R.drawable.my_center_cup;
        int titleStrid = R.string.Center_Rank_Cup_Tds;
        switch (dataList.get(position).getType()) {
            case RankType.CupType:
                deviceImgid = R.drawable.my_center_cup;
                titleStrid = R.string.Center_Rank_Cup_Tds;
                break;
            case RankType.TapType:
                deviceImgid = R.drawable.my_center_tap;
                titleStrid = R.string.Center_Rank_Tap_Tds;
                break;
            case RankType.WaterType:
                deviceImgid = R.drawable.my_center_purifier;
                titleStrid = R.string.Center_Rank_Purifier_Tds;
                break;
            case RankType.CupVolumType:
                deviceImgid = R.drawable.my_center_cup;
                titleStrid = R.string.Center_Rank_Cup_Vol;
                break;
        }
        rankViewHolder.iv_deviceImg.setImageResource(deviceImgid);
        rankViewHolder.tv_title.setText(mContext.getString(titleStrid));
        rankViewHolder.tv_rank.setText(String.valueOf(dataList.get(position).getRank()));
        rankViewHolder.tv_best.setText(String.valueOf(dataList.get(position).getMax()));
        if (0 == dataList.get(position).getLikenumaber()) {
            rankViewHolder.tv_likeNum.setTextColor(Color.GRAY);
        } else {
            rankViewHolder.tv_likeNum.setTextColor(Color.RED);
        }
        rankViewHolder.tv_likeNum.setText(String.valueOf(dataList.get(position).getLikenumaber()));
        if (dataList.get(position).getIcon() != null && dataList.get(position).getIcon() != "") {
            imageHelper.loadImage(rankViewHolder.iv_firstHeadImg, dataList.get(position).getIcon());
        } else {
            rankViewHolder.iv_firstHeadImg.setImageResource(R.mipmap.icon_default_headimage);
        }

//        rankViewHolder.iv_deviceImg.setImageResource(dataList.get(position).deviceImgId);
//        rankViewHolder.tv_title.setText(dataList.get(position).title);
//        rankViewHolder.tv_rank.setText(String.valueOf(dataList.get(position).rank));
//        rankViewHolder.tv_best.setText(String.valueOf(dataList.get(position).best));
//        if (dataList.get(position).likeNum == 0) {
//            rankViewHolder.tv_likeNum.setTextColor(Color.GRAY);
//        } else {
//            rankViewHolder.tv_likeNum.setTextColor(Color.RED);
//        }
//        rankViewHolder.tv_likeNum.setText(String.valueOf(dataList.get(position).likeNum));
//        rankViewHolder.iv_firstHeadImg.setImageResource(dataList.get(position).firstHeadImg);
        rankViewHolder.llay_bestTds.setOnClickListener(this);
        rankViewHolder.llay_bestTds.setTag(position);
        rankViewHolder.rlay_Rank.setTag(position);
        rankViewHolder.rlay_Rank.setOnClickListener(this);
        rankViewHolder.llay_LikeMe.setOnClickListener(this);
        rankViewHolder.llay_LikeMe.setTag(position);

//        StringBuilder firstText = new StringBuilder();
        String you = "";
        if (dataList.get(position).getUserid() != null && dataList.get(position).getUserid() == userid) {
//            firstText.append("您");
            you = mContext.getString(R.string.nin) + " ";
        }
//        firstText.append("夺得");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(dataList.get(position).getNotime()));
//
//        firstText.append(calendar.get(Calendar.MONTH) + 1);
//        firstText.append("月排行榜冠军");
        String month = "";
        if (((OznerApplication) ((Activity) mContext).getApplication()).isLanguageCN()) {
            month = String.valueOf(calendar.get(Calendar.MONTH) + 1);
        } else {
            month = monthsStr[calendar.get(Calendar.MONTH)];
        }

//        rankViewHolder.tv_firstText.setText(firstText);
//        String strFormat =
        rankViewHolder.tv_firstText.setText(String.format(mContext.getString(R.string.Center_obtainChamp), month));
        return convertView;
    }

    @Override
    public void onClick(View v) {
        int pos = (int) v.getTag();
        switch (v.getId()) {
            case R.id.llay_bestTds:
            case R.id.rlay_Rank:
                Intent intent = new Intent(mContext, CenterRankActivity.class);
                intent.putExtra("rankType", dataList.get(pos).getType());
                mContext.startActivity(intent);
                break;
            case R.id.llay_LikeMe:
                Intent likemeIntent = new Intent(mContext, LikeMeActivity.class);
                likemeIntent.putExtra("rankType", dataList.get(pos).getType());
                mContext.startActivity(likemeIntent);
                break;
        }
    }

    class LongClickActoin implements View.OnLongClickListener {
        int pos;
        View delView;

        public LongClickActoin(View view, int pos) {
            this.delView = view;
            this.pos = pos;
        }

        @Override
        public boolean onLongClick(View v) {
            new AlertDialog.Builder(mContext).setMessage("是否删除？").setPositiveButton("是", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    rightRemoveAnimation(delView, pos);
                }
            }).setNegativeButton("否", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).show();

            return true;
        }
    }

    /**
     * item删除动画
     */
    private void rightRemoveAnimation(final View view, final int position) {
        final Animation animation = (Animation) AnimationUtils.loadAnimation(
                mContext, R.anim.chatto_remove_anim);
        animation.setAnimationListener(new Animation.AnimationListener() {
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

                dataList.remove(dismissPosition);
                notifyDataSetChanged();
                if (deleteLisenter != null) {
                    deleteLisenter.onDeleteItem(dataList);
                }
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

    class RankViewHolder {
        public LinearLayout llay_root;
        public ImageView iv_deviceImg;
        public TextView tv_title;
        public TextView tv_rank;
        public TextView tv_best;
        public TextView tv_likeNum;
        public ImageView iv_firstHeadImg;
        public TextView tv_firstText;
        public LinearLayout llay_bestTds;
        public RelativeLayout rlay_Rank;
        public LinearLayout llay_LikeMe;
    }

    class MyLoadImgListener extends SimpleImageLoadingListener {
        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            ((ImageView) view).setImageBitmap(ImageHelper.toRoundBitmap(mContext, loadedImage));
            super.onLoadingComplete(imageUri, view, loadedImage);
        }
    }
}
