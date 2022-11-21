import os

# path="/home/sejung/Patch-NetVLAD/patchnetvlad/image_files"
path="/data_disk/home/taein/pyeongjoo/visual_localization/Patch-NetVLAD/mobius/query" ########## img_file_dir path
file_list=os.listdir(path)

for i in file_list:
    print(i)
    # f = open("patchnetvlad/dataset_imagenames/image_names_index.txt", 'a')
    f = open("/data_disk/home/taein/pyeongjoo/visual_localization/Patch-NetVLAD/patchnetvlad/dataset_imagenames/mobius_query.txt", 'a') ######## where to write file
    f.write(i+'\n')

f.close()


# 목적 디렉토리 : /data_disk/home/taein/pyeongjoo/visual_localization/Patch-NetVLAD/patchnetvlad/dataset_imagenames