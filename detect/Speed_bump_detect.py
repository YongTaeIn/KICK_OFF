## 과속방지턱 과속 판단 (자이로+GPS 사용)
## 2022/10/02
## az의 기준치를 정하면 끝.(직접 타면서 테스트 해야함.)

import requests
from math import sin, cos, sqrt, atan2, radians
import time
import os

# # 킥보드 속도 
# kick_speed=float(all_speed_gps[-1][-1])
# # 킥보드 위도, 경도 
# kick_lat=float(all_speed_gps[-1][2])
# kick_lon=float(all_speed_gps[-1][3])
##  1. kick_speed
##  2. Az,Gz 둘다


#get mobious data
def getdata(url):
    response = requests.request("GET", url, headers=headers, data=payload)
    lst=response.text.split(":")
    for i in range(0,len(lst)):
        if "con" in lst[i]:
            lst[i+1]=lst[i+1].replace('}','').replace('"','')

            
            if kick_id in lst[i+1]:
                
                return str(lst[i+1])

def getbuffdata(url):
    response = requests.request("GET", url, headers=headers, data=payload)
    lst=response.text.split(":")
    for i in range(0,len(lst)):
        if "con" in lst[i]:
            lst[i+1]=lst[i+1].replace('}','').replace('"','')
            return str(lst[i+1])

# gps 거리구하는 함수
def lat_long_dist(lat1,lon1,lat2,lon2):
    # function for calculating ground distance between two lat-long locations
    R = 6373.0 # approximate radius of earth in km. 
    lat1 = radians( float(lat1) )
    lon1 = radians( float(lon1) )
    lat2 = radians( float(lat2) )
    lon2 = radians( float(lon2) )
    dlon = lon2 - lon1
    dlat = lat2 - lat1
    a = sin(dlat / 2)**2 + cos(lat1) * cos(lat2) * sin(dlon / 2)**2
    c = 2 * atan2(sqrt(a), sqrt(1 - a))
    distance = round(R * c, 6)
    return distance




while(1):

    buff_list = []
    # buff gps 불러오기    
    all_url = "http://203.250.148.120:20519/Mobius/kick_off/map/speed_bump/gps?fu=1&ty=4"

    payload={}
    headers = {
        'Accept': 'application/json',
        'X-M2M-RI': '12345',
        'X-M2M-Origin': 'SOrigin'
    }

    response = requests.request("GET", all_url, headers=headers, data=payload)

    ID_all = []


    for i in range(len(response.json()["m2m:uril"])) :
        ID_all.append(response.json()["m2m:uril"][i].split("/")[5])


    for i in range(len(ID_all)) :

        detail_url = "http://203.250.148.120:20519/Mobius/kick_off/map/speed_bump/gps/" + ID_all[i]

        payload={}
        headers = {
            'Accept': 'application/json',
            'X-M2M-RI': '12345',
            'X-M2M-Origin': 'SOrigin'
        }

        response = requests.request("GET", detail_url, headers=headers, data=payload)

        buff_list.append(response.json()["m2m:cin"]["con"].split(" "))
    #

    #print(buff_list)

    url1 = "http://203.250.148.120:20519/Mobius/kick_off/data/gps/la"
    url2 = "http://203.250.148.120:20519/Mobius/kick_off/data/gyro/la"
    #데이터의 rn값으로 url 불러올수 있다.


    #특정 킥보드의 아이디 골라서 바꿔주면 된다.
    kick_id="MFBE29"
    #heder and payload
    payload={}
    headers = {
        'Accept': 'application/json',
        'X-M2M-RI': '12345',
        'X-M2M-Origin': 'SOrigin'
    }

    

    # # 킥보드 현재 위치 및 속도 출력
    # print('current kickboard gps:', kick_lat, kick_lon)
    # print('current kickboard speed =',kick_speed)
    a=int(0)
    #while True:
    gps_list=getdata(url1).split(" ")
    gyro_list=getdata(url2).split(" ")

    #실시간 킥보드 데이터
    lat=gps_list[1]
    lon=gps_list[2]
    speed=gps_list[3]
    kick_speed=float(speed)*3600
    gx=gyro_list[1]
    gy=gyro_list[2]
    gz=gyro_list[3]
    ax=gyro_list[4]
    ay=gyro_list[5]
    az=gyro_list[6]
    #print("lat: "+lat+" lon: "+lon+" speed: "+speed)
    #print(gx,gy,gz,ax,ay,az)

    for i in range(len(buff_list)):
        a=buff_list[i][0]
        b=buff_list[i][1]
        print(lat_long_dist(a,b,lat,lon))
        if lat_long_dist(a,b,lat,lon) < 0.01:  # 미터 단위임.
            
                # 원래 부등호 > !!!
            if float(kick_speed) > float(20):  #'5' 라는 값을 수집만 해서 바꾸면 됨.

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

                    # 누적벌점 : 6번, 방지턱 누적벌점 : 10번

                    # 3번 사용자의 정보만 가져오기
                    if (response.json()["m2m:cin"]["con"].split(" ")[0] == "kick@email.com"):

                        penalty = str(int(response.json()["m2m:cin"]["con"].split(" ")[8]) + 1)
                        penalty_sub = str(int(response.json()["m2m:cin"]["con"].split(" ")[10]) + 1)

                        response_list = response.json()["m2m:cin"]["con"].split(" ")
                        response_list[8] = penalty
                        response_list[10] = penalty_sub
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

                        penalty_list = [str(1), str(lat), str(lon)]
                        penalty_str = " ".join(penalty_list)
                        
                        payload = "{\n    \"m2m:cin\": {\n        \"con\" : \""+penalty_str+"\"\n    }\n}"
                        headers = {
                        'Accept': 'application/json',
                        'X-M2M-RI': '12345',
                        'X-M2M-Origin': '{{aei}}',
                        'Content-Type': 'application/vnd.onem2m-res+json; ty=4'
                        }

                        requests.request("POST", penalty_zone_url, headers=headers, data=payload)

                print("warning")
                time.sleep(10)
            else:
                print('normal')


