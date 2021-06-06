import os
import numpy as np
from tqdm import tqdm
import multiprocessing
import subprocess
import re

ontology_name = 'SnomedCT'
#ontology_name = 'HeLis'
depth = 2

seed_dir = 'data/{}/nhs_seed_random_iri/'.format(ontology_name)
ontology_path = 'data/SnomedCT/ontology_202007.owl'
#ontology_path = 'data/HeLis/helis_v1.00.origin.owl'

create_sig_owlfiles_jar = 'Create-sig-owlfiles.jar'
sig_extension_jar = 'sigExtension-ghadah.jar'
tmp_dir = 'temp/'

java_path = r'"C:\Program Files\Java\jdk-14\bin\java.exe"'

ontology_path = os.path.abspath(ontology_path)
create_sig_owlfiles_jar = os.path.abspath(create_sig_owlfiles_jar)
sig_extension_jar = os.path.abspath(sig_extension_jar)
tmp_dir = os.path.abspath(tmp_dir) + '/'
seed_extended_pattern = seed_dir[:-1] + '_extended_{}/{}'

class_pattern = r'Declaration\(Class\(\<(.*?)\>\)\)'

def work(fname):
    print("start")

    result_file = seed_extended_pattern.format(depth, fname)
    result_file = open(result_file, 'w')

    tmp_file = tmp_dir + fname.replace('.', '_{}.'.format(depth), 1)

    iris = np.loadtxt(seed_dir + fname, dtype=str, comments=None, ndmin=1)
    url = "/".join(iris[0].split('/')[:-1]) + '/'
    with open(tmp_file, 'w') as f:
        for item in iris:
            f.write(item.split('/')[-1] + '\n')

    command = '{} -jar {} {} {}'.format(java_path, create_sig_owlfiles_jar, tmp_file, url)
    #print(command)
    ret = subprocess.call(command)
    assert ret == 0

    command = '{} -jar {} {} {} {}'.format(java_path, sig_extension_jar, tmp_file + '_sig_ontology.owl', ontology_path, depth)
    #print(command)
    ret = subprocess.call(command)
    assert ret == 0

    owl_name = tmp_file + '_sig_ontology.owl' + '_extendedSig.owl'
    owl_file = np.loadtxt(owl_name, dtype='str', comments=None, ndmin=1)
    for item in owl_file:
        res = re.findall(class_pattern, item)
        if len(res) > 0:
            result_file.write(res[0] + '\n')

    result_file.close()

    print(fname)

if __name__ == '__main__':
    pool = multiprocessing.Pool(4)
    tasks = os.listdir(seed_dir)[::-1]
    r = pool.map_async(work, tasks)
    r.wait()