# Open NLP

POS tagging and Named-entity recognizing

## Distribution
Binary distribution can be downloaded [here](https://github.com/yohanesgultom/nlp-experiments/blob/master/java/nlp/dist/yohanes.nlp-dist.zip) (JRE 1.7 or later required, Unix or Windows only)

## Usage
Please find usage guide in the [README](https://github.com/yohanesgultom/nlp-experiments/blob/master/java/nlp/README.dist.md)

## Building
Prerequisites:
* JDK 1.7 or later
* Maven 3.3.9 or later

Building program:
```
$ cd java/nlp
$ mvn clean package
```
# NLTK

## Prequisites

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
