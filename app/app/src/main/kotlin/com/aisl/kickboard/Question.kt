package com.aisl.kickboard;

data class Question(
    val id: Int, //문제 번호
    val question: String, //문제
    val image: Int, //이미지
    val optionOne: String, //option은 객관식 답변들
    val optionTwo: String,
    val optionThree: String,
    val optionFour: String,
    val correctAnswer: Int //정답
)