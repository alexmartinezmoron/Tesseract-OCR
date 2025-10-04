package com.example.tesseractocr;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    
    private Button btnSelectImage;
    private ImageView imageView;
    private TextView tvResult;
    private TextView tvProgress;
    
    private TessBaseAPI tessBaseAPI;
    private String dataPath;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSelectImage = findViewById(R.id.btnSelectImage);
        imageView = findViewById(R.id.imageView);
        tvResult = findViewById(R.id.tvResult);
        tvProgress = findViewById(R.id.tvProgress);

        // Initialize Tesseract
        dataPath = getFilesDir() + "/tesseract/";
        initializeTesseract();

        // Setup image picker launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        processImage(imageUri);
                    }
                });

        btnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionAndPickImage();
            }
        });
    }

    private void checkPermissionAndPickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ uses READ_MEDIA_IMAGES
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        PERMISSION_REQUEST_CODE);
            } else {
                openImagePicker();
            }
        } else {
            // Android 12 and below use READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE);
            } else {
                openImagePicker();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                Toast.makeText(this, R.string.permission_required, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void initializeTesseract() {
        try {
            // Create tessdata directory
            File dir = new File(dataPath + "tessdata/");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // Copy trained data file from assets to tessdata directory
            String trainedDataPath = dataPath + "tessdata/spa.traineddata";
            File trainedDataFile = new File(trainedDataPath);
            
            if (!trainedDataFile.exists()) {
                // Copy from assets
                InputStream in = getAssets().open("tessdata/spa.traineddata");
                OutputStream out = new FileOutputStream(trainedDataFile);
                
                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                in.close();
                out.close();
            }

            // Initialize TessBaseAPI
            tessBaseAPI = new TessBaseAPI();
            tessBaseAPI.init(dataPath, "spa");
            
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.error_init_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void processImage(Uri imageUri) {
        try {
            tvProgress.setText(R.string.status_processing);
            
            // Load bitmap from URI
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            if (bitmap == null) {
                Toast.makeText(this, R.string.error_no_image, Toast.LENGTH_SHORT).show();
                tvProgress.setText(R.string.status_ready);
                return;
            }

            // Display image
            imageView.setImageBitmap(bitmap);

            // Perform OCR in background thread
            new Thread(() -> {
                try {
                    tessBaseAPI.setImage(bitmap);
                    String extractedText = tessBaseAPI.getUTF8Text();
                    
                    // Update UI on main thread
                    runOnUiThread(() -> {
                        if (extractedText == null || extractedText.trim().isEmpty()) {
                            tvResult.setText("No se detectó texto en la imagen");
                        } else {
                            tvResult.setText(extractedText);
                        }
                        tvProgress.setText(R.string.status_complete);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, R.string.error_ocr_failed, Toast.LENGTH_SHORT).show();
                        tvProgress.setText(R.string.status_ready);
                    });
                }
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
        if (tessBaseAPI != null) {
            tessBaseAPI.end();
        }
    }
}
