package yohanes.nlp.tools.util;

import opennlp.tools.util.Span;
import opennlp.tools.util.eval.FMeasure;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by yohanesgultom on 17/05/16.
 */
public class FMeasureTest {

    final static String PERSON = "PERSON";
    final static String ORGANIZATION = "ORGANIZATION";
    final static String LOCATION = "LOCATION";
    final static double ERROR_MARGIN = 0.001;

    // References (gold standard): <ENAMEX =”PERSON”>Kristoff Bjorgman</ENAMEX> menemani <ENAMEX=”PERSON”>Anna</ENAMEX> ketika <ENAMEX =”PERSON”>Anna</ENAMEX> mencari kakaknya di <ENAMEX =”LOCATION”>Gunung Utara</ENAMEX>.
    static Object[] references = new Span[]{ new Span(0, 2, PERSON), new Span(3, 4, PERSON), new Span(5, 6, PERSON), new Span(9, 11, LOCATION) };

    // Predictions: <ENAMEX =”ORGANIZATION”>Kristoff Bjorgman</ENAMEX> menemani <ENAMEX=”PERSON”>Anna</ENAMEX> ketika <ENAMEX =”PERSON”>Anna</ENAMEX> mencari kakaknya di <ENAMEX =”ORGANIZATION”>Gunung</ENAMEX> <ENAMEX=”LOCATION”>Utara</ENAMEX>.
    static Object[] predictions = new Span[]{ new Span(0, 2, ORGANIZATION), new Span(3, 4, PERSON), new Span(5, 6, PERSON), new Span(9, 10, ORGANIZATION), new Span(10, 11, LOCATION) };

    @Test
    public void testFMeasure() {
        FMeasure f1 = new FMeasure();
        f1.updateScores(references, predictions);
        Assert.assertEquals(0.4, f1.getPrecisionScore(), ERROR_MARGIN);
        Assert.assertEquals(0.5, f1.getRecallScore(), ERROR_MARGIN);
        Assert.assertEquals(2.0 * 0.4 * 0.5 / (0.4 + 0.5), f1.getFMeasure(), ERROR_MARGIN);
    }

    @Test
    public void testFMeasureMUC() {
        int cor = FMeasureMUC.countCOR(references, predictions);
        Assert.assertEquals(6, cor);
        FMeasureMUC f1 = new FMeasureMUC();
        f1.updateScores(references, predictions);
        Assert.assertEquals(0.6, f1.getPrecisionScore(), ERROR_MARGIN);
        Assert.assertEquals(0.75, f1.getRecallScore(), ERROR_MARGIN);
        Assert.assertEquals(2.0 * 0.6 * 0.75 / (0.6 + 0.75), f1.getFMeasure(), ERROR_MARGIN);
    }
}
