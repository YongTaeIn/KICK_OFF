## 급제동 판단 (자이로 사용)
## 2022/10/02
## ax의 기준치를 정하면 끝.(직접 타면서 테스트 해야함.)

import requests
import time


#get mobious data
def getdata(url):
    response = requests.request("GET", url, headers=headers, data=payload)
    lst=response.text.split(":")
    for i in range(0,len(lst)):
        if "con" in lst[i]:
            lst[i+1]=lst[i+1].replace('}','').replace('"','')

            
            if kick_id in lst[i+1]:
                
                return lst[i+1]


while(1) :
    url1 = "http://203.250.148.120:20519/Mobius/kick_off/data/gps/la"
    url2 = "http://203.250.148.120:20519/Mobius/kick_off/data/gyro/la"

    kick_id="MFBE29"
    #heder and payload
    payload={}
    headers = {
        'Accept': 'application/json',
        'X-M2M-RI': '12345',
        'X-M2M-Origin': 'SOrigin'
    }

    gps_list=getdata(url1).split(" ")

    #실시간 킥보드 데이터
    lat=gps_list[1]
    lon=gps_list[2]
    gyro_list=getdata(url2).split(" ")
    gx=gyro_list[1]
    gy=gyro_list[2]
    gz=gyro_list[3]
    ax=gyro_list[4]
    ay=gyro_list[5]
    az=gyro_list[6]



    ## 킥보드 속도 
    # kick_speed=float(all_speed_gps[-1][-1])
    # check_speed=float(all_speed_gps[-2][-1])
    # kick_change_speed=(kick_speed - check_speed)

    # # 킥보드 현재 위치 및 속도 출력
    # print('current kickboard speed =',kick_speed)
    # print('previous kickboard spped =', check_speed)
    # print('kickboard speed rate of change =',kick_changespeed)

    # (현재 속도 - 1초 전 속도)/(0.1초) > n 이상이면 급제동
    # n을 10 또는 15 정도로 생각중
    #if kick_change_speed > 10 and ax < -5.5:

    # 부등호 방향 바꿨어용 ( < -> 이게 원래 코드)

    print(gx, gy, gz, ax,ay, az)

    if abs(float(gx)) > 9:  # ax 값 테스트

        # 사용자의 모든 정보 가져오기

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

            # 누적벌점 : 8번, 급정거 누적벌점 : 9번

            # 3번 사용자의 정보만 가져오기
            if (response.json()["m2m:cin"]["con"].split(" ")[0] == "kick@email.com"):
                print("3번 사용자")

                penalty = str(int(response.json()["m2m:cin"]["con"].split(" ")[8]) + 1)
                penalty_sub = str(int(response.json()["m2m:cin"]["con"].split(" ")[9]) + 1)

                response_list = response.json()["m2m:cin"]["con"].split(" ")
                response_list[8] = penalty
                response_list[9] = penalty_sub
                #print(response_list)

                response_list[12] = "1"

                # 벌점 수정
                response_str = " ".join(response_list)
                #print(response_str)


                # 원래 데이터 삭제
                payload = ""
                headers = {
                'Accept': 'application/xml',
                'X-M2M-RI': '12345',
                'X-M2M-Origin': '{{aei}}'
                }

                response = requests.request("DELETE", detail_url, headers=headers, data=payload)


                # 새로운 벌점으로 재생성
                create_url = "http://203.250.148.120:20519/Mobius/kick_off/user/account"

                payload = "{\n    \"m2m:cin\": {\n        \"con\" : \""+response_str+"\"\n    }\n}"
                headers = {
                'Accept': 'application/json',
                'X-M2M-RI': '12345',
                'X-M2M-Origin': '{{aei}}',
                'Content-Type': 'application/vnd.onem2m-res+json; ty=4'
                }

                requests.request("POST", create_url, headers=headers, data=payload)

                # penalty_zone에 번호 + gps 보내기
                penalty_zone_url = "http://203.250.148.120:20519/Mobius/kick_off/user/penalty_zone"

                penalty_list = [str(0), str(lat), str(lon)]
                penalty_str = " ".join(penalty_list)
                
                payload = "{\n    \"m2m:cin\": {\n        \"con\" : \""+penalty_str+"\"\n    }\n}"
                headers = {
                'Accept': 'application/json',
                'X-M2M-RI': '12345',
                'X-M2M-Origin': '{{aei}}',
                'Content-Type': 'application/vnd.onem2m-res+json; ty=4'
                }

                requests.request("POST", penalty_zone_url, headers=headers, data=payload)
                print('warning')
                time.sleep(10)

            else:
                print('normal')

