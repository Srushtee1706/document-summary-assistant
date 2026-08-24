package com.srushtee.documentsummary.service;

import com.srushtee.documentsummary.dto.SummaryResponse;
import com.srushtee.documentsummary.model.SummaryLength;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExtractiveSummarizer implements Summarizer {

    private static final Set<String> STOP_WORDS = Set.of(
            "the", "is", "a", "an", "and", "or", "but",
            "in", "on", "at", "to", "for", "of", "with",
            "by", "from", "as", "this", "that", "it",
            "are", "was", "were", "be", "been", "has",
            "have", "had", "will", "would", "can", "could",
            "about", "into", "than", "then", "their", "there"
    );

    @Override
    public SummaryResponse summarize(
            String text,
            SummaryLength length) {

        if (text == null || text.isBlank()) {

            return new SummaryResponse(
                    "",
                    List.of()
            );
        }

        String cleanedText = text
                .replaceAll("\\s+", " ")
                .trim();

        String[] sentences =
                cleanedText.split("(?<=[.!?])\\s+");

        int sentenceCount =
                getSentenceCount(length);

        /*
         * If the document is already short,
         * return the complete text.
         */
        if (sentences.length <= sentenceCount) {

            List<String> keyPoints =
                    Arrays.stream(sentences)
                            .map(String::trim)
                            .filter(sentence ->
                                    !sentence.isBlank())
                            .limit(5)
                            .toList();

            return new SummaryResponse(
                    cleanedText,
                    keyPoints
            );
        }

        Map<String, Integer> wordFrequency =
                calculateWordFrequency(cleanedText);

        List<SentenceScore> scoredSentences =
                scoreSentences(
                        sentences,
                        wordFrequency
                );

        /*
         * Select the highest-scoring sentences
         * for the summary.
         */
        Set<Integer> selectedIndexes =
                scoredSentences.stream()
                        .sorted(
                                Comparator.comparingDouble(
                                        SentenceScore::score
                                ).reversed()
                        )
                        .limit(sentenceCount)
                        .map(SentenceScore::index)
                        .collect(Collectors.toSet());

        String summary =
                buildSummary(
                        sentences,
                        selectedIndexes
                );

        /*
         * Select up to 5 highest-scoring sentences
         * as key points.
         */
        List<String> keyPoints =
                scoredSentences.stream()
                        .sorted(
                                Comparator.comparingDouble(
                                        SentenceScore::score
                                ).reversed()
                        )
                        .limit(5)
                        .map(score ->
                                sentences[
                                        score.index()
                                ].trim()
                        )
                        .filter(sentence ->
                                !sentence.isBlank())
                        .toList();

        return new SummaryResponse(
                summary,
                keyPoints
        );
    }

    private Map<String, Integer> calculateWordFrequency(
            String text) {

        Map<String, Integer> frequency =
                new HashMap<>();

        String[] words = text
                .toLowerCase()
                .replaceAll("[^a-zA-Z\\s]", "")
                .split("\\s+");

        for (String word : words) {

            if (word.length() < 3 ||
                    STOP_WORDS.contains(word)) {

                continue;
            }

            frequency.merge(
                    word,
                    1,
                    Integer::sum
            );
        }

        return frequency;
    }

    private List<SentenceScore> scoreSentences(
            String[] sentences,
            Map<String, Integer> wordFrequency) {

        List<SentenceScore> scores =
                new ArrayList<>();

        for (int i = 0;
             i < sentences.length;
             i++) {

            String sentence =
                    sentences[i];

            String[] words = sentence
                    .toLowerCase()
                    .replaceAll("[^a-zA-Z\\s]", "")
                    .split("\\s+");

            double score = 0;

            for (String word : words) {

                if (wordFrequency.containsKey(word)) {

                    score +=
                            wordFrequency.get(word);
                }
            }

            if (words.length > 0) {

                score =
                        score / words.length;
            }

            scores.add(
                    new SentenceScore(
                            i,
                            score
                    )
            );
        }

        return scores;
    }

    private String buildSummary(
            String[] sentences,
            Set<Integer> selectedIndexes) {

        StringBuilder summary =
                new StringBuilder();

        /*
         * Iterate in original document order,
         * instead of score order.
         */
        for (int i = 0;
             i < sentences.length;
             i++) {

            if (selectedIndexes.contains(i)) {

                if (!summary.isEmpty()) {
                    summary.append(" ");
                }

                summary.append(
                        sentences[i]
                );
            }
        }

        return summary.toString();
    }

    private int getSentenceCount(
            SummaryLength length) {

        return switch (length) {

            case SHORT -> 3;

            case MEDIUM -> 5;

            case LONG -> 8;
        };
    }

    private record SentenceScore(
            int index,
            double score
    ) {
    }
}