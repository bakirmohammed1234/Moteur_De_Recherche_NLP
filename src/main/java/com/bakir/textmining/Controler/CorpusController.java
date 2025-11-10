package com.bakir.textmining.Controler;

import com.bakir.textmining.Service.CorpusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/corpus")
@CrossOrigin(origins = "*")

public class CorpusController {

    private final CorpusService corpusService;

    @Autowired
    public CorpusController(CorpusService corpusService) {
        this.corpusService = corpusService;
    }

    /**
     * Get corpus statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCorpusStats() {
        Map<String, Object> stats = corpusService.getCorpusStats();
        return ResponseEntity.ok(stats);
    }


    @PostMapping("/add")
    public ResponseEntity<String> addDocument(
            @RequestParam String docId,
            @RequestBody String content) {
        try {
            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Document content cannot be empty");
            }

            corpusService.addDocument(docId, content);
            return ResponseEntity.ok("Document added successfully: " + docId);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error adding document: " + e.getMessage());
        }
    }


    @PostMapping("/upload")
    public ResponseEntity<String> uploadDocuments(
            @RequestParam("files") MultipartFile[] files) {
        try {
            int successCount = 0;

            for (MultipartFile file : files) {
                if (file.isEmpty()) continue;

                String content = new BufferedReader(
                        new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))
                        .lines()
                        .reduce("", (acc, line) -> acc + line + "\n");

                String docId = file.getOriginalFilename();
                corpusService.addDocument(docId, content);
                successCount++;
            }

            return ResponseEntity.ok(
                    String.format("Successfully uploaded %d documents", successCount));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading documents: " + e.getMessage());
        }
    }

    /**
     * Reload corpus from directory
     */
    @PostMapping("/reload")
    public ResponseEntity<String> reloadCorpus() {
        try {
            corpusService.loadCorpusFromDirectory();
            return ResponseEntity.ok("Corpus reloaded successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error reloading corpus: " + e.getMessage());
        }
    }
}