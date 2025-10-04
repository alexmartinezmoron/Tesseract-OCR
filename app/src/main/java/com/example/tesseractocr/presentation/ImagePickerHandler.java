package com.example.tesseractocr.presentation;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.tesseractocr.R;

/**
 * Handles image selection and permission management
 */
public class ImagePickerHandler {
    private static final int PERMISSION_REQUEST_CODE = 100;

    private final AppCompatActivity activity;
    private final ActivityResultLauncher<Intent> imagePickerLauncher;
    private final ImageSelectedCallback callback;

    public interface ImageSelectedCallback {
        void onImageSelected(Uri imageUri);
    }

    public ImagePickerHandler(AppCompatActivity activity, ImageSelectedCallback callback) {
        this.activity = activity;
        this.callback = callback;
        
        // Setup image picker launcher
        this.imagePickerLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            callback.onImageSelected(imageUri);
                        }
                    }
                });
    }

    /**
     * Check permissions and open image picker
     */
    public void selectImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ uses READ_MEDIA_IMAGES
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        PERMISSION_REQUEST_CODE);
            } else {
                openImagePicker();
            }
        } else {
            // Android 12 and below use READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE);
            } else {
                openImagePicker();
            }
        }
    }

    /**
     * Handle permission result
     */
    public void onPermissionResult(int requestCode, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                Toast.makeText(activity, R.string.permission_required, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Open the image picker
     */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    public static int getPermissionRequestCode() {
        return PERMISSION_REQUEST_CODE;
    }
}
