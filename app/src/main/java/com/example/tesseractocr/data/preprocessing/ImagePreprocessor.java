package com.example.tesseractocr.data.preprocessing;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;

import com.example.tesseractocr.domain.usecases.PreprocessImageUseCase;

/**
 * Implementation of image preprocessing for OCR
 * Handles various image transformations to improve OCR accuracy
 */
public class ImagePreprocessor implements PreprocessImageUseCase {
    private static final int MAX_DIMENSION = 2000;
    private static final float DEFAULT_CONTRAST = 1.0f;

    @Override
    public Bitmap execute(Bitmap bitmap) {
        // Apply preprocessing steps
        bitmap = resizeIfNeeded(bitmap);
        bitmap = adjustContrast(bitmap, DEFAULT_CONTRAST);
        return bitmap;
    }

    /**
     * Resize bitmap if it exceeds maximum dimensions
     */
    private Bitmap resizeIfNeeded(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (width > MAX_DIMENSION || height > MAX_DIMENSION) {
            float scale = Math.min((float) MAX_DIMENSION / width, (float) MAX_DIMENSION / height);
            int newWidth = Math.round(width * scale);
            int newHeight = Math.round(height * scale);
            return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        }
        return bitmap;
    }

    /**
     * Convert bitmap to grayscale
     */
    public Bitmap toGrayscale(Bitmap bitmap) {
        Bitmap grayscale = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(grayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return grayscale;
    }

    /**
     * Adjust contrast of the bitmap
     */
    private Bitmap adjustContrast(Bitmap bitmap, float contrast) {
        ColorMatrix cm = new ColorMatrix();
        cm.set(new float[] {
                contrast, 0, 0, 0, 0,
                0, contrast, 0, 0, 0,
                0, 0, contrast, 0, 0,
                0, 0, 0, 1, 0
        });

        Bitmap result = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return result;
    }

    /**
     * Apply Gaussian blur to smooth image
     */
    public Bitmap applyGaussianBlur(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        // Gaussian kernel 3x3
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

    /**
     * Apply adaptive threshold for binarization
     */
    public Bitmap applyAdaptiveThreshold(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        int blockSize = 15;
        int C = 10;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int sum = 0;
                int count = 0;

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

    /**
     * Rotate bitmap according to orientation
     */
    public Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();

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
}
