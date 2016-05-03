package yohanes.nlp;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.NameSample;
import opennlp.tools.namefind.NameSampleDataStream;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.Span;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Collections;

/**
 * Created by yohanes on 23/04/16.
 */
public class Recognizer {

    public void convertTrainFile(String inputFilePath, String outFilePath) throws Exception {
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

    public static void main(String[] args) {
        Charset charset = Charset.forName("UTF-8");
        String trainFilepath = (args.length >= 1) ? args[0] : "train.txt";
        String sentence[] = new String[]{"Samsung", "mendarat", "di", "Bandara", "Halim", "." };
        ObjectStream<String> lineStream = null;
        ObjectStream<NameSample> sampleStream = null;
        try {
            Recognizer app = new Recognizer();
            String trainDir = trainFilepath.substring(0, trainFilepath.lastIndexOf(File.separator) + 1);
            String trainName = trainFilepath.substring(trainFilepath.lastIndexOf(File.separator) + 1, trainFilepath.lastIndexOf("."));
            String convertedTrainFilepath = trainDir + trainName + ".train" ;
            app.convertTrainFile(trainFilepath, convertedTrainFilepath);

            lineStream = new PlainTextByLineStream(new FileInputStream(convertedTrainFilepath), charset);
            sampleStream = new NameSampleDataStream(lineStream);
            TokenNameFinderModel model = NameFinderME.train("id", "train", sampleStream, Collections.<String, Object>emptyMap());
            NameFinderME nfm = new NameFinderME(model);
            Span nameSpans[] = nfm.find(sentence);
            for (Span sp:nameSpans) {
                String text = "";
                for (int i = sp.getStart(); i < sp.getEnd(); i++) {
                    if (i > sp.getStart()) {
                        text += " ";
                    }
                    text += sentence[i];
                }
                System.out.println(text+ " " + sp.getType());
            }
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
