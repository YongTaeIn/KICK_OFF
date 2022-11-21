# path내에 모든 파일 삭제함.
# 명령어   python jpg_delete.py



import os 
path='../inference/images/'

def DeleteAllFiles(filePath):
    if os.path.exists(filePath):
        for file in os.scandir(filePath):
            os.remove(file.path)
        return 'Remove All File'
    else:
        return 'Directory Not found'
    
    
    
    

DeleteAllFiles(path)

