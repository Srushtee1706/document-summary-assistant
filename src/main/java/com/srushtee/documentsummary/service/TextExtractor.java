package com.srushtee.documentsummary.service;

import org.springframework.web.multipart.MultipartFile;

public interface TextExtractor {

    boolean supports(MultipartFile file);

    String extractText(MultipartFile file);
}