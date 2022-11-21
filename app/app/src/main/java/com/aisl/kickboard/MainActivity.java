package com.aisl.kickboard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import java.util.Date;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity{
    LinearLayout home_ly;
    BottomNavigationView bottomNavigationView;
    private HomeFragment homeFragment;
    private HistoryFragment historyFragment;
    private UserFragment userFragment;

    private String timeData;
    private String userData;
    private String loginData;
    private String parkingData;
    private String finalData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        // 킥보드 주행 시작, 종료 시간 가져오기
        timeData = intent.getStringExtra("kick_time");
        // 사용자 정보 가져오기
        userData = intent.getStringExtra("user_info");
        // 사용자 아이디 가져오기
        loginData = intent.getStringExtra("user_login");
        // 주차장 정보 가져오기
        parkingData = intent.getStringExtra("parking_gps");

        init(); //객체 정의
        SettingListener(); //리스너 등록


        if(timeData!=null){
            Log.d("DataCheck", timeData);
        }
        if(userData!=null){
            Log.d("DataCheck", userData);
        }
        if(loginData!=null){
            Log.d("DataCheck", loginData);
        }

        if(userData!=null && loginData!=null && timeData!=null){
            // [ID] [이름 나이 성별 면허증취득일] [운행시간] + 7s
            finalData = loginData + " " + userData + " " + timeData.split(",")[2] + "0 0 0 0 0 0 0";  // 뒤에 각종 정보 받아와야함 (HomeFragment)
            Log.d("MainActivity", finalData);
        }

        //맨 처음 시작할 탭 설정
        bottomNavigationView.setSelectedItemId(R.id.menu_home);
    }

    private void init() {
        home_ly = findViewById(R.id.home_ly);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
    }

    private void SettingListener() {
        //선택 리스너 등록
        bottomNavigationView.setOnNavigationItemSelectedListener(new TabSelectedListener());
    }

    class TabSelectedListener implements BottomNavigationView.OnNavigationItemSelectedListener{
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.menu_home: {
                    if(homeFragment==null){
                        // HomeFragment 초기 생성
                        homeFragment = new HomeFragment();
                        getSupportFragmentManager().beginTransaction().add(R.id.home_ly, homeFragment).commit();
                    } else {
                        // 이미 HomeFragment 가 존재할 경우
                        getSupportFragmentManager().beginTransaction().show(homeFragment).commit();
                    }
                    // 킥보드 이용자 로그인 데이터 전송
                    Bundle bundle = new Bundle();
                    bundle.putString("user_login", loginData);
                    bundle.putString("user_info", userData);

                    homeFragment.setArguments(bundle);
                    getSupportFragmentManager().beginTransaction().show(homeFragment).commit();

                    // HomeFragment 이외의 Fragment 는 가려주기
                    if(historyFragment!=null){
                        getSupportFragmentManager().beginTransaction().hide(historyFragment).commit();
                    }
                    if(userFragment!=null){
                        getSupportFragmentManager().beginTransaction().hide(userFragment).commit();
                    }
                    return true;
                }
                case R.id.menu_history: {
                    if(historyFragment==null){
                        // HistoryFragment 초기 생성
                        historyFragment = new HistoryFragment();
                        getSupportFragmentManager().beginTransaction().add(R.id.home_ly, historyFragment).commit();
                    } else {
                        // HistoryFragment 가 존재할 경우
                        getSupportFragmentManager().beginTransaction().show(historyFragment).commit();
                    }
                    // 킥보드 시간 데이터 및 사용자 전송
                    Bundle bundle = new Bundle();
                    bundle.putString("user_login", loginData);
                    bundle.putString("kick_time", timeData);
                    historyFragment.setArguments(bundle);
                    getSupportFragmentManager().beginTransaction().show(historyFragment).commit();

                    // HistoryFragment 이외의 Fragment 는 가려주기
                    if(homeFragment!=null){
                        getSupportFragmentManager().beginTransaction().hide(homeFragment).commit();
                    }
                    if(userFragment!=null){
                        getSupportFragmentManager().beginTransaction().hide(userFragment).commit();
                    }
                    return true;
                }
                case R.id.menu_user: {
                    if(userFragment==null){
                        // UserFragment 초기 생성
                        userFragment = new UserFragment();
                        getSupportFragmentManager().beginTransaction().add(R.id.home_ly, userFragment).commit();
                    } else {
                        // UserFragment 가 존재할 경우
                        getSupportFragmentManager().beginTransaction().show(userFragment).commit();
                    }
                    // 킥보드 이용자 로그인 데이터 및 이용자 데이터 전송
                    Bundle bundle = new Bundle();
                    bundle.putString("user_login", loginData);
                    bundle.putString("final_data", finalData);
                    userFragment.setArguments(bundle);
                    getSupportFragmentManager().beginTransaction().show(userFragment).commit();

                    // UserFragment 이외의 Fragment 는 가려주기
                    if(homeFragment!=null){
                        getSupportFragmentManager().beginTransaction().hide(homeFragment).commit();
                    }
                    if(historyFragment!=null){
                        getSupportFragmentManager().beginTransaction().hide(historyFragment).commit();
                    }
                    return true;
                }
            }
            return false;
        }
    }
}