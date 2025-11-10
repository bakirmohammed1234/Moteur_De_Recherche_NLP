package com.bakir.textmining.Service;

import com.bakir.textmining.model.TextAnalysisResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import safar.util.tokenization.impl.SAFARTokenizer;
import safar.util.tokenization.interfaces.ITokenizer;
import safar.basic.morphology.stemmer.factory.StemmerFactory;
import safar.basic.morphology.stemmer.interfaces.IStemmer;
import safar.basic.morphology.stemmer.model.WordStemmerAnalysis;

import java.util.*;

@Service
public class TextMiningService {

    private static final Logger logger = LoggerFactory.getLogger(TextMiningService.class);
    private final ITokenizer tokenizer;
    private final IStemmer stemmer;
    private final CorpusService corpusService;
    private final StopWordsService stopWordsService;

    @Autowired
    public TextMiningService(CorpusService corpusService, StopWordsService stopWordsService) throws Exception {
        this.tokenizer = new SAFARTokenizer();
        this.stemmer = StemmerFactory.getImplementation("ISRI_STEMMER");
        this.corpusService = corpusService;
        this.stopWordsService = stopWordsService;
    }


    public TextAnalysisResult analyzeText(String text) throws Exception {

        logger.info("Starting text analysis with corpus support...");


        String[] tokenArray = tokenizer.tokenize(text);
        List<String> tokens = Arrays.asList(tokenArray);
        logger.debug("Tokenization complete: {} tokens found", tokens.size());


        List<String> stemrs = performStemming(tokenArray);
        logger.debug("Stemming complete: {} stems generated", stemrs.size());


        Map<String, Double> tfidfValues = corpusService.calculateTfIdf(stemrs);
        logger.debug("TF-IDF calculation complete");


        Map<String, Double> similarities = corpusService.calculateCosineSimilarities(stemrs);
        logger.debug("Cosine similarity calculation complete");


        TextAnalysisResult result = new TextAnalysisResult(
                tokens,
                stemrs,
                tfidfValues,
                similarities
        );

        logger.info("Text analysis completed successfully");
        return result;
    }


    private List<String> performStemming(String[] tokens) {
        List<String> stemrs = new ArrayList<>();

        for (String token : tokens) {

            if (stopWordsService.isStopWord(token)) {
                logger.debug("Skipping stop word: {}", token);
                continue;
            }

            try {
                List<WordStemmerAnalysis> analyses = stemmer.stem(token);

                if (analyses != null && !analyses.isEmpty()
                        && !analyses.get(0).getListStemmerAnalysis().isEmpty()) {
                    String stem = analyses.get(0).getListStemmerAnalysis().get(0).getMorpheme();
                    stemrs.add(stem);
                } else {
                    stemrs.add(token);
                }
            } catch (Exception e) {
                logger.warn("Stemming failed for token '{}': {}", token, e.getMessage());
                stemrs.add(token);
            }
        }

        return stemrs;
    }
}