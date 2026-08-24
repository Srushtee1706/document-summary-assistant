package com.srushtee.documentsummary.controller;

import com.srushtee.documentsummary.dto.SummaryRequest;
import com.srushtee.documentsummary.dto.SummaryResponse;
import com.srushtee.documentsummary.service.SummarizationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/summarize")
public class SummaryController {

    private final SummarizationService summarizationService;

    public SummaryController(
            SummarizationService summarizationService) {

        this.summarizationService =
                summarizationService;
    }

    @PostMapping
    public ResponseEntity<SummaryResponse> summarize(
            @Valid @RequestBody SummaryRequest request) {

        SummaryResponse response =
                summarizationService.summarize(
                        request.text(),
                        request.length()
                );

        return ResponseEntity.ok(response);
    }
}