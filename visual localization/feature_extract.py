#!/usr/bin/env python

'''
Copyright (c) 2021 Stephen Hausler, Sourav Garg, Ming Xu, Michael Milford and Tobias Fischer

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.


Extracts Patch-NetVLAD local and NetVLAD global features from a given directory of images.

Configuration settings are stored in configs folder, with compute heavy performance or light-weight alternatives
available.

Features are saved into a nominated output directory, with one file per image per patch size.

Code is dynamic and can be configured with essentially *any* number of patch sizes, by editing the config files.
'''


import argparse
import configparser
import os
from os.path import join, exists, isfile
from os import makedirs

import time

from tqdm.auto import tqdm
import torch
import torch.nn as nn
from torch.utils.data import DataLoader
import numpy as np

from patchnetvlad.tools.datasets import PlaceDataset
from patchnetvlad.models.models_generic import get_backend, get_model, get_pca_encoding
from patchnetvlad.tools import PATCHNETVLAD_ROOT_DIR


def feature_extract(eval_set, model, device, opt, config):
    if not exists(opt.output_features_dir):
        makedirs(opt.output_features_dir)

    output_local_features_prefix = join(opt.output_features_dir, 'patchfeats')
    output_global_features_filename = join(opt.output_features_dir, 'globalfeats.npy')

    pool_size = int(config['global_params']['num_pcs'])

    test_data_loader = DataLoader(dataset=eval_set, num_workers=int(config['global_params']['threads']),
                                  batch_size=int(config['feature_extract']['cacheBatchSize']),
                                  shuffle=False, pin_memory=(not opt.nocuda))

    model.eval()
    with torch.no_grad():
        tqdm.write('====> Extracting Features')
        db_feat = np.empty((len(eval_set), pool_size), dtype=np.float32)

                        #indices는 그냥 색인의 불가함, 쿼리를 넣었을때는 당연히 1이 나오겠지
        for iteration, (input_data, indices) in \
                enumerate(tqdm(test_data_loader, position=1, leave=False, desc='Test Iter'.rjust(15)), 1):

            indices_np = indices.detach().numpy()
            input_data = input_data.to(device)
            # print(input_data)   #벡터값
            #print(input_data.shape)
            image_encoding = model.encoder(input_data)
            #encoder -> vgg16
            # print(image_encoding)   #벡터값 인코딩한거
            #print(image_encoding.shape)
            if config['global_params']['pooling'].lower() == 'patchnetvlad':
                vlad_local, vlad_global = model.pool(image_encoding)

                vlad_global_pca = get_pca_encoding(model, vlad_global)
                db_feat[indices_np, :] = vlad_global_pca.detach().cpu().numpy()

                for this_iter, this_local in enumerate(vlad_local):
                    this_patch_size = model.pool.patch_sizes[this_iter]

                    db_feat_patches = np.empty((this_local.size(0), pool_size, this_local.size(2)),
                                              dtype=np.float32)
                    grid = np.indices((1, this_local.size(0)))
                    this_local_pca = get_pca_encoding(model, this_local.permute(2, 0, 1).reshape(-1, this_local.size(1))).\
                        reshape(this_local.size(2), this_local.size(0), pool_size).permute(1, 2, 0)
                    db_feat_patches[grid, :, :] = this_local_pca.detach().cpu().numpy()

                    for i, val in enumerate(indices_np):
                        image_name = os.path.splitext(os.path.basename(eval_set.images[val]))[0]
                        filename = output_local_features_prefix + '_' + 'psize{}_'.format(this_patch_size) + image_name + '.npy'
                        np.save(filename, db_feat_patches[i, :, :])
            else:
                vlad_global = model.pool(image_encoding)            #넷블라드!!! model_generic의 add_module부분
                #print(vlad_global)
                #print(vlad_global.shape)#[1,8192] 4096 x 2일텐데..
                vlad_global_pca = get_pca_encoding(model, vlad_global)
                #print(vlad_global_pca)
                #print("here")
                #print(vlad_global_pca.shape) #[4096] pca는 차원 축소시켜주는 역할
                # x가 cuda 디바이스로 올라갔을 때 
                # c = x.numpy() (x) : cuda 디바이스로 올라갔을 때 바로 numpy로 
                # 바꿀 수 없기 때문에 cpu로 보내준후 바꾸어 주어야한다.
                db_feat[indices_np, :] = vlad_global_pca.detach().cpu().numpy()

    np.save(output_global_features_filename, db_feat)


def main():
# custom
# python feature_extract.py \
# --config_path patchnetvlad/configs/performance.ini \
# --dataset_file_path=/home/sejung/Patch-NetVLAD/patchnetvlad/dataset_imagenames/boston_query.txt \
# --dataset_root_dir=/data_disk/sejung_datasets/ryeong_workspace/space_search/Boston/yeah/union \
# --output_features_dir patchnetvlad/output_features/for_indexing_boston_all_query

# index_smap
# python feature_extract.py \
# --config_path patchnetvlad/configs/performance.ini \
# --dataset_file_path=image_names_index_meta.txt \
# --dataset_root_dir=/home/sejung/Patch-NetVLAD/patchnetvlad/image_files_smap/ \
# --output_features_dir patchnetvlad/output_features/first_index

#query_
# python feature_extract.py \
# --config_path patchnetvlad/configs/performance.ini \
# --dataset_file_path=image_names_query.txt \
# --dataset_root_dir=/home/sejung/Patch-NetVLAD/patchnetvlad/image_files \
# --output_features_dir patchnetvlad/output_features/first_query

#query_smap
# python feature_extract.py \
# --config_path patchnetvlad/configs/performance.ini \
# --dataset_file_path=image_names_query.txt \
# --dataset_root_dir=/home/sejung/Patch-NetVLAD/patchnetvlad/image_files_smap \
# --output_features_dir patchnetvlad/output_features/first_query

# 메타몽그레이
#python feature_extract.py \
#--config_path patchnetvlad/configs/performance.ini \
#--dataset_file_path=meta_to_real.txt \
#--dataset_root_dir=/data_disk/space_search/trans_dataset/berlin/train/real \
#--output_features_dir patchnetvlad/output_features/meta_to_real
#########################################################지하주차장 디렉토리###########################################################################
# mobius DB
# python feature_extract.py \
# --config_path patchnetvlad/configs/performance.ini \
# --dataset_file_path=mobius_underdb.txt \
# --dataset_root_dir=/data_disk/home/taein/pyeongjoo/visual_localization/Patch-NetVLAD/mobius/underunion \
# --output_features_dir patchnetvlad/output_features/mobius_underdb

# mobius QUERY
# python feature_extract.py \
# --config_path patchnetvlad/configs/performance.ini \
# --dataset_file_path=mobius_underquery.txt \
# --dataset_root_dir=/data_disk/home/taein/pyeongjoo/visual_localization/Patch-NetVLAD/mobius/underunion \
# --output_features_dir patchnetvlad/output_features/mobius_underquery


##############################################################################이 명령어 입니다.##############################################################################
# mobius DB
# python feature_extract.py \
# --config_path patchnetvlad/configs/performance.ini \
# --dataset_file_path=mobius_db.txt \
# --dataset_root_dir=/data_disk/home/taein/pyeongjoo/visual_localization/Patch-NetVLAD/mobius/union \
# --output_features_dir patchnetvlad/output_features/mobius_db

# mobius QUERY
# python feature_extract.py \
# --config_path patchnetvlad/configs/performance.ini \
# --dataset_file_path=mobius_query.txt \
# --dataset_root_dir=/data_disk/home/taein/pyeongjoo/visual_localization/Patch-NetVLAD/mobius/union \
# --output_features_dir patchnetvlad/output_features/mobius_query
##############################################################################이 명령어 입니다.##############################################################################

    parser = argparse.ArgumentParser(description='Patch-NetVLAD-Feature-Extract')
    parser.add_argument('--config_path', type=str, default=join(PATCHNETVLAD_ROOT_DIR, 'configs/performance.ini'),
                        help='File name (with extension) to an ini file that stores most of the configuration data for patch-netvlad')
    parser.add_argument('--dataset_file_path', type=str, required=True,
                        help='Full path (with extension) to a text file that stores the save location and name of all images in the dataset folder')
    parser.add_argument('--dataset_root_dir', type=str, default='',
                        help='If the files in dataset_file_path are relative, use dataset_root_dir as prefix.')
    parser.add_argument('--output_features_dir', type=str, default=join(PATCHNETVLAD_ROOT_DIR, 'output_features'),
                        help='Path to store all patch-netvlad features')
    parser.add_argument('--nocuda', action='store_true', help='If true, use CPU only. Else use GPU.')

    opt = parser.parse_args()
    # print(opt)
    # print(opt.config_path)
    # print(opt.dataset_file_path)
    # print(opt.dataset_root_dir)
    configfile = opt.config_path    
    assert os.path.isfile(configfile)
    config = configparser.ConfigParser()
    config.read(configfile)

    cuda = not opt.nocuda
    if cuda and not torch.cuda.is_available():
        raise Exception("No GPU found, please run with --nocuda")

    device = torch.device("cuda" if cuda else "cpu")

    encoder_dim, encoder = get_backend()
    # enc_dim = 512
    # enc = models.vgg16(pretrained=True)

    if not os.path.isfile(opt.dataset_file_path):
        opt.dataset_file_path = join(PATCHNETVLAD_ROOT_DIR, 'dataset_imagenames', opt.dataset_file_path)

    dataset = PlaceDataset(None, opt.dataset_file_path, opt.dataset_root_dir, None, config['feature_extract'])

    # must resume to do extraction
    resume_ckpt = config['global_params']['resumePath'] + config['global_params']['num_pcs'] + '.pth.tar'       #모델 불러오기

    # backup: try whether resume_ckpt is relative to PATCHNETVLAD_ROOT_DIR
    if not isfile(resume_ckpt):                                     #처음에 할때 다운로드하는거
        resume_ckpt = join(PATCHNETVLAD_ROOT_DIR, resume_ckpt)
        if not isfile(resume_ckpt):
            from download_models import download_all_models
            download_all_models(ask_for_permission=True)

    if isfile(resume_ckpt):
        print("=> loading checkpoint '{}'".format(resume_ckpt))
        checkpoint = torch.load(resume_ckpt, map_location=lambda storage, loc: storage)
        assert checkpoint['state_dict']['WPCA.0.bias'].shape[0] == int(config['global_params']['num_pcs'])
        config['global_params']['num_clusters'] = str(checkpoint['state_dict']['pool.centroids'].shape[0])

        model = get_model(encoder, encoder_dim, config['global_params'], append_pca_layer=True)             ###모델 선언
        model.load_state_dict(checkpoint['state_dict'])
        #모델을 저장할 때는 두 가지 방법 중 한 방법을 선택할 수 있는데, 모델 전체를 저장하는 방법과 모델의 state_dict만 저장하는 것이다.
        #모델 전체를 저장한다는 것의 의미는 모델 파라미터 뿐만 아니라, 옵티마이저(Optimizer), 에포크, 스코어 등 모든 상태를 저장한다는 것이다.
        #만약 나중에 이어서 학습을 한다던지, 코드에 접근할 권한이 없는 사용자가 모델을 사용할 수 있도록 허락해주고 싶을 때 등의 경우에 사용하는 것이 바람직하다.
        #  모델 전체를 저장하는 만큼, 상대적으로 더 큰 용량을 가지게 된다.

        #Pytorch에서 모델의 state_dict은 학습가능한 매개변수가 담겨있는 딕셔너리(Dictionary)이다.
        #가중치와 편향이 이에 해당한다. 그러나 매개변수 이외에는 정보가 담겨있지 않기 때문에, 코드 상으로 모델이 구현되어 있는 경우에만 로드하는 방법을 통해 사용할 수 있다.
        #state_dict만 저장하면 파일의 용량이 가벼워진다는 장점이 있다.
        #print("모델", model)
        #print("중간점")
        #print("checkpoint", checkpoint['state_dict'])
        
        if int(config['global_params']['nGPU']) > 1 and torch.cuda.device_count() > 1:
            model.encoder = nn.DataParallel(model.encoder)
            # if opt.mode.lower() != 'cluster':
            model.pool = nn.DataParallel(model.pool)

       
        model = model.to(device)
        print("=> loaded checkpoint '{}'".format(resume_ckpt, ))
    else:
        raise FileNotFoundError("=> no checkpoint found at '{}'".format(resume_ckpt))

    start = time.time()
    feature_extract(dataset, model, device, opt, config)
    end = time.time()
    print(" Extracting time interval is")
    print(f"{end - start:.10f} sec")

    torch.cuda.empty_cache()  # garbage clean GPU memory, a bug can occur when Pytorch doesn't automatically clear the
    # memory after runs
    print('\n\nDone. Finished extracting and saving features')


if __name__ == "__main__":
    main()
