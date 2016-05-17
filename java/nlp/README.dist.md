# Read Me (dist)

This is a Read Me file for zip distribution downloadable from [here](https://github.com/yohanesgultom/nlp-experiments/blob/master/java/nlp/dist/yohanes.nlp-dist.zip). Assuming you have downloaded the zip and extract it, go inside the folder to `bin` subdirectory to run commands below.

> Commands below are for Unix system. For (so-non-programmer-like) Windows system please use `nlp.bat` instead of `./nlp`

## Name Entity Recognizer

Provide an MUC annotated text file containing Indonesian such as [training_data.clean](https://github.com/yohanesgultom/nlp-experiments/blob/master/data/ner/training_data.clean) as example. The output will be [output.txt](https://github.com/yohanesgultom/nlp-experiments/blob/master/data/ner/output.txt) (in the same folder) and scenario id, an integer value representing index of scenario (use 1 for default scenario)

Usage:
```
$ ./nlp ner [raw text file for NER] [scenario id]
```

Example:
```
$ ./nlp ner training_data.clean 1
```

To do cross-validation evaluation, provide language code (Id = indonesian), same MUC annotated text file, proportion of training data & test data sentences and finally the scenario id. Optionally, eval type (0: Exact Match or 1: MUC) can be provided

Usage:
```
$ ./nlp ner -eval [language code] [raw text file for NER] [train data:test data] [scenario id] [optional:eval type]
```

Example:
```
$ ./nlp ner -eval id /home/yohanesgultom/Workspace/nlp-experiments/data/ner/training_data.txt 9:1 0
$ ./nlp ner -eval id /home/yohanesgultom/Workspace/nlp-experiments/data/ner/training_data.txt 9:1 1 1
```

## POS Tagger

Train POS tagger using [train_file](https://github.com/yohanesgultom/nlp-experiments/blob/master/data/pos-tagging/Indonesian_Manually_Tagged_Corpus_ID.tsv) and run a test by tagging a [test_file](https://github.com/yohanesgultom/nlp-experiments/blob/master/data/pos-tagging/Wikipedia.txt). The result will be created in the same directory.

Usage:
```
$ ./nlp pos-tag [train_file] [test_file]
```

Example:
```
$ ./nlp pos-tag Indonesian_Manually_Tagged_Corpus_ID.tsv Wikipedia.txt
```

Another option is to do a cross-validation test by using a proportion of training sentences:testing sentences as argument (example below).

Usage:
```
$ ./nlp pos-tag -split [train:test] [train_file]
```

Example:
```
$ ./nlp pos-tag -split 9:1 Indonesian_Manually_Tagged_Corpus_ID.tsv
```
