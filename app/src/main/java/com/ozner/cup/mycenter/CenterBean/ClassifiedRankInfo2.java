package com.ozner.cup.mycenter.CenterBean;

/**
 * Created by xinde on 2015/12/31.
 */
public class ClassifiedRankInfo2 {
    private int id;
    private int rank;
    private int max;
    private int likenumaber;
    private String userid;
    private String vuserid;
    private String type;
    private int notify;
    private long notime;
    private String nickname;
    private String icon;
    private int score;

    public int getId(){
        return this.id;
    }
    public void setId(int id){
        this.id = id;
    }
    public int getRank(){
        return this.rank;
    }
    public void setRank(int rank){
        this.rank = rank;
    }
    public int getMax(){
        return this.max;
    }
    public void setMax(int max){
        this.max = max;
    }
    public int getLikenumaber(){
        return this.likenumaber;
    }
    public void setLikenumaber(int likenumaber){
        this.likenumaber = likenumaber;
    }
    public String getUserid(){
        return this.userid;
    }
    public void setUserid(String userid){
        this.userid = userid;
    }
    public String getVuserid(){
        return this.vuserid;
    }
    public void setVuserid(String vuserid){
        this.vuserid = vuserid;
    }
    public String getType(){
        return this.type;
    }
    public void setType(String type){
        this.type = type;
    }
    public int getNotify(){
        return this.notify;
    }
    public void setNotify(int notify){
        this.notify = notify;
    }
    public long getNotime(){
        return this.notime;
    }
    public void setNotime(String notime){
        this.notime = Long.parseLong(notime.replace("/Date(", "").replace(")/", ""));
    }
    public String getNickname(){
        return this.nickname;
    }
    public void setNickname(String nickname){
        this.nickname = nickname;
    }
    public String getIcon(){
        return this.icon;
    }
    public void setIcon(String icon){
        this.icon = icon;
    }
    public int getScore(){
        return this.score;
    }
    public void setScore(int score){
        this.score = score;
    }
}

