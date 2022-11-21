## this is for explaning code_made by  Taein Yong


# 1.사진판단 코드 
#   터미널 디렉토리 : (taein_ver_2) taein@aisl3:~/kick_board/yolov5$
#   명령어         :  python detect.py --source ./inference/images/test.jpg --weights runs/train/exp22/weights/best.pt --conf 0.4



# 2. 실행된 파일 저장되는 디렉토리
#    runs/detect/result
#



# 3. 모델에 돌려보고 싶은 사진 디렉토리
#    inference/images/
#



# 4.  mobius 서버에서 인코딩값을 가져와서 디코딩으로 이미지 받는 코드 
#    디렉토리: get_image_Folder/get_image.py
#    명령어:   python get_image.py



# 5. path내에 모든 파일 삭제하는 코드
#    디렉토리  : get_image_Folder/jpg_delete.py
#    명령어    : python jpg_delete.py



# 6. 120서버에서 pothole_detected_image 로 인코딩해서 자정하는 파일.
#    디렉토리  : get_image_Folder/send_image.py 
#    명령어    : python send_image.py



# 7. Final_이거 명령어 하나면 끝.
#    디렉토리  : get_image_Folder/integrated.py
#    명령어    : python integrated.py




last update version 2022_11_12