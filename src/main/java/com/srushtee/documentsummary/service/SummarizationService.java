package com.srushtee.documentsummary.service;

import com.srushtee.documentsummary.dto.SummaryResponse;
import com.srushtee.documentsummary.model.SummaryLength;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SummarizationService {

    private static final Logger logger =
            LoggerFactory.getLogger(SummarizationService.class);

    private static final int MAX_TEXT_LENGTH = 12000;

    private final GeminiSummarizer geminiSummarizer;
    private final ExtractiveSummarizer extractiveSummarizer;

    public SummarizationService(
            GeminiSummarizer geminiSummarizer,
            ExtractiveSummarizer extractiveSummarizer) {

        this.geminiSummarizer = geminiSummarizer;
        this.extractiveSummarizer = extractiveSummarizer;
    }

    public SummaryResponse summarize(
            String text,
            SummaryLength length) {

        String preparedText =
                prepareText(text);

        if (preparedText.isBlank()) {

            return new SummaryResponse(
                    "No readable text was found in the document.",
                    List.of()
            );
        }

        try {

            /*
             * Primary summarization using Gemini.
             *
             * Gemini now returns both:
             * 1. Summary
             * 2. Key points
             */
            SummaryResponse response =
                    geminiSummarizer.summarize(
                            preparedText,
                            length
                    );

            logger.info(
                    "Gemini summarization completed successfully."
            );

            return response;

        } catch (Exception exception) {

            /*
             * If Gemini fails, use the extractive
             * summarizer as a fallback.
             */
            logger.warn(
                    "Gemini summarization failed. "
                            + "Using extractive fallback.",
                    exception
            );

            return extractiveSummarizer.summarize(
                    preparedText,
                    length
            );
        }
    }

    private String prepareText(String text) {

        if (text == null || text.isBlank()) {
            return "";
        }

        /*
         * OCR can produce excessive whitespace,
         * repeated line breaks and fragmented text.
         */
        String cleanedText = text
                .replace("\r", " ")
                .replace("\n", " ")
                .replaceAll("\\s+", " ")
                .trim();

        if (cleanedText.length() <= MAX_TEXT_LENGTH) {
            return cleanedText;
        }

        logger.info(
                "Document text exceeded {} characters. "
                        + "Truncating before summarization.",
                MAX_TEXT_LENGTH
        );

        return cleanedText.substring(
                0,
                MAX_TEXT_LENGTH
        );
    }
}