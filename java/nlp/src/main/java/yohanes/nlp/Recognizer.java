package yohanes.nlp;

import opennlp.tools.namefind.*;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.Span;
import opennlp.tools.util.TrainingParameters;
import opennlp.tools.util.eval.FMeasure;
import opennlp.tools.util.featuregen.*;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by yohanes on 23/04/16.
 */
public class Recognizer {

    private static final String DEFAULT_MODEL_FILE = "train.id.model";
    private static final String MODEL_1_FILE= "train.id.model";

    private TokenNameFinderModel model;
    private NameFinderME nameFinder;

    public Recognizer() throws Exception {
        InputStream modelIn = this.getClass().getResourceAsStream(DEFAULT_MODEL_FILE);
        TokenNameFinderModel newModel = new TokenNameFinderModel(modelIn);
        this.model = newModel;
        this.nameFinder = new NameFinderME(this.model);
        modelIn.close();
    }

    public Recognizer(int scenario) throws Exception {
        InputStream modelIn;
        switch (scenario) {
            case 1:
                modelIn = this.getClass().getResourceAsStream(MODEL_1_FILE);
                break;
            default:
                modelIn = this.getClass().getResourceAsStream(DEFAULT_MODEL_FILE);
        }
        TokenNameFinderModel newModel = new TokenNameFinderModel(modelIn);
        this.model = newModel;
        this.nameFinder = new NameFinderME(this.model);
        modelIn.close();
    }


    public Recognizer(String trainFilepath, String lang, String name, int type, String modelOutPath) throws Exception {
        switch (type) {
            case 1:
                this.train1(trainFilepath, lang, name);
                break;
            default:
                this.train(trainFilepath, lang, name);
        }
        // save model
        if (StringUtils.isNotEmpty(modelOutPath)) {
            BufferedOutputStream modelOut = new BufferedOutputStream(new FileOutputStream(modelOutPath));
            model.serialize(modelOut);
            modelOut.close();
        }
    }

    public void setModel(TokenNameFinderModel newModel) {
        this.model = newModel;
        if (this.nameFinder == null || !this.model.equals(newModel)) {
            this.nameFinder = new NameFinderME(this.model);
        }
    }

    private void train(String trainFilepath, String lang, String name) throws Exception {
        ObjectStream<String> lineStream = new PlainTextByLineStream(new FileInputStream(trainFilepath), Charset.forName("UTF-8"));
        ObjectStream<NameSample> sampleStream = new NameSampleDataStream(lineStream);
        TokenNameFinderModel newModel = NameFinderME.train(lang, name, sampleStream, Collections.<String, Object>emptyMap());
        this.setModel(newModel);
        sampleStream.close();
        lineStream.close();
    }


    private void train1(String trainFilepath, String lang, String name) throws Exception {
        ObjectStream<String> lineStream = new PlainTextByLineStream(new FileInputStream(trainFilepath), Charset.forName("UTF-8"));
        ObjectStream<NameSample> sampleStream = new NameSampleDataStream(lineStream);
        AdaptiveFeatureGenerator featureGenerator = new CachedFeatureGenerator(
            new AdaptiveFeatureGenerator[]{
                    new WindowFeatureGenerator(new TokenFeatureGenerator(), 2, 2),
                    new WindowFeatureGenerator(new TokenClassFeatureGenerator(true), 2, 2),
                    new OutcomePriorFeatureGenerator(),
                    new PreviousMapFeatureGenerator(),
                    new BigramNameFeatureGenerator(),
                    new SentenceFeatureGenerator(true, false)
            }
        );
        TokenNameFinderModel newModel = NameFinderME.train(lang, name, sampleStream, TrainingParameters.defaultParams(), featureGenerator, Collections.<String, Object>emptyMap());
        this.setModel(newModel);
        sampleStream.close();
        lineStream.close();
    }

    public Span[] find(String[] tokens) {
        return this.nameFinder.find(tokens);
    }

    public void find(String testFilePath, String resultFilePath) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(testFilePath));
        File outFile = new File(resultFilePath);

        // if file doesn't exists, then create it
        if (!outFile.exists()) {
            outFile.createNewFile();
        }

        FileWriter fw = new FileWriter(outFile.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);

        String[] tokens;
        String line, toWrite;
        Span[] spans;
        SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;
        while ((line = br.readLine()) != null) {
            tokens = tokenizer.tokenize(line.trim());
            spans = this.find(tokens);
            toWrite = Recognizer.spansToMUCAnnotatedString(spans, tokens);
            if (toWrite != null) {
                bw.write(toWrite);
            }
        }

        // close streams
        if (br != null) br.close();
        if (bw != null) bw.close();
    }

    public FMeasure evaluateDefault(String testFilePath) throws IOException {
        TokenNameFinderEvaluator evaluator = new TokenNameFinderEvaluator(this.nameFinder);
        ObjectStream<String> lineStream = new PlainTextByLineStream(new FileInputStream(testFilePath), Charset.forName("UTF-8"));
        ObjectStream<NameSample> sampleStream = new NameSampleDataStream(lineStream);
        evaluator.evaluate(sampleStream);
        return evaluator.getFMeasure();
    }

    public static void convertTrainFile(String inputFilePath, String outFilePath) throws Exception {
        Recognizer.convertTrainFile(inputFilePath, outFilePath, null, 0f);
    }

    public static void convertTrainFile(String inputFilePath, String outTrainFilePath, String outTestFilePath, float proportionFrac) throws Exception {
        File outTrainFile = new File(outTrainFilePath);
        // if file does not exists, then create it
        if (!outTrainFile.exists()) {
            outTrainFile.createNewFile();
        }

        // read files
        BufferedReader br = new BufferedReader(new FileReader(inputFilePath));

        String line;
        List<String> lines = new ArrayList<String>();
        while ((line = br.readLine()) != null) {
            if (StringUtils.isNotEmpty(line)) {
                line = line.split("\t")[0];
                line = StringUtils.replace(line, "<ENAMEX TYPE=\"PERSON\">", " <START:person> ");
                line = StringUtils.replace(line, "<ENAMEX TYPE=\"ORGANIZATION\">", " <START:organization> ");
                line = StringUtils.replace(line, "<ENAMEX TYPE=\"LOCATION\">", " <START:location> ");
                line = StringUtils.replace(line, "</ENAMEX>", " <END> ");
//                line = StringUtils.replace(line, ",", "");
//                line = StringUtils.replace(line, "\"", "");
//                line = StringUtils.replace(line, "(", "");
//                line = StringUtils.replace(line, ")", "");
//                line = StringUtils.replace(line, "-", " ");
//                line = StringUtils.replace(line, "/", " ");
                lines.add(line.trim() + "\n");
            }
        }
        if (br != null) br.close();

        // splitting train file
        BufferedWriter bwTrain = new BufferedWriter(new FileWriter(outTrainFile.getAbsoluteFile()));
        if (proportionFrac > 0f && StringUtils.isNotEmpty(outTestFilePath)) {
            File outTestFile = new File(outTestFilePath);
            if (!outTestFile.exists()) {
                outTestFile.createNewFile();
            }
            BufferedWriter bwTest = new BufferedWriter(new FileWriter(outTestFile.getAbsoluteFile()));
            int limit = Math.round(lines.size() * proportionFrac);
            for (int i = 0; i < lines.size(); i++) {
                if (i < limit) {
                    bwTrain.write(lines.get(i));
                } else {
                    bwTest.write(lines.get(i));
                }
            }
            if (bwTest != null) bwTest.close();
        } else {
            for (String l:lines) {
                bwTrain.write(l);
            }
        }
        if (bwTrain != null) bwTrain.close();

    }

    public static List<String> spansToList(Span[] spans, String[] tokens) {
        ArrayList<String> res = new ArrayList<String>();
        for (Span sp:spans) {
            String text = "";
            for (int i = sp.getStart(); i < sp.getEnd(); i++) {
                if (i > sp.getStart()) {
                    text += " ";
                }
                text += tokens[i];
            }
            res.add(text + "\t" + sp.getType());
        }
        return res;
    }

    public static String spansToMUCAnnotatedString(Span[] spans, String[] tokens) {
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) {
                res.append(" ");
            }
            boolean found = false;
            for (Span sp:spans) {
                if (i == sp.getStart()) {
                    res.append("<ENAMEX TYPE=\"").append(sp.getType().toUpperCase()).append("\">");
                }
                if (i >= sp.getStart() && i < sp.getEnd()) {
                    res.append(tokens[i]);
                    found = true;
                }
                if (i == (sp.getEnd() - 1)) {
                    res.append("</ENAMEX>");
                }
            }
            if (!found) {
                res.append(tokens[i]);
            }
        }
        return res.toString();
    }

    public static void main(String[] args) {
        String trainFilepath = (args.length >= 1) ? args[0] : "train.txt";
        int trainType = 0;
        try {
            trainType = (args.length >= 2) ? Integer.parseInt(args[1]) : 0;
        } catch (Exception e) {
            trainType = 0;
        }

        String sentence[] = new String[]{"Jokowi", "mendarat", "di", "Bandara", "Halim", "." };
        String sentenceStr = "Jokowi mendarat di Bandara Halim.";
        ObjectStream<String> lineStream = null;
        ObjectStream<NameSample> sampleStream = null;
        try {
            String modelName = DEFAULT_MODEL_FILE;
            String trainDir = trainFilepath.substring(0, trainFilepath.lastIndexOf(File.separator) + 1);
            String trainName = trainFilepath.substring(trainFilepath.lastIndexOf(File.separator) + 1, trainFilepath.lastIndexOf("."));
            String convertedTrainFilepath = trainDir + trainName + ".train" ;
            String modelFilepath = trainDir + modelName ;
            Recognizer.convertTrainFile(trainFilepath, convertedTrainFilepath);

            Recognizer recognizer = new Recognizer(convertedTrainFilepath, "id", modelName, trainType, modelFilepath);
            FMeasure f1 = recognizer.evaluateDefault(convertedTrainFilepath);
            System.out.println(f1);
            Span[] nameSpans = recognizer.find(sentence);
            System.out.println(Recognizer.spansToList(nameSpans, sentence));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (lineStream != null) lineStream.close();
                if (sampleStream != null) sampleStream.close();
            } catch (Exception ex) {

            }
        }
    }

}
