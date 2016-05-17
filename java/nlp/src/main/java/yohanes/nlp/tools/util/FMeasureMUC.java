package yohanes.nlp.tools.util;

import opennlp.tools.util.Span;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by yohanesgultom on 17/05/16.
 */
public class FMeasureMUC {
    private long cor;
    private long act;
    private long pos;

    public FMeasureMUC() {
    }

    public double getPrecisionScore() {
        return this.act > 0L?(double)this.cor / (double)this.act:0.0D;
    }

    public double getRecallScore() {
        return this.pos > 0L?(double)this.cor / (double)this.pos:0.0D;
    }

    public double getFMeasure() {
        return this.getPrecisionScore() + this.getRecallScore() > 0.0D?2.0D * this.getPrecisionScore() * this.getRecallScore() / (this.getPrecisionScore() + this.getRecallScore()):-1.0D;
    }

    public void updateScores(Object[] references, Object[] predictions) {
        this.cor += (long)countCOR(references, predictions); // target
        this.act += (long)predictions.length * 2; // selected
        this.pos += (long)references.length * 2; // target
    }

    public void mergeInto(FMeasureMUC measure) {
        this.act += measure.act;
        this.cor += measure.cor;
        this.pos += measure.pos;
    }

    public String toString() {
        return "Precision: " + Double.toString(this.getPrecisionScore()) + "\n" + "Recall: " + Double.toString(this.getRecallScore()) + "\n" + "F-Measure: " + Double.toString(this.getFMeasure());
    }

    static int countCOR(Object[] references, Object[] predictions) {
        ArrayList predListSpans = new ArrayList(predictions.length);
        Collections.addAll(predListSpans, predictions);
        int cor = 0;
        Object matchedItem = null;

        for(int referenceIndex = 0; referenceIndex < references.length; ++referenceIndex) {
            Object referenceName = references[referenceIndex];

            for(int predIndex = 0; predIndex < predListSpans.size(); ++predIndex) {
                Span referenceSpan = (Span) referenceName;
                Span predictionSpan = (Span) predListSpans.get(predIndex);
                // only count span with type (predicted as named-entity
                if (predictionSpan.getType() != null) {
                    // compare TYPE
                    if(referenceSpan.getStart() <= predictionSpan.getStart()
                            && referenceSpan.getEnd() >= predictionSpan.getEnd()
                            && predictionSpan.getType().equals(referenceSpan.getType())) {
                        matchedItem = predictionSpan;
                        cor++;
                    }
                    // compare TEXT
                    if (referenceSpan.getStart() == predictionSpan.getStart()
                            && referenceSpan.getEnd() == predictionSpan.getEnd()) {
                        matchedItem = predictionSpan;
                        cor++;
                    }
                }
            }

            if(matchedItem != null) {
                predListSpans.remove(matchedItem);
            }
        }

        return cor;
    }

    public static double precision(Object[] references, Object[] predictions) {
        return predictions.length > 0?(double)countCOR(references, predictions) / (double)predictions.length:0.0D / 0.0;
    }

    public static double recall(Object[] references, Object[] predictions) {
        return references.length > 0?(double)countCOR(references, predictions) / (double)references.length:0.0D / 0.0;
    }
}
