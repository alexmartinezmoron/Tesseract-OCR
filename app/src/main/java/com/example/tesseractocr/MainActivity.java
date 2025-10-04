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

            // Configuración optimizada para tickets
            tessBaseAPI.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO);
            tessBaseAPI.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST,
                    "ABCDEFGHIJKLMNÑOPQRSTUVWXYZabcdefghijklmnñopqrstuvwxyzáéíóúÁÉÍÓÚ0123456789.,€$:-/ ");

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.error_init_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap getCorrectlyOrientedBitmap(Uri imageUri) throws Exception {
        InputStream inputStream = getContentResolver().openInputStream(imageUri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        inputStream.close();

        // Leer orientación EXIF
        InputStream exifStream = getContentResolver().openInputStream(imageUri);
        androidx.exifinterface.media.ExifInterface exif = new androidx.exifinterface.media.ExifInterface(exifStream);
        exifStream.close();

        int orientation = exif.getAttributeInt(
                androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION,
                androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL
        );

        return rotateBitmap(bitmap, orientation);
    }

    private Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        android.graphics.Matrix matrix = new android.graphics.Matrix();

        switch (orientation) {
            case androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90);
                break;
            case androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180);
                break;
            case androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(270);
                break;
            case androidx.exifinterface.media.ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.postScale(-1, 1);
                break;
            case androidx.exifinterface.media.ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.postScale(1, -1);
                break;
            default:
                return bitmap;
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private Bitmap resizeIfNeeded(Bitmap bitmap) {
        int maxDimension = 2000;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (width > maxDimension || height > maxDimension) {
            float scale = Math.min((float) maxDimension / width, (float) maxDimension / height);
            int newWidth = Math.round(width * scale);
            int newHeight = Math.round(height * scale);
            return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        }
        return bitmap;
    }

    private Bitmap toGrayscale(Bitmap bitmap) {
        Bitmap grayscale = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        android.graphics.Canvas canvas = new android.graphics.Canvas(grayscale);
        android.graphics.Paint paint = new android.graphics.Paint();
        android.graphics.ColorMatrix cm = new android.graphics.ColorMatrix();
        cm.setSaturation(0);
        paint.setColorFilter(new android.graphics.ColorMatrixColorFilter(cm));
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return grayscale;
    }

//    private Bitmap applyGaussianBlur(Bitmap bitmap) {
//        // Para tickets, un blur ligero es suficiente
//        return jp.wasabeef.glide.transformations.BlurTransformation(25, 3).transform(
//                android.content.Context.getApplicationContext(),
//                new com.bumptech.glide.load.engine.Resource<Bitmap>() {
//                    @Override
//                    public Bitmap get() { return bitmap; }
//                    // ... otros métodos requeridos
//                },
//                bitmap.getWidth(),
//                bitmap.getHeight()
//        );
//    }

    private Bitmap applyGaussianBlur(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        // Kernel gaussiano 3x3
        float[] kernel = {
                1/16f, 2/16f, 1/16f,
                2/16f, 4/16f, 2/16f,
                1/16f, 2/16f, 1/16f
        };

        int[] result = new int[width * height];

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                float r = 0, g = 0, b = 0;
                int k = 0;

                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        int pixel = pixels[(y + dy) * width + (x + dx)];
                        r += android.graphics.Color.red(pixel) * kernel[k];
                        g += android.graphics.Color.green(pixel) * kernel[k];
                        b += android.graphics.Color.blue(pixel) * kernel[k];
                        k++;
                    }
                }

                result[y * width + x] = android.graphics.Color.rgb((int)r, (int)g, (int)b);
            }
        }

        Bitmap blurred = Bitmap.createBitmap(width, height, bitmap.getConfig());
        blurred.setPixels(result, 0, width, 0, 0, width, height);
        return blurred;
    }

    private Bitmap adjustContrast(Bitmap bitmap, float contrast) {
        android.graphics.ColorMatrix cm = new android.graphics.ColorMatrix();
        cm.set(new float[] {
                contrast, 0, 0, 0, 0,
                0, contrast, 0, 0, 0,
                0, 0, contrast, 0, 0,
                0, 0, 0, 1, 0
        });

        Bitmap result = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
        android.graphics.Canvas canvas = new android.graphics.Canvas(result);
        android.graphics.Paint paint = new android.graphics.Paint();
        paint.setColorFilter(new android.graphics.ColorMatrixColorFilter(cm));
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return result;
    }

    private Bitmap applyAdaptiveThreshold(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        int blockSize = 15; // Tamaño del bloque para umbral adaptativo
        int C = 10; // Constante de ajuste

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int sum = 0;
                int count = 0;

                // Calcular promedio local
                for (int dy = -blockSize/2; dy <= blockSize/2; dy++) {
                    for (int dx = -blockSize/2; dx <= blockSize/2; dx++) {
                        int nx = x + dx;
                        int ny = y + dy;
                        if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                            int pixel = pixels[ny * width + nx];
                            sum += android.graphics.Color.red(pixel);
                            count++;
                        }
                    }
                }

                int threshold = sum / count - C;
                int pixel = pixels[y * width + x];
                int gray = android.graphics.Color.red(pixel);

                int newColor = gray > threshold ? 0xFFFFFFFF : 0xFF000000;
                pixels[y * width + x] = newColor;
            }
        }

        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        result.setPixels(pixels, 0, width, 0, 0, width, height);
        return result;
    }

    private Bitmap preprocessTicketImage(Bitmap bitmap) {
        // Redimensionar si es muy grande
        bitmap = resizeIfNeeded(bitmap);

        // Convertir a escala de grises (aunque sea B&N)
        //bitmap = toGrayscale(bitmap);

        // Aplicar desenfoque gaussiano para suavizar arrugas
        //bitmap = applyGaussianBlur(bitmap);

        // Aumentar contraste
        bitmap = adjustContrast(bitmap, 1f);

        // Aplicar umbral adaptativo (mejor para arrugas)
        //bitmap = applyAdaptiveThreshold(bitmap);

        return bitmap;
    }

    private void processImage(Uri imageUri) {
        try {
            tvProgress.setText(R.string.status_processing);

            // Cargar imagen con orientación correcta
            Bitmap bitmap = getCorrectlyOrientedBitmap(imageUri);

            if (bitmap == null) {
                Toast.makeText(this, R.string.error_no_image, Toast.LENGTH_SHORT).show();
                tvProgress.setText(R.string.status_ready);
                return;
            }

            // Aplicar preprocesamiento para tickets arrugados
            Bitmap processedBitmap = preprocessTicketImage(bitmap);

            // Mostrar imagen procesada
            imageView.setImageBitmap(processedBitmap);

            // Perform OCR in background thread
            new Thread(() -> {
                try {
                    tessBaseAPI.setImage(processedBitmap);
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
