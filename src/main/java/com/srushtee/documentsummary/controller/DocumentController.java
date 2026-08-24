package com.srushtee.documentsummary.controller;

import com.srushtee.documentsummary.dto.DocumentProcessResponse;
import com.srushtee.documentsummary.dto.SummaryResponse;
import com.srushtee.documentsummary.model.SummaryLength;
import com.srushtee.documentsummary.service.DocumentExtractionService;
import com.srushtee.documentsummary.service.SummarizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentExtractionService extractionService;
    private final SummarizationService summarizationService;

    public DocumentController(
            DocumentExtractionService extractionService,
            SummarizationService summarizationService) {

        this.extractionService = extractionService;
        this.summarizationService = summarizationService;
    }

    @PostMapping("/extract")
    public ResponseEntity<Map<String, String>> extractText(
            @RequestParam("file") MultipartFile file) {

        String extractedText =
                extractionService.extractText(file);

        return ResponseEntity.ok(
                Map.of(
                        "fileName",
                        file.getOriginalFilename(),
                        "text",
                        extractedText
                )
        );
    }

    @PostMapping("/process")
    public ResponseEntity<DocumentProcessResponse> processDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "MEDIUM")
            SummaryLength length) {

        String extractedText =
                extractionService.extractText(file);

        SummaryResponse summaryResponse =
                summarizationService.summarize(
                        extractedText,
                        length
                );

        DocumentProcessResponse response =
                new DocumentProcessResponse(
                        file.getOriginalFilename(),
                        summaryResponse.summary(),
                        summaryResponse.keyPoints()
                );

        return ResponseEntity.ok(response);
    }
}