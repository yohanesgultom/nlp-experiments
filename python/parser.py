# author yohanes.gultom@gmail.com
# inspired by http://tech.swamps.io/training-a-simple-pcfg-parser-using-nltk/

import nltk
import re
import tagger
import sys
import time
from nltk import Tree
from nltk.grammar import ProbabilisticProduction, Nonterminal
from pprint import pprint
from nltk.tag import tnt
from progress.bar import Bar

def corpus2treesplain(text):
    """ Parse the corpus and return a list of Trees """
    rawparses = text.split("\n\n")
    trees = []

    for rp in rawparses:
        if not rp.strip():
            continue

        try:
            t = Tree.fromstring(rp)
            trees.append(t)
        except ValueError:
            logging.error('Malformed parse: "%s"' % rp)

    return trees

def corpus2trees(text):
    """ Parse the corpus and return a list of Trees """
    rawparses = text.split('\n')
    trees = []
    for rp in rawparses:
        if not rp.strip():
            continue

        try:
            for s in split_tree(rp):
                s = remove_terminal_tree_format(s)
                t = Tree.fromstring(s)
                trees.append(t)
        except ValueError:
            logging.error('Malformed parse: "%s"' % rp)

    return trees

def split_tree(text):
    stack = []
    res = []
    sentence = ''
    for c in text:
        sentence += c
        if c == ')':
            if len(stack) == 1:
                res.append(sentence.strip())
                sentence = ''
            elif len(stack) == 0:
                raise ValueError('imbalance parentheses: %s' % text)
            stack.pop()
        elif c == '(':
            stack.append(c)
    return res

def remove_terminal_tree_format(text):
    p = re.compile('\([^\(|\)]*\)')
    pin = re.compile('\(([^\(|\)]*)\)')
    new = text.strip()
    for m in p.findall(text):
        s = pin.search(m)
        new = new.replace(m, s.group(1))
    return new

def trees2productions(trees):
    """ Transform list of Trees to a list of productions """
    productions = []
    for t in trees:
        productions += t.productions()
    return productions

def trees2postagged(trees):
    tagged = []
    for t in trees:
        tagged.append(t.pos())
        # sent = []
        # for s in t.subtrees(lambda t: t.height() == 2):
        #     sent.append((s.leaves()[0], s.label()))
        # tagged.append(sent)
    return tagged

def splittrees(trees, testnum):
    skip = len(trees) / testnum
    test = []
    train = []
    next = 0
    for i in range(len(trees)):
        if (len(test) < testnum) and (i == next):
            test.append(trees[i])
            next += skip
        else:
            train.append(trees[i])
    return train, test

class PCFGViterbiParser(nltk.ViterbiParser):
    def __init__(self, grammar, trace=0):
        super(PCFGViterbiParser, self).__init__(grammar, trace)

    @classmethod
    def train(cls, content, root):
        if not isinstance(content, basestring):
            content = content.read()

        trees = corpus2trees(content)
        print 'Training parser with {0} sentences'.format(len(trees))
        productions = trees2productions(trees)

        pcfg = nltk.grammar.induce_pcfg(nltk.grammar.Nonterminal(root), productions)
        return cls(pcfg)

    @classmethod
    def train_trees(cls, trees, root):
        print 'Training parser with {0} sentences'.format(len(trees))
        productions = trees2productions(trees)
        pcfg = nltk.grammar.induce_pcfg(nltk.grammar.Nonterminal(root), productions)
        return cls(pcfg)

    def parse(self, tokens, tagger = None):
        # tokens = self._preprocess(list(tokens))
        if (tagger == None):
            tagged = nltk.pos_tag(tokens)
        else:
            tagged = tagger.tag(tokens)
        # print tagged
        missing = False
        for tok, pos in tagged:
            if not self._grammar._lexical_index.get(tok):
                missing = True
                self._grammar._productions.append(ProbabilisticProduction(Nonterminal(pos), [tok], prob=0.000001))
        if missing:
            self._grammar._calculate_indexes()
        return super(PCFGViterbiParser, self).parse(tokens)

    def parse_batch(self, tagged):
        missing = False
        tokens = []
        for tok, pos in tagged:
            tokens.append(tok)
            if not self._grammar._lexical_index.get(tok):
                missing = True
                self._grammar._productions.append(ProbabilisticProduction(Nonterminal(pos), [tok], prob=0.000001))
        if missing:
            self._grammar._calculate_indexes()
        return super(PCFGViterbiParser, self).parse(tokens)

if __name__ == "__main__":
    t0 = time.clock()
    argc = len(sys.argv)
    if argc == 3:
        # eg: python parser.py ../data/prob-parsing/150324.001-300.bracket ../data/prob-parsing/Wikipedia.txt
        content = open(sys.argv[1], 'r').read()
        trees = corpus2trees(content)
        tnt_pos_tagger = tnt.TnT()
        train_data = tagger.parse_train_data(sys.argv[1])
        tagger_train_data = trees2postagged(trees)
        print 'Training tagger with {0} sentences'.format(len(tagger_train_data))
        tnt_pos_tagger.train(tagger_train_data)
        print 'Tagging {0}'.format(sys.argv[2])
        test_data = tagger.tag_file(sys.argv[2], tnt_pos_tagger)
        viterbi_parser = PCFGViterbiParser.train(open(sys.argv[1], 'r'), root='S')
        bar = Bar('Parsing {0} sentences'.format(len(test_data)), max=len(test_data))
        output = open(sys.argv[2] + '.nltk.bracket', 'w')
        for tagged in test_data:
            trees = viterbi_parser.parse_batch(tagged)
            for t in trees:
                if len(t) > 0:
                    output.write(str(t))
                    break
            bar.next()
        bar.finish()
        output.close()

    elif argc == 4:
        # eg: python parser.py ../data/prob-parsing/150324.001-300.bracket 50 sentences.bracket
        content = open(sys.argv[1], 'r').read()
        trees = corpus2trees(content)
        test_num = int(sys.argv[2])
        # parser_test_start = len(trees) - test_num - 1
        # parser_test_data = trees[parser_test_start:len(trees)-1]
        # parser_train_data = trees[0:parser_test_start+1]
        parser_train_data, parser_test_data = splittrees(trees, test_num)
        viterbi_parser = PCFGViterbiParser.train_trees(parser_train_data, root='S')
        tnt_pos_tagger = tnt.TnT()
        tagger_train_data = trees2postagged(trees)
        print 'Training tagger with {0} sentences'.format(len(tagger_train_data))
        tnt_pos_tagger.train(tagger_train_data)
        bar = Bar('Parsing {0} sentences'.format(len(parser_test_data)), max=len(parser_test_data))
        correct = 0
        output = open(sys.argv[3], 'w')
        for t in parser_test_data:
            tagged = tnt_pos_tagger.tag(t.leaves())
            trees = viterbi_parser.parse_batch(tagged)
            for t in trees:
                if len(t) > 0:
                    correct += 1
                    output.write(str(t))
                    break
            bar.next()
        bar.finish()
        output.close()
        acc = correct / float(len(parser_test_data)) * 100
        print 'Accuracy: {0} %'.format(acc)

    print 'Total time: {0} s'.format(time.clock() - t0)
