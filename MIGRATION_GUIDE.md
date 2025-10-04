# Migration Guide: Monolithic to Clean Architecture

## Overview

This guide explains the changes made during the Clean Architecture refactoring and how to work with the new structure.

## What Changed?

### Before: Monolithic Structure
```
MainActivity.java (417 lines)
├── UI Code
├── Permission Handling
├── Image Loading
├── Image Preprocessing
├── Tesseract Initialization
└── OCR Processing
```

### After: Clean Architecture
```
MainActivity.java (152 lines)
├── presentation/
│   └── ImagePickerHandler.java
├── domain/
│   ├── models/OcrResult.java
│   └── usecases/
│       ├── ProcessImageUseCase.java
│       ├── PreprocessImageUseCase.java
│       └── RecognizeTextUseCase.java
└── data/
    ├── repository/TesseractRepository.java
    ├── preprocessing/
    │   ├── ImagePreprocessor.java
    │   └── ImageLoader.java
    └── ocr/TesseractOcrEngine.java
```

## Code Migration Examples

### 1. Image Preprocessing

#### Before:
```java
// In MainActivity.java
private Bitmap preprocessTicketImage(Bitmap bitmap) {
    bitmap = resizeIfNeeded(bitmap);
    bitmap = adjustContrast(bitmap, 1f);
    return bitmap;
}

private Bitmap resizeIfNeeded(Bitmap bitmap) {
    // ... implementation
}

private Bitmap adjustContrast(Bitmap bitmap, float contrast) {
    // ... implementation
}
```

#### After:
```java
// In ImagePreprocessor.java (data layer)
public class ImagePreprocessor implements PreprocessImageUseCase {
    @Override
    public Bitmap execute(Bitmap bitmap) {
        bitmap = resizeIfNeeded(bitmap);
        bitmap = adjustContrast(bitmap, DEFAULT_CONTRAST);
        return bitmap;
    }
    // ... methods
}

// In MainActivity.java (presentation layer)
imagePreprocessor = new ImagePreprocessor();
```

### 2. OCR Processing

#### Before:
```java
// In MainActivity.java
private void processImage(Uri imageUri) {
    // ... load image
    new Thread(() -> {
        tessBaseAPI.setImage(bitmap);
        String extractedText = tessBaseAPI.getUTF8Text();
        // ... update UI
    }).start();
}
```

#### After:
```java
// In TesseractOcrEngine.java (data layer)
public class TesseractOcrEngine implements RecognizeTextUseCase {
    @Override
    public OcrResult execute(Bitmap bitmap) {
        tessBaseAPI.setImage(bitmap);
        String extractedText = tessBaseAPI.getUTF8Text();
        return OcrResult.success(extractedText);
    }
}

// In ProcessImageUseCase.java (domain layer)
public OcrResult execute(Bitmap bitmap) {
    Bitmap processed = preprocessImageUseCase.execute(bitmap);
    return recognizeTextUseCase.execute(processed);
}

// In MainActivity.java (presentation layer)
new Thread(() -> {
    OcrResult result = processImageUseCase.execute(bitmap);
    runOnUiThread(() -> displayResult(result));
}).start();
```

### 3. Tesseract Initialization

#### Before:
```java
// In MainActivity.java
private void initializeTesseract() {
    // Create directories
    // Copy trained data
    // Initialize TessBaseAPI
    tessBaseAPI = new TessBaseAPI();
    tessBaseAPI.init(dataPath, "spa");
    // ... configuration
}
```

#### After:
```java
// In TesseractRepository.java (data layer)
public class TesseractRepository {
    public boolean initialize() {
        // Create directories
        // Copy trained data
        // Initialize TessBaseAPI
        // ... configuration
        return true;
    }
}

// In MainActivity.java (presentation layer)
tesseractRepository = new TesseractRepository(this);
tesseractRepository.initialize();
```

### 4. Permission Handling

#### Before:
```java
// In MainActivity.java
private void checkPermissionAndPickImage() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        // Check and request permissions
    } else {
        // Check and request permissions
    }
}

@Override
public void onRequestPermissionsResult(...) {
    // Handle result
}

private void openImagePicker() {
    // Open picker
}
```

#### After:
```java
// In ImagePickerHandler.java (presentation layer)
public class ImagePickerHandler {
    public void selectImage() {
        // Check and request permissions
    }
    
    public void onPermissionResult(...) {
        // Handle result
    }
}

// In MainActivity.java (presentation layer)
imagePickerHandler = new ImagePickerHandler(this, this::processImage);
imagePickerHandler.selectImage();
```

## How to Add a New Feature

### Example: Adding ML Kit OCR

#### Step 1: Create ML Kit Implementation
```java
// Create: data/ocr/MLKitOcrEngine.java
public class MLKitOcrEngine implements RecognizeTextUseCase {
    @Override
    public OcrResult execute(Bitmap bitmap) {
        // ML Kit implementation
        TextRecognizer recognizer = TextRecognition.getClient();
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        
        Task<Text> result = recognizer.process(image);
        // ... process result
        return OcrResult.success(extractedText);
    }
}
```

#### Step 2: Add Configuration
```java
// In MainActivity.java
private void initializeDependencies() {
    // ... existing code
    
    // Choose OCR engine based on configuration
    RecognizeTextUseCase recognizeTextUseCase;
    if (shouldUseMLKit()) {
        recognizeTextUseCase = new MLKitOcrEngine();
    } else {
        recognizeTextUseCase = new TesseractOcrEngine(tesseractRepository);
    }
    
    processImageUseCase = new ProcessImageUseCase(
        preprocessImageUseCase, 
        recognizeTextUseCase
    );
}
```

#### Step 3: Done!
No changes needed to:
- UI code
- Use cases
- Other implementations

### Example: Adding Custom Preprocessing

#### Step 1: Create Custom Preprocessor
```java
// Create: data/preprocessing/TicketPreprocessor.java
public class TicketPreprocessor implements PreprocessImageUseCase {
    @Override
    public Bitmap execute(Bitmap bitmap) {
        bitmap = removeNoise(bitmap);
        bitmap = enhanceContrast(bitmap);
        bitmap = sharpenText(bitmap);
        return bitmap;
    }
}
```

#### Step 2: Use It
```java
// In MainActivity.java
PreprocessImageUseCase preprocessImageUseCase = new TicketPreprocessor();
```

## Common Tasks

### 1. Debugging OCR Issues

**Before:** Debug through MainActivity's 400 lines
**After:** Go directly to TesseractOcrEngine.java

### 2. Testing Preprocessing

**Before:** Must test through full MainActivity
**After:** Test ImagePreprocessor directly:

```java
@Test
public void testResizeIfNeeded() {
    ImagePreprocessor preprocessor = new ImagePreprocessor();
    Bitmap large = createLargeBitmap(3000, 3000);
    Bitmap result = preprocessor.resizeIfNeeded(large);
    assertTrue(result.getWidth() <= 2000);
}
```

### 3. Changing UI

**Before:** Mixed with business logic
**After:** Change only MainActivity.java, no side effects

### 4. Swapping OCR Engine

**Before:** Rewrite large portions of MainActivity
**After:** Create new class implementing RecognizeTextUseCase

## API Changes

### Creating Dependencies

#### Before:
```java
// Everything in MainActivity
private TessBaseAPI tessBaseAPI;
private String dataPath;
```

#### After:
```java
// Inject dependencies
private TesseractRepository tesseractRepository;
private ImagePreprocessor imagePreprocessor;
private ProcessImageUseCase processImageUseCase;

// Initialize in onCreate
initializeDependencies();
```

### Processing Images

#### Before:
```java
processImage(Uri imageUri);
// Returns nothing, updates UI directly
```

#### After:
```java
// Load image
Bitmap bitmap = imageLoader.loadBitmap(imageUri);

// Process in background
OcrResult result = processImageUseCase.execute(bitmap);

// Handle result
if (result.isSuccess()) {
    displayText(result.getText());
} else {
    showError(result.getErrorMessage());
}
```

## Benefits You'll Notice

### 1. Easier Testing
```java
// Mock dependencies easily
PreprocessImageUseCase mockPreprocessor = mock(PreprocessImageUseCase.class);
RecognizeTextUseCase mockOcr = mock(RecognizeTextUseCase.class);

ProcessImageUseCase useCase = new ProcessImageUseCase(
    mockPreprocessor, 
    mockOcr
);

// Test use case in isolation
OcrResult result = useCase.execute(testBitmap);
```

### 2. Clear Responsibilities
- **Need to change UI?** → Edit MainActivity or ImagePickerHandler
- **Need to fix OCR?** → Edit TesseractOcrEngine
- **Need to improve preprocessing?** → Edit ImagePreprocessor
- **Need to change business logic?** → Edit ProcessImageUseCase

### 3. Parallel Development
- Developer A: Works on new UI features (MainActivity)
- Developer B: Implements ML Kit (MLKitOcrEngine)
- Developer C: Improves preprocessing (ImagePreprocessor)
- No merge conflicts!

### 4. Easier Code Review
- Small, focused files (10-100 lines each)
- Clear purpose for each file
- Easy to understand changes

## Troubleshooting

### Issue: App crashes on image selection

**Check:**
1. `TesseractRepository.initialize()` returned true
2. `ImageLoader` successfully loads image
3. Training data file exists in assets

**Debug:**
```java
// Add logging in each layer
// Data layer
Log.d("ImageLoader", "Loading image: " + uri);

// Domain layer  
Log.d("ProcessImageUseCase", "Starting preprocessing");

// Presentation layer
Log.d("MainActivity", "Displaying result");
```

### Issue: OCR returns no text

**Check:**
1. `ImagePreprocessor` is working correctly
2. `TesseractOcrEngine` receives bitmap
3. Tesseract is initialized

**Debug:**
```java
// Save preprocessed image to debug
Bitmap processed = preprocessImageUseCase.execute(bitmap);
saveToFile(processed, "debug_preprocessed.png");
```

### Issue: Permissions not working

**Check:**
- `ImagePickerHandler.onPermissionResult()` is called from MainActivity

## Files Modified

### Deleted Functions from MainActivity:
- `checkPermissionAndPickImage()`
- `openImagePicker()`
- `initializeTesseract()`
- `getCorrectlyOrientedBitmap()`
- `rotateBitmap()`
- `resizeIfNeeded()`
- `toGrayscale()`
- `applyGaussianBlur()`
- `adjustContrast()`
- `applyAdaptiveThreshold()`
- `preprocessTicketImage()`

### Moved To:
- **ImagePickerHandler**: Permission and image selection logic
- **TesseractRepository**: Tesseract initialization
- **ImageLoader**: Image loading and orientation
- **ImagePreprocessor**: All preprocessing algorithms
- **TesseractOcrEngine**: OCR execution
- **ProcessImageUseCase**: Workflow coordination

### Kept in MainActivity:
- UI component references
- `onCreate()` setup
- `onDestroy()` cleanup
- `processImage()` coordination (simplified)

## Migration Checklist

If you're working on an old branch, here's how to update:

- [ ] Pull latest changes
- [ ] Update imports in MainActivity
- [ ] Remove old code replaced by new classes
- [ ] Update any custom modifications to use new architecture
- [ ] Test image selection
- [ ] Test OCR processing
- [ ] Test error handling

## Questions?

### Where do I put new UI code?
→ `MainActivity.java` or create new handler in `presentation/`

### Where do I put new business logic?
→ Create new use case in `domain/usecases/`

### Where do I put new OCR implementation?
→ Create new engine in `data/ocr/` implementing `RecognizeTextUseCase`

### Where do I put new preprocessing?
→ Add method to `ImagePreprocessor` or create new implementation

### How do I add new dependencies?
→ Pass through constructor, inject in `initializeDependencies()`

## Next Steps

1. Read `CLEAN_ARCHITECTURE.md` for detailed explanation
2. Review `ARCHITECTURE_DIAGRAM.md` for visual reference
3. Explore the new code structure
4. Try adding ML Kit using the guide above
5. Write tests for individual components

## Summary

| What | Where | Why |
|------|-------|-----|
| UI Logic | `MainActivity.java`, `presentation/` | Easy to modify UI |
| Business Logic | `domain/usecases/` | Testable, reusable |
| OCR Implementation | `data/ocr/` | Swappable engines |
| Preprocessing | `data/preprocessing/` | Isolated, testable |
| Tesseract Management | `data/repository/` | Centralized lifecycle |

The refactoring maintains all existing functionality while providing a solid foundation for future enhancements.
