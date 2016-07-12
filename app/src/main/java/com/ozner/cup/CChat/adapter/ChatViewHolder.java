package com.ozner.cup.CChat.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ozner.cup.R;

/**
 * Created by C-sir@hotmail.com  on 2016/1/7.
 */
public class ChatViewHolder extends RecyclerView.ViewHolder {
    View RootView;
    TextView fromContent, toContent, chat_time;//, time;
    LinearLayout fromContainer;
    LinearLayout toContainer;
    ImageView iv_left, iv_right, iv_sendfail;

    public ChatViewHolder(View v) {
        super(v);
        this.RootView = v;
        this.fromContainer = (LinearLayout) v.findViewById(R.id.chart_from_container);
        this.toContainer = (LinearLayout) v.findViewById(R.id.chart_to_container);
        this.fromContent = (TextView) v.findViewById(R.id.chatfrom_content);
        this.toContent = (TextView) v.findViewById(R.id.chatto_content);
        this.iv_left = (ImageView) v.findViewById(R.id.chatfrom_icon);
        this.iv_right = (ImageView) v.findViewById(R.id.chatto_icon);
        this.chat_time = (TextView) v.findViewById(R.id.chat_time);
        iv_sendfail = (ImageView) v.findViewById(R.id.iv_sendfail);
    }
}
