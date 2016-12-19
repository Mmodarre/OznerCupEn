package com.ozner.qianye.CChat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.ozner.qianye.BuildConfig;
import com.ozner.qianye.CChat.adapter.ChatLVAdapter;
import com.ozner.qianye.CChat.adapter.FaceGVAdapter;
import com.ozner.qianye.CChat.adapter.FaceVPAdapter;
import com.ozner.qianye.CChat.bean.ChatInfo;
import com.ozner.qianye.CChat.bean.ChatMessage;
import com.ozner.qianye.CChat.view.MyEditText;
import com.ozner.qianye.Command.ChatCommand;
import com.ozner.qianye.Command.FootFragmentListener;
import com.ozner.qianye.Command.ImageHelper;
import com.ozner.qianye.Command.OznerPreference;
import com.ozner.qianye.Command.UserDataPreference;
import com.ozner.qianye.Device.OznerApplication;
import com.ozner.qianye.R;
import com.ozner.qianye.mycenter.LoadingDialog;
import com.xuzhiyong.ui.PictureChooseActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.msebera.android.httpclient.Header;

/**
 * Modify by C-sir@hotmail.com
 */
public class CChatFragment extends Fragment implements OnClickListener, FootFragmentListener {
    private static final String TAG = "CChatFragment";
    private String deviceId, mMobile, mCustomId, newSign, token;
    LoadingDialog loadingDialog;
    static final int RequestPictureChoose = 0x100;
    static final int RequestCamera = 0x101;
    private ViewPager mViewPager;
    private LinearLayout mDotsLayout;
    private MyEditText input;
    private Button send;
    private RecyclerView mListView;
    private ChatLVAdapter mLvAdapter;
    File cameraFile;
    InputMethodManager inputManager;
    private LinearLayout fram_rootView;
    //    private TextView tv_list_title;
    private RelativeLayout rlay_input, rlay_record;
    private LinearLayout llay_face_container, llay_picSelect, llay_picture, llay_camera;
    LinearLayout chat_face_container;
    private ImageView image_face, iv_selectPic, iv_voice;//表情图标
    ImageView iv_call;
    // 7列3行
    private int columns = 6;
    private int rows = 4;
    private List<View> views = new ArrayList<View>();
    private List<String> staticFacesList;
    private List<ChatMessage> msgList = new ArrayList<>();
    private LinkedList<ChatInfo> infos = new LinkedList<ChatInfo>();
    private SimpleDateFormat sd;
    MyTextWatch myTextWatch = new MyTextWatch();
    private String reply = "";//模拟回复
    private int clickid = 0;
    private int curPage = 0;
    private final int PageSize = 40;
    private boolean isSendImg = false;//是否发送的图片

    //屏幕高度
    private int screenHeight = 0;
    //软件盘弹起后所占高度阀值
    private int keyHeight = 0;
    private FootFragmentListener mFootFragmentListener;

    String recMac = "";
    private ProgressDialog waitDialog;
    private boolean isSending = false;
    private ChatMessageHelper chatMessageHelper = null;
    private String chatUserId;
    private String sendImgMsg = null;
    private final int timeLength = String.valueOf(new Date().getTime()).length();


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mFootFragmentListener = (FootFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        Log.e(TAG, "CChatFragment_onAttach");
    }


    @Override
    public void onDetach() {
        if (waitDialog != null) {
            waitDialog.dismiss();
            waitDialog = null;
        }
        Log.e(TAG, "CChatFragment_onDetach");
        super.onDetach();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initViews(getView());
        Bundle bundle = getArguments();
        if (bundle != null) {
            recMac = bundle.getString("MAC");
        }
        waitDialog = new ProgressDialog(getContext());
        inputManager = ((InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE));

        loadingDialog = LoadingDialog.createLoading(getContext());

        mMobile = UserDataPreference.GetUserData(getContext(), UserDataPreference.Mobile, null);
        deviceId = UserDataPreference.GetUserData(getContext(), UserDataPreference.BaiduDeviceId, null);
        Log.e(TAG, "onActivityCreated: deviceId=" + deviceId);

        initChat();
        showFootNav();
        initChatMsgList();
        NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    private void initChatMsgList() {

        Log.e(TAG, "userid:" + OznerPreference.GetValue(getContext(), UserDataPreference.UserId, null));
        chatUserId = OznerPreference.GetValue(getContext(), UserDataPreference.UserId, null);
        if (chatUserId != null) {
            chatUserId = chatUserId.replace("-", "");
            chatMessageHelper = chatMessageHelper.getInstance(chatUserId);
            chatMessageHelper.InitTable(getContext());
            msgList = chatMessageHelper.getMessageList(getContext());
        }

        if (msgList != null && msgList.size() > 0) {
            infos.clear();
            Log.e(TAG, "MsgList_size:" + msgList.size());
            for (ChatMessage msgItem : msgList) {
                insertChatMsg(msgItem);
            }
            mLvAdapter.notifyDataSetChanged();
            httpHandler.post(new Runnable() {
                @Override
                public void run() {
                    scrollMsgListTo(msgList.size());
//                    mListView.scrollToPosition(msgList.size());
                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        try {
            View view = inflater.inflate(R.layout.chat_main2, container, false);
            ((ImageView) view.findViewById(R.id.iv_call)).setImageBitmap(ImageHelper.loadResBitmap(getContext(),R.mipmap.chat_call));
            ((ImageView) view.findViewById(R.id.image_face)).setImageBitmap(ImageHelper.loadResBitmap(getContext(),R.drawable.chat_face));
            ((ImageView) view.findViewById(R.id.iv_selectPic)).setImageBitmap(ImageHelper.loadResBitmap(getContext(),R.drawable.circle_add));
            ((ImageView) view.findViewById(R.id.iv_voice)).setImageBitmap(ImageHelper.loadResBitmap(getContext(),R.drawable.msgbox_voice));

            initStaticFaces();
            return view;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("SimpleDateFormat")
    private void initViews(View view) {
        OznerApplication.changeTextFont((ViewGroup) view);
        fram_rootView = (LinearLayout) view.findViewById(R.id.rootview);
//        rlay_chat_main = (RelativeLayout) view.findViewById(R.id.chat_main);
        mListView = (RecyclerView) view.findViewById(R.id.message_chat_listview);
//        tv_header = (TextView) view.findViewById(R.id.tv_header);
//        rlay_back = (RelativeLayout) view.findViewById(R.id.rlay_back);
        iv_call = (ImageView) view.findViewById(R.id.iv_call);
        //表情图标
        image_face = (ImageView) view.findViewById(R.id.image_face);
        //表情布局
        chat_face_container = (LinearLayout) view.findViewById(R.id.chat_face_container);
        llay_face_container = (LinearLayout) view.findViewById(R.id.llay_face_container);//表情布局
        llay_picSelect = (LinearLayout) view.findViewById(R.id.llay_picSelect);//选择相片布局
        llay_camera = (LinearLayout) view.findViewById(R.id.llay_camera);//相机
        llay_picture = (LinearLayout) view.findViewById(R.id.llay_picture);//照片
//        rlay_list_title = (RelativeLayout) view.findViewById(R.id.rlay_list_title);
//        tv_list_title = (TextView) view.findViewById(R.id.tv_list_title);
        mViewPager = (ViewPager) view.findViewById(R.id.face_viewpager);
        iv_selectPic = (ImageView) view.findViewById(R.id.iv_selectPic);
        //表情下小圆点
        mDotsLayout = (LinearLayout) view.findViewById(R.id.face_dots_container);
        rlay_input = (RelativeLayout) view.findViewById(R.id.rlay_input);
        rlay_record = (RelativeLayout) view.findViewById(R.id.rlay_record);
        input = (MyEditText) view.findViewById(R.id.input_sms);
        iv_voice = (ImageView) view.findViewById(R.id.iv_voice);
        send = (Button) view.findViewById(R.id.send_sms);
        sd = new SimpleDateFormat("MM-dd HH:mm");
        mLvAdapter = new ChatLVAdapter(getContext(), infos);
        mListView.setAdapter(mLvAdapter);
        GridLayoutManager mLayoutManager = new GridLayoutManager(getContext(), 1);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mListView.setLayoutManager(mLayoutManager);
        mViewPager.setOnPageChangeListener(new PageChange());

        iv_call.setOnClickListener(this);
        input.setOnClickListener(this);
//        rlay_back.setOnClickListener(this);
        iv_voice.setOnClickListener(this);
        iv_selectPic.setOnClickListener(this);
        llay_camera.setOnClickListener(this);
        llay_picture.setOnClickListener(this);
        input.addTextChangedListener(myTextWatch);
        //表情按钮
        image_face.setOnClickListener(this);
        // 发送
        send.setOnClickListener(this);
        //	mListView.setOnRefreshListenerHead(this);
        mListView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
                    if (chat_face_container.getVisibility() == View.VISIBLE) {
                        chat_face_container.setVisibility(View.GONE);
                    }
                    hideSoftInputView();
//                    showFootNav();
                }
                return false;
            }
        });
        InitViewPager();

        //获取屏幕高度
        screenHeight = getActivity().getWindowManager().getDefaultDisplay().getHeight();
        //阀值设置为屏幕高度的1/3
        keyHeight = screenHeight / 3;

        fram_rootView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (oldBottom != 0 && bottom != 0 && (oldBottom - bottom > keyHeight)) {
                    hideFootNav();
                    scrollMsgListTo(infos.size());
                } else if (oldBottom != 0 && bottom != 0 && (bottom - oldBottom > keyHeight)) {
                    showFootNav();
                    scrollMsgListTo(infos.size());
                }

            }
        });

        input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    hideFootNav();
                } else {
                    showFootNav();
                }
            }
        });

    }

    //检查SIM卡状态
    private boolean isSimCardReady() {
        TelephonyManager tm = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
        if (TelephonyManager.SIM_STATE_READY == tm.getSimState()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onResume() {
//        initViews(getView());
        hideFaceContainer();
        showFootNav();
        NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        UserDataPreference.SetUserData(getContext(), UserDataPreference.NewChatmsgCount, "0");
        super.onResume();
    }

    @Override
    public void onClick(View arg0) {

        switch (arg0.getId()) {
            case R.id.iv_call:
                OznerApplication.callSeviceChat(getContext());
//                if (isSimCardReady()) {
//                    Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:4008202667"));
//                    startActivity(callIntent);
//                } else {
//                    AlertDialog tipDialog = new AlertDialog.Builder(getContext()).setMessage(getString(R.string.Chat_SimCardTips))
//                            .setPositiveButton(getString(R.string.ensure), new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    dialog.dismiss();
//                                }
//                            }).create();
//                    tipDialog.setCanceledOnTouchOutside(false);
//                    tipDialog.show();
//                }
                break;
            case R.id.rlay_back:
//                if (getFragmentManager().popBackStackImmediate()) {
                getFragmentManager().popBackStack();
//                }
                break;
            case R.id.input_sms://输入框
                Log.e(TAG, "input_click");
                hideFootNav();
                if (chat_face_container.getVisibility() == View.VISIBLE) {
                    chat_face_container.setVisibility(View.GONE);
                    clickid = 0;
                }
                break;
            case R.id.image_face://表情
//                showFootNav();
                hideSoftInputView();//隐藏软键盘
                if (chat_face_container.getVisibility() == View.VISIBLE && R.id.image_face == clickid) {
                    chat_face_container.setVisibility(View.GONE);
                } else {
                    chat_face_container.setVisibility(View.VISIBLE);
                    llay_picSelect.setVisibility(View.GONE);
                    llay_face_container.setVisibility(View.VISIBLE);
                }
                scrollMsgListTo(infos.size());
                clickid = R.id.image_face;
                break;
            case R.id.send_sms://发送
                if (!isSending) {
                    reply = input.getText().toString();
                    if (!TextUtils.isEmpty(reply)) {
                        String msg = faceTransToMsg(reply);
                        Log.e(TAG, "sendmsg:" + msg);
                        isSending = true;
                        isSendImg = false;
                        chatSendMsg(mCustomId, msg, token, newSign);
                    }
                }
                break;
            case R.id.iv_voice:
                hideSoftInputView();
                hideFaceContainer();
                if (rlay_record.getVisibility() == View.VISIBLE) {
                    rlay_record.setVisibility(View.GONE);
                    rlay_input.setVisibility(View.VISIBLE);
                } else {
                    rlay_record.setVisibility(View.VISIBLE);
                    rlay_input.setVisibility(View.GONE);
                }
                break;
            case R.id.iv_selectPic:
                hideSoftInputView();
                if (chat_face_container.getVisibility() == View.VISIBLE && R.id.iv_selectPic == clickid) {
                    chat_face_container.setVisibility(View.GONE);
                } else {
                    chat_face_container.setVisibility(View.VISIBLE);
                    llay_picSelect.setVisibility(View.VISIBLE);
                    llay_face_container.setVisibility(View.GONE);
                }
                scrollMsgListTo(infos.size());
                clickid = R.id.iv_selectPic;
                break;
            case R.id.llay_camera:
                startCamera();
                break;
            case R.id.llay_picture:
                Intent picIntent = new Intent();
                picIntent.setClass(getActivity(), PictureChooseActivity.class);
                startActivityForResult(picIntent, RequestPictureChoose);
                break;
            default:
                break;
        }
    }

    /*
     * 初始表情 *
     */
    private void InitViewPager() {
        // 获取页数
        for (int i = 0; i < getPagerCount(); i++) {
            views.add(viewPagerItem(i));
            LayoutParams params = new LayoutParams(16, 16);
            mDotsLayout.addView(dotsItem(i), params);
        }
        FaceVPAdapter mVpAdapter = new FaceVPAdapter(views);
        mViewPager.setAdapter(mVpAdapter);
        mDotsLayout.getChildAt(0).setSelected(true);
    }

    private View viewPagerItem(int position) {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.face_gridview, null);//表情布局
        GridView gridview = (GridView) layout.findViewById(R.id.chart_face_gv);
        /**
         * 注：因为每一页末尾都有一个删除图标，所以每一页的实际表情columns *　rows　－　1; 空出最后一个位置给删除图标
         * */
        List<String> subList = new ArrayList<String>();
        subList.addAll(staticFacesList
                .subList(position * (columns * rows - 1),
                        (columns * rows - 1) * (position + 1) > staticFacesList
                                .size() ? staticFacesList.size() : (columns
                                * rows - 1)
                                * (position + 1)));
        /**
         * 末尾添加删除图标
         * */
        subList.add("emotion_del_normal.png");
        FaceGVAdapter mGvAdapter = new FaceGVAdapter(subList, getContext());
        gridview.setAdapter(mGvAdapter);
        gridview.setNumColumns(columns);
        // 单击表情执行的操作
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    String png = ((TextView) ((LinearLayout) view).getChildAt(1)).getText().toString();
                    if (!png.contains("emotion_del_normal")) {// 如果不是删除图标
                        insert(getFace(png));
                    } else {
                        delete();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        return gridview;
    }

    private SpannableStringBuilder getFace(String png) {
        SpannableStringBuilder sb = new SpannableStringBuilder();
        try {
            /**
             * 经过测试，虽然这里tempText被替换为png显示，但是但我单击发送按钮时，获取到輸入框的内容是tempText的值而不是png
             * 所以这里对这个tempText值做特殊处理
             * 格式：#[face/png/f_static_000.png]#，以方便判斷當前圖片是哪一個
             * */
            String tempText = "#[" + png + "]#";
            sb.append(tempText);
            sb.setSpan(
                    new ImageSpan(getContext(), BitmapFactory
                            .decodeStream(getActivity().getAssets().open(png))), sb.length()
                            - tempText.length(), sb.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return sb;
    }

    /**
     * 向输入框里添加表情
     */
    private void insert(CharSequence text) {
        int iCursorStart = Selection.getSelectionStart((input.getText()));
        int iCursorEnd = Selection.getSelectionEnd((input.getText()));
        if (iCursorStart != iCursorEnd) {
            ((Editable) input.getText()).replace(iCursorStart, iCursorEnd, "");
        }
        int iCursor = Selection.getSelectionEnd((input.getText()));
        ((Editable) input.getText()).insert(iCursor, text);
    }

    /**
     * 删除图标执行事件
     * 注：如果删除的是表情，在删除时实际删除的是tempText即图片占位的字符串，所以必需一次性删除掉tempText，才能将图片删除
     */
    private void delete() {
        if (input.getText().length() != 0) {
            int iCursorEnd = Selection.getSelectionEnd(input.getText());
            int iCursorStart = Selection.getSelectionStart(input.getText());
            if (iCursorEnd > 0) {
                if (iCursorEnd == iCursorStart) {
                    if (isDeletePng(iCursorEnd)) {
                        String st = "#[face/png/f_static_000.png]#";
                        ((Editable) input.getText()).delete(
                                iCursorEnd - st.length(), iCursorEnd);
                    } else {
                        ((Editable) input.getText()).delete(iCursorEnd - 1,
                                iCursorEnd);
                    }
                } else {
                    ((Editable) input.getText()).delete(iCursorStart,
                            iCursorEnd);
                }
            }
        }
    }

    /**
     * 判断即将删除的字符串是否是图片占位字符串tempText 如果是：则讲删除整个tempText
     **/
    private boolean isDeletePng(int cursor) {
        String st = "#[face/png/f_static_000.png]#";
        String content = input.getText().toString().substring(0, cursor);
        if (content.length() >= st.length()) {
            String checkStr = content.substring(content.length() - st.length(),
                    content.length());
            String regex = "(\\#\\[face/png/f_static_)\\d{3}(.png\\]\\#)";
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(checkStr);
            return m.matches();
        }
        return false;
    }

    private ImageView dotsItem(int position) {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.dot_image, null);
        ImageView iv = (ImageView) layout.findViewById(R.id.face_dot);
        iv.setId(position);
        return iv;
    }

    /**
     * 根据表情数量以及GridView设置的行数和列数计算Pager数量
     *
     * @return
     */
    private int getPagerCount() {
        int count = staticFacesList.size();
        return count % (columns * rows - 1) == 0 ? count / (columns * rows - 1)
                : count / (columns * rows - 1) + 1;
    }

    /**
     * 初始化表情列表staticFacesList
     */
    private void initStaticFaces() {
        try {
            staticFacesList = new ArrayList<String>();
            String[] faces = getActivity().getAssets().list("face/png");
            //将Assets中的表情名称转为字符串一一添加进staticFacesList
            for (int i = 0; i < faces.length; i++) {
                staticFacesList.add(faces[i]);
            }
            //去掉删除图片
            staticFacesList.remove("emotion_del_normal.png");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 表情页改变时，dots效果也要跟着改变
     */
    class PageChange implements OnPageChangeListener {
        @Override
        public void onPageScrollStateChanged(int arg0) {
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageSelected(int arg0) {
            for (int i = 0; i < mDotsLayout.getChildCount(); i++) {
                mDotsLayout.getChildAt(i).setSelected(false);
            }
            mDotsLayout.getChildAt(arg0).setSelected(true);
        }

    }

    /**
     * 发送的信息
     *
     * @param message
     *
     * @return
     */
    private ChatInfo getChatInfoTo(String message) {
        ChatInfo info = new ChatInfo();
        info.content = message;
        info.fromOrTo = 1;
        info.time = sd.format(new Date());
        return info;
    }

    private ChatInfo getChatInfo(ChatMessage message) {
        ChatInfo info = new ChatInfo();
        info.content = message.getContent();
        info.fromOrTo = message.getOper();
        info.time = sd.format(new Date(message.getTime()));
        info.isSendSuc = message.getIsSendSuc();
        return info;
    }

    /**
     * 接收的信息
     *
     * @param message
     *
     * @return
     */
    private ChatInfo getChatInfoFrom(String message) {
        ChatInfo info = new ChatInfo();
        info.content = message;
        info.fromOrTo = 0;
        info.time = sd.format(new Date());
        return info;
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    mLvAdapter.setList(infos);
                    mLvAdapter.notifyDataSetChanged();
                    //	mListView.onRefreshCompleteHeader();
                    break;
            }
        }
    };

    public void hideSoftInputView() {
        InputMethodManager manager = ((InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE));
        if (getActivity().getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getActivity().getCurrentFocus() != null)
                manager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }


    public void showFootNav() {
        inputHandle.sendEmptyMessage(0x02);
    }


    public void hideFootNav() {
        inputHandle.sendEmptyMessage(0x01);
    }

    InputHandle inputHandle = new InputHandle();

    class InputHandle extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (CChatFragment.this.isAdded() && !CChatFragment.this.isDetached() && !CChatFragment.this.isRemoving()) {
                FrameLayout.LayoutParams fp;
                switch (msg.what) {
                    case 0x01://hide
                        getFragmentManager().beginTransaction().hide(getFragmentManager().findFragmentById(R.id.framen_foot_tab)).commitAllowingStateLoss();
                        fp = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                        fp.setMargins(0, 0, 0, 0);
                        fram_rootView.setLayoutParams(fp);
                        break;
                    case 0x02://up
                        getFragmentManager().beginTransaction().show(getFragmentManager().findFragmentById(R.id.framen_foot_tab)).commitAllowingStateLoss();
                        fp = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                        fp.setMargins(0, 0, 0, (int) dpToPx(54));
                        fram_rootView.setLayoutParams(fp);
                        break;
                }
            }
            super.handleMessage(msg);
        }
    }

    private void setFromMsg(String fromMsg) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent(fromMsg);
        chatMessage.setTime(Calendar.getInstance().getTimeInMillis());
        chatMessage.setOper(2);
        chatMessage.setIsSendSuc(-1);

        infos.add(getChatInfo(chatMessage));
        mLvAdapter.notifyDataSetChanged();
        scrollMsgListTo(infos.size());
    }

    private void setToMsg(String toMsg) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent(toMsg);
        chatMessage.setTime(Calendar.getInstance().getTimeInMillis());
        chatMessage.setOper(1);
        chatMessage.setIsSendSuc(1);
//        infos.add(0, getChatInfo(chatMessage));
        infos.add(getChatInfo(chatMessage));
        mLvAdapter.notifyDataSetChanged();
        scrollMsgListTo(infos.size());
//        infos.add(getChatInfoTo(toMsg));
//        mLvAdapter.notifyDataSetChanged();
//        scrollMsgListTo(infos.size());
    }

//    private void insertToMsg(String tomsg) {
//        infos.add(0, getChatInfoTo(tomsg));
//        mLvAdapter.notifyDataSetChanged();
////        scrollMsgListTo(0);
//    }

//    private void insertFromMsg(String fromMsg) {
//        infos.add(0, getChatInfoFrom(fromMsg));
//        mLvAdapter.notifyDataSetChanged();
////        scrollMsgListTo(0);
//    }

    private void insertChatMsg(ChatMessage chatMessage) {
//        infos.add(0, getChatInfo(chatMessage));
        infos.add(getChatInfo(chatMessage));
    }

    private void scrollMsgListTo(int pos) {
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mListView.getLayoutManager();
//        linearLayoutManager.scrollToPosition(pos);
        linearLayoutManager.smoothScrollToPosition(mListView, null, pos);
    }

    private void scrollMsgListTo(int pos, boolean isInit) {
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mListView.getLayoutManager();
        if (isInit) {
            linearLayoutManager.scrollToPosition(pos);
        } else {
            linearLayoutManager.smoothScrollToPosition(mListView, null, pos);
        }
    }

    private HttpHandler httpHandler = new HttpHandler();

    class HttpHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (CChatFragment.this.isAdded() && !CChatFragment.this.isDetached() && !CChatFragment.this.isRemoving()) {
                switch (msg.what) {
                    case 0x123://获取token
                        ChatCommand.OznerChatToken chatToken = (ChatCommand.OznerChatToken) msg.obj;
                        if (chatToken.state == 0) {
                            Log.e(TAG, "token:" + chatToken.access_token);
                            token = chatToken.access_token;
                            String newstr = "access_token=" + token + "&appid=" + ChatCommand.appid + "&appsecret=" + ChatCommand.appsecret;
                            newSign = ChatCommand.Md5(newstr);
                            if (mMobile != null && mMobile != "" && token != null && token != ""
                                    && newSign != null && newSign != "") {
                                chatGetUserInfo(mMobile, token, newSign);
                            } else {
                                if (waitDialog != null) {
                                    waitDialog.dismiss();
                                }
                                Toast.makeText(getContext(), getString(R.string.Chat_Phone_Err), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e(TAG, "获取token失败，msg:" + chatToken.msg + ",code:" + chatToken.state);
                        }
                        break;
                    case 0x124://登录
                        if (waitDialog != null) {
                            waitDialog.dismiss();
                        }
                        ChatCommand.OznerChatLoginInfo chatLoginInfo = (ChatCommand.OznerChatLoginInfo) msg.obj;
                        if (BuildConfig.DEBUG) {
                            Log.e(TAG, "chatLogin:" + chatLoginInfo != null ? JSON.toJSONString(chatLoginInfo) : "null");
                        }
                        if (chatLoginInfo.state == 0) {
                            Log.e(TAG, "chatLoginInfo:" + chatLoginInfo.kfName + "," + chatLoginInfo.kfid);
                        } else {
                            if (chatLoginInfo.msg != null) {
                                Toast.makeText(getContext(), chatLoginInfo.msg, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), getString(R.string.Chat_Login_err), Toast.LENGTH_SHORT).show();
                            }
                        }
                        break;
                    case 0x125://发送消息
                        isSending = false;
                        ChatCommand.OznerChatSendReturn chatSendReturn = (ChatCommand.OznerChatSendReturn) msg.obj;
                        ChatMessage sendMsg = new ChatMessage();
                        sendMsg.setTime(Calendar.getInstance().getTimeInMillis());
                        sendMsg.setOper(1);
                        if (chatSendReturn != null && (chatSendReturn.state == 0 || chatSendReturn.state == 1007)) {
                            sendMsg.setIsSendSuc(1);
                        } else {
                            sendMsg.setIsSendSuc(0);
                        }
                        if (!isSendImg) {//发送的是普通信息
                            sendMsg.setContent(reply);
                            infos.add(infos.size(), getChatInfo(sendMsg));
                            mLvAdapter.notifyDataSetChanged();
                            scrollMsgListTo(infos.size());
                            input.setText("");
                            reply = "";
                        } else {
                            if (sendImgMsg != null) {
                                sendMsg.setContent(sendImgMsg);
                            }
                            sendImgMsg = null;
                        }
                        if (sendMsg != null && sendMsg.getContent().length() > 0) {
                            if (chatMessageHelper != null) {
                                chatMessageHelper.InsertMessage(getContext(), sendMsg);
                            }
                        }
                        break;
                    case 0x126://获取会员信息
                        ChatCommand.OznerChatUserInfo resInfo = (ChatCommand.OznerChatUserInfo) msg.obj;
                        if (resInfo != null) {
                            if (resInfo.state == 0) {
                                if (resInfo.userCount > 0) {
                                    Log.e(TAG, "userInfo:" + resInfo.UserList.get(0).customer_name + ", " + resInfo.UserList.get(0).customer_id + ", "
                                            + resInfo.UserList.get(0).mobile + ", "
                                            + resInfo.UserList.get(0).BigAreaName + ", " + resInfo.UserList.get(0).weixin_openId);
                                    mCustomId = resInfo.UserList.get(0).customer_id;
                                    chatLogin(mCustomId, deviceId, token, newSign);
                                    if (msgList != null && msgList.size() > 0) {
                                    } else {
                                        getHistoryMsg(mCustomId, curPage, PageSize, token, newSign);
                                    }
                                } else {
                                    Toast.makeText(getContext(), getString(R.string.Chat_not_focus_ozner), Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, "LoginFail:没有关注净水家公众号");
                                }
                            } else {
                                if (resInfo.msg != null) {
                                    Toast.makeText(getContext(), resInfo.msg, Toast.LENGTH_SHORT).show();
                                }
                                Log.e(TAG, "userInfo:state:" + resInfo.state + ",msg:" + resInfo.msg);
                            }
                        } else {
                            Toast.makeText(getContext(), getString(R.string.innet_wrong), Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "userInfo:fail");
                        }
                        break;
                    case 0x127://获取历史信息
                        ChatCommand.OznerHistoryResult historyResult = (ChatCommand.OznerHistoryResult) msg.obj;
                        if (historyResult.state == 0) {
                            Log.e(TAG, "historyRes:total:" + historyResult.totalCount + ", get:" + historyResult.getCount);
                            if (historyResult.getCount > 0) {
                                ChatMessage lastMsg = null;
                                if (msgList != null && msgList.size() > 0)
                                    lastMsg = msgList.get(msgList.size() - 1);
                                for (int i = 0; i < historyResult.getCount; i++) {
                                    if (lastMsg == null) {
                                        //当前条件不插入信息列表
                                        int curIndex = historyResult.getCount - i - 1;
                                        ChatMessage chatMessage = new ChatMessage();
                                        chatMessage.setContent(historyResult.historyMsgs.get(curIndex).message);
                                        chatMessage.setOper(historyResult.historyMsgs.get(curIndex).oper);
                                        if (historyResult.historyMsgs.get(curIndex).oper == 1) {//发送
                                            chatMessage.setIsSendSuc(1);//发送成功
                                        }
                                        if (String.valueOf(historyResult.historyMsgs.get(curIndex).timeetamp).length() < timeLength) {
                                            chatMessage.setTime(historyResult.historyMsgs.get(curIndex).timeetamp * 1000);
//                                            Log.e(TAG, "handleMessage: msgTime_less:" + historyResult.historyMsgs.get(curIndex).timeetamp);
                                        } else {
                                            chatMessage.setTime(historyResult.historyMsgs.get(curIndex).timeetamp);
//                                            Log.e(TAG, "handleMessage: msgTime_more:" + historyResult.historyMsgs.get(curIndex).timeetamp);
                                        }
                                        if (chatMessageHelper != null) {
                                            chatMessageHelper.InsertMessage(getContext(), chatMessage);
                                        }
                                        insertChatMsg(chatMessage);
                                        mLvAdapter.notifyDataSetChanged();
                                    }
                                }

                                scrollMsgListTo(historyResult.getCount);
                            }
                        }
                        Log.e(TAG, "historyRes:" + historyResult.state + ", " + historyResult.msg);
                        break;
                    case 0x128://上传图片
                        ChatCommand.OznerUploadResult oznerUploadResult = (ChatCommand.OznerUploadResult) msg.obj;
                        if (oznerUploadResult.state == 0 && oznerUploadResult.imgUrl != null) {
                            sendImgMsg = "<div><img width=\"260px\" src=\"" + oznerUploadResult.imgUrl + "\"/></div>";
                            isSendImg = true;
                            chatSendMsg(mCustomId, sendImgMsg, token, newSign);
                        } else {
                            if (oznerUploadResult.msg != null) {
                                Toast.makeText(getContext(), oznerUploadResult.msg, Toast.LENGTH_SHORT).show();
                            }
                            Log.e(TAG, "uploadFail:" + oznerUploadResult.state + ", " + oznerUploadResult.msg);
                        }
                        break;
                    case 0x129:
                        infos.removeLast();
                        ChatMessage chatMessage = new ChatMessage();
                        chatMessage.setContent(getString(R.string.Chat_pic_upload_Err));
                        chatMessage.setOper(1);
                        chatMessage.setIsSendSuc(0);//发送成功
                        chatMessage.setTime(Calendar.getInstance().getTimeInMillis());
                        if (chatMessageHelper != null) {
                            chatMessageHelper.InsertMessage(getContext(), chatMessage);
                        }
                        insertChatMsg(chatMessage);
                        mLvAdapter.notifyDataSetChanged();
                        scrollMsgListTo(infos.size());
                        break;
                }
            }
            super.handleMessage(msg);
        }
    }

    private String faceTransToMsg(String msg) {
        String regex = "(\\#\\[face/png/f_static_)\\d{3}(.png\\]\\#)";
        Pattern p = Pattern.compile(regex);
        String sendMsg = msg;
        Matcher m = p.matcher(sendMsg);
        String startdiv = "<div style=\"font-size:14px;font-family:微软雅黑\">";
        String enddiv = "</div>";
        while (m.find()) {
            String tempText = m.group();
            String num = tempText.substring("#[face/png/f_static_".length(), tempText.length() - ".png]#".length());

            String faceimg = "<img class=\"imgEmotion\" src=\"" + ChatCommand.CHAT_HOST + "/templates/common/images/"
                    + Integer.parseInt(num)
                    + ".gif\" >";
            sendMsg = sendMsg.replace(tempText, faceimg);
        }
        return startdiv + sendMsg + enddiv;
    }

    /**
     * bitmap转为base64
     *
     * @param bitmap
     *
     * @return
     */
    public static String bitmapToBase64(Bitmap bitmap) {

        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);

                baos.flush();
                baos.close();

                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /*
    *获取accesstoken
     */
    private void initChat() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ChatCommand.OznerChatToken result = ChatCommand.oznerChatGetToken();
                Message message = new Message();
                message.what = 0x123;
                message.obj = result;
                httpHandler.sendMessage(message);
            }
        }).start();
    }

    /*
   *咨询测试，登录
    */
    private void chatLogin(final String customid, final String deviceid, final String ac_token, final String sign) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ChatCommand.OznerLoginPars pars = new ChatCommand.OznerLoginPars();
                pars.customer_id = customid;
                pars.device_id = deviceid;
                pars.ct_id = "1";
                ChatCommand.OznerChatLoginInfo chatLoginInfo = ChatCommand.oznerLogin(pars, ac_token, sign);
                Message message = new Message();
                message.what = 0x124;
                message.obj = chatLoginInfo;
                httpHandler.sendMessage(message);
            }
        }).start();

    }

    /*
    *退出登录
     */
    private void chatLogout(final String customid, final String ac_token, final String sign) {
        String result = ChatCommand.oznerChatLogout(customid, ac_token, sign);
        Log.e(TAG, "CChatLogout:" + result);
    }

    /*
    *咨询发送消息
     */
    private void chatSendMsg(final String customeid, final String msg, final String ac_token, final String sign) {
//        loadingDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                ChatCommand.OznerSendPars pars = new ChatCommand.OznerSendPars();
                pars.device_id = deviceId;
                pars.customer_id = customeid;
                pars.msg = msg;
                ChatCommand.OznerChatSendReturn result = ChatCommand.oznerSendMsg(pars, ac_token, sign);
                Message message = new Message();
                message.what = 0x125;
                message.obj = result;
                httpHandler.sendMessage(message);
            }
        }).start();
    }

    /*
    *获取会员信息
     */
    private void chatGetUserInfo(final String mobile, final String ac_token, final String sign) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ChatCommand.OznerUserInfoPars pars = new ChatCommand.OznerUserInfoPars();
                pars.mobile = mobile;
                ChatCommand.OznerChatUserInfo result = ChatCommand.oznerLoadUserInfo(pars, ac_token, sign);
                Message message = new Message();
                message.what = 0x126;
                message.obj = result;
                httpHandler.sendMessage(message);
            }
        }).start();
    }


    /*
    *获取历史消息
     */
    private void getHistoryMsg(String customid, int page, int pagesize, String ac_token, String sign) {
        ChatCommand.OznerHistoryPars historyPars = new ChatCommand.OznerHistoryPars();
        historyPars.customer_id = customid;
        historyPars.pagesize = String.valueOf(pagesize);
        historyPars.page = String.valueOf(page);
        chatGetHistoryMsg(historyPars, ac_token, sign);
    }

    private void chatGetHistoryMsg(final ChatCommand.OznerHistoryPars pars, final String ac_token, final String sign) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ChatCommand.OznerHistoryResult historyResult = ChatCommand.oznerGetHistoryMsg(pars, ac_token, sign);
                Message message = new Message();
                message.what = 0x127;
                message.obj = historyResult;
                httpHandler.sendMessage(message);
            }
        }).start();
    }

    /*
    *上传图片
     */
    private void chatUploadImg(final String imgPath, final String ac_token, final String sign) {

//        loadingDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                ChatCommand.OznerUploadResult oznerUploadResult = ChatCommand.oznerUploadImage(imgPath, ac_token, sign);
                Message message = new Message();
                message.what = 0x128;
                message.obj = oznerUploadResult;
                httpHandler.sendMessage(message);
            }
        }).start();
    }

    /*
    *上传图片2
     */
    private void chatUploadImg2(final String imgPath, final String ac_token, final String sign, boolean isSave) {
        try {

            Bitmap bitmap = BitmapFactory.decodeFile(imgPath);
            String url = ChatCommand.CHAT_HOST + String.format(ChatCommand.CHAT_UPLOAD_IMG_URL, ac_token, sign);
            AsyncHttpClient client = new AsyncHttpClient();
            RequestParams params = new RequestParams();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // bitmap = compressImage(bitmap);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 10, baos);
            InputStream sbs = new ByteArrayInputStream(baos.toByteArray());

            params.put("photo", sbs);
            sbs.close();
            client.post(url, params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int i, Header[] headers, byte[] bytes) {
                    String result = new String(bytes);
                    ChatCommand.OznerUploadResult uploadResult = new ChatCommand.OznerUploadResult();
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        uploadResult.state = jsonObject.getInt("code");
                        uploadResult.msg = jsonObject.getString("msg");
                        uploadResult.imgUrl = jsonObject.getJSONObject("result").getString("picpath");
                    } catch (JSONException e) {
                        e.printStackTrace();
                        uploadResult.state = -2;
                        uploadResult.msg = e.getMessage();
                    }
                    Message msg = new Message();
                    msg.what = 0x128;
                    msg.obj = uploadResult;
                    httpHandler.sendMessage(msg);
                }

                @Override
                public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                    if (bytes != null) {
                        String result = new String(bytes);
                        ChatCommand.OznerUploadResult uploadResult = new ChatCommand.OznerUploadResult();
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            uploadResult.state = jsonObject.getInt("code");
                            uploadResult.msg = jsonObject.getString("msg");
                            //uploadResult.imgUrl = jsonObject.getJSONObject("result").getString("picpath");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            uploadResult.state = -2;
                            uploadResult.msg = e.getMessage();
                        }
                        Message msg = new Message();
                        msg.what = 0x129;
                        msg.obj = uploadResult;
                        httpHandler.sendMessage(msg);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Bitmap compressImage(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > 200 && options > 10) {  //循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;//每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;
    }

    public void hideFaceContainer() {
        if (chat_face_container.getVisibility() == View.VISIBLE) {
            chat_face_container.setVisibility(View.GONE);
            clickid = 0;
        }
    }

    class MyTextWatch implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() > 0) {
                send.setVisibility(View.VISIBLE);
                iv_selectPic.setVisibility(View.GONE);
            } else {
                send.setVisibility(View.GONE);
                iv_selectPic.setVisibility(View.VISIBLE);
            }
        }
    }

    private void startCamera() {
        Intent intentFromCapture = new Intent("android.media.action.IMAGE_CAPTURE");
        cameraFile = new File(getContext().getExternalFilesDir(null) + UUID.randomUUID().toString() + ".jpg");
        intentFromCapture.putExtra("output", Uri.fromFile(cameraFile));
        this.startActivityForResult(intentFromCapture, RequestCamera);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        showFootNav();
        switch (requestCode) {
            case RequestPictureChoose:
                if (data != null) {
                    String[] camertData = data.getExtras().getStringArray(PictureChooseActivity.INTENT_PICTRUES);
                    ArrayList picList = data.getStringArrayListExtra(PictureChooseActivity.INTENT_PICTRUES);
                    if (camertData != null) {
                        for (String str : camertData) {
                            Log.e(TAG, "resultCameraData:" + str);
//                            chatUploadImg(str, token, newSign);
                            chatUploadImg2(str, token, newSign, false);
                            setToMsg(String.format("<img src=\"%s\"/>", str));
                        }
                    } else if (picList != null) {
                        for (int i = 0; i < picList.size(); i++) {
                            String filePath = picList.get(i).toString();

                            // chatUploadImg(filePath, token, newSign);
                            chatUploadImg2(filePath, token, newSign, false);
                            setToMsg(String.format("<img src=\"%s\"/>", filePath));
                        }
                    } else {
                        Log.e(TAG, "select nothing");
                    }
                }
                break;
            case RequestCamera:
                if (resultCode == getActivity().RESULT_OK) {
                    Log.e(TAG, "camera_img_getAbsolutePath:" + cameraFile.getAbsolutePath());
                    chatUploadImg2(cameraFile.getAbsolutePath(), token, newSign, true);
//                    insert(cameraFile.getAbsolutePath());
                    setToMsg(String.format("<img src=\"%s\"/>", cameraFile.getAbsolutePath()));
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected float dpToPx(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

//    //内容区域切换

    @Override
    public void ShowContent(int i, String mac) {

    }

    @Override
    public void ChangeRawRecord() {
    }

    @Override
    public void CupSensorChange(String data) {
    }

    @Override
    public void RecvChatData(String data) {
        //Recv Data
        try {
//            NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
//            notificationManager.cancelAll();
//            UserDataPreference.SetUserData(getContext(), UserDataPreference.NewChatmsgCount, String.valueOf(0));
            ChatCommand.reSetMsgCount(getContext());
            JSONObject resObj = new JSONObject(data);
            String msg;
            if (resObj.has("custom_content")) {
                msg = resObj.getJSONObject("custom_content").getString("data");
            } else {
                msg = resObj.getString("data");
            }
            if (msg != null && msg != "") {
                setFromMsg(msg);
//                if (chatMessageHelper != null) {
//                    ChatMessage chatMessage = new ChatMessage();
//                    chatMessage.setContent(msg);
//                    chatMessage.setOper(2);
//                    chatMessage.setTime(Calendar.getInstance().getTimeInMillis());
//                    chatMessageHelper.InsertMessage(getContext(), chatMessage);
//                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Chat_RecvChatData:" + e.getMessage());
        }
    }

    @Override
    public void DeviceDataChange() {

    }

    @Override
    public void ContentChange(String mac, String state) {
    }
}
