# -*- coding: utf-8 -*-
import numpy as np
import argparse
import random
import re
import multiprocessing
import gensim
import sys
from nltk import word_tokenize
import time

sys.path.append('../rdf2vec/')
sys.path.append('../lib/')
sys.path.append('.')
from Evaluator import Evaluator
from RDF2Vec_Embed import get_rdf2vec_walks

parser = argparse.ArgumentParser(description="The is to evaluate RDF2Vec.")
parser.add_argument("--onto_file", type=str, default="ontology.owl")
parser.add_argument("--train_file", type=str, default="train.csv")
parser.add_argument("--valid_file", type=str, default="valid.csv")
parser.add_argument("--test_file", type=str, default="test.csv")
parser.add_argument("--class_file", type=str, default="classes.txt")
parser.add_argument("--inferred_ancestor_file", type=str, default="inferred_ancestors.txt")

# hyper parameters
parser.add_argument("--embedsize", type=int, default=100, help="Embedding size of word2vec")
parser.add_argument("--URI_Doc", type=str, default="yes")
parser.add_argument("--Lit_Doc", type=str, default="yes")
parser.add_argument("--Mix_Doc", type=str, default="yes")
parser.add_argument("--Mix_Type", type=str, default="random", help="random, all")
parser.add_argument("--Embed_Out_URI", type=str, default="yes")
parser.add_argument("--Embed_Out_Words", type=str, default="yes")
parser.add_argument("--input_type", type=str, default="concatenate", help='concatenate, minus')

parser.add_argument("--walk_depth", type=int, default=2)
parser.add_argument("--walker", type=str, default="wl", help="random, wl")
parser.add_argument("--axiom_file", type=str, default='axioms.txt', help="Corpus of Axioms")
parser.add_argument("--annotation_file", type=str, default='annotations.txt', help="Corpus of Literals")

parser.add_argument("--pretrained", type=str, default="none",
                    help="~/w2v_model/enwiki_model/word2vec_gensim or none")

FLAGS, unparsed = parser.parse_known_args()

if FLAGS.Embed_Out_Words.lower() == 'yes' and FLAGS.Mix_Doc.lower() == 'no' and \
        FLAGS.Lit_Doc.lower() == 'no' and FLAGS.pretrained == 'none':
    print('Can not embed words with no Lit Doc or Mix Doc or no pretrained model')
    sys.exit(0)

def URI_parse(uri):
    """Parse a URI: remove the prefix, parse the name part (Camel cases are plit)"""
    uri = re.sub("http[a-zA-Z0-9:/._-]+#", "", uri)
    uri = uri.replace('_', ' ').replace('-', ' ').replace('.', ' ').replace('/', ' '). \
        replace('"', ' ').replace("'", ' ')
    words = []
    for item in uri.split():
        matches = re.finditer('.+?(?:(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])|$)', item)
        for m in matches:
            word = m.group(0)
            if word.isalpha():
                words.append(word.lower())
    return words


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
        if FLAGS.Embed_Out_Words.lower() == 'yes' and FLAGS.Embed_Out_URI.lower() == 'yes':
            v_uri = model.wv.get_vector(instance) if instance in model.wv.vocab else np.zeros(model.vector_size)
            v_word = word_embeding(inst=instance)
            feature_vectors.append(np.concatenate((v_uri, v_word)))

        elif FLAGS.Embed_Out_Words.lower() == 'no' and FLAGS.Embed_Out_URI.lower() == 'yes':
            v_uri = model.wv.get_vector(instance) if instance in model.wv.vocab else np.zeros(model.vector_size)
            feature_vectors.append(v_uri)

        elif FLAGS.Embed_Out_Words.lower() == 'yes' and FLAGS.Embed_Out_URI.lower() == 'no':
            v_word = word_embeding(inst=instance)
            feature_vectors.append(v_word)

        else:
            print("Unknown embed out type")
            sys.exit(0)

    return feature_vectors


def pre_process_words(words):
    text = ' '.join([re.sub(r'https?:\/\/.*[\r\n]*', '', word, flags=re.MULTILINE) for word in words])
    tokens = word_tokenize(text)
    processed_tokens = [token.lower() for token in tokens if token.isalpha()]
    return processed_tokens

import pickle
from tqdm import tqdm

print("\n		1.Extract corpus and learning embedding ... \n")
classes = [line.strip() for line in open(FLAGS.class_file).readlines()]
candidate_num = len(classes)
uri_label = dict()
annotations = list()

for line in tqdm(open(FLAGS.annotation_file, encoding='utf-8').readlines()):
    tmp = line.strip().split()
    if tmp[1] == 'http://www.w3.org/2000/01/rdf-schema#label':
        uri_label[tmp[0]] = pre_process_words(tmp[2:])
    elif tmp[0] in classes:
        annotations.append(tmp)

# with open("./classes.pkl", 'wb') as f:
#     pickle.dump(classes, f)
# with open("./uri_label.pkl", 'wb') as f:
#     pickle.dump(uri_label, f)
# with open("./annotations.pkl", 'wb') as f:
#     pickle.dump(annotations, f)
# print("done")
# exit()

with open('./classes.pkl', 'rb') as f:
    classes = pickle.load(f)
with open("./uri_label.pkl", 'rb') as f:
    uri_label = pickle.load(f)
with open("./annotations.pkl", 'rb') as f:
    annotations = pickle.load(f)

# ps = []
# for i in tqdm(range(len(classes))):
#     label = uri_label.get(classes[i], [])
#     label = ' '.join(label)
#     ps.append(label)
# np.savetxt('export_label.csv', ps, fmt="%s", encoding='utf-8')

walk_sentences, axiom_sentences = list(), list()
if FLAGS.URI_Doc.lower() == 'yes':
    walks_ = get_rdf2vec_walks(onto_file=FLAGS.onto_file, walker_type=FLAGS.walker,
                               walk_depth=FLAGS.walk_depth, classes=classes)
    print('Extracted {} walks for {} classes!'.format(len(walks_), len(classes)))
    walk_sentences += [list(map(str, x)) for x in walks_]
    for line in open(FLAGS.axiom_file).readlines():
        axiom_sentence = [item for item in line.strip().split()]
        axiom_sentences.append(axiom_sentence)
    print('Extracted %d axiom sentences' % len(axiom_sentences))
URI_Doc = walk_sentences + axiom_sentences
# with open("./URI_DOC.pkl", 'wb') as f:
#     pickle.dump(URI_Doc, f)
# with open("./URI_DOC.pkl", 'rb') as f:
#     URI_Doc = pickle.load(f)

Lit_Doc = list()
if FLAGS.Lit_Doc.lower() == 'yes':
    for annotation in tqdm(annotations):
        processed_words = pre_process_words(annotation[2:])
        if len(processed_words) > 0:
            Lit_Doc.append(uri_label[annotation[0]] + processed_words)

    print('Extracted %d literal annotations' % len(Lit_Doc))

    for sentence in tqdm(walk_sentences):
        lit_sentence = list()
        for item in sentence:
            if item in uri_label:
                lit_sentence += uri_label[item]
            elif item.startswith('http://www.w3.org'):
                lit_sentence += [item.split('#')[1].lower()]
            else:
                lit_sentence += [item]
        Lit_Doc.append(lit_sentence)

    for sentence in tqdm(axiom_sentences):
        lit_sentence = list()
        for item in sentence:
            lit_sentence += uri_label[item] if item in uri_label else [item.lower()]
        Lit_Doc.append(lit_sentence)
# with open("./Lit_Doc.pkl", 'wb') as f:
#     pickle.dump(Lit_Doc, f)
# with open("./Lit_Doc.pkl", 'rb') as f:
#     Lit_Doc = pickle.load(f)

Mix_Doc = list()
if FLAGS.Mix_Doc.lower() == 'yes':
    for sentence in tqdm(walk_sentences):
        if FLAGS.Mix_Type.lower() == 'all':
            for index in range(len(sentence)):
                mix_sentence = list()
                for i, item in enumerate(sentence):
                    if i == index:
                        mix_sentence += [item]
                    else:
                        if item in uri_label:
                            mix_sentence += uri_label[item]
                        elif item.startswith('http://www.w3.org'):
                            mix_sentence += [item.split('#')[1].lower()]
                        else:
                            mix_sentence += [item]
                Mix_Doc.append(mix_sentence)
        elif FLAGS.Mix_Type.lower() == 'random':
            random_index = random.randint(0, len(sentence)-1)
            mix_sentence = list()
            for i, item in enumerate(sentence):
                if i == random_index:
                    mix_sentence += [item]
                else:
                    if item in uri_label:
                        mix_sentence += uri_label[item]
                    elif item.startswith('http://www.w3.org'):
                        mix_sentence += [item.split('#')[1].lower()]
                    else:
                        mix_sentence += [item]
            Mix_Doc.append(mix_sentence)

    for sentence in tqdm(axiom_sentences):
        if FLAGS.Mix_Type.lower() == 'all':
            for index in range(len(sentence)):
                random_index = random.randint(0, len(sentence) - 1)
                mix_sentence = list()
                for i, item in enumerate(sentence):
                    if i == random_index:
                        mix_sentence += [item]
                    else:
                        mix_sentence += uri_label[item] if item in uri_label else [item.lower()]
                Mix_Doc.append(mix_sentence)
        elif FLAGS.Mix_Type.lower() == 'random':
            random_index = random.randint(0, len(sentence)-1)
            mix_sentence = list()
            for i, item in enumerate(sentence):
                if i == random_index:
                    mix_sentence += [item]
                else:
                    mix_sentence += uri_label[item] if item in uri_label else [item.lower()]
            Mix_Doc.append(mix_sentence)
# with open("./Mix_Doc.pkl", 'wb') as f:
#     pickle.dump(Mix_Doc, f)

# print('URI_Doc: %d, Lit_Doc: %d, Mix_Doc: %d' % (len(URI_Doc), len(Lit_Doc), len(Mix_Doc)))
all_doc = URI_Doc + Lit_Doc + Mix_Doc
random.shuffle(all_doc)

# with open("./ALL_DOC.pkl", 'wb') as f:
#     pickle.dump(all_doc, f)

# with open('./ALL_DOC.pkl', 'rb') as f:
#     all_doc = pickle.load(f)

# with open('123.txt', 'r', encoding='utf-8') as f:
#     all_doc = f.readlines()

# from utils import EpochSaver

print("Load done")

iteration = 20
# # learn the embeddings
model_ = gensim.models.Word2Vec(all_doc, size=FLAGS.embedsize, window=5, workers=multiprocessing.cpu_count(),
                                sg=1, iter=iteration , negative=25, min_count=1, seed=42, callbacks=[EpochSaver("./checkpoints")])

for i in [20]:
    model_ = gensim.models.Word2Vec.load('./checkpoints/epoch_{}.model'.format(i))
    print("MODEL DONE")
    classes_e = embed(model=model_, instances=classes)
    #np.savetxt('export_class.csv', classes, fmt="%s")
    np.savetxt('export_classes_e_{}.csv'.format(i), classes_e, fmt="%.8f")