from datetime import datetime

import requests
import base64 
import time
import io
import matplotlib.pyplot as plt
import cv2
import numpy as np
import json
url_1 = "http://203.250.148.120:20519/Mobius/kick_off/data/image/la"



payload={}
headers = {
  'Accept': 'application/json',
  'X-M2M-RI': '12345',
  'X-M2M-Origin': 'SOrigin'
}
now = datetime.now()
#연결체크
print("listening!")
a=now.strftime('%H_%M_%S')
#킥보드의 자료형의 데이터를 가져옴

response = requests.request("GET", url_1, headers=headers, data=payload)
data = response.json()["m2m:cin"]["con"]

imgdata=base64.b64decode(data)

filename=str(a)


with open('../inference/images/'+filename+'.jpg', 'wb') as f:
    f.write(imgdata)
    
# # file_binary=io.BytesIO(base64.b64decode(data))
# with open(filename+'.jpg','wb') as file:
#       file.write(io.BytesIO(base64.b64decode(data)))


  
