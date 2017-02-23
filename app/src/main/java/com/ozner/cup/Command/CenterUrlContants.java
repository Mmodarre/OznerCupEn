package com.ozner.cup.Command;

/**
 * Created by xinde on 2016/1/11.
 */
public class CenterUrlContants {
    //基础url
    private static String baseUrl = "http://www.oznerwater.com/lktnew/wap/app/Oauth2.aspx?";

    //我的小金库
    private static String myMoneyUrl = "http://www.oznerwater.com/lktnew/wapnew/Member/MyCoffers.aspx";
    //我的订单
    private static String myOrderUrl = "http://www.oznerwater.com/lktnew/wapnew/Orders/OrderList.aspx";
    //领红包
    public static String getRedPacUrl = "http://www.oznerwater.com/lktnew/wapnew/Member/GrapRedPackages.aspx";
    public static String getShareHBUrl = "http://www.oznerwater.com/lktnew/wap/wxoauth.aspx?gourl=http://www.oznerwater.com/lktnew/wap/Member/InvitedMemberBrand.aspx";

    //我的券
    private static String myTicketUrl = "http://www.oznerwater.com/lktnew/wapnew/Member/AwardList.aspx";
    //分享礼卡
    private static String shareCardUrl = "http://www.oznerwater.com/lktnew/wapnew/ShareLk/ShareTicketList.aspx";

    // 商城
    public static String mallUrl = "http://www.oznerwater.com/lktnew/wap/mall/mallHomePage.aspx";

    //水探头滤芯商城
    public static String tapshopUrl = "http://www.oznerwater.com/lktnew/wap/mall/goodsDetail.aspx?gid=39";
    //空净滤芯商城
    public static String kjShopUrl = "http://www.oznerwater.com/lktnew/wap/mall/goodsDetail.aspx?gid=64&il=1";

    //智能杯
    public static String filterCupUrl = "http://www.oznerwater.com/lktnew/wap/mall/goodsDetail.aspx?gid=7";
    //滤芯状态金色伊泉
    public static String filterGoldSpringUrl = "http://www.oznerwater.com/lktnew/wap/mall/goodsDetail.aspx?gid=43";
    //滤芯状态谁探头
    public static String filterTapUrl = "http://www.oznerwater.com/lktnew/wap/mall/goodsDetail.aspx?gid=36";

    //365安心服务
    public static String securityServiceUrl = "http://www.oznerwater.com/lktnew/wap/mall/goodsDetail.aspx?gid=9";
    //迷你净水器滤芯购买链接
    public static String miniPurifierUrl = "http://www.oznerwater.com/lktnew/wap/shopping/confirmOrderFromQrcode.aspx?gid=68";
    //台式净水器滤芯购买链接
//    public static String deskPurifierUrl = "http://www.oznerwater.com/lktnew/wap/shopping/confirmOrderFromQrcode.aspx?gid=69";
    public static String deskPurifierUrl = "http://www.oznerwater.com/lktnew/wap/shopping/confirmOrderFromQrcode.aspx?gid=65";
    //补水仪精华液购买链接
    public static String buyReplenWaterUrl = "http://www.oznerwater.com/lktnew/wap/mall/goodsDetail.aspx?gid=203";

//    /*
//    *可单独使用链接
//     */
//    //浩泽365安心服务
//    public static String SecurityService = "http://www.oznerwater.com/lktnew/wap/other/FAQGD.aspx";


    private static String getformatUrl(String gourl) {
        String result = baseUrl + "mobile=%s&UserTalkCode=%s&Language=%s&Area=%s&goUrl=" + gourl;
        return result;
    }

    //格式化我的小金库url
    public static String formatMyMoneyUrl(String mobile, String usertoken, String language, String area) {
        String result = getformatUrl(myMoneyUrl);
        result = String.format(result, mobile, usertoken, language, area);
        return result;
    }

    //格式化我的订单url
    public static String formatMyOrderUrl(String mobile, String usertoken, String language, String area) {
        String result = getformatUrl(myOrderUrl);
        result = String.format(result, mobile, usertoken, language, area);
        return result;
    }

    //格式化领红包url
    public static String formatRedPacUrl(String mobile, String usertoken, String language, String area) {
        String result = getformatUrl(getRedPacUrl);
        result = String.format(result, mobile, usertoken, language, area);
        return result;
    }

    //格式化我的券url
    public static String formatMyTicketUrl(String mobile, String usertoken, String language, String area) {
        String result = getformatUrl(myTicketUrl);
        result = String.format(result, mobile, usertoken, language, area);
        return result;
    }

    //格式化分享礼卡url
    public static String formatShareCardUrl(String mobile, String usertoken, String language, String area) {
        String result = getformatUrl(shareCardUrl);
        result = String.format(result, mobile, usertoken, language, area);
        return result;
    }

    //格式化商城url
    public static String getMallUrl(String mobile, String usertoken, String language, String area) {
        String result = getformatUrl(mallUrl);
        result = String.format(result, mobile, usertoken, language, area);
        return result;
    }

    //水探头滤芯商城
    public static String formatTapShopUrl(String mobile, String usertoken, String language, String area) {
        String result = getformatUrl(tapshopUrl);
        result = String.format(result, mobile, usertoken, language, area);
        return result;
    }

    //RO滤芯商城
    public static String formatRoShopUrl(String mobile, String usertoken, String language, String area) {
        String result = getformatUrl(mallUrl);
        result = String.format(result, mobile, usertoken, language, area);
        return result;
    }

    //空净滤芯商城
    public static String formatKjShopUrl(String mobile, String usertoken, String language, String area) {
        String result = getformatUrl(kjShopUrl);
        result = String.format(result, mobile, usertoken, language, area);
        return result;
    }

    //格式化水杯url
    public static String formatFilterCupUrl(String mobile, String usertoken, String language, String area) {
        String result = getformatUrl(filterCupUrl);
        result = String.format(result, mobile, usertoken, language, area);
        return result;
    }

    //格式化水探头
    public static String formatFilterTapUrl(String mobile, String usertoken, String language, String area) {
        String result = getformatUrl(filterTapUrl);
        result = String.format(result, mobile, usertoken, language, area);
        return result;
    }

    //格式化金色伊泉url
    public static String formatFilterGoldSpringUrl(String mobile, String usertoken, String language, String area) {
        String result = getformatUrl(filterGoldSpringUrl);
        result = String.format(result, mobile, usertoken, language, area);
        return result;
    }

    //格式化365安心服务url
    public static String formatSecurityServiceUrl(String mobile, String usertoken, String language, String area) {
        String result = getformatUrl(securityServiceUrl);
        result = String.format(result, mobile, usertoken, language, area);
        return result;
    }

    //格式化迷你净水器滤芯套餐url
    public static String formatMiniPurifierUrl(String mobile, String usertoken, String language, String area) {
        String result = getformatUrl(miniPurifierUrl);
        return String.format(result, mobile, usertoken, language, area);
    }

    //格式化台式净水器滤芯套餐url
    public static String formatDeskPurifierUrl(String mobile, String usertoken, String language, String area) {
        String result = getformatUrl(deskPurifierUrl);
        return String.format(result, mobile, usertoken, language, area);
    }

    /**
     * 格式化补水仪购买精华液链接
     *
     * @param mobile
     * @param usertoken
     * @param language
     * @param area
     *
     * @return
     */
    public static String formatBuyReplenWaterUrl(String mobile, String usertoken, String language, String area) {
        String result = getformatUrl(buyReplenWaterUrl);
        return String.format(result, mobile, usertoken, language, area);
    }

    public static String formatUrl(String goUrl, String mobile, String usertoken, String language, String area) {
        String result = getformatUrl(goUrl);
        return String.format(result, mobile, usertoken, language, area);
    }
}
