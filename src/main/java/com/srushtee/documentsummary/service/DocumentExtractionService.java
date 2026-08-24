package com.srushtee.documentsummary.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class DocumentExtractionService {

    private final List<TextExtractor> extractors;

    public DocumentExtractionService(
            List<TextExtractor> extractors) {

        this.extractors = extractors;
    }

    public String extractText(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "File cannot be empty"
            );
        }

        for (TextExtractor extractor : extractors) {

            if (extractor.supports(file)) {
                return extractor.extractText(file);
            }
        }

        throw new IllegalArgumentException(
                "Unsupported file type. "
                        + "Only PDF and image files are supported."
        );
    }
}