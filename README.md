# NLP experiments

POS tagging and probabilistic parsing with well-known opensource tools

### Prequisites

* JDK 1.7 or later
* Maven 3.3.9 or later

## Open NLP

Building program:
```
$ cd java/nlp
$ mvn -Dmaven.test.skip=true package
```

### POS Tagging

POS tagging with predefined training and test data:
```
$ sh target/appassembler/bin/nlp pos-tag ../../data/pos-tagging/Indonesian_Manually_Tagged_Corpus_ID.tsv ../../data/pos-tagging/Wikipedia.txt
```

POS tagging by splitting training data to training and test data:
```
$ sh target/appassembler/bin/nlp pos-tag -split 10:1 ../../data/pos-tagging/Indonesian_Manually_Tagged_Corpus_ID.tsv
```
