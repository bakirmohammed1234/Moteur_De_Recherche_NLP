package com.bakir.textmining.Controler;

import com.bakir.textmining.model.TextAnalysisResult;
import com.bakir.textmining.Service.TextMiningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/text")
@CrossOrigin(origins = "*")
public class TextMiningController {

    private final TextMiningService textMiningService;

    @Autowired
    public TextMiningController(TextMiningService textMiningService) {
        this.textMiningService = textMiningService;
    }


    @PostMapping("/mining")
    public ResponseEntity<?> analyzeText(@RequestBody String text) {
        try {
            if (text == null || text.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Error: Text to analyze cannot be empty.");
            }

            TextAnalysisResult result = textMiningService.analyzeText(text);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error analyzing text: " + e.getMessage());
        }
    }


}