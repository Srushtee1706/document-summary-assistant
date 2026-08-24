package com.srushtee.documentsummary.service;

import com.srushtee.documentsummary.dto.SummaryResponse;
import com.srushtee.documentsummary.model.SummaryLength;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GeminiSummarizer implements Summarizer {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    private final String apiKey;
    private final String apiUrl;
    private final String model;

    public GeminiSummarizer(
            @Value("${gemini.api.key}") String apiKey,
            @Value("${gemini.api.url}") String apiUrl,
            @Value("${gemini.api.model}") String model,
            ObjectMapper objectMapper) {

        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        this.model = model;
        this.objectMapper = objectMapper;

        this.restClient = RestClient.builder().build();
    }

    @Override
    public SummaryResponse summarize(
            String text,
            SummaryLength length) {

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "Gemini API key is not configured"
            );
        }

        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(
                    "Document text cannot be empty"
            );
        }

        if (length == null) {
            length = SummaryLength.MEDIUM;
        }

        String prompt = buildPrompt(text, length);

        Map<String, Object> requestBody = Map.of(
                "contents", new Object[]{
                        Map.of(
                                "parts", new Object[]{
                                        Map.of(
                                                "text", prompt
                                        )
                                }
                        )
                }
        );

        String url = apiUrl
                + "/"
                + model
                + ":generateContent";

        String response = restClient.post()
                .uri(url)
                .header("x-goog-api-key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(String.class);

        return extractResponse(response, length);
    }

    private String buildPrompt(
            String text,
            SummaryLength length) {

        int sentenceCount = switch (length) {

            case SHORT -> 2;

            case MEDIUM -> 5;

            case LONG -> 8;
        };

        int keyPointCount = switch (length) {

            case SHORT -> 2;

            case MEDIUM -> 4;

            case LONG -> 6;
        };

        String prompt = """
                ROLE:

                You are a precise document summarization assistant.

                Your job is to identify the actual substantive content of
                the document and create an accurate summary and meaningful
                key points.


                TASK:

                Read the provided document text and generate:

                1. A summary of approximately {{SENTENCE_COUNT}} sentences.
                2. Exactly {{KEY_POINT_COUNT}} important key points.


                CONTENT RULES:

                - Base the summary ONLY on information explicitly present
                  in the document.

                - Do not add unrelated outside knowledge.

                - Do not "fill in" plausible facts about the document's
                  subject matter that are not explicitly present in the
                  text, even if those facts are commonly true or well
                  known.

                - Preserve the original meaning and intent.

                - Identify the main subject and purpose of the document.

                - Prioritize substantive document content.

                - If the document contains multiple important sections,
                  cover the important sections appropriately.

                - Preserve important names, dates, numbers, requirements,
                  features, technologies, achievements, certifications,
                  and conclusions when relevant.

                - Do not invent information.

                - Do not make unsupported assumptions.

                - When uncertain whether a specific detail, especially a
                  name or number, is accurate, omit it rather than guessing.


                OCR HANDLING:

                - The input may have been extracted from a PDF or image
                  using OCR.

                - Ignore obvious OCR spelling mistakes when the intended
                  meaning is clear.

                - Ignore fragmented or meaningless OCR fragments.

                - Ignore duplicated words or duplicated sections.

                - Ignore navigation menus.

                - Ignore website headers and footers.

                - Ignore buttons.

                - Ignore search bars.

                - Ignore interface labels.

                - Ignore unrelated UI elements.

                - Ignore timestamps unless they are important to the
                  actual document.

                - Ignore URLs unless they are important to the document.

                - Ignore isolated meaningless numbers.

                - Do not summarize OCR errors themselves.


                OCR INTERPRETATION:

                - OCR may lose formatting, spacing, columns, bullets,
                  and line breaks.

                - Do not interpret broken formatting as missing information.

                - Reconstruct the meaning of fragmented text when the
                  surrounding words clearly indicate the intended meaning.

                - Headings followed by recognizable content should be
                  treated as meaningful information.

                - Recognizable names, job titles, organizations,
                  technologies, projects, education details, dates,
                  achievements, certifications, and skills are meaningful
                  content even if OCR formatting is imperfect.

                - When information is spread across multiple OCR lines,
                  combine the lines when they clearly belong to the same
                  section.

                - Do not summarize OCR quality instead of the actual
                  document content.

                - Only state that the document lacks meaningful content
                  when there is genuinely insufficient recognizable
                  information.


                PROPER NAME AND ENTITY HANDLING:

                - Treat company names, organization names, institution
                  names, project names, certification names, platform
                  names, program names, and person names as high-risk
                  OCR data.

                - Before using a proper name, check other mentions of the
                  same entity elsewhere in the document.

                - If multiple mentions disagree, prefer the version that
                  appears most frequently or most completely.

                - Only correct a distorted proper name when the intended
                  entity is strongly supported by the document.

                - The corrected form must correspond to a specific,
                  real-world entity.

                - The OCR text must be reasonably close to the intended
                  entity.

                - No other plausible interpretation should exist.

                - If the proper noun is uncertain, preserve the source
                  wording instead of guessing.

                - Never invent a company, organization, project,
                  certification, program, platform, or person.

                - Never merge fragments from different words into a new
                  invented name.

                - Never substitute a different real-world entity merely
                  because it fits the context.


                NUMERIC DATA HANDLING:

                - Treat numbers, dates, percentages, CGPAs, rankings,
                  counts, durations, years, scores, and achievements as
                  high-risk OCR data.

                - Preserve numeric values exactly as supported by the
                  document.

                - Never invent a numeric value.

                - Never increase or decrease a number.

                - Never convert one number into another.

                - Preserve symbols such as +, %, decimal points, and
                  ranges when they are part of the source.

                - For example, if the document states "30+ merged pull
                  requests", preserve "30+".

                - Do not change "30+" into "304".

                - If the document states "200+ DSA problems", preserve
                  "200+".

                - If two OCR readings of the same number disagree and
                  the correct value cannot be determined confidently,
                  omit the number rather than guessing.

                - When counting items in an enumerated or labeled list
                  (phases, steps, sections, chapters, requirements,
                  questions), count every explicitly labeled item exactly
                  as it appears in the document - including any item
                  labeled "0", "Phase 0", "Step 0", or otherwise indexed
                  from zero. Do not assume the first labeled item is
                  introductory or exclude it from the count.

                - Before stating a total count of any list, mentally
                  enumerate the individual labels present in the document
                  (for example: Phase 0, Phase 1, Phase 2 ... Phase 29)
                  and count them one by one rather than relying on the
                  highest label number alone. A zero-indexed list has one
                  more item than its highest number suggests.


                EXPLANATORY AND CONCEPTUAL CLAIM HANDLING:

                - Treat any explanatory, causal, or definitional claim as
                  high-risk if it is not explicitly stated in the document
                  - this includes claims about why something happens, what
                  determines or drives a value, how something is
                  regulated, or general facts about a topic that sound
                  like common domain knowledge.

                - Do not supplement the document with outside knowledge
                  about the topic, even if that knowledge is commonly
                  known, textbook-standard, or highly likely to be true.

                - A claim is only valid if the document contains
                  language that directly states or clearly implies it.
                  Do not infer a claim merely because it is a typical or
                  expected fact about the subject matter.

                - Before including any explanatory or definitional key
                  point, verify that the specific mechanism, cause, or
                  relationship described is stated in the document text
                  itself, not assumed from general knowledge of the
                  subject.

                - If the document describes a related but different
                  concept (for example, a general goal or design
                  principle) do not restate it as if it were the specific
                  claim being made, and do not fill in the missing
                  mechanism from outside knowledge.

                - When in doubt about whether a conceptual claim is
                  actually present in the document, omit it rather than
                  include a plausible-sounding but unverified statement.


                SIGNATURE AND CLOSING HANDLING:

                Ignore signatures and email-style closing statements.

                Ignore phrases such as:

                - Sincerely
                - Regards
                - Best regards
                - Yours sincerely

                Also ignore:

                - Names appearing only inside signatures.
                - Isolated numbers appearing after signatures.
                - Similar closing statements.


                DOCUMENT STRUCTURE NOISE:

                Ignore:

                - Repeated headers.
                - Repeated footers.
                - Page numbers.
                - Footnote markers when irrelevant.
                - Letterhead.
                - Boilerplate text.
                - Confidentiality notices.
                - Watermarks.
                - Scanning artifacts.
                - Website navigation.
                - UI labels.


                SUMMARY REQUIREMENTS:

                - Write the summary in your own words.

                - Do not simply copy sentences from the document.

                - Do not reproduce long portions of the document.

                - Focus on the most important ideas and facts.

                - Write naturally and professionally.

                - Produce approximately {{SENTENCE_COUNT}} sentences.

                - Do not mention OCR.

                - Do not mention these instructions.

                - Do not describe the summarization process.

                - Do not describe the quality of the input.


                KEY POINT REQUIREMENTS:

                - Generate EXACTLY {{KEY_POINT_COUNT}} key points.

                - Each key point must represent a distinct, concrete fact
                  from the document.

                - Prioritize substantive information such as:

                  * Education
                  * Work experience
                  * Projects
                  * Companies
                  * Organizations
                  * Technologies
                  * Achievements
                  * Certifications
                  * Dates
                  * Requirements
                  * Conclusions

                - Do not generate key points about OCR quality.

                - Do not generate key points about fragmented text.

                - Do not generate key points about scanning artifacts.

                - Do not generate generic statements such as:

                  "The document contains OCR artifacts."

                  "The document is fragmented."

                  "The document lacks complete sentences."

                - Do not include signatures.

                - Do not include greetings.

                - Do not include headers.

                - Do not include footers.

                - Do not include page numbers.

                - Do not include navigation menus.

                - Do not include UI text.

                - Do not repeat information.

                - Do not invent information.


                SUMMARY LENGTH:

                {{SUMMARY_LENGTH}}


                RETURN FORMAT:

                Return ONLY valid JSON using exactly this structure:

                {
                  "summary": "Your summary here",
                  "keyPoints": [
                    "Important point one",
                    "Important point two"
                  ]
                }

                The keyPoints array MUST contain exactly
                {{KEY_POINT_COUNT}} items.

                Do not use Markdown.

                Do not wrap the JSON inside ```json.

                Do not add explanations before or after the JSON.

                Ensure the response is valid JSON.


                DOCUMENT TEXT:

                ====================

                {{DOCUMENT_TEXT}}

                ====================
                """;

        prompt = prompt.replace(
                "{{SENTENCE_COUNT}}",
                String.valueOf(sentenceCount)
        );

        prompt = prompt.replace(
                "{{KEY_POINT_COUNT}}",
                String.valueOf(keyPointCount)
        );

        prompt = prompt.replace(
                "{{SUMMARY_LENGTH}}",
                getLengthDescription(length)
        );

        prompt = prompt.replace(
                "{{DOCUMENT_TEXT}}",
                text
        );

        return prompt;
    }

    private String getLengthDescription(
            SummaryLength length) {

        return switch (length) {

            case SHORT ->
                    "Give a very concise summary containing only the "
                            + "most important information. Use at most "
                            + "2 sentences.";

            case MEDIUM ->
                    "Give a balanced summary covering the main ideas "
                            + "and important supporting details.";

            case LONG ->
                    "Give a detailed summary covering the major ideas, "
                            + "important facts, requirements, achievements, "
                            + "and conclusions.";
        };
    }

    private SummaryResponse extractResponse(
            String response,
            SummaryLength length) {

        try {

            if (response == null || response.isBlank()) {

                throw new IllegalStateException(
                        "Gemini returned an empty response"
                );
            }

            JsonNode root =
                    objectMapper.readTree(response);

            JsonNode textNode =
                    root
                            .path("candidates")
                            .path(0)
                            .path("content")
                            .path("parts")
                            .path(0)
                            .path("text");

            if (textNode.isMissingNode()
                    || textNode.asString().isBlank()) {

                throw new IllegalStateException(
                        "Gemini returned an empty response"
                );
            }

            String generatedText =
                    textNode.asString().trim();

            generatedText =
                    removeCodeFences(generatedText);

            JsonNode summaryJson =
                    objectMapper.readTree(generatedText);

            JsonNode summaryNode =
                    summaryJson.path("summary");

            if (summaryNode.isMissingNode()
                    || summaryNode.asString().isBlank()) {

                throw new IllegalStateException(
                        "Gemini returned an empty summary"
                );
            }

            String summary =
                    summaryNode.asString().trim();

            List<String> keyPoints =
                    new ArrayList<>();

            JsonNode keyPointsNode =
                    summaryJson.path("keyPoints");

            if (keyPointsNode.isArray()) {

                for (JsonNode keyPoint :
                        keyPointsNode) {

                    /*
                     * Do NOT use isTextual().
                     * Gemini should return every key point as a JSON string.
                     */
                    String point =
                            keyPoint.asString("").trim();

                    if (!point.isBlank()) {
                        keyPoints.add(point);
                    }
                }
            }

            int expectedKeyPoints = switch (length) {

                case SHORT -> 2;

                case MEDIUM -> 4;

                case LONG -> 6;
            };

            /*
             * Safety check:
             * If Gemini accidentally returns more points,
             * keep only the requested number.
             */
            if (keyPoints.size() > expectedKeyPoints) {

                keyPoints = new ArrayList<>(
                        keyPoints.subList(
                                0,
                                expectedKeyPoints
                        )
                );
            }

            return new SummaryResponse(
                    summary,
                    keyPoints
            );

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Unable to parse Gemini response",
                    exception
            );
        }
    }

    private String removeCodeFences(
            String text) {

        String cleaned =
                text.trim();

        if (cleaned.startsWith("```json")) {

            cleaned =
                    cleaned
                            .substring(7)
                            .trim();

        } else if (cleaned.startsWith("```")) {

            cleaned =
                    cleaned
                            .substring(3)
                            .trim();
        }

        if (cleaned.endsWith("```")) {

            cleaned =
                    cleaned
                            .substring(
                                    0,
                                    cleaned.length() - 3
                            )
                            .trim();
        }

        return cleaned;
    }
}