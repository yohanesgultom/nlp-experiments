package yohanes.nlp;

import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static org.junit.Assert.*;
/**
 * Created by yohanes on 12/04/16.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ParserTest {
    static final String rawTrainFile = "150324.001-300.bracket";
    static final String trainFile = "150324.001-300.train";
    static final String modelFile = "150324.001-300.bin";
    static final String testFile = "Wikipedia.txt";
    static final String resultFile = "Wikipedia.parsed";

    Parser parser = new Parser();
    private String dir;

    @Before
    public void before() {
        URL rawTrainURL = this.getClass().getResource(rawTrainFile);
        dir = rawTrainURL.getPath().substring(0, rawTrainURL.getPath().lastIndexOf(File.separator) + 1);
    }

    @Test
    public void test01ConvertTrainFile() {
        try {
            String rawTrainFilePath = dir + rawTrainFile;
            String trainFilePath = dir + trainFile;
            parser.convertTrainFile(rawTrainFilePath, trainFilePath);
            assert true;
        } catch (Exception e) {
            e.printStackTrace();
            assert false;
        }
    }

    public void testParse() {
        InputStream modelIn = null;
        String[] sentences = { "Some say November .", "I say 1992 ."};
        try {
            modelIn = new FileInputStream("/home/yohanes/Downloads/en-parser-chunking.bin");
            ParserModel model = new ParserModel(modelIn);
            opennlp.tools.parser.Parser apiParser = ParserFactory.create(model);
            for (String sentence:sentences) {
                String tree = parser.parse(apiParser, sentence);
                System.out.println(tree);
            }
            assert true;
        } catch (Exception e) {
            e.printStackTrace();
            assert false;
        } finally {
            if (modelIn != null) {
                try {
                    modelIn.close();
                }
                catch (IOException e) {
                }
            }
        }

    }

}
