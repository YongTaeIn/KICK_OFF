package com.aisl.kickboard;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class UserFragment extends Fragment {
    String message = "";
    String final_data = "";

    // *********************************************************************************************

    public UserFragment() {
        // Required empty public constructor
    }

    public static UserFragment newInstance() {
        UserFragment fragment = new UserFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user,container,false);

        // 사용자 정보
        message = this.getArguments().getString("user_login");
        final_data = this.getArguments().getString("final_data");

        if(message!=null){
            TextView tv_user_id = view.findViewById(R.id.tv_user_id);
            tv_user_id.setText(message);
        }

        // 로그인 버튼
        Button btn_login = view.findViewById(R.id.btn_login);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(),LoginActivity.class);    // Intent 이동
                startActivity(intent);
            }
        });

        // 회원가입 버튼
        Button btn_register = view.findViewById(R.id.btn_register);
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(),RegisterActivity.class);    // Intent 이동
                startActivity(intent);
            }
        });

        // 로그아웃 버튼
        Button btn_logout = view.findViewById(R.id.btn_logout);
        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(),EndActivity.class);    // Intent 이동

                if(final_data!=null){
                    intent.putExtra("final_data", final_data);
                    Log.d("EndActivity", final_data);
                }
                startActivity(intent);
            }
        });
        // Inflate the layout for this fragment
        return view;
    }
}