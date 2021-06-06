import pickle
import os
import numpy as np
import gensim
from sklearn.metrics import ndcg_score, roc_auc_score
from scipy.spatial.distance import cdist

refset_iri_dir = './nrc_refset_iri/'
seed_random_iri_dir = './142857/'

with open('./classes.pkl', 'rb') as f:
    classes = pickle.load(f)
with open("./uri_label.pkl", 'rb') as f:
    uri_label = pickle.load(f)

def load_concept_iri(filepath):
    concept_iri = np.loadtxt(filepath, dtype='str', comments=None)
    iri2idx = dict()
    for index, iri in enumerate(concept_iri):
        iri2idx[iri] = index
    return concept_iri, iri2idx

concept_iri, iri2idx = load_concept_iri('./export_class.csv')

def embed(model, instances):

    def word_embeding(inst):
        v = np.zeros(model.vector_size)
        if inst in uri_label:
            words = uri_label.get(inst)
            n = 0
            for word in words:
                if word in model.wv.vocab:
                    v += model.wv.get_vector(word)
                    n += 1
            return v / n if n > 0 else v
        else:
            return v

    feature_vectors = []
    for instance in instances:
        v_uri = model.wv.get_vector(instance) if instance in model.wv.vocab else np.zeros(model.vector_size)
        v_word = word_embeding(inst=instance)
        feature_vectors.append(np.concatenate((v_uri, v_word)))

    return feature_vectors

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

def get_refset_name(raw_name):
    name = raw_name.split("Active")[0].split('_')[-1]
    return name


rnd = np.random.RandomState(42)
refset_fnames = os.listdir(refset_iri_dir)
refset_fnames = rnd.permutation(refset_fnames)

for roun in [20]:
    with open('./nrc_select_ft_12345.txt', 'w') as tgt_f:
        for cross in range(10):
            print("Cross", cross)
            test_fnames = refset_fnames[cross*3:cross*3+3]
            train_fnames = [item for item in refset_fnames if not item in test_fnames]

            ft_corpus = list()
            for fname in train_fnames:
                with open(refset_iri_dir + fname, 'r') as f:
                    refset_iri = np.loadtxt(f, dtype=str, ndmin=1)
                    #for i in range(max(min(len(refset_iri), 5000), 100)):
                    for i in range(max(len(refset_iri), 1)):
                        length = rnd.randint(2,10)
                        target = rnd.choice(refset_iri, length, replace=False)
                        tmp = []
                        for s in target:
                            tmp.append(s)
                            tmp.append(fname)
                        ft_corpus.append(tmp[:-1])
            print("Generating corpus done")

            model = gensim.models.Word2Vec.load('./checkpoints/epoch_20.model')
            print("Load model done")
            model.build_vocab(ft_corpus, update=True)
            model.callbacks=[]
            model.train(ft_corpus, total_examples=len(ft_corpus), epochs=roun)
            print("Train done")
            concept_embed = np.array(embed(model=model, instances=classes))
                # np.savetxt('export_classes_e_20_cross_{}.csv'.format(cross), classes_e, fmt="%.8f")
            #concept_embed = np.zeros(shape=(len(concept_iri), 200))

            print("Embedding done")
            for fname in test_fnames:
                refset_iri = np.loadtxt(refset_iri_dir + fname, dtype=str)
                refset_name = get_refset_name(fname)
                refset_seed_iri = np.loadtxt(seed_random_iri_dir + fname, dtype=str)
                # print(refset_seed_iri)
                # print(refset_name)
                # exit()
                y_true = np.zeros(shape=(len(concept_iri)))
                for item in refset_iri:
                    y_true[iri2idx[item]] = 1.0
                for size in [1,2,3,4,5]:

                    idxes = [iri2idx[i] for i in refset_seed_iri[:size]]
                    print(idxes)

                    rank = compute_rank_by_embed(idxes, concept_embed, concept_iri)
                    y_pred = compute_sim_by_rank(rank, concept_iri)

                    ndcg = ndcg_score([y_true], [y_pred])
                    auc = roc_auc_score(y_true, y_pred)

                    tgt_f.write("refset={:35s} seed_size={} NNRank score: NDCG={:.4f} AUC={:.4f}\n".format(refset_name, size, ndcg, auc))
                    tgt_f.flush()
            print("Evlautate done")