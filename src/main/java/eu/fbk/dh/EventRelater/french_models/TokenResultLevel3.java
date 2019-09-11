package eu.fbk.dh.EventRelater.french_models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenResultLevel3 {
    @JsonProperty
    private AnalysisResult[] analysis_list;

    public AnalysisResult[] getAnalysis_list() {
        return analysis_list;
    }

    public void setAnalysis_list(AnalysisResult[] analysis_list) {
        this.analysis_list = analysis_list;
    }

    @Override
    public String toString() {
        return "TokenResultLevel3{" +
                "analysis_list=" + Arrays.toString(analysis_list) +
                '}';
    }
}
