package com.aisl.kickboard

object Constants{

    const val USER_ID : String = "user_id"  // 사용자 아이디
    const val USER_NAME : String = "user_name"  // 사용자 이름
    const val USER_BIRTH : String = "user_birth"    // 사용자 생년월일
    const val USER_SEX : String = "user_sex"    // 사용자 성별

    const val TOTAL_QUESTIONS: String = "total_question"    // 질문 개수 
    const val CORRECT_ANSWERS : String = "correct_answer"   // 정답 개수

    fun getQuestions() : ArrayList<Question> {  
        val questionsList = ArrayList<Question>()
        // 1
        val que1 = Question(
            1, "다음 중 PM(개인형 이동장치)가 아닌 것은?", R.drawable.img_quiz_one,
            "1. 세그웨이", "2. 전동킥보드", "3. 전기자전거(PAS 방식)", "4. 전동보드", 3
        )
        questionsList.add(que1)
        // 2
        val que2 = Question(
            2, "전동 킥보드의 최대 주행속도는?", R.drawable.img_quiz_two,
            "1. 15km/h", "2. 25km/h", "3. 35km/h", "4. 45km/h", 2
        )
        questionsList.add(que2)
        // 3
        val que3 = Question(
            2, "다음 중 개인형 이동장치(PM) 운전자의 준수사항으로 옳은 것은?", R.drawable.img_quiz_three,
            "1. 승차정원을 초과하여 운전", "2. PM 면허를 취소당한 사람이 운전",
            "3. 만 13세 이상인 사람이 PM 면허 취득 없이 운전", "4. 횡단보도에서 PM을 들고 횡단", 4
        )
        questionsList.add(que3)
        // 4
        val que4 = Question(
            2, "다음 중 전동킥보드의 주차장소로 옳은 것은?", R.drawable.img_quiz_four,
            "1. 횡단보도 앞", "2. 지하철 출입구 앞", "3. 전동킥보드 지정 주차공간", "4. 버스정류장 옆", 3
        )
        questionsList.add(que4)
        // 5
        val que5 = Question(
                2, "다음 중 전동킥보드를 탈 수 없는 장소로 옳은 것은?", R.drawable.img_quiz_five,
                "1. 자전거도로", "2. 차도 우측 끝", "3. 차도 인도 구분 없는 골목길", "4. 고속도로", 4
        )
        questionsList.add(que5)
        // 질문 배열 목록 반환
        return questionsList
    }
}