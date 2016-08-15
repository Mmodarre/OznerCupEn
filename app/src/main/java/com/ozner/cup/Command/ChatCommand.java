package com.ozner.cup.Command;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.HttpGet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.UUID;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.client.config.RequestConfig;
import cz.msebera.android.httpclient.client.methods.CloseableHttpResponse;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.FileEntity;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.impl.client.CloseableHttpClient;
import cz.msebera.android.httpclient.impl.client.HttpClients;
import cz.msebera.android.httpclient.protocol.HTTP;
import cz.msebera.android.httpclient.util.EntityUtils;

/**
 * Created by xinde on 2015/12/18.
 */
public class ChatCommand {
    private final static int RetryCount = 3;
    public static final String appid = "hzapi";
    public static final String appsecret = "8af0134asdffe12";
    public static final int UploadTimeOut = 6 * 10000; // 超时时间
//            public static final String CHAT_HOST = "http://192.168.172.21";
    public static final String CHAT_HOST = "http://dkf.ozner.net";
    public static final String CHAT_ACCESS_TOKEN_URL = "/api/token.ashx?appid=%s&appsecret=%s&sign=%s";//获取token
    public static final String CHAT_LOGIN_URL = "/api/customerlogin.ashx?access_token=%s&sign=%s";//咨询登录
    public static final String CHAT_USERINFO_URL = "/api/member.ashx?access_token=%s&sign=%s";//用户信息获取
    public static final String CHAT_SEND_MSG_URL = "/api/customermsg.ashx?access_token=%s&sign=%s";//用户发送信息
    public static final String CHAT_HISTORY_RECORD_URL = "/api/historyrecord.ashx?access_token=%s&sign=%s";//获取历史记录
    public static final String CHAT_KILL_QUEUE = "/api/cuskillqueue.ashx?access_token=%s&sign=%s";//用户结束会话
    public static final String CHAT_UPLOAD_IMG_URL = "/api/uploadpic.ashx?access_token=%s&sign=%s";//上传图片

    public static void addPushMsgCount(Context context) {
        String countStr = UserDataPreference.GetUserData(context, UserDataPreference.NewChatmsgCount, "0");
        int chatNewCount = Integer.parseInt(countStr);
        chatNewCount++;
        UserDataPreference.SetUserData(context, UserDataPreference.NewChatmsgCount, String.valueOf(chatNewCount));
    }

    public static void reSetMsgCount(Context context) {
        UserDataPreference.SetUserData(context, UserDataPreference.NewChatmsgCount, "0");
    }

//    public static void addPushMsgCount(Context context, int msgCount) {
//        String countStr = UserDataPreference.GetUserData(context, UserDataPreference.NewChatmsgCount, "0");
//        int chatNewCount = Integer.parseInt(countStr);
//        chatNewCount += msgCount;
//        UserDataPreference.SetUserData(context, UserDataPreference.NewChatmsgCount, String.valueOf(chatNewCount));
//    }

    /*
    *access_token返回对象
     */
    public static class OznerChatToken {
        public int state;
        public String access_token;
        public String msg;
        public int expires_in;
    }

    /*
    * 登录返回信息返回对象
     */
    public static class OznerChatLoginInfo {
        public int state;
        public String msg;
        public int kfid;//客服id
        public String kfName;//客服姓名
    }

    /*
    * 返回的会员信息返回对象
     */
    public static class OznerChatUserInfo {
        public int state;
        public String msg;
        public int userCount;
        public ArrayList<UserResult> UserList = new ArrayList<UserResult>();

        public static class UserResult {
            public String customer_id;//会员id
            public String customer_name;//昵称
            public String mobile;
            public String customer_ident;//银卡
            public String city_name;
            public String BigAreaName;//大区名
            public String weixin_openId;//微信openid
            public String email;
            public String province_name;
            public String reg_time;
            public String sex;
            public int grade_id;
        }
    }


    /*
    * 会员发送信息返回结果
     */
    public static class OznerChatSendReturn {
        public int state;
        public String msg;
        public String result;
    }

    /*
    *function: 获取access_token
    * 状态：测试完成
     */
    public static OznerChatToken oznerChatGetToken() {
        OznerChatToken chatToken = new OznerChatToken();
        String getpar = "appid=" + ChatCommand.appid + "&appsecret=" + ChatCommand.appsecret;
        String oldSign = ChatCommand.Md5(getpar);
        String url = String.format(ChatCommand.CHAT_ACCESS_TOKEN_URL, ChatCommand.appid, ChatCommand.appsecret, oldSign);
        String result = oznerChatGet(url);
//        Log.i("tag", "token_result:" + result);
        if (result != null && result != "") {
            try {
                JSONObject jo = new JSONObject(result);
                chatToken.msg = jo.getString("msg");
                chatToken.state = jo.getInt("code");
                if (jo.getInt("code") == 0) {
                    chatToken.access_token = jo.getJSONObject("result").getString("access_token");
                    chatToken.expires_in = jo.getJSONObject("result").getInt("expires_in");
                }
            } catch (JSONException e) {
                e.printStackTrace();
                chatToken.state = -1;
                chatToken.msg = e.getMessage();
            }
        } else {
            chatToken.state = -1;
            chatToken.msg = "null";
        }
        return chatToken;
    }

    /*
    *用户登录参数对象，全部必填
     */
    public static class OznerLoginPars {
        public String customer_id;
        public String device_id;
        public String ct_id;
    }

    /*
    *用户登录
    * newSign 是第二次的签名（access_token=%s&appid=%s&appsecret=%s)
    * 状态：测试完成
     */
    public static OznerChatLoginInfo oznerLogin(OznerLoginPars pars, String token, String newSign) {
        OznerChatLoginInfo loginInfo = new OznerChatLoginInfo();
        String loginUrl = String.format(CHAT_LOGIN_URL, token, newSign);
        JSONObject reqpar = new JSONObject();
        try {
            if (pars.customer_id != null && !pars.customer_id.isEmpty())
                reqpar.put("customer_id", pars.customer_id);
            if (pars.device_id != null && !pars.device_id.isEmpty())
                reqpar.put("device_id", pars.device_id);
            if (pars.ct_id != null && !pars.ct_id.isEmpty())
                reqpar.put("ct_id", pars.ct_id);
            reqpar.put("channel_id", "5");
            String result = oznerChatPost(loginUrl, reqpar.toString());
            Log.i("tag", "login_return:" + result);
            if (result != null && result != "") {
                JSONObject resObj = new JSONObject(result);
                loginInfo.state = resObj.getInt("code");
                loginInfo.msg = resObj.getString("msg");
                if (resObj.getInt("code") == 0) {
                    loginInfo.kfid = resObj.getJSONObject("result").getInt("kfid");
                    loginInfo.kfName = resObj.getJSONObject("result").getString("kfname");
                }
            } else {
                loginInfo.state = -1;
                loginInfo.msg = "result is null";
            }
        } catch (JSONException e) {
            e.printStackTrace();
            loginInfo.state = -2;
            loginInfo.msg = e.getMessage();
//            return null;
        }
        return loginInfo;
    }

    /*
    *用户登出
     */
    public static String oznerChatLogout(String customid, String ac_token, String sign) {
        String logoutUrl = String.format(CHAT_KILL_QUEUE, ac_token, sign);
        JSONObject reqpar = new JSONObject();
        try {
            reqpar.put("access_token", ac_token);
            reqpar.put("customer_id", customid);
            String result = oznerChatPost(logoutUrl, reqpar.toString());
            return result;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
    *会员发送消息参数对象
    * device_id,必填
     */
    public static class OznerSendPars {
        public String customer_id;
        public String device_id;
        public String msg;
    }

    /*
    * 会员发送消息，返回0-成功；其他-失败
    * 状态：测试完成
     */
    public static OznerChatSendReturn oznerSendMsg(OznerSendPars pars, String token, String newSign) {
        String sendUrl = String.format(CHAT_SEND_MSG_URL, token, newSign);
        OznerChatSendReturn chatSendReturn = new OznerChatSendReturn();
        JSONObject senmsg = new JSONObject();
        try {
            if (pars != null) {
                if (pars.customer_id != null && !pars.customer_id.isEmpty())
                    senmsg.put("customer_id", pars.customer_id);
                if (pars.device_id != null && !pars.device_id.isEmpty())
                    senmsg.put("device_id", pars.device_id);
                if (pars.msg != null && !pars.msg.isEmpty())
                    senmsg.put("msg", pars.msg);
                chatSendReturn.state = -1;
            }
            senmsg.put("channel_id", "5");
            String result = oznerChatPost(sendUrl, senmsg.toString());
            Log.i("tag", "sendmsg_return:" + result);
            if (result != null && result != "") {
                JSONObject resObj = new JSONObject(result);
                chatSendReturn.state = resObj.getInt("code");
                chatSendReturn.msg = resObj.getString("msg");
                chatSendReturn.result = resObj.getString("result");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            chatSendReturn.state = -1;
            chatSendReturn.msg = e.getMessage();
            return chatSendReturn;
        }
        return chatSendReturn;
    }

    /*
    *获取会员信息，全部选填，不填为获取全部会员信息
    * 查询会员，尽量传mobile参数
     */
    public static class OznerUserInfoPars {
        public String id;
        public String customer_id;
        public String customer_name;
        public String mobile;
        public String email;
        public String wx_open_id;
        public String ucode;//推广码
    }

    /*
    * 获取会员信息，0-成功，-1 - 请求失败，其他-失败
    * 状态：测试完毕
     */
    public static OznerChatUserInfo oznerLoadUserInfo(OznerUserInfoPars pars, String token, String newSign) {
        String loadUserUrl = String.format(CHAT_USERINFO_URL, token, newSign);
        OznerChatUserInfo chatUserInfo = new OznerChatUserInfo();
        JSONObject reqpar = new JSONObject();
        try {
            if (pars != null) {
                if (pars.customer_id != null && !pars.customer_id.isEmpty())
                    reqpar.put("customer_id", pars.customer_id);
                if (pars.mobile != null && !pars.mobile.isEmpty())
                    reqpar.put("mobile", pars.mobile);
                if (pars.customer_name != null && !pars.customer_name.isEmpty())
                    reqpar.put("customer_name", pars.customer_name);
                if (pars.email != null && !pars.email.isEmpty()) {
                    reqpar.put("email", pars.email);
                }
                if (pars.ucode != null && !pars.ucode.isEmpty()) {
                    reqpar.put("ucode", pars.ucode);
                }
                if (pars.wx_open_id != null && !pars.wx_open_id.isEmpty()) {
                    reqpar.put("wx_openid_id", pars.wx_open_id);
                }
            }

            String result = oznerChatPost(loadUserUrl, reqpar.toString());
            Log.i("tag", "userInfo:" + result);
            if (result != null && result != "") {

                JSONObject resObj = new JSONObject(result);
                chatUserInfo.state = resObj.getInt("code");
                chatUserInfo.msg = resObj.getString("msg");
                if (resObj.getInt("code") == 0) {
                    JSONObject resJo = resObj.getJSONObject("result");
                    int count = resJo.getInt("count");
                    chatUserInfo.userCount = resJo.getInt("count");
                    if (count > 0) {
                        JSONArray jsonArray = resJo.getJSONArray("list");
                        for (int i = 0; i < count; i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            OznerChatUserInfo.UserResult userResult = new OznerChatUserInfo.UserResult();
                            userResult.customer_id = jsonObject.getString("customer_id");
                            userResult.mobile = jsonObject.getString("mobile");
                            userResult.customer_name = jsonObject.getString("customer_name");
                            userResult.BigAreaName = jsonObject.getString("BigAreaName");
                            userResult.email = jsonObject.getString("email");
                            userResult.grade_id = jsonObject.getInt("grade_id");
                            userResult.city_name = jsonObject.getString("city_name");
                            userResult.customer_ident = jsonObject.getString("customer_ident");
                            userResult.province_name = jsonObject.getString("province_name");
                            userResult.weixin_openId = jsonObject.getString("weixin_open_id");
                            userResult.reg_time = jsonObject.getString("reg_time");
                            userResult.sex = jsonObject.getString("sex");
                            chatUserInfo.UserList.add(userResult);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            chatUserInfo.state = -1;
            chatUserInfo.msg = e.getMessage();
            return chatUserInfo;
        }
        return chatUserInfo;
    }


    /*
    *获取历史消息输入参数
     */
    public static class OznerHistoryPars {
        public String kf_id;
        public String customer_id;
        public String record_id;
        public String page;
        public String pagesize;
        public String order;//升序a，降序d,默认降序
    }

    /*
    *获取历史消息返回结果对象
     */
    public static class OznerHistoryResult {
        public int state;
        public String msg;
        public int totalCount;
        public int getCount;
        public ArrayList<HistoryMsg> historyMsgs = new ArrayList<HistoryMsg>();

        public static class HistoryMsg {
            public long timeetamp;
            public int flow_id;
            public String kf_account;
            public String kf_id;
            public String message;
            public int oper;
            public int queue_id;
            public String customerId;
            public String customerNmae;
        }
    }

    /*
    *获取历史消息
    * 状态：测试完成
     */
    public static OznerHistoryResult oznerGetHistoryMsg(final OznerHistoryPars pars, final String token, final String sign) {
        OznerHistoryResult oznerHistoryResult = new OznerHistoryResult();
        String historyUrl = String.format(CHAT_HISTORY_RECORD_URL, token, sign);
        Log.e("tag", "getHistoryMsgUrl:" + historyUrl);
        JSONObject reqPars = new JSONObject();
        if (pars != null) {
            try {
                if (pars.kf_id != null && !pars.kf_id.isEmpty()) {
                    reqPars.put("kf_id", pars.kf_id);
                }
                if (pars.customer_id != null && !pars.customer_id.isEmpty()) {
                    reqPars.put("customer_id", pars.customer_id);
                }
                if (pars.record_id != null && !pars.record_id.isEmpty()) {
                    reqPars.put("record_id", pars.record_id);
                }
                if (pars.pagesize != null && !pars.pagesize.isEmpty()) {
                    reqPars.put("pagesize", pars.pagesize);
                }
                if (pars.order != null && !pars.order.isEmpty()) {
                    reqPars.put("order", pars.order);
                }
                String result = oznerChatPost(historyUrl, reqPars.toString());
                Log.i("tag", "historyMsg:" + result);
                if (result != null && result != "") {

                    JSONObject resObj = new JSONObject(result);
                    oznerHistoryResult.state = resObj.getInt("code");
                    oznerHistoryResult.msg = resObj.getString("msg");
                    if (resObj.getInt("code") == 0) {
                        JSONObject resJo = resObj.getJSONObject("result");
                        oznerHistoryResult.totalCount = resJo.getInt("count");
                        if (resJo.getInt("count") > 0) {
                            JSONArray jsonArray = resJo.getJSONArray("list");
                            oznerHistoryResult.getCount = jsonArray.length();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                OznerHistoryResult.HistoryMsg msgitem = new OznerHistoryResult.HistoryMsg();
                                msgitem.flow_id = jsonObject.getInt("_flow_id");
                                msgitem.kf_account = jsonObject.getString("_kf_account");
                                msgitem.kf_id = jsonObject.getString("_kf_id");
                                msgitem.message = jsonObject.getString("_message");
                                msgitem.oper = jsonObject.getInt("_oper");
                                msgitem.queue_id = jsonObject.getInt("_queue_id");
                                msgitem.timeetamp = jsonObject.getLong("_add_timestamp");
                                msgitem.customerId = jsonObject.getString("_customer_id");
                                msgitem.customerNmae = jsonObject.getString("_customer_name");
                                oznerHistoryResult.historyMsgs.add(msgitem);
                            }
                        }
                    }
                } else {
                    oznerHistoryResult.state = -1;
                    oznerHistoryResult.msg = "result is null";
                }
            } catch (JSONException e) {
                e.printStackTrace();
                oznerHistoryResult.state = -1;
                oznerHistoryResult.msg = e.getMessage();
                return oznerHistoryResult;
            }
        }
        return oznerHistoryResult;
    }

    /*
    * 上传图片返回对象
     */
    public static class OznerUploadResult {
        public int state;
        public String msg;
        public String imgUrl;
//        public int httpCode;
//        public String httpMsg;
    }

    /*
   *上传图片
     */
    public static OznerUploadResult oznerUploadImage(String imgPath, final String token, final String newsign) {
        OznerUploadResult uploadResult = new OznerUploadResult();
        Log.i("tag", "oznerUploadImage");
        String uploadUrl = String.format(CHAT_UPLOAD_IMG_URL, token, newsign);
        File imgFile = new File(imgPath);
        return uploadFile(ChatCommand.CHAT_HOST + uploadUrl, imgFile);
    }

    public static OznerUploadResult uploadFile(String uploadUrl, File file) {
        OznerUploadResult oznerUploadResult = new OznerUploadResult();
        String BOUNDARY = UUID.randomUUID().toString(); // 边界标识 随机生成
        String PREFIX = "--", LINE_END = "\r\n";
        String CONTENT_TYPE = "multipart/form-data"; // 内容类型
//        String RequestURL = "http://192.168.0.100:7080/YkyPhoneService/Uploadfile1";
        try {
            URL url = new URL(uploadUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(UploadTimeOut);
            conn.setConnectTimeout(UploadTimeOut);
            conn.setDoInput(true); // 允许输入流
            conn.setDoOutput(true); // 允许输出流
            conn.setUseCaches(false); // 不允许使用缓存
            conn.setRequestMethod("POST"); // 请求方式
            conn.setRequestProperty("Charset", "utf-8"); // 设置编码
            conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary="
                    + BOUNDARY);
            if (file != null) {
                /**
                 * 当文件不为空，把文件包装并且上传
                 */
                OutputStream outputSteam = conn.getOutputStream();

                DataOutputStream dos = new DataOutputStream(outputSteam);
                StringBuffer sb = new StringBuffer();
                sb.append(PREFIX);
                sb.append(BOUNDARY);
                sb.append(LINE_END);
                /**
                 * 这里重点注意： name里面的值为服务器端需要key 只有这个key 才可以得到对应的文件
                 * filename是文件的名字，包含后缀名的 比如:abc.png
                 */

                sb.append("Content-Disposition: form-data; name=\"img\"; filename=\""
                        + file.getName() + "\"" + LINE_END);
                sb.append("Content-Type: application/octet-stream; charset=utf-8" + LINE_END);
                sb.append(LINE_END);
                dos.write(sb.toString().getBytes());
                InputStream is = new FileInputStream(file);
                byte[] bytes = new byte[1024 * 6];
                int len = 0;
                while ((len = is.read(bytes)) != -1) {
                    dos.write(bytes, 0, len);
                }
                is.close();
                dos.write(LINE_END.getBytes());
                byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END)
                        .getBytes();
                dos.write(end_data);
                dos.flush();
                /**
                 * 获取响应码 200=成功 当响应成功，获取响应的流
                 */
                int resCode = conn.getResponseCode();
                Log.i("tag", "uploadImg_Method:" + resCode + ", " + conn.getResponseMessage());
                oznerUploadResult.state = resCode;
                oznerUploadResult.msg = conn.getResponseMessage();

                if (resCode == 200) {
                    InputStream inputStream = conn.getInputStream();
                    StringBuilder resStr = new StringBuilder();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line = "";
                    while ((line = reader.readLine()) != null) {
                        resStr.append(line);
                    }
                    Log.i("tag", "uploadImg_Result:" + resStr);
                    inputStream.close();
                    try {
                        JSONObject resJObject = new JSONObject(resStr.toString());
                        oznerUploadResult.state = resJObject.getInt("code");
                        oznerUploadResult.msg = resJObject.getString("msg");
                        oznerUploadResult.imgUrl = resJObject.getJSONObject("result").getString("picpath");
                    } catch (JSONException e) {
                        e.printStackTrace();
                        oznerUploadResult.state = -1;
                        oznerUploadResult.msg = e.getMessage();
                        return oznerUploadResult;
                    }
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            oznerUploadResult.state = -2;
            oznerUploadResult.msg = e.getMessage();
            return oznerUploadResult;
        } catch (IOException e) {
            e.printStackTrace();
            oznerUploadResult.state = -3;
            oznerUploadResult.msg = e.getMessage();
            return oznerUploadResult;
        }
        return oznerUploadResult;
    }


    public static String Md5(String string) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("ASCII"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Huh, MD5 should be supported?", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Huh, UTF-8 should be supported?", e);
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }

    /*
    *get 方法获取数据
     */
    public static String oznerChatGet(String url) {
        return httpGetString(CHAT_HOST + url, 5000);
    }

    /*
    * post 方法获取数据
    */
    public static String oznerChatPost(String url, String parm) {
        String value = null;
        for (int i = 0; i < RetryCount; i++) {
            Log.e("CsirNet:" + i, url + ":" + parm);
            value = httpPostString(CHAT_HOST + url, parm, 5000);
            if (value != null) {
                return value;
            }
            Log.e("CsirNetWorkRetry:" + i, url);
        }
        return value;
    }


    public static String httpGetString(String url, int timeout) {
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(timeout).setConnectTimeout(timeout).build();//设置请求和传输超时时间
        //Null 重试次数
        for (int i = 0; i < RetryCount; i++) {
            String value = responseGetString(url, requestConfig);
            if (value != null) {
                return value;
            }
            Log.e("CsirNetWorkRetry:" + i, url);
        }
        return null;
    }

    public static String httpPostString(String url, String parm, int timeout) {
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(timeout).setConnectTimeout(timeout).build();//设置请求和传输超时时间
        //Null 重试次数
        for (int i = 0; i < RetryCount; i++) {
            String value = responsePostString(url, parm, requestConfig);
            if (value != null) {
                return value;
            }
            Log.e("CsirNetWorkRetry:" + i, url);
        }
        return null;
    }


    private static String responseGetString(String url, RequestConfig requestConfig) {
        try {
            //声明HttpClient对象
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(url);
            httpGet.setConfig(requestConfig);
            CloseableHttpResponse response2 = httpclient.execute(httpGet);
            try {
                if (response2.getStatusLine().getStatusCode() == 200) {
                    HttpEntity entity2 = response2.getEntity();
                    String strResult = EntityUtils.toString(entity2);
                    return strResult;
                }

            } finally {
                response2.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        return null;
    }

    private static String responsePostString(String url, String parm, RequestConfig requestConfig) {
        try {
            StringEntity parmentity = new StringEntity(parm, HTTP.UTF_8);
            //声明HttpClient对象
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(url);
            httpPost.setConfig(requestConfig);
            httpPost.setEntity(parmentity);
            CloseableHttpResponse response2 = httpclient.execute(httpPost);
            try {
                if (response2.getStatusLine().getStatusCode() == 200) {
                    HttpEntity entity2 = response2.getEntity();
                    String strResult = EntityUtils.toString(entity2);
                    return strResult;
                }
            } finally {
                response2.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        return null;
    }

    private static String uploadPostRequest(String url, String filePath, RequestConfig requestConfig) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(requestConfig);
        FileEntity fileEntity = new FileEntity(new File(filePath));
        httpPost.setEntity(fileEntity);
        try {
            CloseableHttpResponse response = httpClient.execute(httpPost);
            try {
                if (response.getStatusLine().getStatusCode() == 200) {
                    HttpEntity resEntity = response.getEntity();
                    String strResult = EntityUtils.toString(resEntity);
                    return strResult;
                }
            } finally {
                response.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

}
