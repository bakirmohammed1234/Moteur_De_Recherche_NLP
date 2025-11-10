package com.bakir.textmining.Service;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

@Service
public class StopWordsService {

    private static final Logger logger = LoggerFactory.getLogger(StopWordsService.class);
    private final Set<String> stopWords = new HashSet<>();

    @PostConstruct
    public void loadStopWords() {
        try {
            InputStream is = getClass().getClassLoader()
                    .getResourceAsStream("stopwords/arabic_stopwords.txt");

            if (is != null) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(is, StandardCharsets.UTF_8));

                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("#")) {
                        stopWords.add(line);
                    }
                }
                reader.close();
                logger.info("Loaded {} Arabic stop words", stopWords.size());
            } else {
                loadDefaultStopWords();
                logger.warn("Stop words file not found, using default set");
            }
        } catch (Exception e) {
            logger.error("Error loading stop words: {}", e.getMessage());
            loadDefaultStopWords();
        }
    }

    /**
     * Default Arabic stop words
     */
    private void loadDefaultStopWords() {
        String[] defaultStopWords = {
                "ال", "الـ", "هو", "هي", "هم", "هن", "أنت", "أنتم", "أنتن",
                "أنا", "نحن", "هذا", "هذه", "ذلك", "تلك", "هؤلاء", "أولئك",
                "في", "من", "إلى", "على", "عن", "مع", "ب", "ل", "ك",
                "و", "أو", "لكن", "ثم", "أم", "إما", "لا",
                "كان", "يكون", "ليس", "قد", "لم", "لن",
                "ما", "ماذا", "من", "متى", "أين", "كيف", "لماذا", "هل",
                "كل", "بعض", "غير", "عند", "حتى", "بين", "أن", "إن",
                "التي", "الذي", "اللذان", "اللتان", "الذين", "اللاتي", "اللواتي"
        };

        for (String word : defaultStopWords) {
            stopWords.add(word);
        }
    }

    public boolean isStopWord(String word) {
        return stopWords.contains(word);
    }

    public java.util.List<String> filterStopWords(java.util.List<String> words) {
        return words.stream()
                .filter(word -> !isStopWord(word))
                .collect(java.util.stream.Collectors.toList());
    }

    public Set<String> getStopWords() {
        return new HashSet<>(stopWords);
    }

    public int getStopWordsCount() {
        return stopWords.size();
    }
}