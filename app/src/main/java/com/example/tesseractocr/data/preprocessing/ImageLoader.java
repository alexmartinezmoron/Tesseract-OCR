package com.example.tesseractocr.data.preprocessing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import androidx.exifinterface.media.ExifInterface;

import java.io.InputStream;

/**
 * Utility class for loading images with correct orientation
 */
public class ImageLoader {
    private final Context context;
    private final ImagePreprocessor imagePreprocessor;

    public ImageLoader(Context context, ImagePreprocessor imagePreprocessor) {
        this.context = context;
        this.imagePreprocessor = imagePreprocessor;
    }

    /**
     * Load bitmap from URI with correct orientation
     */
    public Bitmap loadBitmap(Uri imageUri) throws Exception {
        // Load bitmap
        InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        inputStream.close();

        if (bitmap == null) {
            throw new Exception("Failed to load image");
        }

        // Read EXIF orientation
        InputStream exifStream = context.getContentResolver().openInputStream(imageUri);
        ExifInterface exif = new ExifInterface(exifStream);
        exifStream.close();

        int orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
        );

        // Rotate bitmap according to orientation
        return imagePreprocessor.rotateBitmap(bitmap, orientation);
    }
}
