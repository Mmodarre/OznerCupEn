package com.ozner.qianye.Command;

/**
 * Created by C-sir@hotmail.com  on 2015/11/25.
 */
public class PageState {
    public static final String MAC = "MAC";
    public static final String OZNERLOCALDEVICEADD = "net.ozner.oznerproject.add";
    public static final String CENTER_DEVICE_TYPE = "center_device_type";
    public static final String CENTER_DEVICE_ADDRESS = "center_device_addr";
    public static final String DEVICE_ADDRES = "DeviceAddress";
    public static final String DRINK_GOAL = "DrinkGoal";//区分不同杯子的饮水目标
    public static final String DEVICE_WEIGHT = "DeviceWeight";//区分不同杯子的体重
    public static final String FilterUsePre = "FilterUsePre";//滤芯剩余百分比
    public static final String FilterUpdateTime = "FilterUpdateTime";//滤芯更新时间
    public static final String TotalClean = "TotalClean";//滤芯状态咨询点击
    public static final String Sex = "Sex";//补水仪主人性别
    public static final String FaceSkinValue = "FaceSkinValue";// 脸部 检测肤质值
    public static final String HandSkinValue = "HandSkinValue";// 手部 检测肤质值
    public static final String EyesSkinValue = "EyesSkinValue";// 眼部 检测肤质值
    public static final String NeckSkinValue = "NeckSkinValue";// 颈部 检测肤质值
    public static final String Time1 = "Time1";//补水仪检测时间1
    public static final String Time2 = "Time2";//               2
    public static final String Time3 = "Time3";//               3
    public static final String sortPosi = "sortPosi";//侧边栏里的位置
    public static final String TapType = "TapType";//区分探头、TDS pen

    public static final int ZHINNEGSHUIBEI = 1;//智能水杯
    public static final int SHUITANTOU = 2;//水探头
    public static final int YINSHUIJI = 3;
    public static final int TDS = 4;//TDS
    public static final int YINSHUILIANG = 5;   //饮水量
    public static final int SHUIWEN = 6;//水温
    public static final int ADDDEVICE = 7;//添加设备
    public static final int MYPAGE = 8;//我的页面
    public static final int ZIXUNYEMIAN = 9;//咨询页面
    public static final int SHANGCHEGYEMIAN = 10;//商城页面
    public static final int WODESHEBEI = 11;//我的设备页面
    public static final int DEVICECHANGE = 12;//侧边栏切换设备
    public static final int UpdateCupSetting = 13;//杯子设置信息更新
    public static final int DeleteDevice = 14;//删除杯子
    public static final int CenterDeviceClick = 15;//个人中心我的杯子点击
    public static final int FilterStatusChat = 16;//滤芯状态咨询点击


}
