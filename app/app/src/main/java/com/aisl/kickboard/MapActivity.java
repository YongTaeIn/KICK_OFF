package com.aisl.kickboard;

import static java.lang.Math.abs;
import static java.lang.Thread.sleep;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraAnimation;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.CircleOverlay;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.overlay.PathOverlay;
import com.naver.maps.map.util.FusedLocationSource;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static NaverMap naverMap;  // 네이버 지도 객체 생성
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;   // FusedLocationSource 권한 요청 코드
    private FusedLocationSource locationSource; // FusedLocationSource 객체 생성
    // 위치 정보 액세스 권한 요청
    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,   // GPS와 네트워크를 이용하여 단말기 위치 식별
            Manifest.permission.ACCESS_COARSE_LOCATION // 네트워크를 이용하여 단말기 위치 식별
    };
    // 출발 시 GPS 정보
    static String gpsInfo = "";
    private String gps_kickID = "";

    // (아마도 출발 시) 킥보드 Gyro 정보
    static String gyroInfo = "";

    // 킥보드 gps 정보
    KickGps gps = new KickGps();
    // 킥보드 gyro 정보
    KickGyro gyro = new KickGyro();

    // Pothole 정보
    private String potholeInfo = "";
    private String[] potholeInfo1;
    private String potholeGps;

    // 방지턱 정보
    private String bumpInfo = "";
    private String[] bumpInfo1;
    private String bumpGps;
    ArrayList<Double>[] bumpGpsData;

    // 방지턱 정보
    private String zoneInfo = "";
    private String[] zoneInfo1;
    private String zoneGps;
    ArrayList<Double>[] zoneGpsData;

    // 주차장 정보
    private String parkingInfo = "";
    private String[] parkingInfo1;
    private String parkingGps;
    ArrayList<Double>[] parkingGpsData;

    // 주차 확인 정보
    private String[] parkingCheck;

    // Map 상태
    boolean map_status = true;
    // 킥보드 경로
    private PathOverlay path = new PathOverlay();
    private List<LatLng> coords = new ArrayList<>();

    // 벌점
    int stop_point = 0;
    int bump_point = 0;
    int zone_point = 0;

    // 현재 날짜 및 시간
    long getStartTime;
    String startTime;
    String login_id;
    String user_data;


    // *********************************************************************************************

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        
        // 출발 시 킥보드 GPS 및 시간 데이터 가져오기
        Intent intent = getIntent();
        String gpsData = intent.getStringExtra("gps_data");
        startTime = intent.getStringExtra("time_data");
        login_id = intent.getStringExtra("user_login");
        user_data = intent.getStringExtra("user_info");


        // 출발 시간
        getStartTime = System.currentTimeMillis();

        // 네이버 지도 불러움
        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment) fm.findFragmentById(R.id.map_navermap);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.map_navermap, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);
        // 사용자 gps 권한 설정
        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);


        // 운행 종료 버튼
        Button btn_map_end = findViewById(R.id.btn_map_end);
        btn_map_end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postParking();
                getParking();
            }
        });

        // 배너 깜빡임
        TextView map_banner = findViewById(R.id.txt_map_banner);
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.blink_banner);
        map_banner.startAnimation(anim);

        // 킥보드 gps 경로 추적
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 킥보드 gps 초기값
                gps.KickGpsInfo = gpsData.split(" ");
                gps_kickID = gps.KickGpsInfo[0];
                gps.KickLatitude = Double.parseDouble(gps.KickGpsInfo[1]);
                gps.KickLongitude = Double.parseDouble(gps.KickGpsInfo[2]);

                Log.d("MapActivity", "KickBoard GPS connection " + gps_kickID + "," + gps.KickLatitude + " " + gps.KickLatitude);
                // 킥보드 경로 초기값 설정
                Collections.addAll(coords,
                        new LatLng(gps.KickLatitude, gps.KickLongitude),
                        new LatLng(gps.KickLatitude+0.000001, gps.KickLongitude+0.000001)
                );
                path.setCoords(coords);

                // 킥보드 gps 데이터 불러옴 (1초 주기)
                do {
                    try{
                        GpsKickBoard();
                        sleep(1000);
                    } catch (InterruptedException e) {
                        Log.d("MapActivity", "GpsKickBoard() Fail");
                        e.printStackTrace();
                    }
                } while(map_status!=false);
            }
        }).start();


        // 킥보드 - 포트홀 띄우기
        new Thread(new Runnable() {
            @Override
            public void run() {
                getPotholeData();
                do {
                    try{
                        // 포트홀 정보 업데이트 (10초주기)
                        sleep(10000);
                        GetNewPothole();
                    } catch (InterruptedException e) {
                        Log.d("MapActivity", "GetNewPothole() Fail");
                        e.printStackTrace();
                    }
                } while(map_status!=false);
            }
        }).start();


        // 킥보드 - 주차장 띄우기
        new Thread(new Runnable() {
            @Override
            public void run() {
                getParkingSpaceData();
            }
        }).start();


        // 킥보드 - 스쿨존 불러오고 알림 (1초 주기)
        new Thread(new Runnable() {
            @Override
            public void run() {
                getZoneData();
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                for(int i=0;i<zoneGpsData.length;i++){
                    Log.d("ZoneCheck", "*************************");
                    Log.d("ZoneCheck", String.valueOf(zoneGpsData[i]));
                    Log.d("ZoneCheck", "*************************");
                }
                do {
                    try{
                        sleep(1000);
                        // 스쿨존과의 거리 확인 (1초 주기)
                        ZoneDistance(gps.KickGpsID, gps.KickLatitude, gps.KickLongitude, gps.KickSpeed);
                        Log.d("ZoneCheck", gps.KickGpsID);
                        Log.d("ZoneCheck", String.valueOf(gps.KickLatitude));
                        Log.d("ZoneCheck", String.valueOf(gps.KickLongitude));
                        Log.d("ZoneCheck", String.valueOf(gps.KickSpeed));

                    } catch (InterruptedException e) {
                        Log.d("MapActivity", "BufferDistance() Fail");
                        e.printStackTrace();
                    }
                } while(map_status!=false);
            }
        }).start();


        // 킥보드 - 과속방지턱 불러오고 알림 (1초 주기)
        new Thread(new Runnable() {
            @Override
            public void run() {
                getBumpData();
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                for(int i=0;i<bumpGpsData.length;i++){
                    Log.d("BumpCheck", "*************************");
                    Log.d("BumpCheck", String.valueOf(bumpGpsData[i]));
                    Log.d("BumpCheck", "*************************");
                }
                do {
                    try{
                        sleep(1000);
                        // 과속방지턱과의 거리 확인 (1초 주기)
                        BufferDistance(gps.KickGpsID, gps.KickLatitude, gps.KickLongitude, gps.KickSpeed);
                        Log.d("BumpCheck", gps.KickGpsID);
                        Log.d("BumpCheck", String.valueOf(gps.KickLatitude));
                        Log.d("BumpCheck", String.valueOf(gps.KickLongitude));
                        Log.d("BumpCheck", String.valueOf(gps.KickSpeed));

                    } catch (InterruptedException e) {
                        Log.d("MapActivity", "GetNewPothole() Fail");
                        e.printStackTrace();
                    }
                } while(map_status!=false);
            }
        }).start();


        // 킥보드 - Gyro 데이터 불러오고 급제동 확인 (1초 주기)
        new Thread(new Runnable() {
            @Override
            public void run() {
                do {
                    try{
                        GyroKickBoard();
                        sleep(1000);
                    } catch (InterruptedException e) {
                        Log.d("MapActivity", "GyroKickBoard() Fail");
                        e.printStackTrace();
                    }
                } while(map_status!=false);
            }
        }).start();


    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;
        naverMap.setLocationSource(locationSource); // 현재 위치 표시
        UiSettings uiSettings = naverMap.getUiSettings();
        uiSettings.setZoomControlEnabled(true);
        uiSettings.setLocationButtonEnabled(true);
        ActivityCompat.requestPermissions(this, PERMISSIONS, LOCATION_PERMISSION_REQUEST_CODE);
        naverMap.addOnLocationChangeListener(location ->
                Log.d("MapActivity", location.getLatitude() + ", " + location.getLongitude()));  // 현재 위치
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
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    //  초기 설정 ***********************************************************************************

    // 킥보드 GPS 데이터 불러오는 함수
    public void GpsKickBoard() {
        // OkHttp 클라이언트 객체 생성
        OkHttpClient client = new OkHttpClient().newBuilder().build();

        // GET 요청 객체 생성
        Request.Builder builder = new Request.Builder().url("http://203.250.148.120:20519/Mobius/kick_off/data/gps/la").get();
        builder.addHeader("Accept", "application/json").addHeader("X-M2M-RI", "12345").addHeader("X-M2M-Origin", "SOrigin");
        Request request = builder.build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
            }
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        ResponseBody body = response.body();
                        if (body != null) {
                            // Mobius 데이터로부터 킥보드 gps 정보 추출
                            JSONObject jsonObject = new JSONObject(response.body().string());
                            JSONObject jsonObject1 = (JSONObject) jsonObject.get("m2m:cin");
                            gpsInfo = jsonObject1.getString("con");
                            Log.d("MapActivity", "KickBoard Gps Response: " + gpsInfo);

                            // 킥보드 GPS 데이터
                            gps.KickGpsInfo = gpsInfo.split(" ");
                            gps.KickGpsID = gps.KickGpsInfo[0];
                            gps.KickLatitude = Double.parseDouble(gps.KickGpsInfo[1]);
                            gps.KickLongitude = Double.parseDouble(gps.KickGpsInfo[2]);
                            gps.KickSpeed = Double.parseDouble(gps.KickGpsInfo[3]);
                            // UI 업데이트
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            showKickBoardPath(gps.KickGpsID, gps.KickLatitude, gps.KickLongitude);
                                        }
                                    });
                                }
                            }).start();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // GPS 데이터를 이용하여 맵에 경로 나타내는 함수
    private void showKickBoardPath(String ID, double latitude, double longitude) {
        Log.d("MapActivity", "ID: " + ID + " Latitude: " + latitude + " Longitude: " + longitude);  // 현재 킥보드 위치
        if (ID.equals(gps_kickID)) {
            // 경로 추가
            coords.add(new LatLng(latitude, longitude));
            path.setCoords(coords);
            // 맵에 경로 표시
            path.setColor(Color.parseColor("#FFD53B"));
            path.setOutlineColor(Color.BLACK);
            path.setMap(naverMap);  // 지도에 경로 업데이트
            Log.d("MapActivity", "GPS Tracking ...");
        }
    }

    // *********************************************************************************************


    // 포트홀 ***************************************************************************************

    // 모비우스 포트홀 데이터 한번에 불러오는 함수
    private void getPotholeData(){
        // 포트홀 데이터 한번에 받는 Url
        String Url = "http://203.250.148.120:20519/Mobius/kick_off/map/pothole/gps?fu=1&ty=4";
        // OkHttp 클라이언트 객체 생성
        OkHttpClient client = new OkHttpClient().newBuilder().build();

        // GET 요청 객체 생성
        Request.Builder builder = new Request.Builder().url(Url).get();
        builder.addHeader("Accept", "application/json").addHeader("X-M2M-RI", "12345").addHeader("X-M2M-Origin", "SOrigin");
        Request request = builder.build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
            }
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        ResponseBody body = response.body();
                        if (body != null) {
                            // Mobius 데이터로부터 데이터 모음 추출
                            JSONObject jsonObject = new JSONObject(response.body().string());
                            potholeInfo = jsonObject.getString("m2m:uril");
                            potholeInfo1 =  potholeInfo.split(",");

                            for(int i=0;i<potholeInfo1.length;i++){
                                potholeInfo1[i] = potholeInfo1[i].replaceAll("[^0-9-]", "");
                                Log.d("PotholeCheck", potholeInfo1[i]);
                                getPothole(potholeInfo1[i]);

                            }
                            Log.d("PotholeCheck", "Pothole Gps Data Process is done.");
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // 기존 포트홀 데이터 불러오는 함수
    private void getPothole(String Pothole_ID){
        String Url = "http://203.250.148.120:20519/Mobius/kick_off/map/pothole/gps/" + Pothole_ID;
        Log.d("PotholeCheck", "Url: " + Url);

        // OkHttp 클라이언트 객체 생성
        OkHttpClient client = new OkHttpClient().newBuilder().build();

        Request.Builder builder = new Request.Builder().url(Url).get();
        builder.addHeader("Accept", "application/json").addHeader("X-M2M-RI", "12345").addHeader("X-M2M-Origin", "SOrigin");
        Request request = builder.build();

        Call call = client.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
            }
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        ResponseBody body = response.body();
                        if (body != null) {
                            // Mobius 데이터로부터 킥보드 gps 정보 추출
                            JSONObject jsonObject = new JSONObject(response.body().string());
                            JSONObject jsonObject1 = (JSONObject) jsonObject.get("m2m:cin");
                            potholeGps = jsonObject1.getString("con");
                            Log.d("PotholeCheck", "Pothole Gps Response: " + potholeGps);
                            double latitude = Double.parseDouble(potholeGps.split(" ")[0]);
                            double longitude = Double.parseDouble(potholeGps.split(" ")[1]);

                            // UI 업데이트
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            showPothole(latitude, longitude);
                                        }
                                    });
                                }
                            }).start();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // 새로운 포트홀 데이터 불러오는 함수
    private void GetNewPothole(){
        // OkHttp 클라이언트 객체 생성
        OkHttpClient client = new OkHttpClient().newBuilder().build();

        // GET 요청 객체 생성
        Request.Builder builder = new Request.Builder().url("http://203.250.148.120:20519/Mobius/kick_off/map/pothole/gps/la").get();
        builder.addHeader("Accept", "application/json").addHeader("X-M2M-RI", "12345").addHeader("X-M2M-Origin", "SOrigin");
        Request request = builder.build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
            }
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        ResponseBody body = response.body();
                        if (body != null) {
                            // Mobius 데이터로부터 킥보드 gps 정보 추출
                            JSONObject jsonObject = new JSONObject(response.body().string());
                            JSONObject jsonObject1 = (JSONObject) jsonObject.get("m2m:cin");
                            potholeInfo = jsonObject1.getString("con");
                            Log.d("MapActivity", "Pothole Gps Response: " + potholeInfo);
                            double latitude = Double.parseDouble(potholeInfo.split(" ")[0]);
                            double longitude = Double.parseDouble(potholeInfo.split(" ")[1]);

                            // UI 업데이트
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            showPothole(latitude, longitude);
                                        }
                                    });
                                }
                            }).start();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // 포트홀 마커 맵에 띄우는 함수
    private static void showPothole(double latitude, double longitude) {
        Log.d("PotholeCheck", "Pothole Latitude: " + latitude + " Pothole Longitude: " + longitude);  // 포트홀 위치
        // Pothole 마커
        Marker PotholeMarker = new Marker();
        OverlayImage image = OverlayImage.fromResource(R.drawable.ic_map_speedbump);
        PotholeMarker.setPosition(new LatLng(latitude, longitude));  // 새로운 포트홀 위치 설정
        PotholeMarker.setIcon(image);    // 포트홀 마커 이미지
        PotholeMarker.setWidth(80);
        PotholeMarker.setHeight(80);
        PotholeMarker.setMap(naverMap);  // 지도에 마커 띄움
        Log.d("PotholeCheck", "New Pothole Marker: " + latitude + " " + longitude);
    }

    // *********************************************************************************************


    // 과속방지턱 ************************************************************************************

    // 모비우스 과속방지턱 데이터 한번에 불러오는 함수
    private void getBumpData(){
        // 과속방지턱 데이터 한번에 받는 Url
        String Url = "http://203.250.148.120:20519/Mobius/kick_off/map/speed_bump/gps?fu=1&ty=4";
        // OkHttp 클라이언트 객체 생성
        OkHttpClient client = new OkHttpClient().newBuilder().build();

        // GET 요청 객체 생성
        Request.Builder builder = new Request.Builder().url(Url).get();
        builder.addHeader("Accept", "application/json").addHeader("X-M2M-RI", "12345").addHeader("X-M2M-Origin", "SOrigin");
        Request request = builder.build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
            }
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        ResponseBody body = response.body();
                        if (body != null) {
                            // Mobius 데이터로부터 데이터 모음 추출
                            JSONObject jsonObject = new JSONObject(response.body().string());
                            bumpInfo = jsonObject.getString("m2m:uril");
                            bumpInfo1 =  bumpInfo.split(",");
                            bumpGpsData =  new ArrayList[bumpInfo1.length];

                            for(int i=0;i<bumpInfo1.length;i++){
                                bumpInfo1[i] = bumpInfo1[i].replaceAll("[^0-9-]", "");
                                Log.d("BumpCheck", bumpInfo1[i]);
                                getBump(i, bumpInfo1[i]);
                            }
                            Log.d("BumpCheck", "Bump Gps Data Process is done.");
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // 기존 과속방지턱 데이터 불러오는 함수
    private void getBump(int index, String Bump_ID){
        String Url = "http://203.250.148.120:20519/Mobius/kick_off/map/speed_bump/gps/" + Bump_ID;
        Log.d("BumpCheck", "Url: " + Url);
        bumpGpsData[index] = new ArrayList<Double>();

        // OkHttp 클라이언트 객체 생성
        OkHttpClient client = new OkHttpClient().newBuilder().build();

        Request.Builder builder = new Request.Builder().url(Url).get();
        builder.addHeader("Accept", "application/json").addHeader("X-M2M-RI", "12345").addHeader("X-M2M-Origin", "SOrigin");
        Request request = builder.build();

        Call call = client.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
            }
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        ResponseBody body = response.body();
                        if (body != null) {
                            // Mobius 데이터로부터 과속방지턱 정보 추출
                            JSONObject jsonObject = new JSONObject(response.body().string());
                            JSONObject jsonObject1 = (JSONObject) jsonObject.get("m2m:cin");
                            bumpGps = jsonObject1.getString("con");
                            // bumpInfo2[index] = bumpGps;
                            Log.d("BumpCheck", "Speed Bump Gps Response: " + bumpGps);
                            double latitude = Double.parseDouble(bumpGps.split(" ")[0]);
                            double longitude = Double.parseDouble(bumpGps.split(" ")[1]);

                            bumpGpsData[index].add(latitude);
                            bumpGpsData[index].add(longitude);

                            // UI 업데이트
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            showSpeedBump(latitude, longitude);
                                        }
                                    });
                                }
                            }).start();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // 과속방지턱 마커 맵에 띄우는 함수
    private static void showSpeedBump (double latitude, double longitude) {
        Log.d("BumpCheck", "Speed Bump Latitude: " + latitude + " Pothole Longitude: " + longitude);  // 과속방지턱 위치
        // 방지턱 마커
        Marker SpeedBumpMarker = new Marker();
        OverlayImage image = OverlayImage.fromResource(R.drawable.ic_map_speedbump);
        SpeedBumpMarker.setPosition(new LatLng(latitude, longitude));  // 새로운 과속방지턱 위치 설정
        SpeedBumpMarker.setIcon(image);    // 과속방지턱 마커 이미지
        SpeedBumpMarker.setWidth(80);
        SpeedBumpMarker.setHeight(80);
        SpeedBumpMarker.setMap(naverMap);  // 지도에 마커 띄움
        Log.d("BumpCheck", "New Speed Bump Marker: " + latitude + " " + longitude);
    }

    // 킥보드와 과속방지턱 간의 거리 확인 함수
    private void BufferDistance(String ID, double latitude, double longitude, double speed){
        // 주행 중인 킥보드인 경우
        if(ID.equals((gps_kickID))){
            for(int i=0;i<bumpGpsData.length;i++){
                // 과속방지턱과 킥보드 사이 거리가 100m 이하인 경우
                double bumpLatitude = bumpGpsData[i].get(0);
                double bumpLongitude = bumpGpsData[i].get(1);
                Log.d("BumpCheck", "Speed Bump GPS: " + bumpLatitude + " " + bumpLongitude);
                // 킥보드와 과속방지턱 사이의 거리가 10m 이하인 경우
                if(getDistance(latitude, longitude, bumpLatitude, bumpLongitude) < 10){
                    // 킥보드 속도가 15km/h 이상인 경우 - 감속 알림
                    if((speed*3600)>20){
                        // 알림 설정
                        // 진동 (1초)
                        Vibrator vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                        vib.vibrate(1000);
                        // 소리
                        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), notification);
                        ringtone.play();
                        // 토스트 (Toast)
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run()
                            {
                                Toast t = Toast.makeText(getApplicationContext(), "Speed Bump! Slow down ", Toast.LENGTH_SHORT);
                                t.show();
                            }
                        }, 1000);
                    }
                }
            }
        }
    }

    // 두 지점 사이 거리 구하는 함수 (거리 단위: m)
    private double getDistance(double latitude1, double longitude1, double latitude2, double longitude2) {

        double theta = longitude1 - longitude2;
        double dist = Math.sin(deg2rad(latitude1))* Math.sin(deg2rad(latitude2)) + Math.cos(deg2rad(latitude1))*Math.cos(deg2rad(latitude2))*Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60*1.1515*1609.344;
        Log.d("MapActivity", "The distance between KickBoard and Bump: "+ dist);
        return dist; //단위 meter
    }

    //10진수를 radian(라디안)으로 변환
    private static double deg2rad(double deg){
        return (deg * Math.PI/180.0);
    }

    //radian(라디안)을 10진수로 변환
    private static double rad2deg(double rad){
        return (rad * 180 / Math.PI);
    }

    // *********************************************************************************************

    // 스쿨존 ***************************************************************************************

    // 모비우스 스쿨존 데이터 한번에 불러오는 함수
    private void getZoneData(){
        // 스쿨존 데이터 한번에 받는 Url
        String Url = "http://203.250.148.120:20519/Mobius/kick_off/map/school_zone/gps?fu=1&ty=4";
        // OkHttp 클라이언트 객체 생성
        OkHttpClient client = new OkHttpClient().newBuilder().build();

        // GET 요청 객체 생성
        Request.Builder builder = new Request.Builder().url(Url).get();
        builder.addHeader("Accept", "application/json").addHeader("X-M2M-RI", "12345").addHeader("X-M2M-Origin", "SOrigin");
        Request request = builder.build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
            }
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        ResponseBody body = response.body();
                        if (body != null) {
                            // Mobius 데이터로부터 데이터 모음 추출
                            JSONObject jsonObject = new JSONObject(response.body().string());
                            zoneInfo = jsonObject.getString("m2m:uril");
                            zoneInfo1 =  zoneInfo.split(",");
                            zoneGpsData =  new ArrayList[zoneInfo1.length];

                            for(int i=0;i<zoneInfo1.length;i++){
                                zoneInfo1[i] = zoneInfo1[i].replaceAll("[^0-9-]", "");
                                Log.d("ZoneCheck", zoneInfo1[i]);
                                getZone(i, zoneInfo1[i]);
                            }
                            Log.d("ZoneCheck", "Zone Gps Data Process is done.");
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // 기존 스쿨존 데이터 불러오는 함수
    private void getZone(int index, String Zone_ID){
        String Url = "http://203.250.148.120:20519/Mobius/kick_off/map/school_zone/gps/" + Zone_ID;
        Log.d("ZoneCheck", "Url: " + Url);
        zoneGpsData[index] = new ArrayList<Double>();

        // OkHttp 클라이언트 객체 생성
        OkHttpClient client = new OkHttpClient().newBuilder().build();

        Request.Builder builder = new Request.Builder().url(Url).get();
        builder.addHeader("Accept", "application/json").addHeader("X-M2M-RI", "12345").addHeader("X-M2M-Origin", "SOrigin");
        Request request = builder.build();

        Call call = client.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
            }
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        ResponseBody body = response.body();
                        if (body != null) {
                            // Mobius 데이터로부터 과속방지턱 정보 추출
                            JSONObject jsonObject = new JSONObject(response.body().string());
                            JSONObject jsonObject1 = (JSONObject) jsonObject.get("m2m:cin");
                            zoneGps = jsonObject1.getString("con");
                            Log.d("ZoneCheck", "Zone Gps Response: " + zoneGps);
                            double latitude = Double.parseDouble(zoneGps.split(" ")[0]);
                            double longitude = Double.parseDouble(zoneGps.split(" ")[1]);

                            zoneGpsData[index].add(latitude);
                            zoneGpsData[index].add(longitude);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // 킥보드와 스쿨존 간의 거리 확인 함수
    private void ZoneDistance(String ID, double latitude, double longitude, double speed){
        // 주행 중인 킥보드인 경우
        if(ID.equals((gps_kickID))){
            for(int i=0;i<zoneGpsData.length;i++){
                double zoneLatitude = zoneGpsData[i].get(0);
                double zoneLongitude = zoneGpsData[i].get(1);
                Log.d("ZoneCheck", "School Zone GPS: " + zoneLatitude + " " + zoneLongitude);
                // 킥보드와가 스쿨존에 진입한 경우
                if(getDistance(latitude, longitude, zoneLatitude, zoneLongitude) < 300){
                    // 킥보드 속도가 20km/h 이상인 경우 - 감속 알림
                    if((speed*3600)>15){
                        // 알림 설정
                        // 진동 (1초)
                        Vibrator vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                        vib.vibrate(1000);
                        // 소리
                        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), notification);
                        ringtone.play();
                        // 토스트 (Toast)
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run()
                            {
                                Toast t = Toast.makeText(getApplicationContext(), "School Zone! Slow down ", Toast.LENGTH_SHORT);
                                t.show();
                            }
                        }, 1000);
                    }
                }
            }
        }
    }


    // *********************************************************************************************

    // 급제동 확인 ***********************************************************************************

    // 킥보드 Gyro 데이터 불러오는 함수
    public String GyroKickBoard() {
        // OkHttp 클라이언트 객체 생성
        OkHttpClient client = new OkHttpClient().newBuilder().build();

        // GET 요청 객체 생성
        Request.Builder builder = new Request.Builder().url("http://203.250.148.120:20519/Mobius/kick_off/data/gyro/la").get();
        builder.addHeader("Accept", "application/json").addHeader("X-M2M-RI", "12345").addHeader("X-M2M-Origin", "SOrigin");
        Request request = builder.build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
            }
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        ResponseBody body = response.body();
                        if (body != null) {
                            // Mobius 데이터로부터 킥보드 gyro 정보 추출
                            JSONObject jsonObject = new JSONObject(response.body().string());
                            JSONObject jsonObject1 = (JSONObject) jsonObject.get("m2m:cin");
                            gyroInfo = jsonObject1.getString("con");
                            Log.d("MapActivity", "KickBoard Gyro Response: " + gyroInfo);

                            // 킥보드 Gyro 데이터
                            gyro.KickGyroInfo = gyroInfo.split(" ");
                            gyro.KickGyroID = gyro.KickGyroInfo[0];
                            gyro.KickGx = Double.parseDouble(gyro.KickGyroInfo[1]);
                            gyro.KickGy = Double.parseDouble(gyro.KickGyroInfo[2]);
                            gyro.KickGz = Double.parseDouble(gyro.KickGyroInfo[3]);
                            gyro.KickAx = Double.parseDouble(gyro.KickGyroInfo[4]);
                            gyro.KickAy = Double.parseDouble(gyro.KickGyroInfo[5]);
                            gyro.KickAz = Double.parseDouble(gyro.KickGyroInfo[6]);

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            speedCheck(gyro.KickGyroID, gyro.KickGx);
                                        }
                                    });
                                }
                            }).start();

                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        return gpsInfo;
    }

    // 급제동 체크 함수
    private void speedCheck(String ID, double gx){
        if(ID.equals(gps_kickID)){
            if(abs(gx)>9){
                Log.d("MapActivity", "Sudden Stop");
                // 알림 설정
                // 진동 (1초)
                Vibrator vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                vib.vibrate(1000);
                // 소리
                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), notification);
                ringtone.play();
                // 토스트 (Toast)
                Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run()
                    {
                        Toast t = Toast.makeText(getApplicationContext(), "Sudden Stop!", Toast.LENGTH_SHORT);
                        t.show();
                    }
                }, 1000);
            }
        }
    }

    // *********************************************************************************************



    // 주차장 ***************************************************************************************

    // 모비우스 주차장 데이터 한번에 불러오는 함수
    private void getParkingSpaceData(){
        // 포트홀 데이터 한번에 받는 Url
        String Url = "http://203.250.148.120:20519/Mobius/kick_off/map/parking_space/gps?fu=1&ty=4";
        // OkHttp 클라이언트 객체 생성
        OkHttpClient client = new OkHttpClient().newBuilder().build();

        // GET 요청 객체 생성
        Request.Builder builder = new Request.Builder().url(Url).get();
        builder.addHeader("Accept", "application/json").addHeader("X-M2M-RI", "12345").addHeader("X-M2M-Origin", "SOrigin");
        Request request = builder.build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
            }
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        ResponseBody body = response.body();
                        if (body != null) {
                            // Mobius 데이터로부터 데이터 모음 추출
                            JSONObject jsonObject = new JSONObject(response.body().string());
                            parkingInfo = jsonObject.getString("m2m:uril");
                            parkingInfo1 =  parkingInfo.split(",");

                            for(int i=0;i<parkingInfo1.length;i++){
                                parkingInfo1[i] = parkingInfo1[i].replaceAll("[^0-9-]", "");
                                Log.d("ParkingCheck", parkingInfo1[i]);
                                getParkingSpace(parkingInfo1[i]);
                            }
                            Log.d("ParkingCheck", "Parking Space Gps Data Process is done.");
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // 기존 주차장 데이터 불러오는 함수
    private void getParkingSpace(String Parking_ID){
        String Url = "http://203.250.148.120:20519/Mobius/kick_off/map/parking_space/gps/" + Parking_ID;
        Log.d("ParkingCheck", "Url: " + Url);

        // OkHttp 클라이언트 객체 생성
        OkHttpClient client = new OkHttpClient().newBuilder().build();

        Request.Builder builder = new Request.Builder().url(Url).get();
        builder.addHeader("Accept", "application/json").addHeader("X-M2M-RI", "12345").addHeader("X-M2M-Origin", "SOrigin");
        Request request = builder.build();

        Call call = client.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
            }
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        ResponseBody body = response.body();
                        if (body != null) {
                            // Mobius 데이터로부터 킥보드 gps 정보 추출
                            JSONObject jsonObject = new JSONObject(response.body().string());
                            JSONObject jsonObject1 = (JSONObject) jsonObject.get("m2m:cin");
                            parkingGps = jsonObject1.getString("con");
                            Log.d("ParkingCheck", "Parking Space Gps Response: " + parkingGps);
                            double latitude = Double.parseDouble(parkingGps.split(" ")[0]);
                            double longitude = Double.parseDouble(parkingGps.split(" ")[1]);

                            // UI 업데이트
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            showParkingSpace("SPACE", latitude, longitude);
                                        }
                                    });
                                }
                            }).start();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // 주차장 마커 맵에 띄우는 함수
    private static void showParkingSpace(String ID, double latitude, double longitude) {
        Log.d("ParkingCheck", "Parking Latitude: " + latitude + " Parking Longitude: " + longitude);  // 주차장 위치
        // 주차장 마커
        if(ID.equals("SPACE")){
            Marker ParkingSpaceMarker = new Marker();
            OverlayImage image = OverlayImage.fromResource(R.drawable.ic_map_parking);
            ParkingSpaceMarker.setPosition(new LatLng(latitude, longitude));  // 새로운 주차장 위치 설정
            ParkingSpaceMarker.setIcon(image);    // 주차장 마커 이미지
            ParkingSpaceMarker.setWidth(80);
            ParkingSpaceMarker.setHeight(80);
            ParkingSpaceMarker.setMap(naverMap);  // 지도에 마커 띄움
            Log.d("ParkingCheck", "New Parking Space Marker: " + latitude + " " + longitude);
        } else if (ID.equals("GROUND")){
            Marker ParkingSpaceMarker = new Marker();
            OverlayImage image = OverlayImage.fromResource(R.drawable.ic_home_parking);
            ParkingSpaceMarker.setPosition(new LatLng(latitude, longitude));  // 새로운 주차장 위치 설정
            ParkingSpaceMarker.setIcon(image);    // 주차장 마커 이미지
            ParkingSpaceMarker.setWidth(150);
            ParkingSpaceMarker.setHeight(120);
            ParkingSpaceMarker.setMap(naverMap);  // 지도에 마커 띄움
            Log.d("ParkingCheck", "New Parking Space Marker: " + latitude + " " + longitude);
        } else if(ID.equals("UNDER")){
            Marker ParkingSpaceMarker = new Marker();
            OverlayImage image = OverlayImage.fromResource(R.drawable.ic_map_under);
            ParkingSpaceMarker.setPosition(new LatLng(latitude, longitude));  // 새로운 주차장 위치 설정
            ParkingSpaceMarker.setIcon(image);    // 주차장 마커 이미지
            ParkingSpaceMarker.setWidth(130);
            ParkingSpaceMarker.setHeight(120);
            ParkingSpaceMarker.setMap(naverMap);  // 지도에 마커 띄움
            Log.d("ParkingCheck", "New Parking Space Marker: " + latitude + " " + longitude);
        }

    }

    // *********************************************************************************************


    // 주차 확인 ************************************************************************************

    private void postParking(){
        // Url 수정 필요
        String Url = "http://203.250.148.120:20519/Mobius/kick_off/map/parking_space/check";

        // OkHttp 클라이언트 객체 생성
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/vnd.onem2m-res+json; ty=4");
        String content = "{\n    \"m2m:cin\": {\n        \"con\": " + "\"" + "OK" + "\"" + "\n    }\n}";

        // POST 요청 객체 생성
        RequestBody body = RequestBody.create(mediaType, content);
        Request request = new Request.Builder()
                .url(Url)
                .method("POST", body)
                .addHeader("Accept", "application/json")
                .addHeader("X-M2M-RI", "12345")
                .addHeader("X-M2M-Origin", "{{aei}}")
                .addHeader("Content-Type", "application/vnd.onem2m-res+json; ty=4")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                // 데이터 전송 성공
                ResponseBody body = response.body();
                if(body!=null){
                    Log.d("EndActivity", "Send Parking Checking Signal Success");
                }
                else{
                    Log.d("EndActivity", "Send Parking Checking Signal Fail");
                }
            }
        });
    }

    private void getParking(){
        try {
            sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String Url = "http://203.250.148.120:20519/Mobius/kick_off/map/parking_space/result/la";

        // OkHttp 클라이언트 객체 생성
        OkHttpClient client = new OkHttpClient().newBuilder().build();

        Request.Builder builder = new Request.Builder().url(Url).get();
        builder.addHeader("Accept", "application/json").addHeader("X-M2M-RI", "12345").addHeader("X-M2M-Origin", "SOrigin");
        Request request = builder.build();

        Call call = client.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
            }
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        ResponseBody body = response.body();
                        if (body != null) {
                            // Mobius 데이터로부터 과속방지턱 정보 추출
                            JSONObject jsonObject = new JSONObject(response.body().string());
                            JSONObject jsonObject1 = (JSONObject) jsonObject.get("m2m:cin");
                            parkingCheck = jsonObject1.getString("con").split(" ");
                            Log.d("EndActivity", parkingCheck[0]);
                            Log.d("EndActivity", parkingCheck[1]);
                            if(parkingCheck[0].equals("SUCCESS")){
                                // 현재 날짜 및 시간
                                long now = System.currentTimeMillis();
                                long getEndTime = System.currentTimeMillis();
                                Date date = new Date(now);
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd hh:mm:ss");
                                String getTime = dateFormat.format(date);

                                // 운행 시간
                                long getRidingTime = (getEndTime - getStartTime)/1000;

                                Intent intent = new Intent(MapActivity.this, MainActivity.class);   // 화면 이동
                                String message = startTime + "," + getTime + "," + getRidingTime; // 킥보드 출발 시간 및 도착 시간
                                String parking_gps = gps.KickLatitude + " " + gps.KickLongitude;
                                intent.putExtra("kick_time", message);  // 시간 데이터 전송
                                intent.putExtra("user_login", login_id);
                                intent.putExtra("user_info", user_data);

                                // 토스트 (Toast)
                                Handler handler = new Handler(Looper.getMainLooper());
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run()
                                    {
                                        Toast t = Toast.makeText(getApplicationContext(), "Success Parking !", Toast.LENGTH_SHORT);
                                        t.show();
                                    }
                                }, 1000);

                                // UI 업데이트
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                showParkingSpace(parkingCheck[1], Double.parseDouble(parkingCheck[2]), Double.parseDouble(parkingCheck[3]));
                                            }
                                        });
                                    }
                                }).start();

                                sleep(7000);
                                startActivity(intent);
                                map_status = false; // MapActivity Thread 종료
                                finish();   // MapActivity 종료

                            } else {
                                // 토스트 (Toast)
                                Handler handler = new Handler(Looper.getMainLooper());
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run()
                                    {
                                        Toast t = Toast.makeText(getApplicationContext(), "Fail Parking ! Try Again", Toast.LENGTH_SHORT);
                                        t.show();
                                    }
                                }, 1000);
                            }
                        }
                    }
                } catch (JSONException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // *********************************************************************************************
}
