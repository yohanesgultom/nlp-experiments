# Read Me (dist)

This is a Read Me file for appassembler (zip) distribution. Assuming you have downloaded the zip and extract it, go inside the folder to `bin` subdirectory to run commands below.

## Name Entity Recognizer

Annotate a raw text file containing Indonesian sentences separated by newline. See [training_data.clean](https://github.com/yohanesgultom/nlp-experiments/blob/master/data/ner/training_data.clean) as example. The output will be [output.txt](https://github.com/yohanesgultom/nlp-experiments/blob/master/data/ner/output.txt) (in the same folder). While scenario is an integer value representing index of scenario (use 1 for default scenario)

Usage:
```
$ nlp ner [raw text file for NER] [scenario]
```

Example:
```
$ nlp ner training_data.clean 1
```
## POS Tagger

Train POS tagger using [train_file](https://github.com/yohanesgultom/nlp-experiments/blob/master/data/pos-tagging/Indonesian_Manually_Tagged_Corpus_ID.tsv) and run a test by tagging a [test_file](https://github.com/yohanesgultom/nlp-experiments/blob/master/data/pos-tagging/Wikipedia.txt). The result will be created in the same directory.

Usage:
```
$ nlp pos-tag [train_file] [test_file]
```

Example:
```
$ nlp pos-tag Indonesian_Manually_Tagged_Corpus_ID.tsv Wikipedia.txt
```

Another option is to do a cross-validation test by using a proportion of training sentences:testing sentences as argument (example below).

Usage:
```
$ nlp pos-tag -split [train:test] [train_file]
```

Example:
```
$ nlp pos-tag -split 9:1 Indonesian_Manually_Tagged_Corpus_ID.tsv
```
