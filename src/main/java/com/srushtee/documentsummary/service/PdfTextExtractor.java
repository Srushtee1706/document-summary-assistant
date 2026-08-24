package com.srushtee.documentsummary.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class PdfTextExtractor implements TextExtractor {

    @Override
    public boolean supports(MultipartFile file) {

        String contentType = file.getContentType();

        return "application/pdf".equalsIgnoreCase(contentType);
    }

    @Override
    public String extractText(MultipartFile file) {

        try {
            PDDocument document =
                    Loader.loadPDF(file.getBytes());

            try (document) {

                PDFTextStripper stripper =
                        new PDFTextStripper();

                return stripper.getText(document);
            }

        } catch (IOException exception) {

            throw new IllegalStateException(
                    "Unable to extract text from PDF",
                    exception
            );
        }
    }
}