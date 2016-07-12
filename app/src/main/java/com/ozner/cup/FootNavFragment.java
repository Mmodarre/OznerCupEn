package com.ozner.cup;


import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ozner.cup.Command.FootFragmentListener;
import com.ozner.cup.Command.PageState;
import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.Device.OznerApplication;
import com.ozner.cup.slideleft.BaseFragment;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FootNavFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FootNavFragment#newInstance} factory method to
 * create an instance of this fragment.
 * Create by C-sir@hotmail.com
 */
public class FootNavFragment extends BaseFragment implements FootFragmentListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private LinearLayout llsb, llshop, llmsg, llmyself;
    private ImageView ivsb, ivshop, ivmsg, ivmyself, ivmyselfstate;
    private TextView tvsb, tvshop, tvmsg, tvmyself, tvmsgcount;
    private FootFragmentListener mListener;
    private int tab_index = 0;
    private int layoutId;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FootNavFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FootNavFragment newInstance(String param1, String param2) {
        FootNavFragment fragment = new FootNavFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public static FootNavFragment newInstance(Bundle bundle) {
        FootNavFragment fragment = new FootNavFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    public FootNavFragment() {
        // Required empty public constructor
    }

    public void Init(View view) {
        llsb = (LinearLayout) view.findViewById(R.id.foot_view_sb);
        llshop = (LinearLayout) view.findViewById(R.id.foot_view_shop);
        llmsg = (LinearLayout) view.findViewById(R.id.foot_view_message);
        llmyself = (LinearLayout) view.findViewById(R.id.foot_view_my);

        ivsb = (ImageView) view.findViewById(R.id.foot_iv_sb);
        ivshop = (ImageView) view.findViewById(R.id.foot_iv_shop);
        ivmsg = (ImageView) view.findViewById(R.id.foot_iv_msg);
        ivmyself = (ImageView) view.findViewById(R.id.foot_iv_myself);
        ivmyselfstate = (ImageView) view.findViewById(R.id.foot_iv_myself_state);

        tvsb = (TextView) view.findViewById(R.id.foot_tv_sb);
        tvshop = (TextView) view.findViewById(R.id.foot_tv_shop);
        tvmsg = (TextView) view.findViewById(R.id.foot_tv_msg);
        tvmyself = (TextView) view.findViewById(R.id.foot_tv_myself);
        tvmsgcount = (TextView) view.findViewById(R.id.foot_tv_msgcount);

        MyClickListener myClickListener = new MyClickListener();
        llsb.setOnClickListener(myClickListener);
        llshop.setOnClickListener(myClickListener);
        llmsg.setOnClickListener(myClickListener);
        llmyself.setOnClickListener(myClickListener);

        OznerApplication.setControlTextFace(tvsb);
        OznerApplication.setControlTextFace(tvshop);
        OznerApplication.setControlTextFace(tvmsg);
        OznerApplication.setControlTextFace(tvmyself);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_foot_nav, container, false);
        Init(view);
        ShowTabIndex(R.id.foot_view_sb);
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            //    mListener.onFragmentInteraction(uri);
        }

    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    private boolean isFragmentStatOk = true;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        HideTabIndex();
        ShowTabIndex(PageState.WODESHEBEI);
    }

    public class MyClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
//            Fragment fragment=null;
            if (isFragmentStatOk) {
                switch (v.getId()) {
                    case R.id.foot_view_sb:
                        if (tab_index != PageState.WODESHEBEI) {
                            HideTabIndex();
                            ShowTabIndex(PageState.WODESHEBEI);
                            //要执行的操作
                            mListener.ShowContent(PageState.WODESHEBEI, null);
                        }
//                    fragment=new WaterCupFragment();
                        break;
                    case R.id.foot_view_shop:
                        if (tab_index != PageState.SHANGCHEGYEMIAN) {
                            HideTabIndex();
                            ShowTabIndex(PageState.SHANGCHEGYEMIAN);
                            //要执行的操作
                            mListener.ShowContent(PageState.SHANGCHEGYEMIAN, null);
                        }
//                    fragment=new MainConFragment();
                        break;
                    case R.id.foot_view_message:
                        if (tab_index != PageState.ZIXUNYEMIAN) {
                            HideTabIndex();
                            ShowTabIndex(PageState.ZIXUNYEMIAN);
                            //要执行的操作
                            mListener.ShowContent(PageState.ZIXUNYEMIAN, null);
                        }
//                    fragment=new MainConFragment();
                        break;
                    case R.id.foot_view_my:
                        if (tab_index != PageState.MYPAGE) {
                            HideTabIndex();
                            ShowTabIndex(PageState.MYPAGE);
                            //要执行的操作
                            mListener.ShowContent(PageState.MYPAGE, null);
                        }
//                    fragment=new MyFragment();
                        break;
                }
            }
//            if (null!=fragment) {
//                getFragmentManager().beginTransaction().replace(layoutId, fragment).commit();
//            }
        }

    }

    public void ShowTabIndex(int i) {
        switch (i) {
            case PageState.WODESHEBEI:
                ivsb.setImageResource(R.drawable.tab_sb_on);
                tvsb.setTextColor(ContextCompat.getColor(getContext(), R.color.checked));
                break;
            case PageState.SHANGCHEGYEMIAN:
                ivshop.setImageResource(R.drawable.tab_shop_on);
                tvshop.setTextColor(ContextCompat.getColor(getContext(), R.color.checked));
                break;
            case PageState.ZIXUNYEMIAN:
                UserDataPreference.SetUserData(getContext(), UserDataPreference.NewChatmsgCount, "0");
                SetMessageCount(0);
                ivmsg.setImageResource(R.drawable.tab_msg_on);
                tvmsg.setTextColor(ContextCompat.getColor(getContext(), R.color.checked));
                break;
            case PageState.MYPAGE:
                ivmyself.setImageResource(R.drawable.tab_my_on);
                tvmyself.setTextColor(ContextCompat.getColor(getContext(), R.color.checked));
                break;
        }
        tab_index = i;
        isFragmentStatOk = false;
        new Handler().postDelayed(new Runnable() {
            public void run() {
                //execute the task
                isFragmentStatOk = true;
            }
        }, 300);

    }

    public void HideTabIndex() {
        switch (tab_index) {
            case PageState.WODESHEBEI:
                ivsb.setImageResource(R.drawable.tab_sb);
                tvsb.setTextColor(ContextCompat.getColor(getContext(), R.color.font_gray));
                break;
            case PageState.SHANGCHEGYEMIAN:
                ivshop.setImageResource(R.drawable.tab_shop);
                tvshop.setTextColor(ContextCompat.getColor(getContext(), R.color.font_gray));
                break;
            case PageState.ZIXUNYEMIAN:
                ivmsg.setImageResource(R.drawable.tab_msg);
                tvmsg.setTextColor(ContextCompat.getColor(getContext(), R.color.font_gray));
                break;
            case PageState.MYPAGE:
                ivmyself.setImageResource(R.drawable.tab_my);
                tvmyself.setTextColor(ContextCompat.getColor(getContext(), R.color.font_gray));
                break;
        }
    }


    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        try {
            mListener = (FootFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void ShowContent(int i, String mac) {
        HideTabIndex();
        ShowTabIndex(i);
    }

    public void Show(int i) {
        HideTabIndex();
        ShowTabIndex(i);
    }

    @Override
    public void ChangeRawRecord() {

    }

    @Override
    public void CupSensorChange(String address) {

    }

    @Override
    public void DeviceDataChange() {

    }

    @Override
    public void ContentChange(String mac, String state) {

    }

    @Override
    public void RecvChatData(String data) {

    }

    public void SetMessageCount(int i) {
        if(FootNavFragment.this.isAdded()) {
            if (i > 0) {
                tvmsgcount.setText(String.valueOf(i));
                tvmsgcount.setVisibility(View.VISIBLE);
            } else {
                tvmsgcount.setText(String.valueOf(0));
                tvmsgcount.setVisibility(View.GONE);
            }
        }
    }

    public void SetCenterNotify(int i) {
        if (i > 0) {
            ivmyselfstate.setVisibility(View.VISIBLE);
        } else {
            ivmyselfstate.setVisibility(View.GONE);
        }
    }
}
