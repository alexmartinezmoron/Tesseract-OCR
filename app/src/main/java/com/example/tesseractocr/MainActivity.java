package com.example.tesseractocr;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tesseractocr.data.ocr.TesseractOcrEngine;
import com.example.tesseractocr.data.preprocessing.ImageLoader;
import com.example.tesseractocr.data.preprocessing.ImagePreprocessor;
import com.example.tesseractocr.data.repository.TesseractRepository;
import com.example.tesseractocr.domain.models.OcrResult;
import com.example.tesseractocr.domain.usecases.ProcessImageUseCase;
import com.example.tesseractocr.domain.usecases.RecognizeTextUseCase;
import com.example.tesseractocr.domain.usecases.PreprocessImageUseCase;
import com.example.tesseractocr.presentation.ImagePickerHandler;

/**
 * Main Activity - Presentation Layer
 * Handles UI interactions and delegates business logic to use cases
 */
public class MainActivity extends AppCompatActivity {

    // UI Components
    private Button btnSelectImage;
    private ImageView imageView;
    private TextView tvResult;
    private TextView tvProgress;

    // Dependencies - Clean Architecture layers
    private TesseractRepository tesseractRepository;
    private ImagePreprocessor imagePreprocessor;
    private ImageLoader imageLoader;
    private ProcessImageUseCase processImageUseCase;
    private ImagePickerHandler imagePickerHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        btnSelectImage = findViewById(R.id.btnSelectImage);
        imageView = findViewById(R.id.imageView);
        tvResult = findViewById(R.id.tvResult);
        tvProgress = findViewById(R.id.tvProgress);

        // Initialize Clean Architecture layers
        initializeDependencies();

        // Setup image picker handler
        imagePickerHandler = new ImagePickerHandler(this, this::processImage);

        // Setup button click listener
        btnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagePickerHandler.selectImage();
            }
        });
    }

    /**
     * Initialize all dependencies following Clean Architecture
     * Data Layer -> Domain Layer -> Presentation Layer
     */
    private void initializeDependencies() {
        // Data Layer: Repository and implementations
        tesseractRepository = new TesseractRepository(this);
        boolean initialized = tesseractRepository.initialize();
        
        if (!initialized) {
            Toast.makeText(this, R.string.error_init_failed, Toast.LENGTH_SHORT).show();
            return;
        }

        imagePreprocessor = new ImagePreprocessor();
        imageLoader = new ImageLoader(this, imagePreprocessor);
        
        // Domain Layer: Use cases
        PreprocessImageUseCase preprocessImageUseCase = imagePreprocessor;
        RecognizeTextUseCase recognizeTextUseCase = new TesseractOcrEngine(tesseractRepository);
        processImageUseCase = new ProcessImageUseCase(preprocessImageUseCase, recognizeTextUseCase);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        imagePickerHandler.onPermissionResult(requestCode, grantResults);
    }

    /**
     * Process selected image using Clean Architecture use cases
     * This method coordinates the UI updates while delegating business logic
     */
    private void processImage(Uri imageUri) {
        try {
            tvProgress.setText(R.string.status_processing);

            // Load image with correct orientation
            Bitmap bitmap = imageLoader.loadBitmap(imageUri);

            if (bitmap == null) {
                Toast.makeText(this, R.string.error_no_image, Toast.LENGTH_SHORT).show();
                tvProgress.setText(R.string.status_ready);
                return;
            }

            // Display original image
            imageView.setImageBitmap(bitmap);

            // Process OCR in background thread to avoid blocking UI
            new Thread(() -> {
                // Execute use case: preprocess and recognize text
                OcrResult result = processImageUseCase.execute(bitmap);

                // Update UI on main thread
                runOnUiThread(() -> {
                    if (result.isSuccess()) {
                        tvResult.setText(result.getText());
                        tvProgress.setText(R.string.status_complete);
                    } else {
                        tvResult.setText(result.getErrorMessage());
                        Toast.makeText(MainActivity.this, R.string.error_ocr_failed, Toast.LENGTH_SHORT).show();
                        tvProgress.setText(R.string.status_ready);
                    }
                });
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.error_no_image, Toast.LENGTH_SHORT).show();
            tvProgress.setText(R.string.status_ready);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up resources
        if (tesseractRepository != null) {
            tesseractRepository.release();
        }
    }
}
