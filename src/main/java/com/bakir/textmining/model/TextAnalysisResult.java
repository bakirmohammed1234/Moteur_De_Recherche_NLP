package com.bakir.textmining.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TextAnalysisResult {

    @JsonProperty("tokens")
    private List<String> tokens;

    @JsonProperty("racines") // anciennement stems
    private List<String> stems;

    @JsonProperty("tfidf")
    private Map<String, Double> tfidfValues;

    @JsonProperty("similarites") // anciennement cosineSimilarities
    private Map<String, Double> similarities;

    public TextAnalysisResult() {}

    public TextAnalysisResult(List<String> tokens, List<String> stems,
                              Map<String, Double> tfidfValues,
                              Map<String, Double> similarities) {
        this.tokens = tokens;
        this.stems = stems;
        this.tfidfValues = tfidfValues;
        this.similarities = similarities;
    }

    public List<String> getTokens() {
        return tokens;
    }

    public void setTokens(List<String> tokens) {
        this.tokens = tokens;
    }

    public List<String> getStems() {
        return stems;
    }

    public void setStems(List<String> stems) {
        this.stems = stems;
    }

    public Map<String, Double> getTfidfValues() {
        return tfidfValues;
    }

    public void setTfidfValues(Map<String, Double> tfidfValues) {
        this.tfidfValues = tfidfValues;
    }

    public Map<String, Double> getSimilarites() {
        return similarities;
    }

    public void setSimilarites(Map<String, Double> similarities) {
        this.similarities = similarities;
    }
}
