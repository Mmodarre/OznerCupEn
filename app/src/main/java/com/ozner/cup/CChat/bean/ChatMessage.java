package com.ozner.cup.CChat.bean;

/**
 * Created by ozner_67 on 2016/4/11.
 * 用来插入数据库的实体类
 */
public class ChatMessage {
    private int queueid;//信息id
    private int isSendSuc = -1;//发送是否成功
    private String content;//消息内容
    private long time;//消息时间
    private int oper;// 0 是收到的消息，1是发送的消息

    public void setQueueid(int queueid){
        this.queueid = queueid;
    }
    public int getQueueid(){
        return this.queueid;
    }
    public void setIsSendSuc(int isSendSuc) {
        this.isSendSuc = isSendSuc;
    }

    public int getIsSendSuc() {
        return this.isSendSuc;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return this.content;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getTime() {
        return this.time;
    }

    public void setOper(int oper) {
        this.oper = oper;
    }

    public int getOper() {
        return this.oper;
    }
}
