# author yohanes.gultom@gmail.com

import nltk
import re
import pickle
import os
import sys
import random
import time
from nltk.tag import tnt
from progress.bar import Bar

def parse_train_data(train_file_path):
    p = re.compile('<kalimat id=(.+)>((.|\n)*?)</kalimat>')
    file = open(train_file_path, 'r')
    raw = file.read()
    sents = []
    for s in p.findall(raw):
        words = s[1].strip().split('\n')
        sent = []
        for w in words:
            array = w.split('\t')
            sent.append((array[0], array[1]))
        sents.append(sent)
    file.close()
    return sents

def save_pos_tagger(model_file_path):
    pickle_file = open(model_file_path, 'w')
    pickle.dump(tnt_pos_tagger, pickle_file)
    pickle_file.close()

def load_pos_tagger(model_file_path):
    return pickle.load(open(model_file_path, 'rb'))

def tag_file(file_path, tagger):
    input = open(file_path, 'r')
    output = open(file_path + '.nltk.tagged', 'w')
    raw = input.read()
    res = []
    for s in raw.split('\n'):
        tagged = tagger.tag(nltk.word_tokenize(s.strip()))
        res.append(tagged)
        words = []
        for t in tagged:
            words.append(t[0] + '_' + t[1])
        output.write('\t'.join(words))
    input.close()
    output.close()
    return res

def remove_tags(sent):
    res = []
    for w in sent:
        res.append(w[0])
    return res

def evaluate_sentence(expected, actual):
    if (len(expected) != len(actual)):
        raise ValueError('different length: {0} \n {1} '.format(expected, actual))
    correct = 0
    for i in range(len(expected)):
        if expected[i][0] == actual[i][0] and expected[i][1] == actual[i][1]:
            correct += 1
    return correct

def evaluate(train_file_path, test_num, tagger, output_file_path):
    sents = parse_train_data(train_file_path)
    test_start = len(sents) - test_num - 1
    test_data = sents[test_start:len(sents)-1]
    train_data = sents[0:test_start+1]
    print 'Training with {0} sentences'.format(len(train_data))
    tagger.train(train_data)
    output = open(output_file_path, 'w')
    correct = 0
    total = 0
    bar = Bar('Testing with {0} sentences'.format(len(test_data)), max=len(test_data))
    for s in test_data:
        tagged = tagger.tag(remove_tags(s))
        # evaluate
        correct += evaluate_sentence(s, tagged)
        total += len(tagged)
        # write
        words = []
        for t in tagged:
            words.append(t[0] + '_' + t[1])
        output.write('\t'.join(words))
        bar.next()
    bar.finish()
    output.close()
    return correct / float(total) * 100
    # return tagger.evaluate(test_data)

if __name__ == "__main__":
    t0 = time.clock()
    argc = len(sys.argv)
    if argc == 3:
        # eg: python tagger.py ../data/pos-tagging/Indonesian_Manually_Tagged_Corpus_ID.tsv ../data/pos-tagging/Wikipedia.txt
        tnt_pos_tagger = tnt.TnT()
        train_data = parse_train_data(sys.argv[1])
        print 'Training with {0} sentences'.format(len(train_data))
        tnt_pos_tagger.train(train_data)
        print 'Tagging {0}'.format(sys.argv[2])
        tag_file(sys.argv[2], tnt_pos_tagger)
    elif argc == 4:
        # eg: python tagger.py ../data/pos-tagging/Indonesian_Manually_Tagged_Corpus_ID.tsv 1000 sentences.tag
        tnt_pos_tagger = tnt.TnT()
        acc = evaluate(sys.argv[1], int(sys.argv[2]), tnt_pos_tagger, sys.argv[3])
        print 'Accuracy: {0} %'.format(acc)
    print 'Total time: {0} s'.format(time.clock() - t0)
