package com.aisl.kickboard;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class HistoryFragment extends Fragment {
    String message = "";
    String login_id = "";

    // *********************************************************************************************

    public HistoryFragment() {
        // Required empty public constructor
    }

    public static HistoryFragment newInstance() {
        HistoryFragment fragment = new HistoryFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history,container,false);

        // 킥보드 출발시간 및 도착시간
        message = this.getArguments().getString("kick_time");
        login_id = this.getArguments().getString("user_login");

        TextView tv_cv_riding_time = view.findViewById(R.id.tv_cv_riding_time); // 킥보드 출발시간
        TextView tv_cv_parking_time = view.findViewById(R.id.tv_cv_parking_time);   // 킥보드 도착시간
        TextView tv_cv_riding_price = view.findViewById(R.id.tv_cv_riding_price);   // 킥보드 출발비용
        TextView tv_cv_parking_price = view.findViewById(R.id.tv_cv_parking_price); // 킥보드 도착비용

        if(message!=null){
            String[] kick_time = message.split(",");    // 킥보드 시간 데이터 파싱
            double price = Double.parseDouble(message.split(",")[2])*0.007;
            tv_cv_riding_time.setText(kick_time[0]);    // 킥보드 출발 시간 띄우기
            tv_cv_parking_time.setText(kick_time[1]);   // 킥보드 도착 시간 띄우기
            tv_cv_riding_price.setText("$0.5 (For Rent)");  // 킥보드 출발 비용 띄우기
            tv_cv_parking_price.setText("$" + Double.toString(price));  // 킥보드 사용 비용 띄우기
        } else {
            tv_cv_riding_time.setText("End Time");    // 킥보드 출발 시간 띄우기
            tv_cv_parking_time.setText("Start Time");   // 킥보드 도착 시간 띄우기
            tv_cv_riding_price.setText("Rent Price");  // 킥보드 출발 비용 띄우기
            tv_cv_parking_price.setText("Total Price");  // 킥보드 사용 비용 띄우기
        }

        // 면허 시험 시작 버튼
        ImageButton btn_license = view.findViewById(R.id.btn_license);
        btn_license.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(login_id!=null){
                    Intent intent = new Intent(getActivity(),LicenseActivity.class);
                    intent.putExtra("user_login", login_id);
                    startActivity(intent);
                    FragmentManager manager = getActivity().getSupportFragmentManager();
                    manager.beginTransaction().remove(HistoryFragment.this).commit();
                    manager.popBackStack();
                } else {
                    Toast t = Toast.makeText(getActivity(), "Login Please", Toast.LENGTH_SHORT);
                    t.show();
                }
            }
        });
        // Inflate the layout for this fragment
        return view;
    }
}

