# 설명 project_3 에 있는 picturesave에 있는 이미지 전송하는 코드

import base64
import json
from time import sleep 



import time
import os
import sys
import picamera
import time
import datetime
import requests
import json

url = "YourserverUrl/Mobius/kick/image"

savepath='/home/pi/project_3/picturesave'

ID="MFBE29"

# 저장된 폴더에 이미지 삭제하는 함수.
def delete_All_files(filePath):           
    if os.path.exists(filePath):
        for file in os.scandir(filePath):
            os.remove(file.path)
            
def kickcameraCapture():
    with picamera.PiCamera() as camera:
        now=datetime.datetime.now()
        filename=now.strftime('%m%d-%H%M%S')
        # camera.resolution = (1024, 768)   # You can change image size
        camera.resolution = (800, 600)   
        camera.start_preview()
        camera.stop_preview()
        camera.capture(output=savepath+'/'+filename+'.jpg')


url = "YourserverUrl/Mobius/kick/image"

while True:

    delete_All_files(savepath)

    kickcameraCapture()
    
    payload = {}
    payload["m2m:cin"]={}   

    file_directory=os.listdir("./project_3/picturesave/")
    myfile='filename.jpg'
    for filename in file_directory:

        if filename == myfile:
            continue


        with open('./project_3/picturesave/'+filename, 'rb') as img:
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
        print('do')

            