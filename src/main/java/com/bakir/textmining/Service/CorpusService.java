package com.bakir.textmining.Service;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import safar.util.tokenization.impl.SAFARTokenizer;
import safar.util.tokenization.interfaces.ITokenizer;
import safar.basic.morphology.stemmer.factory.StemmerFactory;
import safar.basic.morphology.stemmer.interfaces.IStemmer;
import safar.basic.morphology.stemmer.model.WordStemmerAnalysis;

import jakarta.annotation.PostConstruct;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CorpusService {

    private static final Logger logger = LoggerFactory.getLogger(CorpusService.class);

    private final List<Document> corpus = new ArrayList<>();
    private final Map<String, Integer> documentFrequency = new HashMap<>();
    private int totalDocuments = 0;
    private final ITokenizer tokenizer;
    private final IStemmer stemmer;
    private final StopWordsService stopWordsService;

    public CorpusService(StopWordsService stopWordsService) throws Exception {
        this.tokenizer = new SAFARTokenizer();
        this.stemmer = StemmerFactory.getImplementation("ISRI_STEMMER");
        this.stopWordsService = stopWordsService;
    }

    /**
     * Inner class to represent a document in the corpus
     */
    public static class Document {
        private String id;
        private String content;
        private List<String> stemrs;
        private Map<String, Integer> termFrequency;

        public Document(String id, String content) {
            this.id = id;
            this.content = content;
            this.stemrs = new ArrayList<>();
            this.termFrequency = new HashMap<>();
        }

        public String getId() { return id; }
        public String getContent() { return content; }
        public List<String> getStems() { return stemrs; }
        public Map<String, Integer> getTermFrequency() { return termFrequency; }
        public void setStems(List<String> stemrs) { this.stemrs = stemrs; }
        public void setTermFrequency(Map<String, Integer> tf) { this.termFrequency = tf; }
    }

    /**
     * Load Arabic corpus from directory
     */
    @PostConstruct
    public void loadCorpusFromDirectory() {
        String corpusPath = "corpus/arabic";
        try {
            Path dirPath = Paths.get(corpusPath);
            if (Files.exists(dirPath) && Files.isDirectory(dirPath)) {
                Files.list(dirPath)
                        .filter(path -> path.toString().endsWith(".txt"))
                        .forEach(this::loadDocument);

                calculateDocumentFrequencies();
                logger.info("Loaded {} documents from corpus", totalDocuments);
            } else {
                logger.warn("Corpus directory not found: {}", corpusPath);
            }
        } catch (IOException e) {
            logger.error("Error loading corpus: {}", e.getMessage());
        }
    }

    /**
     * Load a single document from file
     */
    private void loadDocument(Path filePath) {
        try {
            String content = Files.readString(filePath, StandardCharsets.UTF_8);
            String docId = filePath.getFileName().toString();
            addDocument(docId, content);
        } catch (IOException e) {
            logger.error("Error reading file {}: {}", filePath, e.getMessage());
        }
    }

    /**
     * Add a document to the corpus
     */
    public void addDocument(String docId, String content) {
        try {
            Document doc = new Document(docId, content);

            // Tokenize and stem
            String[] tokens = tokenizer.tokenize(content);
            List<String> stemrs = performStemming(tokens);
            doc.setStems(stemrs);

            // Calculate term frequency
            Map<String, Integer> tf = new HashMap<>();
            for (String stem : stemrs) {
                tf.put(stem, tf.getOrDefault(stem, 0) + 1);
            }
            doc.setTermFrequency(tf);

            corpus.add(doc);
            totalDocuments++;

            logger.debug("Added document {} with {} terms", docId, stemrs.size());
        } catch (Exception e) {
            logger.error("Error adding document {}: {}", docId, e.getMessage());
        }
    }

    /**
     * Perform stemming on tokens with stop words filtering
     */
    private List<String> performStemming(String[] tokens) {
        List<String> stemrs = new ArrayList<>();
        for (String token : tokens) {
            // Skip stop words
            if (stopWordsService.isStopWord(token)) {
                continue;
            }

            try {
                List<WordStemmerAnalysis> analyses = stemmer.stem(token);
                if (analyses != null && !analyses.isEmpty()
                        && !analyses.get(0).getListStemmerAnalysis().isEmpty()) {
                    stemrs.add(analyses.get(0).getListStemmerAnalysis().get(0).getMorpheme());
                } else {
                    stemrs.add(token);
                }
            } catch (Exception e) {
                stemrs.add(token);
            }
        }
        return stemrs;
    }

    /**
     * Calculate document frequencies for all terms
     */
    private void calculateDocumentFrequencies() {
        documentFrequency.clear();
        for (Document doc : corpus) {
            Set<String> uniqueTerms = new HashSet<>(doc.getStems());
            for (String term : uniqueTerms) {
                documentFrequency.put(term, documentFrequency.getOrDefault(term, 0) + 1);
            }
        }
    }

    /**
     * Calculate TF-IDF for stems
     */
    public Map<String, Double> calculateTfIdf(List<String> stemrs) {
        Map<String, Double> tfidfValues = new HashMap<>();

        if (totalDocuments == 0) {
            logger.warn("No corpus loaded, returning simple TF values");
            return calculateSimpleTf(stemrs);
        }

        // Calculate term frequency
        Map<String, Integer> termFreq = new HashMap<>();
        for (String stem : stemrs) {
            termFreq.put(stem, termFreq.getOrDefault(stem, 0) + 1);
        }

        // Calculate TF-IDF
        int totalTerms = stemrs.size();
        for (Map.Entry<String, Integer> entry : termFreq.entrySet()) {
            String term = entry.getKey();
            double tf = (double) entry.getValue() / totalTerms;

            int df = documentFrequency.getOrDefault(term, 1);
            double idf = Math.log((double) (totalDocuments + 1) / (df + 1));

            double tfidf = tf * idf;
            tfidfValues.put(term, tfidf);
        }

        return tfidfValues;
    }

    /**
     * Simple TF calculation (fallback)
     */
    private Map<String, Double> calculateSimpleTf(List<String> stemrs) {
        Map<String, Double> tfValues = new HashMap<>();
        Map<String, Integer> termFreq = new HashMap<>();

        for (String stem : stemrs) {
            termFreq.put(stem, termFreq.getOrDefault(stem, 0) + 1);
        }

        int total = stemrs.size();
        for (Map.Entry<String, Integer> entry : termFreq.entrySet()) {
            tfValues.put(entry.getKey(), (double) entry.getValue() / total);
        }

        return tfValues;
    }

    /**
     * Calculate cosine similarity with corpus
     */
    public Map<String, Double> calculateCosineSimilarities(List<String> inputStems) {
        Map<String, Double> similarities = new HashMap<>();

        if (corpus.isEmpty()) {
            logger.warn("No corpus loaded for similarity calculation");
            return similarities;
        }

        Map<String, Integer> inputTf = new HashMap<>();
        for (String stem : inputStems) {
            inputTf.put(stem, inputTf.getOrDefault(stem, 0) + 1);
        }

        for (Document doc : corpus) {
            double similarity = cosineSimilarity(inputTf, doc.getTermFrequency());
            similarities.put(doc.getId(), similarity);
        }

        return similarities.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    /**
     * Calculate cosine similarity between two vectors
     */
    private double cosineSimilarity(Map<String, Integer> vec1, Map<String, Integer> vec2) {
        Set<String> allTerms = new HashSet<>();
        allTerms.addAll(vec1.keySet());
        allTerms.addAll(vec2.keySet());

        double dotProduct = 0.0;
        double magnitude1 = 0.0;
        double magnitude2 = 0.0;

        for (String term : allTerms) {
            int freq1 = vec1.getOrDefault(term, 0);
            int freq2 = vec2.getOrDefault(term, 0);

            dotProduct += freq1 * freq2;
            magnitude1 += freq1 * freq1;
            magnitude2 += freq2 * freq2;
        }

        magnitude1 = Math.sqrt(magnitude1);
        magnitude2 = Math.sqrt(magnitude2);

        if (magnitude1 == 0.0 || magnitude2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (magnitude1 * magnitude2);
    }

    /**
     * Get corpus statistics
     */
    public Map<String, Object> getCorpusStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDocuments", totalDocuments);
        stats.put("uniqueTerms", documentFrequency.size());
        stats.put("averageDocumentLength",
                corpus.stream().mapToInt(d -> d.getStems().size()).average().orElse(0.0));
        return stats;
    }

    public int getTotalDocuments() {
        return totalDocuments;
    }

    public List<Document> getCorpus() {
        return corpus;
    }
}