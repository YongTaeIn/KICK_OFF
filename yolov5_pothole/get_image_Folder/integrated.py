# <코드 설명>
#  1. 디텍트 된 이미지를  인코딩해서 mobiust_server_
#     pot_hole_detected_image  에 저장됨
#
#  2. runs/detect/label
#     runs/detect/result
#     에 있는 파일을 비움.


from time import sleep 
from datetime import datetime

import subscribe
import base64
import json
import io
import time
import os
import sys
import time
import datetime
import requests
import json
import numpy as np
#import cv2


url = "http://203.250.148.120:20519/Mobius/kick_off/map/pothole/image"
url_1 = "http://203.250.148.120:20519/Mobius/kick_off/data/image/la"
url_2 = "http://203.250.148.120:20519/Mobius/kick_off/data/gps?fu=1&ty=4"
url_3 = "http://203.250.148.120:20519/Mobius/kick_off/map/pothole/gps"
url_4="http://203.250.148.120:20519/Mobius/kick_off/data/gps/la"
def DeleteAllFiles(filePath):
    if os.path.exists(filePath):
        for file in os.scandir(filePath):
            os.remove(file.path)
        return 'Remove All File'
    else:
        return 'Directory Not found'            


# mobius에서 raw_image를 받아오는 함수
def get_image():
    payload={}
    headers = {
    'Accept': 'application/json',
    'X-M2M-RI': '12345',
    'X-M2M-Origin': 'SOrigin'
    }
    #now = datetime.now()
    #연결체크
    print("listening!")
    a="test"
    #a=now.strftime('%H_%M_%S')
    #킥보드의 자료형의 데이터를 가져옴

    response = requests.request("GET", url_1, headers=headers, data=payload)
    data = response.json()["m2m:cin"]["con"]

    imgdata=base64.b64decode(data)

    filename=str(a)


    with open('../inference/images/'+filename+'.jpg', 'wb') as f:
        f.write(imgdata)    
        
    print("finsh get_image")
# 120서버에서 디텍트된 이미지를 모비우스에 전송하는 함수
def send_image_2_mobius():
    payload = {}
    payload["m2m:cin"]={}   

    file_directory=os.listdir('../runs/detect/result/')
    myfile='filename.jpg'
    for filename in file_directory:

        if filename == myfile:
            continue


        with open('../runs/detect/result/'+filename, 'rb') as img:
            image_read = base64.b64encode(img.read())
        image_read = image_read.decode('utf-8')
        payload["m2m:cin"]["con"]=image_read
        payload = json.dumps(payload)
        
        headers = {
        'Accept': 'application/json',
        'X-M2M-RI': '12345',
        'X-M2M-Origin': '{{aei}}',
        'Content-Type': 'application/vnd.onem2m-res+json; ty=4'
        }
        response = requests.request("POST", url, headers=headers, data=payload)
        

    
    DeleteAllFiles('../runs/detect/result/')
    DeleteAllFiles('../runs/detect/label/')
    

def get_gps_time():


    payload={}
    headers = {
    'Accept': 'application/json',
    'X-M2M-RI': '12345',
    'X-M2M-Origin': 'SOrigin'
    }

    response = requests.request("GET", url_2, headers=headers, data=payload)

    a=response.text
    k=a[13:-2].replace('"',"").split(",")
    gpsurl=url_4+k[-10]

    response = requests.request("GET", url_4, headers=headers, data=payload)
    data = response.json()["m2m:cin"]["con"]
    
    data=data.split(" ")
    return data[1]+" "+data[2]
    
def send_gps(data):    
    #url_3 = "http://203.253.128.161:7579/Mobius/kick/pot_hole"
    payload = "{\n    \"m2m:cin\": {\n        \"con\": \"%s\"\n    }\n}" %(data)
    headers = {
    'Accept': 'application/json',
    'X-M2M-RI': '12345',
    'X-M2M-Origin': '{{aei}}',
    'Content-Type': 'application/vnd.onem2m-res+json; ty=4'
    }
    response = requests.request("POST", url_3, headers=headers, data=payload)


#export MKL_SERVICE_FORCE_INTEL=1


while True:
    
    # 모비우스에서 이미지 가져옴
    get_image()
    print("complete_pull_image")
    
    # 디텍션 코드 실행
    os.environ['MKL_THREADING_LAYER']='GNU'
    os.system( 'python ../detect.py --source ../inference/images/test.jpg --weights ../runs/train/exp22/weights/best.pt --conf 0.4 ')
    
    file_directory=os.listdir('../runs/detect/result/')
    print(file_directory)
 
    if file_directory !=[]: 
        
        #디텍션 됐을 때 gps전송함.
        data=get_gps_time()
        send_gps(data)
        print("complete_detect")
        time.sleep(5)  # 5초 정도 후에  다시 디텍션 돌아가도록
    
    
    DeleteAllFiles('../inference/images/')
    # 디텍션 된 결과물 모비우스로 전송
    send_image_2_mobius()
    print("complete_push_image")

