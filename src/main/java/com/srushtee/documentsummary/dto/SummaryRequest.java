package com.srushtee.documentsummary.dto;

import com.srushtee.documentsummary.model.SummaryLength;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SummaryRequest(

        @NotBlank(message = "Text cannot be empty")
        String text,

        @NotNull(message = "Summary length is required")
        SummaryLength length

) {
}