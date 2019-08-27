package eu.fbk.dh.EventRelater.article_models;

import java.util.Arrays;

/**
 * @author Mohamad Baalbaki
 */
public class Articles {
    private ArticleResult[] results;

    public ArticleResult[] getResults() {
        return results;
    }

    public void setResults(ArticleResult[] results) {
        this.results = results;
    }

    @Override
    public String toString() {
        return "Articles{" +
                "results=" + Arrays.toString(results) +
                '}';
    }
}
