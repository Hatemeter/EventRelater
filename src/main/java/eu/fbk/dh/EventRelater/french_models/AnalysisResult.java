package eu.fbk.dh.EventRelater.french_models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AnalysisResult {
    @JsonProperty
    private String original_form;
    @JsonProperty
    private String lemma;

    public String getOriginal_form() {
        return original_form;
    }

    public void setOriginal_form(String original_form) {
        this.original_form = original_form;
    }

    public String getLemma() {
        return lemma;
    }

    public void setLemma(String lemma) {
        this.lemma = lemma;
    }

    @Override
    public String toString() {
        return "AnalysisResult{" +
                "original_form='" + original_form + '\'' +
                ", lemma='" + lemma + '\'' +
                '}';
    }
}
