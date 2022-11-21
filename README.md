# KICK_OFF

### 1. detect 
####   - School_zone_detect.py
####  - Speed_bump_detect.py
####   - Sudden_stop_detect.py
####   - Analysis_rating.py

### 2. WEB
1) execute web/src/admin.html

 
###  3. Device (Raspberry pi)
1) activate conda environment
2) python Upload_cam.py
3) python upload_gps.py
4) python upload_gyro.py

###  4. APP
1) download all file in app folder

###  5. Risk Detection (YOLOv5) 
1) activate conda environment
2_1) cd yolov5_pothole/get_image_Folder
2_2) python integrated.py
3_1) cd yolov5_speedbump/get_image_Folder
3_2) python integrated.py

###  6. Parking Check (Visual localization) 
1) activate conda environment
2) python Visual_localization.py
