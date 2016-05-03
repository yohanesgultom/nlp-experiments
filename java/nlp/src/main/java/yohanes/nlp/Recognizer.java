package yohanes.nlp;

import opennlp.tools.namefind.*;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.Span;
import opennlp.tools.util.TrainingParameters;
import opennlp.tools.util.featuregen.*;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by yohanes on 23/04/16.
 */
public class Recognizer {

    private TokenNameFinderModel model;
    private NameFinderME nameFinder;

    public Recognizer(TokenNameFinderModel newModel) {
        this.model = newModel;
        this.nameFinder = new NameFinderME(this.model);
    }

    public Recognizer(String trainFilepath, String lang, String name) throws Exception {
        this.train(trainFilepath, lang, name);
    }

    public Recognizer(String trainFilepath, String lang, String name, int type) throws Exception {
        switch (type) {
            case 1:
                this.train1(trainFilepath, lang, name);
                break;
            default:
                this.train(trainFilepath, lang, name);
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
    }

    public Span[] find(String[] tokens) {
        return this.nameFinder.find(tokens);
    }

    public static void convertTrainFile(String inputFilePath, String outFilePath) throws Exception {
        File outTrainFile = new File(outFilePath);
        // if file does not exists, then create it
        if (!outTrainFile.exists()) {
            outTrainFile.createNewFile();
        }

        // write to files
        BufferedReader br = new BufferedReader(new FileReader(inputFilePath));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outTrainFile.getAbsoluteFile()));

        String line;
        while ((line = br.readLine()) != null) {
            if (StringUtils.isNotEmpty(line)) {
                line = line.split("\t")[0];
                line = StringUtils.replace(line, "<ENAMEX TYPE=\"PERSON\">", "<START:person> ");
                line = StringUtils.replace(line, "<ENAMEX TYPE=\"ORGANIZATION\">", "<START:organization> ");
                line = StringUtils.replace(line, "<ENAMEX TYPE=\"LOCATION\">", "<START:location> ");
                line = StringUtils.replace(line, "</ENAMEX>", " <END>");
                line = StringUtils.replace(line, ",", "");
                line = StringUtils.replace(line, "\"", "");
                bw.write(line.trim() + "\n");
            }
        }

        if (bw != null) bw.close();
        if (br != null) br.close();

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

    public static void main(String[] args) {
        String trainFilepath = (args.length >= 1) ? args[0] : "train.txt";
        String sentence[] = new String[]{"Samsung", "mendarat", "di", "Bandara", "Halim", "." };
        ObjectStream<String> lineStream = null;
        ObjectStream<NameSample> sampleStream = null;
        try {
            String trainDir = trainFilepath.substring(0, trainFilepath.lastIndexOf(File.separator) + 1);
            String trainName = trainFilepath.substring(trainFilepath.lastIndexOf(File.separator) + 1, trainFilepath.lastIndexOf("."));
            String convertedTrainFilepath = trainDir + trainName + ".train" ;
            Recognizer.convertTrainFile(trainFilepath, convertedTrainFilepath);

            Recognizer recognizer = new Recognizer(convertedTrainFilepath, "id", "news.id", 1);
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
