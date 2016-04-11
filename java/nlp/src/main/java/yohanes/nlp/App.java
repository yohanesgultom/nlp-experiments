package yohanes.nlp;

import edu.stanford.nlp.process.AbstractTokenizer;
import opennlp.tools.postag.*;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.util.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class App
{
    private static final String XML_TAG_PATTERN = "<(\"[^\"]*\"|'[^']*'|[^'\">])*>";

    private Pattern pattern;
    private Matcher matcher;

    public App() {
        pattern = Pattern.compile(XML_TAG_PATTERN);
    }

    public String convertTrainString(String line, boolean firstWord) {
        String toWrite = null;
        matcher = pattern.matcher(line);
        if (!matcher.matches()) {
            // read the word if it's not an xml tag
            // replace tab with underscore
            // and replace whitespace with dash
            toWrite = line.replace("\t", "_").replace(" ", "-");
            if (!firstWord) {
                toWrite = " " + toWrite;
            }
        } else if (!firstWord) {
            // write newline every time we see an xml tag
            toWrite = "\n";
        }
        return toWrite;
    }

    public void convertTrainFile(String rawTrainFile, String trainFile) throws IOException {
        BufferedReader br = null;
        String line;
        br = new BufferedReader(new FileReader(rawTrainFile));
        File outFile = new File(trainFile);

        // if file doesn't exists, then create it
        if (!outFile.exists()) {
            outFile.createNewFile();
        }

        FileWriter fw = new FileWriter(outFile.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);

        int count = 0;
        while ((line = br.readLine()) != null) {
            String toWrite = this.convertTrainString(line.trim(), count == 0);
            if (toWrite != null) {
                bw.write(toWrite);
                count++;
            }
        }

        // close streams
        if (br != null) br.close();
        if (bw != null) bw.close();
    }

    public void convertAndSplitTrainFile(String rawTrainFile, String trainFile, String testFile, String testFileVerify, float trainProportion) throws Exception {
        BufferedReader br = null;
        String line;
        br = new BufferedReader(new FileReader(rawTrainFile));

        int count = 0;
        ArrayList<String> sentences = new ArrayList<String>();
        String sentence = "";
        while ((line = br.readLine()) != null) {
            String toWrite = this.convertTrainString(line, count == 0);
            if (toWrite != null) {
                if (toWrite == "\n") {
                    sentences.add(sentence.trim());
                    sentence = "";
                    count = 0;
                } else {
                    sentence += toWrite;
                }
                count++;
            }
        }

        // calculate number of required training data
        int numTrain = Math.round(trainProportion * sentences.size());

        // close read streams
        if (br != null) br.close();

        File outTrainFile = new File(trainFile);
        File outTestFile = new File(testFile);
        File outTestFileVerify = new File(testFileVerify);

        // if file does not exists, then create it
        if (!outTrainFile.exists()) {
            outTrainFile.createNewFile();
        }
        if (!outTestFile.exists()) {
            outTestFile.createNewFile();
        }
        if (!outTestFileVerify.exists()) {
            outTestFileVerify.createNewFile();
        }

        // write to files
        BufferedWriter trainWriter = new BufferedWriter(new FileWriter(outTrainFile.getAbsoluteFile()));
        BufferedWriter testWriter = new BufferedWriter(new FileWriter(outTestFile.getAbsoluteFile()));
        BufferedWriter testWriterVerify = new BufferedWriter(new FileWriter(outTestFileVerify.getAbsoluteFile()));
        Random rand = new Random();
        int countTrain = 0;
        for (int i = 0; i < sentences.size(); i++) {
            // randomize selection until enough number selected
            if ((countTrain < numTrain-1) && rand.nextBoolean()) {
                trainWriter.write(sentences.get(i).trim() + "\n");
                countTrain++;
            } else {
                testWriter.write(this.removeTagsFromSentence(sentences.get(i)).trim() + "\n");
                testWriterVerify.write(sentences.get(i).trim() + "\n");
            }
        }

        // close write streams
        if (trainWriter != null) trainWriter.close();
        if (testWriter != null) testWriter.close();
        if (testWriterVerify != null) testWriterVerify.close();
    }

    public void trainPOSTaggerModel(String trainFile, String modelFile) throws IOException {
//        InputStream dataIn = null;
//        dataIn = new FileInputStream(trainFile);
        InputStreamFactory isf = new MarkableFileInputStreamFactory(new File(trainFile));
        ObjectStream<String> lineStream = new PlainTextByLineStream(isf, "UTF-8");
//        ObjectStream<String> lineStream = new PlainTextByLineStream(dataIn, "UTF-8");
        ObjectStream<POSSample> sampleStream = new WordTagSampleStream(lineStream);
        POSModel model = POSTaggerME.train("id", sampleStream, TrainingParameters.defaultParams(), new POSTaggerFactory());

        // if file doesnt exists, then create it
        File outFile = new File(modelFile);
        if (!outFile.exists()) {
            outFile.createNewFile();
        }
        OutputStream modelOut = new BufferedOutputStream(new FileOutputStream(modelFile));
        model.serialize(modelOut);

        // close streams
//        if (dataIn != null) dataIn.close();
        lineStream.close();
        modelOut.close();
    }

    public String tagPOS(SimpleTokenizer tokenizer, POSTaggerME tagger, String line) {
        StringBuilder sb = new StringBuilder();
        if (line != null && !line.isEmpty()) {
            String[] words = (tokenizer != null) ? tokenizer.tokenize(line) : line.split(" ");
            String[] tags = tagger.tag(words);
            // match words and tags and write it to output
            for (int i = 0; i < words.length; i++) {
                if (i > 0) sb.append(" ");
                sb.append(words[i]).append("_").append(tags[i]);
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private String removeTagsFromSentence(String sentence) throws Exception {
        String res = "";
        String[] words = sentence.split(" ");
        for (String w:words) {
            if (!res.isEmpty()) res += " ";
            res += w.split("_")[0];
        }
        return res;
    }

    public void tagPOS(String modelFile, String testFile, String resultFile, boolean fromTrainData) throws IOException {
        String line;

        // init POS tagger with model
        InputStream modelIn = new FileInputStream(modelFile);
        POSModel model = new POSModel(modelIn);
        POSTaggerME tagger = new POSTaggerME(model);

        // if file doesnt exists, then create it
        File outFile = new File(resultFile);
        if (!outFile.exists()) {
            outFile.createNewFile();
        }
        FileWriter fw = new FileWriter(outFile.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);

        BufferedReader br = new BufferedReader(new FileReader(testFile));
        while ((line = br.readLine()) != null) {
            String tagged = (fromTrainData) ? this.tagPOS(null, tagger, line) : this.tagPOS(SimpleTokenizer.INSTANCE, tagger, line);
            if (tagged != null && !tagged.isEmpty()) {
                bw.write(tagged);
            }
        }

        // close streams
        if (br != null) br.close();
        if (bw != null) bw.close();
    }

    public float calculateAccuracy(String taggedFile, String verifyFile) throws Exception {
        String line;
        BufferedReader taggedReader = new BufferedReader(new FileReader(taggedFile));
        BufferedReader verifyReader = new BufferedReader(new FileReader(verifyFile));

        ArrayList<String> taggedSentences = new ArrayList<String>();
        ArrayList<String> verifySentences = new ArrayList<String>();
        while ((line = taggedReader.readLine()) != null) if (!line.isEmpty()) taggedSentences.add(line);
        while ((line = verifyReader.readLine()) != null) if (!line.isEmpty()) verifySentences.add(line);

        int correct = 0;
        int total = 0;
        for (int i = 0; i < taggedSentences.size(); i++) {
            if (!taggedSentences.get(i).isEmpty()) {
                String[] taggedArray = taggedSentences.get(i).split(" ");
                String[] verifyArray = verifySentences.get(i).split(" ");
                for (int j = 0; j < taggedArray.length; j++) {
                    String taggedTag = taggedArray[j].split("_")[1];
                    String verifyTag = verifyArray[j].split("_")[1];
                    if (taggedTag.equalsIgnoreCase(verifyTag)) correct++;
                    total++;
                }
            }
        }

        taggedReader.close();
        verifyReader.close();
        return correct / (float) total * 100;
    }

    public static void printUsageGuide() {
        System.out.println("POS Tagging");
        System.out.println("-----------");
        System.out.println("Usage: nlp pos-tag [train_file] [test_file]");
        System.out.println("Example: nlp pos-tag Indonesian_Manually_Tagged_Corpus_ID.tsv Wikipedia.txt");
        System.out.println();
        System.out.println("POS Tagging with splitted data");
        System.out.println("-----------");
        System.out.println("Usage: nlp pos-tag -split [train:test] [train_file]");
        System.out.println("Example: nlp pos-tag -split 9:1 Indonesian_Manually_Tagged_Corpus_ID.tsv");
    }

    public static void main(String[] args) {
        App app = new App();

        try {
            String task = args[0];
            // POS tagging task
            if ("pos-tag".equalsIgnoreCase(task)) {
                // handle proportion
                if (args.length == 4 && "-split".equalsIgnoreCase(args[1])) {
                    String rawTrainFile = args[3];
                    String[] proportions = args[2].split(":");
                    int train =  Integer.parseInt(proportions[0]);
                    int test =  Integer.parseInt(proportions[1]);
                    float trainProportion = train / (float) (test + train);
                    String rawTrainDir = rawTrainFile.substring(0, rawTrainFile.lastIndexOf(File.separator) + 1);
                    String rawTrainName = rawTrainFile.substring(rawTrainFile.lastIndexOf(File.separator) + 1, rawTrainFile.lastIndexOf("."));
                    String trainFile = rawTrainDir + rawTrainName + ".opennlp.split.train";
                    String testFile = rawTrainDir + rawTrainName + ".opennlp.split.test";
                    String testFileVerify = rawTrainDir + rawTrainName + ".opennlp.split.test.verify";
                    String modelFile = rawTrainDir + rawTrainName + ".opennlp.split.model";
                    String resultFile = rawTrainDir + rawTrainName + ".opennlp.split.test.tagged";
                    app.convertAndSplitTrainFile(rawTrainFile, trainFile, testFile, testFileVerify, trainProportion);
                    app.trainPOSTaggerModel(trainFile, modelFile);
                    app.tagPOS(modelFile, testFile, resultFile, true);
                    float accuracy = app.calculateAccuracy(resultFile, testFileVerify);
                    System.out.println("Accuracy: " + accuracy + "%");
                    System.out.println("Done");
                }
                // normal split
                else if (args.length == 3) {
                    String rawTrainFile = args[1];
                    String testFile = args[2];
                    String rawTrainDir = rawTrainFile.substring(0, rawTrainFile.lastIndexOf(File.separator) + 1);
                    String rawTrainName = rawTrainFile.substring(rawTrainFile.lastIndexOf(File.separator) + 1, rawTrainFile.lastIndexOf("."));
                    String testDir = testFile.substring(0, testFile.lastIndexOf(File.separator) + 1);
                    String testName = testFile.substring(testFile.lastIndexOf(File.separator) + 1, testFile.lastIndexOf("."));
                    String trainFile = rawTrainDir + rawTrainName + ".opennlp.train";
                    String modelFile = rawTrainDir + rawTrainName + ".opennlp.model";
                    String resultFile = testDir + testName + ".opennlp.tagged";
                    app.convertTrainFile(rawTrainFile, trainFile);
                    app.trainPOSTaggerModel(trainFile, modelFile);
                    app.tagPOS(modelFile, testFile, resultFile, false);
                    System.out.println("Done");
                } else {
                    App.printUsageGuide();
                    System.exit(-1);
                }
            } else {
                App.printUsageGuide();
                System.exit(-1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
