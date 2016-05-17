package yohanes.nlp;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class App
{
    public static String getFileDir(String filePath) {
        return filePath.substring(0, filePath.lastIndexOf(File.separator) + 1);
    }

    public static String getFileName(String filePath) {
        return filePath.substring(filePath.lastIndexOf(File.separator) + 1, filePath.lastIndexOf("."));
    }

    private static void POSTaggingWithDataSplit(Tagger tagger, String rawTrainFile, String proportion) throws Exception {
        String[] proportions = proportion.split(":");
        int train =  Integer.parseInt(proportions[0]);
        int test =  Integer.parseInt(proportions[1]);
        float trainProportion = train / (float) (test + train);
        String rawTrainDir = rawTrainFile.substring(0, rawTrainFile.lastIndexOf(File.separator) + 1);
        String rawTrainName = rawTrainFile.substring(rawTrainFile.lastIndexOf(File.separator) + 1, rawTrainFile.lastIndexOf("."));
        String trainFile = rawTrainDir + rawTrainName + ".opennlp.split.train";
        String testFile = rawTrainDir + rawTrainName + ".opennlp.split.test";
        String testFileVerify = rawTrainDir + rawTrainName + ".opennlp.split.test.verify";
        String modelFile = rawTrainDir + rawTrainName + ".opennlp.split.model";
//        String resultFile = rawTrainDir + rawTrainName + ".opennlp.split.test.tagged";
        String resultFile = rawTrainDir + "sentences.tag";
        tagger.convertAndSplitTrainFile(rawTrainFile, trainFile, testFile, testFileVerify, trainProportion);
        tagger.trainPOSTaggerModel(trainFile, modelFile);
        tagger.tagPOS(modelFile, testFile, resultFile, true);
        float accuracy = tagger.calculateAccuracy(resultFile, testFileVerify);
        System.out.println("Accuracy: " + accuracy + "%");
        System.out.println("Done");
    }

    private static void POSTagging(Tagger tagger, String rawTrainFile, String testFile) throws IOException {
        String rawTrainDir = rawTrainFile.substring(0, rawTrainFile.lastIndexOf(File.separator) + 1);
        String rawTrainName = rawTrainFile.substring(rawTrainFile.lastIndexOf(File.separator) + 1, rawTrainFile.lastIndexOf("."));
        String testDir = testFile.substring(0, testFile.lastIndexOf(File.separator) + 1);
        String testName = testFile.substring(testFile.lastIndexOf(File.separator) + 1, testFile.lastIndexOf("."));
        String trainFile = rawTrainDir + rawTrainName + ".opennlp.train";
        String modelFile = rawTrainDir + rawTrainName + ".opennlp.model";
        String resultFile = testDir + testName + ".opennlp.tagged";
        tagger.convertTrainFile(rawTrainFile, trainFile);
        tagger.trainPOSTaggerModel(trainFile, modelFile);
        tagger.tagPOS(modelFile, testFile, resultFile, false);
        System.out.println("Done");
    }

    public static void printUsageGuide() {
        System.out.println();
        System.out.println("Usage:");
        System.out.println("$ nlp pos-tag [train_file] [test_file]");
        System.out.println("$ nlp pos-tag -split [train:test] [train_file]");
        System.out.println("$ nlp ner [raw text file for NER] [scenario]");
        System.out.println();
        System.out.println("Example:");
        System.out.println("$ nlp pos-tag -split 9:1 Indonesian_Manually_Tagged_Corpus_ID.tsv");
        System.out.println("$ nlp pos-tag Indonesian_Manually_Tagged_Corpus_ID.tsv Wikipedia.txt");
        System.out.println("$ nlp ner training_data.clean 1");
        System.out.println();
    }

    public static void main(String[] args) {

        long t0 = new Date().getTime();

        try {
            String task = args[0];
            // POS tagging task
            if ("pos-tag".equalsIgnoreCase(task)) {
                Tagger tagger = new Tagger();
                // handle proportion
                if (args.length == 4 && "-split".equalsIgnoreCase(args[1])) {
                    POSTaggingWithDataSplit(tagger, args[3], args[2]);
                }
                // predefined train and test data
                else if (args.length == 3) {
                    POSTagging(tagger, args[1], args[2]);
                } else {
                    printUsageGuide();
                    System.exit(-1);
                }
            } else if ("ner".equalsIgnoreCase(task)) {
                Recognizer recognizer;
                if (args.length == 3) {
                    String testFile = args[1];
                    int scenario = Integer.parseInt(args[2]);
                    recognizer = new Recognizer(scenario);
                    recognizer.find(testFile, App.getFileDir(testFile) + "output.txt");
                } else {
                    printUsageGuide();
                    System.exit(-1);
                }
            } else {
                printUsageGuide();
                System.exit(-1);
            }
        } catch (Exception e) {
            System.err.println("Invalid input!");
            System.err.println("");
            e.printStackTrace();
            System.exit(-1);
        }

        System.out.println("Total time: " + ((new Date().getTime() - t0) / (float) 1000) + " s");
    }
}
