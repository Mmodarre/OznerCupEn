package com.ozner.cup.mycenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.ozner.cup.Command.ImageHelper;
import com.ozner.cup.Device.OznerApplication;
import com.ozner.cup.R;
import com.ozner.cup.mycenter.CenterBean.CenterRankInfo2;
import com.ozner.cup.mycenter.CenterBean.RankType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xinde on 2015/12/10.
 */
public class CenterRankAdapter extends BaseAdapter {
    private List<CenterRankInfo2> datalist = new ArrayList<CenterRankInfo2>();
    private LayoutInflater mInflater;
    private String rankType;
    private Context mContext;
    ImageHelper imageHelper;
    MyLoadImgListener imageLoadListener;

    public CenterRankAdapter(Context context, String type) {
        this.mInflater = LayoutInflater.from(context);
//        this.datalist = datalist;
        this.rankType = type;
        this.mContext = context;
        imageHelper = new ImageHelper(context);
        imageLoadListener = new MyLoadImgListener();
        imageHelper.setImageLoadingListener(imageLoadListener);
    }

    public void reloadData(List<CenterRankInfo2> datalist, String type) {
        this.datalist = datalist;
        this.rankType = type;
        this.notifyDataSetInvalidated();
    }

    @Override
    public int getCount() {
        return datalist.size();
    }

    @Override
    public Object getItem(int position) {
        return datalist.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = new ViewHolder();
        if (null == convertView) {
            if (rankType.equals(RankType.CupVolumType)) {
                convertView = mInflater.inflate(R.layout.center_volumn_list_item, null);
            } else {
                convertView = mInflater.inflate(R.layout.center_rank_list_item, null);
            }
            OznerApplication.changeTextFont((ViewGroup)convertView);
            viewHolder.tv_RankValue = (TextView) convertView.findViewById(R.id.tv_RankValue);
            viewHolder.iv_crown = (ImageView) convertView.findViewById(R.id.iv_crown);
            viewHolder.iv_headImg = (ImageView) convertView.findViewById(R.id.iv_headImg);
            viewHolder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            viewHolder.tv_Value = (TextView) convertView.findViewById(R.id.tv_Value);
            viewHolder.iv_likeImg = (ImageView) convertView.findViewById(R.id.iv_likeImg);
            viewHolder.tv_likeNum = (TextView) convertView.findViewById(R.id.tv_likeNum);
            viewHolder.pb_Value = (ProgressBar) convertView.findViewById(R.id.pb_Value);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        int rank = datalist.get(position).getRank();
        viewHolder.tv_RankValue.setText(String.valueOf(rank));

        if (rank <= 3) {
            viewHolder.tv_RankValue.setTextColor(mContext.getResources().getColor(R.color.checked));
        } else {
            viewHolder.tv_RankValue.setTextColor(mContext.getResources().getColor(R.color.MyCenter_RankValue));
        }

        viewHolder.iv_crown.setVisibility(View.VISIBLE);
        if (1 == rank) {
            viewHolder.iv_crown.setImageResource(R.drawable.crown_1);
        } else if (2 == rank) {
            viewHolder.iv_crown.setImageResource(R.drawable.crown_2);
        } else if (3 == rank) {
            viewHolder.iv_crown.setImageResource(R.drawable.crown_3);
        } else {
            viewHolder.iv_crown.setVisibility(View.GONE);
        }
        if (datalist.get(position).getIcon() != null && datalist.get(position).getIcon() != "") {
            imageHelper.loadImage(viewHolder.iv_headImg, datalist.get(position).getIcon());
        } else {
            viewHolder.iv_headImg.setImageResource(R.mipmap.icon_default_headimage);
        }
        if (datalist.get(position).getNickname() != null && datalist.get(position).getNickname() != "") {
            viewHolder.tv_name.setText(datalist.get(position).getNickname());
        } else {
            viewHolder.tv_name.setText(datalist.get(position).getMobile());
        }
        viewHolder.tv_Value.setText(datalist.get(position).getVolume() + "");
        viewHolder.pb_Value.setProgress((int) ((float) datalist.get(position).getVolume() / 400 * 100));
        viewHolder.tv_likeNum.setText(datalist.get(position).getLikeCount() + "");

        if (datalist.get(position).getIsLike() == 1) {
            viewHolder.iv_likeImg.setImageResource(R.drawable.center_heart_red);
        } else {
            viewHolder.iv_likeImg.setImageResource(R.drawable.center_heart_gray);
        }

        return convertView;
    }

    class ViewHolder {
        public TextView tv_RankValue;
        public ImageView iv_crown;
        public ImageView iv_headImg;
        public TextView tv_name;
        public TextView tv_Value;
        public ImageView iv_likeImg;
        public TextView tv_likeNum;
        public ProgressBar pb_Value;
    }

    class MyLoadImgListener extends SimpleImageLoadingListener {
        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            ((ImageView) view).setImageBitmap(ImageHelper.toRoundBitmap(mContext,loadedImage));
            super.onLoadingComplete(imageUri, view, loadedImage);
        }
    }
}
