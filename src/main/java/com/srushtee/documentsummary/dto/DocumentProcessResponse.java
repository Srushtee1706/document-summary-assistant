package com.srushtee.documentsummary.dto;

import java.util.List;

public record DocumentProcessResponse(
        String fileName,
        String summary,
        List<String> keyPoints
) {
}