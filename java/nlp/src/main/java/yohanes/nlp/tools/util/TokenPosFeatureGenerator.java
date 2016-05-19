package yohanes.nlp.tools.util;

import opennlp.tools.util.StringUtil;
import opennlp.tools.util.featuregen.FeatureGeneratorAdapter;
import yohanes.nlp.Tagger;

import java.util.HashMap;
import java.util.List;

/**
 * Created by yohanesgultom on 20/05/16.
 */
public class TokenPosFeatureGenerator extends FeatureGeneratorAdapter {
    private Tagger tagger;
    private static HashMap<Integer, String[]> cache;

    public TokenPosFeatureGenerator(Tagger tagger) {
        this.tagger = tagger;
        if (cache == null) {
            cache = new HashMap<Integer, String[]>();
        }
    }

    public void createFeatures(List<String> features, String[] tokens, int index, String[] preds) {
        String[] tags;
        if (cache.containsKey(tokens.hashCode())) {
            tags = cache.get(tokens.hashCode());
        } else {
            tags = tagger.tagPOS(tokens);
            cache.put(tokens.hashCode(), tags);
        }
        features.add("p=" + tags[index]);
    }
}
