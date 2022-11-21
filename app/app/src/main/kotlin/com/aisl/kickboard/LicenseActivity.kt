package com.aisl.kickboard

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_license.*
import kotlinx.android.synthetic.main.activity_license.view.*

class LicenseActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_license)

        val userID = intent.getStringExtra("user_login")
        var radiobutton = ""

        rg_user.setOnCheckedChangeListener{ group, checkedId ->
            when(checkedId){
                R.id.rb_male -> {
                    radiobutton = "Male"   // 사용자 성별 전송
                }
                R.id.rb_female -> {
                    radiobutton = "Female"   // 사용자 성별 전송
                }
            }
        }

        btn_lic_start.setOnClickListener {
            // 사용자 정보 부족한 경우
            if (et_name.text.toString().isEmpty() || et_birth.text.toString().isEmpty()) {
                Toast.makeText(this,"Please enter your information", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, QuizActivity::class.java)
                intent.putExtra("user_login", userID);
                intent.putExtra(Constants.USER_ID, userID);  // 사용자 아이디 전송
                intent.putExtra(Constants.USER_NAME, et_name.text.toString())   // 사용자 이름 전송
                intent.putExtra(Constants.USER_BIRTH, et_birth.text.toString()) // 사용자 생년월일 전송
                intent.putExtra(Constants.USER_SEX, radiobutton)    // 사용자 성별 전송

                startActivity(intent)
                finish()
            }
        }
    }
}