package com.aisl.kickboard;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.util.FusedLocationSource;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class HomeFragment extends Fragment implements OnMapReadyCallback {
    private NonLeakHandler handler = new NonLeakHandler(this);  // Handler 생성

    // Memory Leak 방지를 위한 Handler
    private static final class NonLeakHandler extends Handler {
        private final WeakReference<HomeFragment> ref;
        public NonLeakHandler(HomeFragment act) {
            ref = new WeakReference<>(act);
        }
        @Override
        public void handleMessage(Message msg) {
            HomeFragment act = ref.get();
            if (act != null) {
                // do work
            }
        }
    }

    private static NaverMap naverMap;  // 네이버 지도 객체 생성
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;   // FusedLocationSource 권한 요청 코드
    private FusedLocationSource locationSource; // FusedLocationSource 객체 생성
    // 위치 정보 액세스 권한 요청
    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,   // GPS와 네트워크를 이용하여 단말기 위치 식별
            Manifest.permission.ACCESS_COARSE_LOCATION // 네트워크를 이용하여 단말기 위치 식별
    };
    // GPS 정보
    static String gpsInfo = "";

    // 킥보드 마커
    private static Marker KickBoardMarker1 = new Marker(); // 킥보드ID:MFBE29
    private static Marker KickBoardMarker2 = new Marker(); // 킥보드ID:ZG62V6
    private static Marker ParkingMarker = new Marker(); // 킥보드 주차

    // Map 상태
    boolean map_status = true;

    // 지도 객체 변수
    private MapView mapView;

    //
    String login_id = "";
    String user_data = "";

    // *********************************************************************************************

    public HomeFragment() {
    }

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // 사용자 정보
        login_id = this.getArguments().getString("user_login");
        user_data = this.getArguments().getString("user_info");


        // Inflate the layout for this fragment
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_home,
                container, false);

        mapView = (MapView) rootView.findViewById(R.id.navermap);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);


        // 운행 시작 버튼
        Button btn_map_start = rootView.findViewById(R.id.btn_map_start);
        btn_map_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 현재 날짜 및 시간
                long now = System.currentTimeMillis();
                Date date = new Date(now);
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                String getTime = dateFormat.format(date);

                Intent intent = new Intent(getActivity(),MapActivity.class);    // Intent 이동
                intent.putExtra("gps_data", gpsInfo);   // 출발 시 킥보드 GPS
                intent.putExtra("time_data", getTime);  // 출발 시간
                intent.putExtra("user_login", login_id);
                intent.putExtra("user_info", user_data);
                map_status = false; // HomeFragment Thread 중단
                startActivity(intent);

                // ********* 고민중 *************
                FragmentManager manager = getActivity().getSupportFragmentManager();
                manager.beginTransaction().remove(HomeFragment.this).commit();
                manager.popBackStack();
                // ******************************

            }
        });

        // 킥보드 GPS 데이터 불러옴 (10초 주기)
        new Thread(new Runnable() {
            @Override
            public void run() {
                do {
                    try{
                        GpsKickBoard();
                        Log.d("HomeFragment", "GpsKickBoard() Success");
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        Log.d("HomeFragment", "GpsKickBoard() Fail");
                        e.printStackTrace();
                    }
                } while(map_status != false);
            }
        }).start();

        return rootView;
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;
        naverMap.setLocationSource(locationSource); // 현재 위치 표시

        UiSettings uiSettings = naverMap.getUiSettings();
        uiSettings.setZoomControlEnabled(true);
        uiSettings.setLocationButtonEnabled(true);
        requestPermissions(PERMISSIONS, LOCATION_PERMISSION_REQUEST_CODE);
        naverMap.addOnLocationChangeListener(location ->
                Log.d("HomeFragment", "My location: " + location.getLatitude() + ", " + location.getLongitude()));  // 현재 위치
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated()) { // 권한 거부
                naverMap.setLocationTrackingMode(LocationTrackingMode.None);
                return;
            } else {    // 권한 허용
                naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);    // 현재 위치 뜸
            }
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);;
        }
    }

    public void GpsKickBoard() throws InterruptedException {
        // OkHttp 클라이언트 객체 생성
        OkHttpClient client = new OkHttpClient().newBuilder().build();

        // GET 요청 객체 생성
        Request.Builder builder = new Request.Builder().url("http://203.250.148.120:20519/Mobius/kick_off/data/gps/la").get();
        builder.addHeader("Accept", "application/json").addHeader("X-M2M-RI", "12345").addHeader("X-M2M-Origin", "SOrigin");
        Request request = builder.build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("HomeFragment", "Response: Http Connection is failed");
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        ResponseBody body = response.body();
                        if (body != null) {
                            // Mobius 데이터로부터 킥보드 gps 정보 추출
                            JSONObject jsonObject = new JSONObject(response.body().string());
                            JSONObject jsonObject1 = (JSONObject) jsonObject.get("m2m:cin");
                            gpsInfo = jsonObject1.getString("con");
                            Log.d("HomeFragment", "Response: " + gpsInfo);
                            // 맵에 킥보드 마커 업데이트
                            String[] KickInfo = gpsInfo.toString().split(" ");
                            String KickID = KickInfo[0];
                            double KickLatitude = Double.parseDouble(KickInfo[1]);
                            double KickLongitude = Double.parseDouble(KickInfo[2]);


                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            showKickBoardLocation(KickID, KickLatitude, KickLongitude);
                                        }
                                    });
                                }
                            }).start();
                        }
                    }
                    else {
                        Log.d("HomeFragment", "Not Response");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static void showKickBoardLocation(String ID, double latitude, double longitude) {
        Log.d("HomeFragment", "ID: " + ID + " latitude: " + latitude + " longitude: " + longitude);  // 현재 킥보드 위치
        OverlayImage image = OverlayImage.fromResource(R.drawable.markerkickboard);
        OverlayImage parkingImage = OverlayImage.fromResource(R.drawable.markerkickboard);
        if (ID.equals("MFBE29")) {  // 킥보드 ID가 MFBE29 인 경우
            Log.d("HomeFragment", "MFBE29 Marker");
            KickBoardMarker1.setMap(null);  // 기존 마커 삭제
            KickBoardMarker1.setPosition(new LatLng(latitude, longitude));  // 현재 킥보드 위치 설정
            KickBoardMarker1.setIcon(image);    // 킥보드 마커 이미지
            KickBoardMarker1.setWidth(130);
            KickBoardMarker1.setHeight(130);
            KickBoardMarker1.setCaptionText("MFBE29");
            KickBoardMarker1.setMap(naverMap);  // 지도에 마커 띄움
        } else if (ID.equals("ZG62V6")) {
            Log.d("HomeFragment", "ZG62V6 Marker");    // 킥보드 ID가 ZG62V6 인 경우
            KickBoardMarker2.setMap(null);  // 기존 마커 삭제
            KickBoardMarker2.setPosition(new LatLng(latitude, longitude));  // 현재 킥보드 위치 설정
            KickBoardMarker2.setIcon(image);
            KickBoardMarker2.setWidth(130);
            KickBoardMarker2.setHeight(130);
            KickBoardMarker2.setCaptionText("ZG62V6");
            KickBoardMarker2.setMap(naverMap);
        } else if (ID.equals("Parking")) {
            Log.d("HomeFragment", "Parking Marker");    // 킥보드 ID가 ZG62V6 인 경우
            ParkingMarker.setMap(null);  // 기존 마커 삭제
            ParkingMarker.setPosition(new LatLng(latitude, longitude));  // 현재 킥보드 위치 설정
            ParkingMarker.setIcon(parkingImage);
            ParkingMarker.setWidth(200);
            ParkingMarker.setHeight(200);
            ParkingMarker.setCaptionText("Parking");
            Log.d("Parking", "success");
            ParkingMarker.setMap(naverMap);
        }
    }


}