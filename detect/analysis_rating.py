import requests

while(1):
    all_url = "http://203.250.148.120:20519/Mobius/kick_off/user/account?fu=1&ty=4"

    payload={}
    headers = {
        'Accept': 'application/json',
        'X-M2M-RI': '12345',
        'X-M2M-Origin': 'SOrigin'
    }

    ID = []

    response = requests.request("GET", all_url, headers=headers, data=payload)

    for i in range(len(response.json()["m2m:uril"])) :
        ID.append(response.json()["m2m:uril"][i].split("/")[4])

    # ID별 정보 가져오기

    for i in range(len(ID)) :

        detail_url = "http://203.250.148.120:20519/Mobius/kick_off/user/account/" + ID[i]

        payload={}
        headers = {
            'Accept': 'application/json',
            'X-M2M-RI': '12345',
            'X-M2M-Origin': 'SOrigin'
        }

        response = requests.request("GET", detail_url, headers=headers, data=payload)

        response_list = response.json()["m2m:cin"]["con"].split(" ")

        # 4: 면허증 취득일
        # license_date = response_list[4]

        # 5: 운행 시간 -> 초로 받기
        drive_time = int(response_list[5])

        # 6: 등급 -> 기본값 -
        rating = response_list[6]

        # 7: 안전/위험 -> 기본값 -
        safety_danger = response_list[7]

        # 8: 벌점 penalty
        penalty = int(response_list[8])

        # 9, 10, 11 급정거/방지턱/스쿨존 누적벌점 penalty_sub
        # penalty_sub = []
        # penalty_sub[0] = response_list[9]
        # penalty_sub[1] = response_list[10]
        # penalty_sub[2] = response_list[11]

        # (누적벌점 / 운행 시간) 값에 따라 등급 A~F) / 안전/위험(50% 기준)

        if (drive_time != 0 and response_list[12] == "1") :
            score = penalty/drive_time

            if score < 0.00015:
                rating = "A"
            elif score < 0.00045:
                rating = "B"
            elif score < 0.00085: # 3600초 기준 대략 벌점 3점
                rating = "C"
            elif score < 0.00125:
                rating = "D"
            else:
                rating = "F"

            if score < 0.00045: 
                safety_danger = "safety"
            elif score < 0.00085:
                safety_danger = "normal"
            else :
                safety_danger = "danger"

            response_list[6] = rating
            response_list[7] = safety_danger

            response_list[12] = "0"

            response_str = " ".join(response_list)

            # 원래 cin 삭제
            payload = ""
            headers = {
            'Accept': 'application/xml',
            'X-M2M-RI': '12345',
            'X-M2M-Origin': '{{aei}}'
            }

            response = requests.request("DELETE", detail_url, headers=headers, data=payload)


            # 새로운 cin 재생성
            create_url = "http://203.250.148.120:20519/Mobius/kick_off/user/account"

            payload = "{\n    \"m2m:cin\": {\n        \"con\" : \""+response_str+"\"\n    }\n}"
            headers = {
            'Accept': 'application/json',
            'X-M2M-RI': '12345',
            'X-M2M-Origin': '{{aei}}',
            'Content-Type': 'application/vnd.onem2m-res+json; ty=4'
            }

            response = requests.request("POST", create_url, headers=headers, data=payload)