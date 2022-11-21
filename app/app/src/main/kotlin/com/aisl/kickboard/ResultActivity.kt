package com.aisl.kickboard

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_license.*
import kotlinx.android.synthetic.main.activity_quiz.*
import kotlinx.android.synthetic.main.activity_result.*
import java.text.SimpleDateFormat
import java.util.*

class ResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val now = System.currentTimeMillis()
        val simpleDateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.KOREAN).format(now)

        val userID = intent.getStringExtra("user_login")
        val userName = intent.getStringExtra(Constants.USER_NAME)
        val userBirth = intent.getStringExtra(Constants.USER_BIRTH)
        val userSex = intent.getStringExtra(Constants.USER_SEX)
        var userInfo = ""

        if(userID!=null){
            Log.d("DataCheck", "ResultActivity" + userID);
        }

        val totalQuestions = intent.getIntExtra(Constants.TOTAL_QUESTIONS, 0) * 20
        val correctAnswer = intent.getIntExtra(Constants.CORRECT_ANSWERS, 0) * 20

        if(correctAnswer>=80){
            tv_name.text = userName
            tv_birth.text = userBirth
            tv_sex.text = userSex
            tv_date.text = simpleDateFormat.toString()
        } else {
            tv_congratulations.text = "Sorry, You Failed"
            tv_name.text = ""
            tv_birth.text = ""
            tv_sex.text = ""
            tv_date.text = ""
            result_logo.setImageResource(R.drawable.img_licensefail)
        }

        tv_score.text = "You got $correctAnswer/$totalQuestions"

        btn_finish.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)

            if(userSex.equals("Male")){
                var userAge = userBirth?.split(".")?.get(0)
                userInfo = "$userName $userAge 0 $simpleDateFormat"
            } else {
                var userAge = userBirth?.split(".")?.get(0)?.toInt()
                userInfo = " $userName $userAge 1 $simpleDateFormat"
            }
            intent.putExtra("user_login", userID)  // 사용자 정보 전송
            intent.putExtra("user_info", userInfo)  // 사용자 정보 전송

            startActivity(intent)
            finish()
        }
    }
}