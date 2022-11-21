#!/usr/bin/env python

'''
MIT License

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

Performs place recognition using a two-stage image retrieval pipeline, where
the first step collects the top 100 database candidates and then geometric
verification produces the top 1 best match for every query.

Requires feature_extract.py to be run first, on both a folder of index/database
images and a folder of query images.

Code already supports the datasets of Nordland, Pittsburgh 30k and Tokyo247,
please run tools/genImageListFile to create new imageNames files with your
filepaths pointing to where you saved these datasets (or, edit the text files
to remove the prefix and insert your own prefix).
'''

from __future__ import print_function

import time

import os
import argparse
import configparser
from os.path import join
from os.path import exists
import torch
import numpy as np
import faiss
from tqdm.auto import tqdm

from patchnetvlad.tools.datasets import PlaceDataset
from patchnetvlad.models.local_matcher import local_matcher
from patchnetvlad.tools import PATCHNETVLAD_ROOT_DIR


def compute_recall(gt, predictions, numQ, n_values, recall_str=''):
    # global_recalls = compute_recall(gt, predictions, eval_set.numQ, n_values, 'NetVLAD')
    # local_recalls = compute_recall(gt, reranked_predictions, eval_set.numQ, n_values, 'PatchNetVLAD')
    correct_at_n = np.zeros(len(n_values))
    for qIx, pred in enumerate(predictions):
        for i, n in enumerate(n_values):
            # if in top N then also in top NN, where NN > N
            if np.any(np.in1d(pred[:n], gt[qIx])):
                correct_at_n[i:] += 1
                break
    recall_at_n = correct_at_n / numQ
    all_recalls = {}  # make dict for output
    for i, n in enumerate(n_values):
        all_recalls[n] = recall_at_n[i]
        tqdm.write("====> Recall {}@{}: {:.4f}".format(recall_str, n, recall_at_n[i]))
    return all_recalls


# def write_kapture_output(opt, eval_set, predictions, outfile_name):
#     if not exists(opt.result_save_folder):
#         os.mkdir(opt.result_save_folder)
#     outfile = join(opt.result_save_folder, outfile_name)
#     print('Writing results to', outfile)
#     with open(outfile, 'w') as kap_out:
#         # kap_out.write('# kapture format: 1.0\n')
#         # kap_out.write('# query_image, map_image\n')       ##이 부분 수정하여 바로 파싱할 수 있도록
#         image_list_array = np.array(eval_set.images) #여기다!!!!!!!!
#         print(len(predictions))
#         for q_idx in range(len(predictions)):
#             full_paths = image_list_array[predictions[q_idx]]
#             # query_full_path = image_list_array[eval_set.numDb + q_idx]
#             for ref_image_name in full_paths:
#                 # kap_out.write(ref_image_name)
#                 # kap_out.write(query_full_path + ', ' + ref_image_name + '\n')
#                 path = ref_image_name
#                 path_parsing = path.split("test_image")
#                 a = path_parsing[1].split("_")
#                 b = a[1].split(("."))
#                 gogo = a[0]+","+b[0]
#                 kap_out.write(gogo)

def write_kapture_output(opt, eval_set, predictions, outfile_name):
    if not exists(opt.result_save_folder):
        os.mkdir(opt.result_save_folder)
    outfile = join(opt.result_save_folder, outfile_name)
    print('Writing results to', outfile)
    with open(outfile, 'w') as kap_out:
        kap_out.write('# kapture format: 1.0\n')
        kap_out.write('# query_image, map_image\n')
        image_list_array = np.array(eval_set.images)
        for q_idx in range(len(predictions)):
            print(len(image_list_array))
            full_paths = image_list_array[predictions[q_idx]]
            query_full_path = image_list_array[eval_set.numDb + q_idx]
            for ref_image_name in full_paths:
                kap_out.write(query_full_path.split('/')[-1] + ', ' + ref_image_name.split('/')[-1] + '\n')


def write_recalls_output(opt, recalls_netvlad, recalls_patchnetvlad, n_values):
    if not exists(opt.result_save_folder):
        os.mkdir(opt.result_save_folder)
    outfile = join(opt.result_save_folder, 'recalls.txt')
    print('Writing recalls to', outfile)
    with open(outfile, 'w') as rec_out:
        for n in n_values:
            rec_out.write("Recall {}@{}: {:.4f}\n".format('NetVLAD', n, recalls_netvlad[n]))
        for n in n_values:
            rec_out.write("Recall {}@{}: {:.4f}\n".format('PatchNetVLAD', n, recalls_patchnetvlad[n]))


def feature_match(eval_set, device, opt, config):
    #input_query_local_features_prefix = join(opt.query_input_features_dir, 'patchfeats')
    input_query_global_features_prefix = join(opt.query_input_features_dir, 'globalfeats.npy')
    #input_index_local_features_prefix = join(opt.index_input_features_dir, 'patchfeats')
    input_index_global_features_prefix = join(opt.index_input_features_dir, 'globalfeats.npy')

    qFeat = np.load(input_query_global_features_prefix)
    pool_size = qFeat.shape[1]      #4096
    dbFeat = np.load(input_index_global_features_prefix)

    if dbFeat.dtype != np.float32:
        qFeat = qFeat.astype('float32')
        dbFeat = dbFeat.astype('float32')

    tqdm.write('====> Building faiss index')
    faiss_index = faiss.IndexFlatL2(pool_size)
    # noinspection PyArgumentList
    faiss_index.add(dbFeat)

    n_values = []
    for n_value in config['feature_match']['n_values_all'].split(","):  # remove all instances of n that are bigger than maxK
        n_values.append(int(n_value))

    if config['feature_match']['pred_input_path'] != 'None':
        predictions = np.load(config['feature_match']['pred_input_path'])  # optionally load predictions from a np file
    else:
        if opt.ground_truth_path and 'tokyo247' in opt.ground_truth_path:
            print('Tokyo24/7: Selecting only one of the 12 cutouts per panorama')
            # followed nnSearchPostprocess in https://github.com/Relja/netvlad/blob/master/datasets/dbTokyo247.m
            # noinspection PyArgumentList
            _, predictions = faiss_index.search(qFeat, max(n_values) * 12)  # 12 cutouts per panorama
            predictions_new = []
            for qIx, pred in enumerate(predictions):
                _, idx = np.unique(np.floor(pred / 12).astype(np.int), return_index=True)
                pred = pred[np.sort(idx)]
                pred = pred[:max(n_values)]
                predictions_new.append(pred)
            predictions = np.array(predictions_new)
            print("here?")
            #여긴 안옴
        else:
            # noinspection PyArgumentList
            #print("qFeat : " ,qFeat)
            #qFeat_shape : (1, 4096)            len(qFeat) : 1              #4096의 의미는 num_pcs(한 이미지당 벡터의 개수)
            #print("dbFeat : ", dbFeat)                                      #mapillary_WPCA4096을 썼으니 4096개와 비교
           #dbFeat_shape : (7158, 4096)         len(dbFeat) : 7158          #4096의 의미는 num_pcs(한 이미지당 벡터의 개수)
            start = time.time()
            #_, predictions = faiss_index.search(qFeat, min(len(qFeat), max(n_values)))      #유사도를 n_values에 있는 최대값 만큼 띄울순 있으나 쿼리 이미지가 1개니까 1개만 띄움
            _, predictions = faiss_index.search(qFeat, 1)
            end = time.time()
            print(" Extracting time interval is")
            print(f"{end - start:.10f} sec")
            #_, predictions = faiss_index.search(qFeat, 50)
            print("distance : ", _)
            print("index_num : ", predictions)
            # _는 distance이고 prediction은 예측값

    # reranked_predictions = local_matcher(predictions, eval_set, input_query_local_features_prefix,
    #                                      input_index_local_features_prefix, config, device)

    # save predictions to files - Kapture Output
    write_kapture_output(opt, eval_set, predictions, 'NetVLAD_predictions.txt')
    #write_kapture_output(opt, eval_set, reranked_predictions, 'PatchNetVLAD_predictions.txt')  

#######################################################이름변경################################################
    print('Finished matching features.')

    # for each query get those within threshold distance
    if opt.ground_truth_path is not None:
        print('Calculating recalls using ground truth.')
        #gt = eval_set.get_positives()

        #global_recalls = compute_recall(gt, predictions, eval_set.numQ, n_values, 'NetVLAD')
        #local_recalls = compute_recall(gt, reranked_predictions, eval_set.numQ, n_values, 'PatchNetVLAD')

        #write_recalls_output(opt, global_recalls, local_recalls, n_values)
    else:
        print('No ground truth was provided; not calculating recalls.')


def main():
#     python feature_match.py \
#   --config_path patchnetvlad/configs/performance.ini \
#   --dataset_root_dir=/data_disk/sejung_datasets/ryeong_workspace/space_search/Boston/yeah/union \
#   --query_file_path=boston_query.txt \
#   --index_file_path=boston.txt \
#   --query_input_features_dir patchnetvlad/output_features/for_indexing_boston_all_query \
#   --index_input_features_dir patchnetvlad/output_features/for_indexing_boston_all \
#   --result_save_folder patchnetvlad/results/boston_first

# 메타버스용
#     python feature_match.py \
#   --config_path patchnetvlad/configs/performance.ini \
#   --dataset_root_dir=/home/sejung/Patch-NetVLAD/patchnetvlad/image_files_smap \
#   --query_file_path=image_names_query.txt \
#   --index_file_path=image_names_index_meta.txt \
#   --query_input_features_dir patchnetvlad/output_features/first_query \
#   --index_input_features_dir patchnetvlad/output_features/first_index \
#   --result_save_folder patchnetvlad/results/maching_output1


#   --ground_truth_path patchnetvlad/dataset_gt_files/pitts30k_test.npz \



#우리데이터셋 학습 + 세종데이터셋 서비스
#     python feature_match.py \
#   --config_path patchnetvlad/configs/performance.ini \
#   --dataset_root_dir=/home/sejung/Patch-NetVLAD/patchnetvlad/image_files \
#   --query_file_path=image_names_query.txt \
#   --index_file_path=image_names_index.txt \
#   --query_input_features_dir patchnetvlad/output_features/space_query_first_try \
#   --index_input_features_dir patchnetvlad/output_features/space_search_first_try \
#   --result_save_folder patchnetvlad/results/space_search_result

#매플러리 프리트레인드 모델 + 세종데이터셋 서비스
#     python feature_match.py \
#   --config_path patchnetvlad/configs/performance.ini \
#   --dataset_root_dir=/home/sejung/Patch-NetVLAD/patchnetvlad/image_files \
#   --query_file_path=image_names_query.txt \
#   --index_file_path=image_names_index.txt \
#   --query_input_features_dir patchnetvlad/output_features/mapillary_query_first_try \
#   --index_input_features_dir patchnetvlad/output_features/mapillary_search_first_try \
#   --result_save_folder patchnetvlad/results/mapillary_search_result

#메타몽 그레이
#     python feature_match.py \
#   --config_path patchnetvlad/configs/performance.ini \
#   --dataset_root_dir=/home/sejung/Patch-NetVLAD/dataset_meta/aroundsejong/color \
#   --query_file_path=metamong_color_query.txt \
#   --index_file_path=metamong_color.txt \
#   --query_input_features_dir patchnetvlad/output_features/metamong_color_query \
#   --index_input_features_dir patchnetvlad/output_features/metamong_color_index \
#   --result_save_folder patchnetvlad/results/metametamong_color
##############################################################################지하주차장 테스트코드 ##############################################################################
# Mobius_test
#     python feature_match.py \
#   --config_path patchnetvlad/configs/performance.ini \
#   --dataset_root_dir=/data_disk/home/sejung/Patch-NetVLAD/patchnetvlad/mobius/underunion \
#   --query_file_path=mobius_underquery.txt \
#   --index_file_path=mobius_underdb.txt \
#   --query_input_features_dir patchnetvlad/output_features/mobius_underquery \
#   --index_input_features_dir patchnetvlad/output_features/mobius_underdb \
#   --result_save_folder patchnetvlad/results/mobius_test_underresult




##############################################################################이 명령어 입니다.##############################################################################
# Mobius_test
#     python feature_match.py \
#   --config_path patchnetvlad/configs/performance.ini \
#   --dataset_root_dir=/data_disk/home/sejung/Patch-NetVLAD/patchnetvlad/mobius/union \
#   --query_file_path=mobius_query.txt \
#   --index_file_path=mobius_db.txt \
#   --query_input_features_dir patchnetvlad/output_features/mobius_query \
#   --index_input_features_dir patchnetvlad/output_features/mobius_db \
#   --result_save_folder patchnetvlad/results/mobius_test_result
##############################################################################이 명령어 입니다.##############################################################################

    parser = argparse.ArgumentParser(description='Patch-NetVLAD-Feature-Match')
    parser.add_argument('--config_path', type=str, default=join(PATCHNETVLAD_ROOT_DIR, 'configs/performance.ini'),
                        help='File name (with extension) to an ini file that stores most of the configuration data for patch-netvlad')
    parser.add_argument('--dataset_root_dir', type=str, default='',
                        help='If the files in query_file_path and index_file_path are relative, use dataset_root_dir as prefix.')
    parser.add_argument('--query_file_path', type=str, required=True,
                        help='Path (with extension) to a text file that stores the save location and name of all query images in the dataset')
    parser.add_argument('--index_file_path', type=str, required=True,
                        help='Path (with extension) to a text file that stores the save location and name of all database images in the dataset')
    parser.add_argument('--query_input_features_dir', type=str, required=True,
                        help='Path to load all query patch-netvlad features')
    parser.add_argument('--index_input_features_dir', type=str, required=True,
                        help='Path to load all database patch-netvlad features')
    parser.add_argument('--ground_truth_path', type=str, default=None,
                        help='Path (with extension) to a file that stores the ground-truth data')
    parser.add_argument('--result_save_folder', type=str, default='results')
    parser.add_argument('--nocuda', action='store_true', help='If true, use CPU only. Else use GPU.')

    opt = parser.parse_args()
    print(opt)

    configfile = opt.config_path
    assert os.path.isfile(configfile)
    config = configparser.ConfigParser()
    config.read(configfile)

    cuda = not opt.nocuda
    if cuda and not torch.cuda.is_available():
        raise Exception("No GPU found, please run with --nocuda")

    device = torch.device("cuda" if cuda else "cpu")

    if not os.path.isfile(opt.query_file_path):
        opt.query_file_path = join(PATCHNETVLAD_ROOT_DIR, 'dataset_imagenames', opt.query_file_path)
    if not os.path.isfile(opt.index_file_path):
        opt.index_file_path = join(PATCHNETVLAD_ROOT_DIR, 'dataset_imagenames', opt.index_file_path)

    dataset = PlaceDataset(opt.query_file_path, opt.index_file_path, opt.dataset_root_dir, opt.ground_truth_path, config['feature_extract'])

    feature_match(dataset, device, opt, config)

    torch.cuda.empty_cache()  # garbage clean GPU memory, a bug can occur when Pytorch doesn't automatically clear the
                              # memory after runs
    print('Done')


if __name__ == "__main__":
    main()