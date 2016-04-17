# author yohanes.gultom@gmail.com
# inspired by http://tech.swamps.io/training-a-simple-pcfg-parser-using-nltk/

import nltk
import re
import tagger
from nltk import Tree
from nltk.grammar import ProbabilisticProduction, Nonterminal
from pprint import pprint
import sys

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

class PCFGViterbiParser(nltk.ViterbiParser):
    def __init__(self, grammar, trace=0):
        super(PCFGViterbiParser, self).__init__(grammar, trace)

    @staticmethod
    def _preprocess(tokens):
        replacements = {
            "(": "-LBR-",
            ")": "-RBR-",
        }
        for idx, tok in enumerate(tokens):
            if tok in replacements:
                tokens[idx] = replacements[tok]

        return tokens

    @classmethod
    def train(cls, content, root):
        if not isinstance(content, basestring):
            content = content.read()

        trees = corpus2trees(content)
        productions = trees2productions(trees)

        pcfg = nltk.grammar.induce_pcfg(nltk.grammar.Nonterminal(root), productions)
        print pcfg
        return cls(pcfg)

    def parse(self, tokens, tagger = None):
        tokens = self._preprocess(list(tokens))
        if (tagger == None):
            tagged = nltk.pos_tag(tokens)
        else:
            tagged = tagger.tag(tokens)
        print tagged
        missing = False
        for tok, pos in tagged:
            if not self._grammar._lexical_index.get(tok):
                missing = True
                self._grammar._productions.append(ProbabilisticProduction(Nonterminal(pos), [tok], prob=0.000001))
        if missing:
            self._grammar._calculate_indexes()
        return super(PCFGViterbiParser, self).parse(tokens)

if __name__ == "__main__":
    viterbi_parser = PCFGViterbiParser.train(open('../data/prob-parsing/150324.001-300.bracket', 'r'), root='ROOT')
    pos_tagger = tagger.load_pos_tagger('../data/pos-tagging/Indonesian_Manually_Tagged_Corpus_ID.tsv.pickle')
    sentence = 'Pemuda pengganti monyet-monyet .'
    trees = viterbi_parser.parse(nltk.word_tokenize(sentence), pos_tagger)
    for t in trees:
        print t
    # rp = remove_terminal_tree_format(rp)
    # t = Tree.fromstring(rp)
