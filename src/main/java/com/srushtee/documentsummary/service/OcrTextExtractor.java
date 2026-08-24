package com.srushtee.documentsummary.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class OcrTextExtractor implements TextExtractor {

    /*
     * Tesseract executable path.
     *
     * Local Windows:
     * If TESSERACT_PATH is configured, that path will be used.
     *
     * Docker/Linux:
     * Tesseract is installed in the container and available
     * through the system PATH, so "tesseract" is used by default.
     */
    private static final String TESSERACT_PATH =
            System.getenv().getOrDefault(
                    "TESSERACT_PATH",
                    "tesseract"
            );

    private static final int SCALE_FACTOR = 2;

    @Override
    public boolean supports(MultipartFile file) {

        String contentType = file.getContentType();

        return contentType != null
                && contentType.toLowerCase().startsWith("image/");
    }

    @Override
    public String extractText(MultipartFile file) {

        Path originalImage = null;
        Path processedImage = null;
        Path outputBase = null;

        try {

            // =====================================================
            // 1. Create temporary original image
            // =====================================================

            originalImage = Files.createTempFile(
                    "ocr-input-",
                    getFileExtension(file)
            );

            file.transferTo(originalImage.toFile());


            // =====================================================
            // 2. Read image
            // =====================================================

            BufferedImage image =
                    ImageIO.read(originalImage.toFile());

            if (image == null) {

                throw new IllegalArgumentException(
                        "Unable to read the uploaded image"
                );
            }


            // =====================================================
            // 3. Preprocess image
            // =====================================================

            BufferedImage processed =
                    preprocessImage(image);


            // =====================================================
            // 4. Save processed image
            // =====================================================

            processedImage =
                    Files.createTempFile(
                            "ocr-processed-",
                            ".png"
                    );

            ImageIO.write(
                    processed,
                    "png",
                    processedImage.toFile()
            );


            // =====================================================
            // 5. Create Tesseract output location
            // =====================================================

            outputBase =
                    Files.createTempFile(
                            "ocr-output-",
                            ""
                    );

            Files.deleteIfExists(outputBase);


            // =====================================================
            // 6. Run Tesseract
            // =====================================================

            ProcessBuilder processBuilder =
                    new ProcessBuilder(
                            TESSERACT_PATH,

                            processedImage.toString(),

                            outputBase.toString(),

                            "-l",
                            "eng",

                            "--oem",
                            "3",

                            "--psm",
                            "6",

                            "-c",
                            "preserve_interword_spaces=1"
                    );

            processBuilder.redirectErrorStream(true);


            Process process =
                    processBuilder.start();


            // =====================================================
            // 7. Read Tesseract console output
            // =====================================================

            StringBuilder processOutput =
                    new StringBuilder();

            try (BufferedReader reader =
                         new BufferedReader(
                                 new InputStreamReader(
                                         process.getInputStream(),
                                         StandardCharsets.UTF_8
                                 ))) {

                String line;

                while ((line = reader.readLine()) != null) {

                    processOutput
                            .append(line)
                            .append(System.lineSeparator());
                }
            }


            // =====================================================
            // 8. Wait for Tesseract
            // =====================================================

            int exitCode =
                    process.waitFor();

            if (exitCode != 0) {

                throw new IllegalStateException(
                        "Tesseract OCR failed: "
                                + processOutput
                );
            }


            // =====================================================
            // 9. Read OCR output
            // =====================================================

            Path outputFile =
                    Path.of(
                            outputBase + ".txt"
                    );


            if (!Files.exists(outputFile)) {

                throw new IllegalStateException(
                        "Tesseract did not produce output"
                );
            }


            String extractedText =
                    Files.readString(
                            outputFile,
                            StandardCharsets.UTF_8
                    );


            // =====================================================
            // 10. Clean OCR output
            // =====================================================

            return cleanText(extractedText);


        } catch (IOException exception) {

            throw new IllegalStateException(
                    "OCR processing failed",
                    exception
            );

        } catch (InterruptedException exception) {

            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "OCR process was interrupted",
                    exception
            );

        } finally {

            // =====================================================
            // Delete original image
            // =====================================================

            deleteFile(originalImage);

            // =====================================================
            // Delete processed image
            // =====================================================

            deleteFile(processedImage);

            // =====================================================
            // Delete Tesseract output
            // =====================================================

            if (outputBase != null) {

                deleteFile(outputBase);

                deleteFile(
                        Path.of(
                                outputBase + ".txt"
                        )
                );
            }
        }
    }


    // =============================================================
    // IMAGE PREPROCESSING
    // =============================================================

    private BufferedImage preprocessImage(
            BufferedImage original) {

        int originalWidth =
                original.getWidth();

        int originalHeight =
                original.getHeight();


        int newWidth =
                originalWidth * SCALE_FACTOR;

        int newHeight =
                originalHeight * SCALE_FACTOR;


        // Create grayscale image
        BufferedImage grayscale =
                new BufferedImage(
                        newWidth,
                        newHeight,
                        BufferedImage.TYPE_BYTE_GRAY
                );


        Graphics2D graphics =
                grayscale.createGraphics();


        // High quality image scaling
        graphics.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC
        );

        graphics.setRenderingHint(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY
        );

        graphics.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );


        graphics.drawImage(
                original,
                0,
                0,
                newWidth,
                newHeight,
                null
        );


        graphics.dispose();


        return grayscale;
    }


    // =============================================================
    // CLEAN OCR TEXT
    // =============================================================

    private String cleanText(
            String text) {

        if (text == null) {
            return "";
        }


        return text
                .replace("\r", "\n")

                // Remove excessive spaces
                .replaceAll("[ \\t]+", " ")

                // Remove excessive blank lines
                .replaceAll("\\n{3,}", "\n\n")

                .trim();
    }


    // =============================================================
    // FILE EXTENSION
    // =============================================================

    private String getFileExtension(
            MultipartFile file) {

        String fileName =
                file.getOriginalFilename();


        if (fileName == null
                || !fileName.contains(".")) {

            return ".png";
        }


        return fileName.substring(
                fileName.lastIndexOf(".")
        );
    }


    // =============================================================
    // SAFE FILE DELETE
    // =============================================================

    private void deleteFile(
            Path path) {

        if (path == null) {
            return;
        }

        try {

            Files.deleteIfExists(path);

        } catch (IOException ignored) {
            // Ignore cleanup failure
        }
    }
}