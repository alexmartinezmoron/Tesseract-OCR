package com.example.tesseractocr.domain.usecases;

import android.graphics.Bitmap;
import com.example.tesseractocr.domain.models.OcrResult;

/**
 * Main use case that coordinates image preprocessing and text recognition
 */
public class ProcessImageUseCase {
    private final PreprocessImageUseCase preprocessImageUseCase;
    private final RecognizeTextUseCase recognizeTextUseCase;

    public ProcessImageUseCase(PreprocessImageUseCase preprocessImageUseCase,
                               RecognizeTextUseCase recognizeTextUseCase) {
        this.preprocessImageUseCase = preprocessImageUseCase;
        this.recognizeTextUseCase = recognizeTextUseCase;
    }

    /**
     * Process an image: preprocess and recognize text
     * @param bitmap The input image
     * @return OcrResult containing the extracted text or error
     */
    public OcrResult execute(Bitmap bitmap) {
        try {
            // Step 1: Preprocess the image
            Bitmap processedBitmap = preprocessImageUseCase.execute(bitmap);
            
            // Step 2: Recognize text from preprocessed image
            return recognizeTextUseCase.execute(processedBitmap);
        } catch (Exception e) {
            return OcrResult.error("Error processing image: " + e.getMessage());
        }
    }
}
