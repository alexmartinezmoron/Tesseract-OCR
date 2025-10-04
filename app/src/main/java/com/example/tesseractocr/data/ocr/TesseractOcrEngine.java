package com.example.tesseractocr.data.ocr;

import android.graphics.Bitmap;

import com.example.tesseractocr.data.repository.TesseractRepository;
import com.example.tesseractocr.domain.models.OcrResult;
import com.example.tesseractocr.domain.usecases.RecognizeTextUseCase;
import com.googlecode.tesseract.android.TessBaseAPI;

/**
 * Tesseract implementation of text recognition
 * This class can be easily replaced with ML Kit or other OCR engines
 */
public class TesseractOcrEngine implements RecognizeTextUseCase {
    private final TesseractRepository tesseractRepository;

    public TesseractOcrEngine(TesseractRepository tesseractRepository) {
        this.tesseractRepository = tesseractRepository;
    }

    @Override
    public OcrResult execute(Bitmap bitmap) {
        try {
            TessBaseAPI tessBaseAPI = tesseractRepository.getTessBaseAPI();
            
            if (tessBaseAPI == null) {
                return OcrResult.error("OCR engine not initialized");
            }

            tessBaseAPI.setImage(bitmap);
            String extractedText = tessBaseAPI.getUTF8Text();

            if (extractedText == null || extractedText.trim().isEmpty()) {
                return OcrResult.success("No se detectó texto en la imagen");
            }

            return OcrResult.success(extractedText);
        } catch (Exception e) {
            return OcrResult.error("Error during text recognition: " + e.getMessage());
        }
    }
}
