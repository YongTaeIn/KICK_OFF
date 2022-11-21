package com.aisl.kickboard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class EndActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end);

        Intent intent = getIntent();
        String finalData = intent.getStringExtra("final_data");
        if(finalData!=null){
            postUser(finalData);
        }
        else{
            String tmpData = "kickoff@email.com PyongjooKim 1996.03.27 0 2022.11.13 600000 0 0 0 0 0 0 0";
            postUser(tmpData);
        }
        finishApp();
    }

    public void postUser(String data){
        String Url = "http://203.250.148.120:20519/Mobius/kick_off/user/account";

        // OkHttp 클라이언트 객체 생성
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/vnd.onem2m-res+json; ty=4");
        String content = "{\n    \"m2m:cin\": {\n        \"con\": " + "\"" + data + "\"" + "\n    }\n}";
        Log.d("EndActivity", content);

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
                    Log.d("EndActivity", "Send User Info Success");
                }
                else{
                    Log.d("EndActivity", "Send User Info Fail");
                }
            }
        });
    }

    public void finishApp(){
        AlertDialog.Builder builder = new AlertDialog.Builder(EndActivity.this);
        builder.setMessage("Do you really want to Exit?");
        builder.setTitle("Exit Notification")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.cancel();
                        Intent intent = new Intent(EndActivity.this, MainActivity.class);   // 화면 이동
                        startActivity(intent);
                        finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.setTitle("Exit Notification");
        alert.show();
    }
}