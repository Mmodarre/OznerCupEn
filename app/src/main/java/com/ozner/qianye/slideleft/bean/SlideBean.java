package com.ozner.qianye.slideleft.bean;

/**
 * Created by gongxibo on 2015/11/26.
 */
public class SlideBean {

    private int icon;
    private String title;
    private String desc;

    public SlideBean(int icon, String title, String desc) {
        this.icon = icon;
        this.title = title;
        this.desc = desc;
    }

    @Override
    public String toString() {
        return title;
    }



    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }


}
