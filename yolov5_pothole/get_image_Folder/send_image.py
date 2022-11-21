# <코드 설명>
#  1. 디텍트 된 이미지를  인코딩해서 mobiust_server_
#     pot_hole_detected_image  에 저장됨
#
#  2. runs/detect/label
#     runs/detect/result
#     에 있는 파일을 비움.


import base64
import json
from time import sleep 

import time
import os
import sys
import time
import datetime
import requests
import json

#url = "http://203.253.128.161:7579/Mobius/kick_off/map/pothole/image"
url = "http://203.250.148.120:20519/Mobius/kick_off/data/image"
  
def DeleteAllFiles(filePath):
    if os.path.exists(filePath):
        for file in os.scandir(filePath):
            os.remove(file.path)
        return 'Remove All File'
    else:
        return 'Directory Not found'            



while True:

    

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
        print("23")

    
    DeleteAllFiles('../runs/detect/result/')
    DeleteAllFiles('../runs/detect/label/')
            