from math import sin, cos, sqrt, atan2, radians

import os
import requests
import time
import json
import base64
import datetime

#############################start with conda
#conda activate patchnetvlad
###############################
#urls
kickurl = "Your server Url/Mobius/kick_off/data/gps/la"
url_1 = "Your server Url/Mobius/kick_off/data/image/la"
parkingreturn="Your server Url/Mobius/kick_off/map/parking_space/check/la"
url_send_parkingdata="Your server Url/Mobius/kick_off/map/parking_space/result"
url_change_check="Your server Url/Mobius/kick_off/map/parking_space/check"
url_change_result="Your server Url/Mobius/kick_off/map/parking_space/result"

sendimageurl="Your server Url/Mobius/kick_off/map/parking_space/image"
sendparkgps="Your server Url/Mobius/kick_off/map/parking_space/gps"

gunja_parking='37.549940 127.073648'

def getgps(url):
    payload={}
    headers = {
    'Accept': 'application/json',
    'X-M2M-RI': '12345',
    'X-M2M-Origin': 'SOrigin'
    }

    response = requests.request("GET", url, headers=headers, data=payload)
    data = response.json()["m2m:cin"]["con"]
    return data


def get_image():
    payload={}
    headers = {
    'Accept': 'application/json',
    'X-M2M-RI': '12345',
    'X-M2M-Origin': 'SOrigin'
    }

    now=datetime.datetime.now()
    a=now.strftime('%m%d-%H%M%S')


    response = requests.request("GET", url_1, headers=headers, data=payload)
    data = response.json()["m2m:cin"]["con"]

    imgdata=base64.b64decode(data)

    filename=str(a)

    with open('/data_disk/home/taein/pyeongjoo/visual_localization/Patch-NetVLAD/mobius/query/'+filename+'.jpg', 'wb') as f:
        f.write(imgdata)    
        
    with open('/data_disk/home/taein/pyeongjoo/visual_localization/Patch-NetVLAD/mobius/union/'+filename+'.jpg', 'wb') as f:
        f.write(imgdata)
        
    with open('/data_disk/home/taein/pyeongjoo/visual_localization/Patch-NetVLAD/mobius/checkdata/'+filename+'.jpg', 'wb') as f:
        f.write(imgdata)
        
    print("finsh get_image")

def gen_file_list():
    path="/data_disk/home/taein/pyeongjoo/visual_localization/Patch-NetVLAD/mobius/query" ########## img_file_dir path
    file_list=os.listdir(path)

    for i in file_list:

        f = open("/data_disk/home/taein/pyeongjoo/visual_localization/Patch-NetVLAD/patchnetvlad/dataset_imagenames/mobius_query.txt", 'a') ######## where to write file
        f.write(i+'\n')

    f.close()

def image_check():
    os.environ['MKL_THREADING_LAYER']='GNU'

    ##extract qurey file feature
    os.system( 'python /data_disk/home/taein/pyeongjoo/visual_localization/Patch-NetVLAD/feature_extract.py \
    --config_path patchnetvlad/configs/performance.ini \
    --dataset_file_path=mobius_query.txt \
    --dataset_root_dir=/data_disk/home/taein/pyeongjoo/visual_localization/Patch-NetVLAD/mobius/union \
    --output_features_dir patchnetvlad/output_features/mobius_query')

    #match feature
    os.system( 'python /data_disk/home/taein/pyeongjoo/visual_localization/Patch-NetVLAD/feature_match.py \
    --config_path patchnetvlad/configs/performance.ini \
    --dataset_root_dir=/data_disk/home/sejung/Patch-NetVLAD/patchnetvlad/mobius/union \
    --query_file_path=mobius_query.txt \
    --index_file_path=mobius_db.txt \
    --query_input_features_dir patchnetvlad/output_features/mobius_query \
    --index_input_features_dir patchnetvlad/output_features/mobius_db \
    --result_save_folder patchnetvlad/results/mobius_test_result')
    
    
    
    
def erase_finish_data():
    file_list = os.listdir('/data_disk/home/taein/pyeongjoo/visual_localization/Patch-NetVLAD/mobius/query')

    #erase capture camera
    os.remove('/data_disk/home/taein/pyeongjoo/visual_localization/Patch-NetVLAD/mobius/query/'+file_list[0])
    os.remove('/data_disk/home/taein/pyeongjoo/visual_localization/Patch-NetVLAD/mobius/union/'+file_list[0])
    os.remove('/data_disk/home/taein/pyeongjoo/visual_localization/Patch-NetVLAD/mobius/checkdata/'+file_list[0])
    os.remove('/data_disk/home/taein/pyeongjoo/visual_localization/Patch-NetVLAD/patchnetvlad/dataset_imagenames/mobius_query.txt')

def get_return():
    payload={}
    headers = {
    'Accept': 'application/json',
    'X-M2M-RI': '12345',
    'X-M2M-Origin': 'SOrigin'
    }

    response = requests.request("GET", parkingreturn, headers=headers, data=payload)
    data = response.json()["m2m:cin"]["con"]
    return data

def send_mobious(url,data):    

    payload = "{\n    \"m2m:cin\": {\n        \"con\": \"%s\"\n    }\n}" %(data)
    headers = {
    'Accept': 'application/json',
    'X-M2M-RI': '12345',
    'X-M2M-Origin': '{{aei}}',
    'Content-Type': 'application/vnd.onem2m-res+json; ty=4'
    }
    response = requests.request("POST", url, headers=headers, data=payload)

def send_image():
    payload = {}
    payload["m2m:cin"]={}   

    file_directory=os.listdir('/data_disk/home/taein/pyeongjoo/visual_localization/Patch-NetVLAD/mobius/checkdata/')
    myfile='filename.jpg'
    for filename in file_directory:

        if filename == myfile:
            continue


        with open('/data_disk/home/taein/pyeongjoo/visual_localization/Patch-NetVLAD/mobius/checkdata/'+filename, 'rb') as img:
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
        response = requests.request("POST",sendimageurl , headers=headers, data=payload)




while True: 
    val=get_return().find("OK")
    if val==-1:
        print('Nonstop')
        DATA='Waiting'
        send_mobious(url_change_check,DATA)

        time.sleep(5)

        
    else:
        print('OK')
        ##### do image parking
        #################################################image check

        get_image()

        gen_file_list()

        image_check()

        ###############################parkingcheck

        f = open("/data_disk/home/taein/pyeongjoo/visual_localization/Patch-NetVLAD/patchnetvlad/results/mobius_test_result/NetVLAD_predictions.txt", 'r')
        lines = f.readlines()
        f.close()

        val=lines[2].find("park")
        if val==-1:
            ##### if not in parking lot
            print('parking none')
            DATA='parking out'
            send_mobious(url_send_parkingdata,DATA)
            DATA='FINISH'
            send_mobious(url_change_check,DATA)

            
            
        else:
            ##### in the parking lot
            print('parking good')
            DATA='SUCCESS GROUND 37.549940 127.073648'
            send_mobious(url_send_parkingdata,DATA)
            DATA='FINISH'
            send_mobious(url_change_check,DATA)

            send_image()

        erase_finish_data()
        
