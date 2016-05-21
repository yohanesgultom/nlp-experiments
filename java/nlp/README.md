# Read Me (dist)

This is a Read Me file for zip distribution downloadable from [here](https://github.com/yohanesgultom/nlp-experiments/blob/master/java/nlp/dist/yohanes.nlp-dist.zip). Assuming you have downloaded the zip and extract it, go inside the folder to `bin` subdirectory to run commands below.

> For Windows users: Commands below are for Unix system. For (so-non-programmer-like) Windows system please use `nlp.bat` instead of `./nlp`


## Name Entity Recognizer

Use predefined model to recognize (annotate) named-entities within a text file (Indonesian Language) such as [training_data.clean](https://github.com/yohanesgultom/nlp-experiments/blob/master/data/ner/training_data.clean). The output will be [output.txt](https://github.com/yohanesgultom/nlp-experiments/blob/master/data/ner/output.txt) (in the same folder as input file). Required arguments are:

* Raw (non-annotated) text (one sentence per line)
* Scenario id (0-7)

Usage:
```
$ ./nlp ner [raw (non-annotated) text file] [scenario id]
```

Example:
```
$ ./nlp ner /home/yohanesgultom/Workspace/nlp-experiments/data/ner/training_data.clean 1
```

Train and evaluate a model using an MUC/ENAMEX-annotated training data and an MUC/ENAMEX-annotated testing data. Required arguments are:
* Language code
* Training corpus (MUC/ENAMEX annotated text file)
* Testing corpus (MUC/ENAMEX annotated text file)
* Scenario id (0-7)
* (Optional) Evaluation type (0: Exact Match or 1: MUC)

> Missed classification will be saved in `[ENAMEX/MUC annotated test file].opennlp.missed`

Usage:
```
$ ./nlp ner -traintest [language code] [ENAMEX/MUC annotated training file] [ENAMEX/MUC annotated testing file] [scenario id] [optional:eval type]
```

Example:
```
$ ./nlp ner -traintest id ~/Workspace/nlp-experiments/data/ner/training_data.txt ~/Workspace/nlp-experiments/data/ner/testing_data.txt 4 1
```

Train and evaluate a model using MUC/ENAMEX-annotated corpus using single dataset/corpus. Required arguments are:
* Language code
* Training corpus (MUC/ENAMEX annotated text file)
* Proportion of training data : test data sentences
* Scenario id (0-7)
* (Optional) Evaluation type (0: Exact Match or 1: MUC)

> Missed classification will be saved in `[ENAMEX/MUC annotated text file].opennlp.missed`

Usage:
```
$ ./nlp ner -eval [language code] [ENAMEX/MUC annotated text file] [train data:test data] [scenario id] [optional:eval type]
```

Example:
```
$ ./nlp ner -eval id /home/yohanesgultom/Workspace/nlp-experiments/data/ner/training_data.txt 9:1 0
$ ./nlp ner -eval id /home/yohanesgultom/Workspace/nlp-experiments/data/ner/training_data.txt 9:1 1 1
```

Evaluate with pre-trained model using test corpus (ENAMEX/annotated). Required arguments are:

* Testing corpus (MUC/ENAMEX annotated text file)
* Scenario id (0-7)
* (Optional) Evaluation type (0: Exact Match or 1: MUC)

> Missed classification will be saved in `[ENAMEX/MUC annotated text file].opennlp.missed`

Usage:
```
$ ./nlp ner -enamex [MUC/ENAMEX annotated text file] [scenario id] [optional:eval type]
```

Example:
```
$ ./nlp ner -enamex /home/yohanesgultom/Workspace/nlp-experiments/data/ner/training_data.txt 1
$ ./nlp ner -enamex /home/yohanesgultom/Workspace/nlp-experiments/data/ner/training_data.txt 1 1
```


## POS Tagger

Train POS tagger using [train_file](https://github.com/yohanesgultom/nlp-experiments/blob/master/data/pos-tagging/Indonesian_Manually_Tagged_Corpus_ID.tsv) and run a test by tagging a [test_file](https://github.com/yohanesgultom/nlp-experiments/blob/master/data/pos-tagging/Wikipedia.txt). The result will be created in the same directory.

Usage:
```
$ ./nlp pos-tag [train_file] [test_file]
```

Example:
```
$ ./nlp pos-tag /home/yohanesgultom/Workspace/nlp-experiments/data/pos-tagging/Indonesian_Manually_Tagged_Corpus_ID.tsv Wikipedia.txt
```

Train POS tagger by splitting corpus to training and test by using a proportion of training sentences:testing sentences as argument (example below).

Usage:
```
$ ./nlp pos-tag -split [train:test] [train_file]
```

Example:
```
$ ./nlp pos-tag -split 9:1 /home/yohanesgultom/Workspace/nlp-experiments/data/pos-tagging/Indonesian_Manually_Tagged_Corpus_ID.tsv
```
