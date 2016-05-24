package yohanes.nlp;

import opennlp.tools.dictionary.Dictionary;
import opennlp.tools.namefind.*;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.util.*;
import opennlp.tools.util.eval.FMeasure;
import opennlp.tools.util.featuregen.*;
import org.apache.commons.lang3.StringUtils;
import yohanes.nlp.tools.util.FMeasureMUC;
import yohanes.nlp.tools.util.TokenNameFinderEvaluatorMUC;
import yohanes.nlp.tools.util.TokenPosFeatureGenerator;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by yohanes on 23/04/16.
 */
public class Recognizer {

    private static final String MODEL_NAME = "train";
    private static final String MODEL_EXT = "model";
    private static final String MODEL_LANG = "id";
    private static final String TAGGER_MODEL = "pos.id.model";

    private static final String MAXENT_CUTOFF = "5";
    private static final String MAXENT_ITERATIONS = "300";

    private TokenNameFinderModel model;
    private TokenNameFinder nameFinder;
    private TokenNameFinderEvaluator evaluatorExactMatch;
    private TokenNameFinderEvaluatorMUC evaluatorMUC;
    private Tagger tagger;

    public Recognizer(int scenario) throws Exception {
        InputStream modelIn = this.getClass().getResourceAsStream(this.getModeFileName(scenario));
        InputStream taggerModelIn = this.getClass().getResourceAsStream(TAGGER_MODEL);
        TokenNameFinderModel newModel = new TokenNameFinderModel(modelIn);
        this.setModel(newModel);
        this.tagger = new Tagger(taggerModelIn);
        modelIn.close();
    }

    public Recognizer(String trainFilepath, String lang, String name, int scenario) throws Exception {
        String modelOutPath;
        // dicts
        InputStream organizationIn = this.getClass().getResourceAsStream("organization.dict");
        InputStream locationIn = this.getClass().getResourceAsStream("location.id.dict");
        InputStream personIn = this.getClass().getResourceAsStream("person.id.dict");
        Dictionary organizationDict = new Dictionary(organizationIn);
        Dictionary locationDict = new Dictionary(locationIn);
        Dictionary personDict = new Dictionary(personIn);
        // tagger
        // init POS tagger with model
        InputStream taggerModelIn = this.getClass().getResourceAsStream("pos.id.model");
        this.tagger = new Tagger(taggerModelIn);

        switch (scenario) {
            case 1:
                this.train(trainFilepath, lang, name, new AdaptiveFeatureGenerator[]{
                        new WindowFeatureGenerator(new TokenClassFeatureGenerator(true), 2, 2),
                        new SentenceFeatureGenerator(true, true),
                });
                break;
            case 2:
                this.train(trainFilepath, lang, name, new AdaptiveFeatureGenerator[]{
                        new WindowFeatureGenerator(new TokenFeatureGenerator(true), 2, 2),
                        new WindowFeatureGenerator(new TokenClassFeatureGenerator(true), 2, 2),
                        new SentenceFeatureGenerator(true, true),
                });
                break;
            case 3:
                this.train(trainFilepath, lang, name, new AdaptiveFeatureGenerator[]{
                        new WindowFeatureGenerator(new TokenFeatureGenerator(true), 2, 2),
                        new WindowFeatureGenerator(new TokenClassFeatureGenerator(true), 2, 2),
                        new SentenceFeatureGenerator(true, true),
                        new OutcomePriorFeatureGenerator(),
                });
                break;
            case 4:
                this.train(trainFilepath, lang, name, new AdaptiveFeatureGenerator[]{
                        new WindowFeatureGenerator(new TokenFeatureGenerator(true), 2, 2),
                        new WindowFeatureGenerator(new TokenClassFeatureGenerator(true), 2, 2),
                        new SentenceFeatureGenerator(true, true),
                        new OutcomePriorFeatureGenerator(),
                        new PreviousMapFeatureGenerator(),
                });
                break;
            case 5:
                this.train(trainFilepath, lang, name, new AdaptiveFeatureGenerator[]{
                        new WindowFeatureGenerator(new TokenPosFeatureGenerator(this.tagger), 2, 2),
                        new WindowFeatureGenerator(new TokenFeatureGenerator(true), 2, 2),
                        new WindowFeatureGenerator(new TokenClassFeatureGenerator(true), 2, 2),
                        new SentenceFeatureGenerator(true, true),
                        new OutcomePriorFeatureGenerator(),
                        new PreviousMapFeatureGenerator(),
                });
                break;
            case 6:
                this.train(trainFilepath, lang, name, new AdaptiveFeatureGenerator[]{
                        new WindowFeatureGenerator(new TokenFeatureGenerator(true), 2, 2),
                        new WindowFeatureGenerator(new TokenClassFeatureGenerator(true), 2, 2),
                        new SentenceFeatureGenerator(true, true),
                        new OutcomePriorFeatureGenerator(),
                        new PreviousMapFeatureGenerator(),
                        new DictionaryFeatureGenerator(locationDict),
                        new DictionaryFeatureGenerator(personDict),
                        new DictionaryFeatureGenerator(organizationDict),
                });
                break;
            case 7:
                this.train(trainFilepath, lang, name, new AdaptiveFeatureGenerator[]{
                        new WindowFeatureGenerator(new TokenPosFeatureGenerator(this.tagger), 2, 2),
                        new WindowFeatureGenerator(new TokenFeatureGenerator(true), 2, 2),
                        new WindowFeatureGenerator(new TokenClassFeatureGenerator(true), 2, 2),
                        new BigramNameFeatureGenerator(),
                        new SentenceFeatureGenerator(true, true),
                        new OutcomePriorFeatureGenerator(),
                        new PreviousMapFeatureGenerator(),
                        new DictionaryFeatureGenerator(locationDict),
                        new DictionaryFeatureGenerator(personDict),
                        new DictionaryFeatureGenerator(organizationDict),
                });
                break;
            default:
                this.train(trainFilepath, lang, name, new AdaptiveFeatureGenerator[]{
                        new WindowFeatureGenerator(new TokenFeatureGenerator(), 2, 2),
                        new WindowFeatureGenerator(new TokenClassFeatureGenerator(true), 2, 2),
                        new SentenceFeatureGenerator(true, false),
                        new OutcomePriorFeatureGenerator(),
                        new PreviousMapFeatureGenerator(),
                        new BigramNameFeatureGenerator(),
                });
        }
        // save model
        modelOutPath = this.getModeFileName(scenario, lang);
        BufferedOutputStream modelOut = new BufferedOutputStream(new FileOutputStream(modelOutPath));
        model.serialize(modelOut);
        modelOut.close();

        // close streams
        organizationIn.close();
        locationIn.close();
        personIn.close();
        taggerModelIn.close();
    }

    private void setModel(TokenNameFinderModel newModel) throws Exception {
        this.model = newModel;
        if (this.nameFinder == null || !this.model.equals(newModel)) {
            this.nameFinder = new NameFinderME(this.model);
        }
    }

    private void initEvaluator(String logFile) throws Exception {
        this.evaluatorExactMatch = new TokenNameFinderEvaluator(this.nameFinder, new LogFileMonitor(logFile));
        this.evaluatorMUC = new TokenNameFinderEvaluatorMUC(this.nameFinder, new LogFileMonitor(logFile));
    }

    private void train(String trainFilepath, String lang, String name, AdaptiveFeatureGenerator[] adaptiveFeatureGenerators) throws Exception {
        ObjectStream<String> lineStream = new PlainTextByLineStream(new FileInputStream(trainFilepath), Charset.forName("UTF-8"));
        ObjectStream<NameSample> sampleStream = new NameSampleDataStream(lineStream);
        AdaptiveFeatureGenerator featureGenerator = new CachedFeatureGenerator(adaptiveFeatureGenerators);
        TrainingParameters trainingParameters = TrainingParameters.defaultParams();
        trainingParameters.put("Cutoff", MAXENT_CUTOFF);
        trainingParameters.put("Iterations", MAXENT_ITERATIONS);
        TokenNameFinderModel newModel = NameFinderME.train(lang, name, sampleStream, trainingParameters, featureGenerator, Collections.<String, Object>emptyMap());
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
                bw.write(toWrite + "\n");
            }
        }

        // close streams
        if (br != null) br.close();
        if (bw != null) bw.close();
    }

    public FMeasure evaluateExactMatch(String testFilePath) throws Exception {
        this.initEvaluator(testFilePath + ".missed");
        evaluatorExactMatch.evaluate(this.getSampleStream(testFilePath));
        return evaluatorExactMatch.getFMeasure();
    }

    public FMeasureMUC evaluateMUC(String testFilePath) throws Exception {
        this.initEvaluator(testFilePath + ".missed");
        evaluatorMUC.evaluate(this.getSampleStream(testFilePath));
        return evaluatorMUC.getFMeasure();
    }

    private ObjectStream<NameSample> getSampleStream(String file) throws IOException {
        InputStreamFactory isf = new MarkableFileInputStreamFactory(new File(file));
        ObjectStream<String> lineStream = new PlainTextByLineStream(isf, "UTF-8");
        return new NameSampleDataStream(lineStream);
    }

    public static void convertTrainFile(String inputFilePath, String outTrainFilePath) throws Exception {
        convertTrainFile(inputFilePath, outTrainFilePath, null, 0f);
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


//            // random splitting
//            int count = 0;
//            Random randomizer = new Random();
//            while (count < limit && lines.size() > 0) {
//                int random = randomizer.nextInt(lines.size());
//                bwTest.write(lines.remove(random));
//                count++;
//            }
//            for (String toWrite:lines) {
//                bwTrain.write(toWrite);
//            }

            // fixed splitting
            for (int i = 0; i < lines.size(); i++) {
                if (i < limit) {
                    bwTrain.write(lines.get(i));
                } else {
                    bwTest.write(lines.get(i));
                }
            }

            if (bwTest != null) bwTest.close();
        } else {
            for (String l : lines) {
                bwTrain.write(l);
            }
        }
        if (bwTrain != null) bwTrain.close();

    }

    public static String spansToMUCAnnotatedString(Span[] spans, String[] tokens) {
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) {
                res.append(" ");
            }
            boolean found = false;
            for (Span sp : spans) {
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

    private String getModeFileName(int scenario) {
        return this.getModeFileName(scenario, MODEL_LANG);
    }

    private String getModeFileName(int scenario, String lang) {
        return StringUtils.join(new String[]{MODEL_NAME, lang, String.valueOf(scenario), MODEL_EXT}, ".");
    }

}

class LogFileMonitor implements TokenNameFinderEvaluationMonitor {

    private Writer writer;

    public LogFileMonitor(String filename) throws Exception {
        File outFile = new File(filename);
        if (!outFile.exists()) {
            outFile.createNewFile();
        }
        FileWriter fw = new FileWriter(outFile.getAbsoluteFile());
        this.writer = new BufferedWriter(fw);
    }

    public void correctlyClassified(NameSample reference, NameSample prediction) {
        // do nothing
    }

    public void missclassified(NameSample reference, NameSample prediction) {
        try {
            this.writer.write(reference.toString() + "\n");
            this.writer.write(prediction.toString() + "\n");
            this.writer.write("\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
