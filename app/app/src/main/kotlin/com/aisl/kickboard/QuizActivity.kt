package com.aisl.kickboard

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_quiz.*

class QuizActivity : AppCompatActivity(), View.OnClickListener {

    private var mCurrentPosition : Int = 1  //첫 질문
    private var mQuestionList : ArrayList<Question>? = null //질문 목록을 설정하기 위해 사용할 수 있는 개체 필요
    private var mSelectedOptionPosition : Int = 0   //현재 선택한 값 체크
    private var mCorrectAnswers : Int = 0   //정답 개수
    private var mUserName: String? = null   //사용자 이름
    private var mUserBirth: String? = null  // 사용자 생년월일
    private var mUserSex: String? = null    // 사용자 성별
    private var mUserID: String? = null     // 사용자 아이디


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        mUserID = intent.getStringExtra("user_login")  // 사용자 아이디 정보 받음
        mUserName = intent.getStringExtra(Constants.USER_NAME)  // 사용자 성별 정보 받음
        mUserBirth = intent.getStringExtra(Constants.USER_BIRTH)    // 사용자 생년월일 정보 받음
        mUserSex = intent.getStringExtra(Constants.USER_SEX)    // 사용자 성별 정보 받음

        mQuestionList = Constants.getQuestions() // 퀴즈 정보 받기

        setQuestion()

        tv_option_one.setOnClickListener(this)
        tv_option_two.setOnClickListener(this)
        tv_option_three.setOnClickListener(this)
        tv_option_four.setOnClickListener(this)
        btn_submit.setOnClickListener(this)
    }

    private fun setQuestion() {
        val question = mQuestionList!![mCurrentPosition - 1]    // 인덱스 0부터 시작
        defaultOptionsView()    //질문을 새로 설정할 때마다 기본 옵션보기 설정
        if (mCurrentPosition == mQuestionList!!.size) {
            //문제를 출력할 때 지금이 size의 끝이라면 버튼에 FINISH 할당
            btn_submit.text = "FINISH"
        } else { //다음 문제가 더 있다면 버튼에 SUBMIT 할당
            btn_submit.text = "SUBMIT"
        }
        progressBar.progress = mCurrentPosition //진행률은 현재위치와 같음
        tv_progress.text = "$mCurrentPosition" + "/" + progressBar.max

        tv_question.text = question!!.question  //보여지고 있는 문제의 text는 question 데이터 클래스에 저장된 question 을 저장(호출)
        iv_image.setImageResource(question.image)
        tv_option_one.text = question.optionOne
        tv_option_two.text = question.optionTwo
        tv_option_three.text = question.optionThree
        tv_option_four.text = question.optionFour
    }

    private fun defaultOptionsView() {
        val options = ArrayList<TextView>() //ui 보기 요소
        options.add(0, tv_option_one)
        options.add(1,tv_option_two)
        options.add(2,tv_option_three)
        options.add(3,tv_option_four)

        //사용자 인터페이스에 만든 드로어블 버튼테두리 색칠에 대해 기본 ui를 할당하는 for 루프 만듦
        for (option in options) {
            option.setTextColor(Color.parseColor("#7A8089"))
            option.typeface = Typeface.DEFAULT
            //drawable 을 사용하기 위해 contextcompat 사용
            option.background = ContextCompat.getDrawable(
                this,
                R.drawable.yellowborder
            )
        }
    }

    private fun answerView(answer: Int, drawalbeView:Int) { //답변보기기능
        when(answer) {
            1-> {
                tv_option_one.background = ContextCompat.getDrawable(
                    this, drawalbeView
                )
            }
            2-> {
                tv_option_two.background = ContextCompat.getDrawable(
                    this, drawalbeView
                )
            }
            3-> {
                tv_option_three.background = ContextCompat.getDrawable(
                    this, drawalbeView
                )
            }
            4-> {
                tv_option_four.background = ContextCompat.getDrawable(
                    this, drawalbeView
                )
            }
        }
    }
    // 선택된 보기
    private fun selectedOptionView(tv: TextView, selectedOptionNum : Int) {
        // 기본 옵션 보기를 설정하여 모든 옵션을 기본값으로 설정
        defaultOptionsView()
        mSelectedOptionPosition = selectedOptionNum // 현재선택한 옵션이 패러미터의 옵션
        //선택된 옵션
        tv.setTextColor(Color.parseColor("#363A43"))
        tv.setTypeface(tv.typeface, Typeface.BOLD) //텍스트 굵게 설정
        tv.background = ContextCompat.getDrawable(
            this,
            R.drawable.quizborder
        )
    }

    override fun onClick(v: View?) { //메서드의 멤버 구현 선택하고 테두리를 볼 수 있도록 구현
        when(v?.id) {
            R.id.tv_option_one -> {
                selectedOptionView(tv_option_one,1) //num을 아래 함수에 넘겨줌
                //activity quiz question xml에 id가 tv option one인 textview를 함수에 직접 전달 됨
            }
            R.id.tv_option_two -> {
                selectedOptionView(tv_option_two,2) //num을 아래 함수에 넘겨줌
                //activity quiz question xml에 id가 tv option two인 textview를 함수에 직접 전달 됨
            }
            R.id.tv_option_three -> {
                selectedOptionView(tv_option_three,3) //num을 아래 함수에 넘겨줌
                //activity quiz question xml에 id가 tv option three인 textview를 함수에 직접 전달 됨
            }
            R.id.tv_option_four -> {
                selectedOptionView(tv_option_four,4) //num을 아래 함수에 넘겨줌
                //activity quiz question xml에 id가 tv option four인 textview를 함수에 직접 전달 됨
            }
            R.id.btn_submit -> {
                if (mSelectedOptionPosition == 0) { //선택한 옵션위치가 0인 상황에서는 다음위치로 이동
                    mCurrentPosition += 1

                    when{
                        mCurrentPosition <= mQuestionList!!.size -> {
                            setQuestion()
                        }
                        else -> {
                            val intent = Intent(this, ResultActivity::class.java)   //이름값을 result 에 넘겨줌
                            intent.putExtra("user_login", mUserID)
                            intent.putExtra(Constants.USER_NAME, mUserName) //액티비티 간 값을 넘겨줄때 사용
                            intent.putExtra(Constants.USER_BIRTH, mUserBirth)
                            intent.putExtra(Constants.USER_SEX, mUserSex)
                            intent.putExtra(Constants.CORRECT_ANSWERS, mCorrectAnswers)
                            intent.putExtra(Constants.TOTAL_QUESTIONS, mQuestionList!!.size)
                            startActivity(intent)
                            finish()
                        }
                    }
                } else {
                    //리스트에 있는 문제들의 data를 가져와서 data의 correctAnswer 값이
                    //고른 옵션 위치와 같다면 초록 아니면 빨간색을 answerView 함수를 통해 할당한다
                    val question = mQuestionList?.get(mCurrentPosition - 1)
                    if (question!!.correctAnswer != mSelectedOptionPosition) {
                        answerView(mSelectedOptionPosition, R.drawable.answerborder)
                    } else {
                        mCorrectAnswers += 1 //정답일경우 추가
                    }
                    answerView(question!!.correctAnswer, R.drawable.answerborder)

                    //정답은 어떤 경우에도 녹색
                    //else 에 묶이면 틀릴 경우 초록이 빨강과 같이 출력되지 않음을 방지하기위해 else 는 안씀

                    if (mCurrentPosition == mQuestionList!!.size) {
                        //선택한 후 다음 질문으로 넘어갈때 size 에 끝이라면 FINISH 로 치환
                        btn_submit.text = "FINISH"
                    } else {
                        btn_submit.text = "GO TO NEXT QUESTION"
                    }
                    // when 에서 문제를 생성하기에 여기서 초기화
                    // 선택한 옵션위치를 다시 0으로 초기화
                    mSelectedOptionPosition = 0
                }

            }
        }
    }
}