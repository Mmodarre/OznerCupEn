package com.ozner.qianye.Main;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ozner.qianye.Command.FootFragmentListener;
import com.ozner.qianye.Command.OznerPreference;
import com.ozner.qianye.Command.UserDataPreference;
import com.ozner.qianye.Device.AddDeviceActivity;
import com.ozner.qianye.Device.OznerApplication;
import com.ozner.qianye.MainActivity;
import com.ozner.qianye.R;
import com.ozner.qianye.control.CProessbarView;
import com.ozner.qianye.control.OnCProessbarValueChangeListener;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MainConFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MainConFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainConFragment extends Fragment implements FootFragmentListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private TextView textView;
    private Button loginout;
    private LinearLayout ozner_ll_log;
    private FootFragmentListener mCDeviceChangeFragmentListenr;
    private CProessbarView cProessbarView;
    private Toolbar toolbar;
    private TextView addDetails;
    private String userid;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MainConFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MainConFragment newInstance(String param1, String param2) {
        MainConFragment fragment = new MainConFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public MainConFragment() {
        // Required empty public constructor
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
        View view = inflater.inflate(R.layout.fragment_main_con, container, false);
        // Inflate the layout for this fragment
        userid = UserDataPreference.GetUserData(getContext(), UserDataPreference.UserId, null);
        InitView(view);
        return view;
    }

    public void InitView(View view) {
        OznerApplication.changeTextFont((ViewGroup) view);
        toolbar = (Toolbar) view.findViewById(R.id.main_toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).myOverlayDrawer.toggleMenu();
            }
        });
        addDetails = (TextView) view.findViewById(R.id.main_add_detail);
        addDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (OznerPreference.IsLogin(getActivity())) {
                    Intent intent = new Intent(getActivity(), AddDeviceActivity.class);
                    getActivity().startActivity(intent);
                } else {
                    Toast.makeText(getContext(), getString(R.string.ShouldLogin), Toast.LENGTH_SHORT).show();
                }
            }
        });
        cProessbarView = (CProessbarView) view.findViewById(R.id.my_cproessbarview);
        cProessbarView.setOnCProessbarValueChangeListener(new OnCProessbarValueChangeListener() {
            @Override
            public void ValueChange(int persent) {
            }
        });
        cProessbarView.updateValue(0);
    }
    // TODO: Rename method, update argument and hook method into UI event
//    public void onButtonPressed(Uri uri) {
//        if ( != null) {
//            ChangeRawRecord.onFragmentInteraction(uri);
//        }
//    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        try {
            mCDeviceChangeFragmentListenr = (FootFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCDeviceChangeFragmentListenr = null;
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

    @Override
    public void ShowContent(int i, String mac) {

    }

    /*
    * 传感器数据发生变化事件
    * */
    @Override
    public void ChangeRawRecord() {
    }

    /*
    * 杯子数据发生变化
    * */
    @Override
    public void CupSensorChange(String address) {
//    {
//        String showtext;
//        OznerDevice oznerDevice= OznerDeviceManager.Instance().getDevice(address);
//        if(oznerDevice!=null)
//        {
//            switch (oznerDevice.Type())
//            {
//                case "CP001":
//                    Cup cup=(Cup)oznerDevice;
//                    showtext =cup.getName()+":"+cup.Sensor().toString();
//                    break;
//                case "SC001": {
//                    Tap tap = (Tap) oznerDevice;
//                    showtext = tap.getName() + ":" + tap.Sensor().toString();
//                }
//                break;
//                default:
//                    return;
//            }
//            Context context=getContext();
//            if(context!=null) {
//                TextView textView = new TextView(context);
//                textView.setText(showtext);
//                ozner_ll_log.addView(textView);
//            }
//        }
    }

    /*
    * 设备列表发生变化
    * */
    @Override
    public void DeviceDataChange() {
    }


    @Override
    public void onResume() {
        super.onResume();
        InitView(getView());
    }

    @Override
    public void ContentChange(String mac, String state) {

    }

    @Override
    public void RecvChatData(String data) {

    }
}
