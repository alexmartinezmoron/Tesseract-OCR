package com.example.tesseractocr.data.repository;

import android.content.Context;
import android.content.res.AssetManager;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Repository for managing Tesseract initialization and lifecycle
 */
public class TesseractRepository {
    private static final String LANGUAGE = "spa";
    private static final String TESSDATA_FOLDER = "tessdata/";
    private static final String TRAINED_DATA_FILE = "spa.traineddata";

    private final Context context;
    private TessBaseAPI tessBaseAPI;
    private String dataPath;

    public TesseractRepository(Context context) {
        this.context = context;
        this.dataPath = context.getFilesDir() + "/tesseract/";
    }

    /**
     * Initialize Tesseract OCR engine
     * @return true if initialization was successful
     */
    public boolean initialize() {
        try {
            // Create tessdata directory
            File dir = new File(dataPath + TESSDATA_FOLDER);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // Copy trained data file from assets to tessdata directory
            String trainedDataPath = dataPath + TESSDATA_FOLDER + TRAINED_DATA_FILE;
            File trainedDataFile = new File(trainedDataPath);

            if (!trainedDataFile.exists()) {
                copyTrainedDataFromAssets(trainedDataFile);
            }

            // Initialize TessBaseAPI
            tessBaseAPI = new TessBaseAPI();
            tessBaseAPI.init(dataPath, LANGUAGE);

            // Optimized configuration
            tessBaseAPI.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO);
            tessBaseAPI.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST,
                    "ABCDEFGHIJKLMNÑOPQRSTUVWXYZabcdefghijklmnñopqrstuvwxyzáéíóúÁÉÍÓÚ0123456789.,€$:-/ ");

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Copy trained data file from assets to internal storage
     */
    private void copyTrainedDataFromAssets(File targetFile) throws Exception {
        AssetManager assetManager = context.getAssets();
        InputStream in = assetManager.open(TESSDATA_FOLDER + TRAINED_DATA_FILE);
        OutputStream out = new FileOutputStream(targetFile);

        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        in.close();
        out.close();
    }

    /**
     * Get the Tesseract API instance
     */
    public TessBaseAPI getTessBaseAPI() {
        return tessBaseAPI;
    }

    /**
     * Release Tesseract resources
     */
    public void release() {
        if (tessBaseAPI != null) {
            tessBaseAPI.end();
            tessBaseAPI = null;
        }
    }
}
