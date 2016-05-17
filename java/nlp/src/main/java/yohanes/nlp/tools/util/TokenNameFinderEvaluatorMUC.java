package yohanes.nlp.tools.util;

import opennlp.tools.namefind.NameSample;
import opennlp.tools.namefind.TokenNameFinder;
import opennlp.tools.namefind.TokenNameFinderEvaluationMonitor;
import opennlp.tools.util.Span;
import opennlp.tools.util.eval.Evaluator;

/**
 * Created by yohanesgultom on 17/05/16.
 */
public class TokenNameFinderEvaluatorMUC extends Evaluator<NameSample> {
    private FMeasureMUC fmeasure = new FMeasureMUC();
    private TokenNameFinder nameFinder;

    public TokenNameFinderEvaluatorMUC(TokenNameFinder nameFinder, TokenNameFinderEvaluationMonitor... listeners) {
        super(listeners);
        this.nameFinder = nameFinder;
    }

    protected NameSample processSample(NameSample reference) {
        if(reference.isClearAdaptiveDataSet()) {
            this.nameFinder.clearAdaptiveData();
        }

        Span[] predictedNames = this.nameFinder.find(reference.getSentence());
        Span[] references = reference.getNames();

        for(int i = 0; i < references.length; ++i) {
            if(references[i].getType() == null) {
                references[i] = new Span(references[i].getStart(), references[i].getEnd(), "default");
            }
        }

        this.fmeasure.updateScores(references, predictedNames);
        return new NameSample(reference.getSentence(), predictedNames, reference.isClearAdaptiveDataSet());
    }

    public FMeasureMUC getFMeasure() {
        return this.fmeasure;
    }
}

