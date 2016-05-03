# NLP experiments

POS tagging and probabilistic parsing with well-known opensource tools

## NLTK

### Prequisites

* Python 2.7.10
* PIP 1.5.6 https://pypi.python.org/pypi/pip
* NLTK 3.2.1 http://www.nltk.org
* Progress 1.2 https://pypi.python.org/pypi/progress

### POS Tagging

POS tagging with predefined training and test data:
```
$ cd python
$ python tagger.py ../data/pos-tagging/Indonesian_Manually_Tagged_Corpus_ID.tsv ../data/pos-tagging/Wikipedia.txt
```

POS tagging by splitting training data to training and test data:
```
$ cd python
$ python tagger.py ../data/pos-tagging/Indonesian_Manually_Tagged_Corpus_ID.tsv 1000 sentences.tag
```

## Open NLP

### Prequisites

* JDK 1.7 or later
* Maven 3.3.9 or later

Building program:
```
$ cd java/nlp
$ mvn -Dmaven.test.skip=true package
```

### POS Tagging

POS tagging with predefined training and test data:
```
$ cd java/nlp
$ sh target/appassembler/bin/nlp pos-tag ../../data/pos-tagging/Indonesian_Manually_Tagged_Corpus_ID.tsv ../../data/pos-tagging/Wikipedia.txt
```

POS tagging by splitting training data to training and test data:
```
$ cd java/nlp
$ sh target/appassembler/bin/nlp pos-tag -split 9:1 ../../data/pos-tagging/Indonesian_Manually_Tagged_Corpus_ID.tsv
```
