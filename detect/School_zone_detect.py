## 어린이 보호구역(자이로 사용)
## 2022/10/02
## az의 기준치를 정하면 끝.(직접 타면서 테스트 해야함.)


import requests
from math import sin, cos, sqrt, atan2, radians
import time

#get mobious data
def getdata(url):
    response = requests.request("GET", url, headers=headers, data=payload)
    lst=response.text.split(":")
    for i in range(0,len(lst)):
        if "con" in lst[i]:
            lst[i+1]=lst[i+1].replace('}','').replace('"','')

            
            if kick_id in lst[i+1]:
                
                return str(lst[i+1])

def schooldata(url):
    response = requests.request("GET", url, headers=headers, data=payload)
    lst=response.text.split(":")
    for i in range(0,len(lst)):
        if "con" in lst[i]:
            lst[i+1]=lst[i+1].replace('}','').replace('"','')
            return str(lst[i+1])


# 스쿨존 과 킥보드 간 거리 구하는 함수
def lat_long_dist(lat1,lon1,lat2,lon2):
    # 
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

while(1) :
    url1 = "http://203.250.148.120:20519/Mobius/kick_off/data/gps/la"

    url2 = "http://203.250.148.120:20519/Mobius/kick_off/map/school_zone/gps/4-20221116015811529"



    kick_id="MFBE29"
    #heder and payload
    payload={}
    headers = {
        'Accept': 'application/json',
        'X-M2M-RI': '12345',
        'X-M2M-Origin': 'SOrigin'
    }

    ##while True:
        
    school_zone=schooldata(url2).split(" ")
    gps_list=getdata(url1).split(" ")

    #실시간 킥보드 데이터
    lat=gps_list[1]   
    lon=gps_list[2]
    kick_speed=gps_list[3]
    kick_speed=float(kick_speed)*3600
    #kick_speed=gps_list[3]*3600


    school_lat_1=school_zone[0]
    school_lon_1=school_zone[1]


    distance_0=lat_long_dist(lat,lon,school_lat_1,school_lon_1)



    # print(distance_0)
    # <
    if distance_0 < float(300/1000):  # 학교 정문(출입문) 과의 거리 300m
        # >
        if float(kick_speed) > float(20):   # 킥보드의 속도 15 km/h 보다 
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

            # print(ID)


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

                # 누적벌점 : 8번, 급정거 누적벌점 : 11번

                # 3번 사용자의 정보만 가져오기
                if (response.json()["m2m:cin"]["con"].split(" ")[0] == "kick@email.com"):
                    print("3번 사용자")

                    penalty = str(int(response.json()["m2m:cin"]["con"].split(" ")[8]) + 1)
                    penalty_sub = str(int(response.json()["m2m:cin"]["con"].split(" ")[11]) + 1)

                    response_list = response.json()["m2m:cin"]["con"].split(" ")
                    response_list[8] = penalty
                    response_list[11] = penalty_sub
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

                    penalty_list = [str(2), str(lat), str(lon)]
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
            print("normal")
            print()
