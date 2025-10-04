package com.example.tesseractocr.domain.models;

/**
 * Domain model representing the result of an OCR operation
 */
public class OcrResult {
    private final String text;
    private final boolean success;
    private final String errorMessage;

    private OcrResult(String text, boolean success, String errorMessage) {
        this.text = text;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    public static OcrResult success(String text) {
        return new OcrResult(text, true, null);
    }

    public static OcrResult error(String errorMessage) {
        return new OcrResult(null, false, errorMessage);
    }

    public String getText() {
        return text;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
