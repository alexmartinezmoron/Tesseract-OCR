package com.example.tesseractocr.domain.usecases;

import android.graphics.Bitmap;

/**
 * Use case for preprocessing images before OCR
 * Handles rotation, resizing, grayscale conversion, contrast adjustment, etc.
 */
public interface PreprocessImageUseCase {
    /**
     * Preprocess an image for OCR
     * @param bitmap The input bitmap
     * @return The preprocessed bitmap
     */
    Bitmap execute(Bitmap bitmap);
}
