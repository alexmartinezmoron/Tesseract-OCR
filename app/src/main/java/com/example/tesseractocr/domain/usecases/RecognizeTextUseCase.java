package com.example.tesseractocr.domain.usecases;

import android.graphics.Bitmap;
import com.example.tesseractocr.domain.models.OcrResult;

/**
 * Use case for text recognition from images
 * This interface allows for different OCR implementations (Tesseract, ML Kit, etc.)
 */
public interface RecognizeTextUseCase {
    /**
     * Recognize text from an image
     * @param bitmap The image to process
     * @return OcrResult containing the extracted text or error
     */
    OcrResult execute(Bitmap bitmap);
}
