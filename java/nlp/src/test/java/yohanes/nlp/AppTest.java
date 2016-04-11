package yohanes.nlp;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AppTest {

    static final String rawTrainFile = "Indonesian_Manually_Tagged_Corpus_ID.tsv";
    static final String trainFile = "Indonesian_Manually_Tagged_Corpus_ID.train";
    static final String modelFile = "Indonesian_Manually_Tagged_Corpus_ID.bin";
    static final String testFile = "Wikipedia.txt";
    static final String resultFile = "Wikipedia.tagged";

    private App app = new App();
    private String dir;

    @Before
    public void before() {
        URL rawTrainURL = this.getClass().getResource(rawTrainFile);
        dir = rawTrainURL.getPath().substring(0, rawTrainURL.getPath().lastIndexOf(File.separator) + 1);
    }

    @Test
    public void test01ConvertTrainString() {
        assertNull(app.convertTrainString("<kalimat id=1>", true));
        assertNull(app.convertTrainString("</kalimat>", true));
        assertEquals("Apel_NN", app.convertTrainString("Apel\tNN", true));
        assertEquals(" Apel_NN", app.convertTrainString("Apel\tNN", false));
    }

    @Test
    public void test02ConvertTrainFile() {
        try {
            String rawTrainFilePath = dir + rawTrainFile;
            String trainFilePath = dir + trainFile;
            app.convertTrainFile(rawTrainFilePath, trainFilePath);
            assert true;
        } catch (Exception e) {
            assert false;
        }
    }

    @Test
    public void test03TrainPOSTaggerModel() {
        try {
            String trainFilePath = dir + trainFile;
            String modelFilePath = dir + modelFile;
            app.trainPOSTaggerModel(trainFilePath, modelFilePath);
            assert true;
        } catch (Exception e) {
            assert false;
        }
    }

    @Test
    public void test04TagPOS() {
        try {
            String modelFilePath = dir + modelFile;
            String testFilePath = dir + testFile;
            String resultFilePath = dir + resultFile;
            app.tagPOS(modelFilePath, testFilePath, resultFilePath, false);
            assert true;
        } catch (Exception e) {
            assert false;
        }
    }
}
