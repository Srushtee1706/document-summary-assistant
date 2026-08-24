package com.srushtee.documentsummary.dto;

import java.util.List;

public record SummaryResponse(
        String summary,
        List<String> keyPoints
) {
}