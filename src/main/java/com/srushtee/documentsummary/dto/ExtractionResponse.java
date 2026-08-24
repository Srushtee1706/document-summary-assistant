package com.srushtee.documentsummary.dto;

public record ExtractionResponse(
        String fileName,
        String fileType,
        String text
) {
}