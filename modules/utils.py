import os
import pandas as pd
import numpy as np
import random
import time
from tqdm import tqdm
from scipy.spatial.distance import cdist

concept_embed_file = 'data/SnomedCT/concept_embedding.csv'
concept_iri_file = 'data/SnomedCT/concept_iri.csv'
concept_label_file = 'data/SnomedCT/concept_label.csv'

refset_txt_dir = 'data/SnomedCT/nrc_refset_iri/'
refset_iri_dir = 'data/SnomedCT/nrc_refset_iri/'
refset_label_dir = 'data/SnomedCT/refset_label/'
refset_embed_dir = 'data/SnomedCT/refset_embed/'
seed_random_iri_dir = 'data/SnomedCT/nrc_seed_random_iri/'
seed_select_iri_dir = 'data/SnomedCT/nrc_seed_select_iri/'

def load_concept_iri(filepath=concept_iri_file):
    concept_iri = np.loadtxt(filepath, dtype='str', comments=None)
    iri2idx = dict()
    for index, iri in enumerate(concept_iri):
        iri2idx[iri] = index
    return concept_iri, iri2idx

def load_concept_embed(filepath=concept_embed_file):
    return np.loadtxt(filepath)

def load_concept_label(filepath=concept_label_file):
    return np.loadtxt(filepath, dtype=str, delimiter='\n')

def get_refset_name(raw_name):
    name = raw_name.split("Active")[0].split('_')[-1]
    return name

def compute_rank_by_embed(seed_idx, concept_embed, concept_iri, metric='cosine'):
    seed_embeds = concept_embed[seed_idx]
    dist = cdist(np.array(seed_embeds).reshape(len(seed_idx), -1), concept_embed, metric=metric).T
    idx = sorted(range(len(dist)), key=lambda x:np.min(dist[x]), reverse=False)
    rank = concept_iri[idx]
    return rank

def compute_sim_by_rank(rank, concept_iri):
    len_rank = len(rank)
    iri2sim = dict()
    for index, iri in enumerate(rank):
        iri2sim[iri] = 1.0 - index / len_rank
    result = []
    for iri in concept_iri:
        result.append(iri2sim[iri])
    return result

def load_selection_result(filepath, iri2idx, shape):
    iris = np.loadtxt(filepath, dtype=str, ndmin=1)
    y = np.zeros(shape=(shape))
    for iri in iris:
        y[iri2idx[iri]] = 1
    return y