package com.srushtee.documentsummary.service;

import com.srushtee.documentsummary.dto.SummaryResponse;
import com.srushtee.documentsummary.model.SummaryLength;

public interface Summarizer {

    SummaryResponse summarize(
            String text,
            SummaryLength length
    );
}