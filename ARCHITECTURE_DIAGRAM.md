# Architecture Diagram - Clean Architecture Implementation

## High-Level Layer View

```
┌─────────────────────────────────────────────────────────────────┐
│                      PRESENTATION LAYER                          │
│                         (UI / View)                              │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  MainActivity.java                                       │   │
│  │  - Display UI                                            │   │
│  │  - Handle button clicks                                  │   │
│  │  - Show results                                          │   │
│  │  - Update progress                                       │   │
│  └────────────┬─────────────────────────────────────────────┘   │
│               │                                                  │
│  ┌────────────▼─────────────────────────────────────────────┐   │
│  │  ImagePickerHandler.java                                 │   │
│  │  - Manage permissions                                    │   │
│  │  - Open image picker                                     │   │
│  │  - Handle permission results                             │   │
│  └──────────────────────────────────────────────────────────┘   │
└──────────────────────────┬──────────────────────────────────────┘
                           │ uses
                           │
┌──────────────────────────▼──────────────────────────────────────┐
│                       DOMAIN LAYER                               │
│                  (Business Logic / Use Cases)                    │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  ProcessImageUseCase.java                                │   │
│  │  - Orchestrate preprocessing                             │   │
│  │  - Orchestrate OCR                                       │   │
│  │  - Handle errors                                         │   │
│  └──────┬──────────────────────────────────────────┬────────┘   │
│         │ uses                                      │ uses       │
│  ┌──────▼─────────────────┐             ┌──────────▼────────┐   │
│  │ PreprocessImageUseCase │             │ RecognizeTextUse  │   │
│  │     (Interface)        │             │ Case (Interface)  │   │
│  └────────────────────────┘             └───────────────────┘   │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  OcrResult.java (Model)                                  │   │
│  │  - text: String                                          │   │
│  │  - success: boolean                                      │   │
│  │  - errorMessage: String                                  │   │
│  └──────────────────────────────────────────────────────────┘   │
└──────────────────────────┬──────────────────────────────────────┘
                           │ implemented by
                           │
┌──────────────────────────▼──────────────────────────────────────┐
│                        DATA LAYER                                │
│                  (Implementations / Data Sources)                │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  TesseractRepository.java                                │   │
│  │  - Initialize Tesseract                                  │   │
│  │  - Copy training data                                    │   │
│  │  - Manage TessBaseAPI lifecycle                          │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  ImagePreprocessor.java                                  │   │
│  │  implements PreprocessImageUseCase                       │   │
│  │  - Resize images                                         │   │
│  │  - Adjust contrast                                       │   │
│  │  - Convert to grayscale                                  │   │
│  │  - Apply Gaussian blur                                   │   │
│  │  - Adaptive threshold                                    │   │
│  │  - Rotate bitmap                                         │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  TesseractOcrEngine.java                                 │   │
│  │  implements RecognizeTextUseCase                         │   │
│  │  - Set image on Tesseract                                │   │
│  │  - Extract text                                          │   │
│  │  - Return OcrResult                                      │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  ImageLoader.java                                        │   │
│  │  - Load bitmap from URI                                  │   │
│  │  - Read EXIF orientation                                 │   │
│  │  - Apply rotation                                        │   │
│  └──────────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────────┘
```

## Data Flow Diagram

```
User Action
    │
    ▼
┌─────────────────────┐
│  User clicks        │
│  "Select Image"     │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│  ImagePickerHandler │
│  - Check permissions│
│  - Open picker      │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│  User selects image │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────────┐
│  MainActivity           │
│  - Receives URI         │
│  - Update progress text │
└──────────┬──────────────┘
           │
           ▼
┌─────────────────────────┐
│  ImageLoader            │
│  - Load bitmap          │
│  - Fix orientation      │
└──────────┬──────────────┘
           │
           ▼
┌─────────────────────────┐
│  Display image in       │
│  ImageView              │
└──────────┬──────────────┘
           │
           ▼
┌─────────────────────────────────┐
│  Background Thread              │
│  ┌───────────────────────────┐  │
│  │  ProcessImageUseCase      │  │
│  │  execute(bitmap)          │  │
│  └──────────┬────────────────┘  │
│             │                    │
│             ▼                    │
│  ┌───────────────────────────┐  │
│  │  ImagePreprocessor        │  │
│  │  - Resize                 │  │
│  │  - Adjust contrast        │  │
│  └──────────┬────────────────┘  │
│             │                    │
│             ▼                    │
│  ┌───────────────────────────┐  │
│  │  TesseractOcrEngine       │  │
│  │  - setImage()             │  │
│  │  - getUTF8Text()          │  │
│  └──────────┬────────────────┘  │
│             │                    │
│             ▼                    │
│  ┌───────────────────────────┐  │
│  │  OcrResult                │  │
│  │  - success: true          │  │
│  │  - text: "..."            │  │
│  └──────────┬────────────────┘  │
└─────────────┼────────────────────┘
              │
              ▼
┌──────────────────────────┐
│  runOnUiThread           │
│  - Update TextView       │
│  - Update progress       │
└──────────────────────────┘
```

## Dependency Injection Flow

```
MainActivity.onCreate()
    │
    ▼
initializeDependencies()
    │
    ├─► Create TesseractRepository
    │       │
    │       └─► repository.initialize()
    │               - Copy training data
    │               - Init TessBaseAPI
    │
    ├─► Create ImagePreprocessor
    │       - Implements PreprocessImageUseCase
    │
    ├─► Create ImageLoader
    │       - Takes ImagePreprocessor
    │
    ├─► Create TesseractOcrEngine
    │       - Takes TesseractRepository
    │       - Implements RecognizeTextUseCase
    │
    └─► Create ProcessImageUseCase
            - Takes PreprocessImageUseCase
            - Takes RecognizeTextUseCase
```

## Class Relationships

```
┌──────────────────────┐
│   MainActivity       │
│ (AppCompatActivity)  │
└────┬────────┬────────┘
     │        │
     │        └──────────────────────┐
     │                               │
     │ has                           │ has
     ▼                               ▼
┌──────────────────┐      ┌──────────────────────┐
│ ProcessImageUse  │      │ ImagePickerHandler   │
│ Case             │      │                      │
└────┬────────┬────┘      └──────────────────────┘
     │        │
     │ uses   │ uses
     ▼        ▼
┌─────────┐  ┌──────────┐
│Preprocess│  │Recognize │
│ImageUse  │  │TextUse   │
│Case      │  │Case      │
│(Interface)  │(Interface)
└────▲────┘  └────▲─────┘
     │            │
     │            │ implements
     │            │
┌────┴──────┐ ┌──┴──────────────┐
│Image      │ │Tesseract        │
│Preprocessor  │OcrEngine        │
└───────────┘ └─┬───────────────┘
                │ uses
                ▼
              ┌─────────────────┐
              │Tesseract        │
              │Repository       │
              └─────────────────┘
```

## Component Interaction Sequence

```
Sequence: Processing an Image

MainActivity -> ImagePickerHandler : selectImage()
ImagePickerHandler -> System : checkPermissions()
System -> ImagePickerHandler : permissionGranted
ImagePickerHandler -> System : openImagePicker()
User -> System : selectsImage(uri)
System -> MainActivity : onImageSelected(uri)
MainActivity -> ImageLoader : loadBitmap(uri)
ImageLoader -> ImagePreprocessor : rotateBitmap(bitmap, orientation)
ImagePreprocessor -> ImageLoader : rotatedBitmap
ImageLoader -> MainActivity : bitmap
MainActivity -> ImageView : setImageBitmap(bitmap)
MainActivity -> Thread : start()
Thread -> ProcessImageUseCase : execute(bitmap)
ProcessImageUseCase -> ImagePreprocessor : execute(bitmap)
ImagePreprocessor -> ProcessImageUseCase : processedBitmap
ProcessImageUseCase -> TesseractOcrEngine : execute(processedBitmap)
TesseractOcrEngine -> TesseractRepository : getTessBaseAPI()
TesseractRepository -> TesseractOcrEngine : tessBaseAPI
TesseractOcrEngine -> TessBaseAPI : setImage(bitmap)
TesseractOcrEngine -> TessBaseAPI : getUTF8Text()
TessBaseAPI -> TesseractOcrEngine : extractedText
TesseractOcrEngine -> ProcessImageUseCase : OcrResult
ProcessImageUseCase -> Thread : result
Thread -> MainActivity : runOnUiThread()
MainActivity -> TextView : setText(result.getText())
MainActivity -> TextView : setStatus("Complete")
```

## Future Extension: Adding ML Kit

```
Current:
┌────────────────────────┐
│ RecognizeTextUseCase   │ ← Interface
└───────────▲────────────┘
            │ implements
┌───────────┴────────────┐
│ TesseractOcrEngine     │
└────────────────────────┘

Future:
┌────────────────────────┐
│ RecognizeTextUseCase   │ ← Interface
└───────────▲────────────┘
            │ implements
     ┌──────┴──────┐
     │             │
┌────┴───────┐ ┌──┴─────────┐
│ Tesseract  │ │ MLKitOcr   │ ← New implementation
│ OcrEngine  │ │ Engine     │
└────────────┘ └────────────┘

Changes needed in MainActivity:
// Switch OCR engine without changing use case
RecognizeTextUseCase recognizeTextUseCase = 
    useMlKit ? new MLKitOcrEngine() : new TesseractOcrEngine(repository);
```

## Package Structure

```
com.example.tesseractocr/
│
├── MainActivity.java                    [Presentation Layer]
│
├── presentation/                         [Presentation Layer]
│   └── ImagePickerHandler.java
│
├── domain/                               [Domain Layer]
│   ├── models/
│   │   └── OcrResult.java
│   └── usecases/
│       ├── ProcessImageUseCase.java
│       ├── PreprocessImageUseCase.java  (interface)
│       └── RecognizeTextUseCase.java    (interface)
│
└── data/                                 [Data Layer]
    ├── repository/
    │   └── TesseractRepository.java
    ├── preprocessing/
    │   ├── ImagePreprocessor.java       (implements PreprocessImageUseCase)
    │   └── ImageLoader.java
    └── ocr/
        └── TesseractOcrEngine.java      (implements RecognizeTextUseCase)
```

## Key Design Patterns Used

### 1. **Dependency Injection**
- Dependencies are passed through constructors
- Makes testing easier with mock objects

### 2. **Repository Pattern**
- `TesseractRepository` manages Tesseract lifecycle
- Abstracts data source details

### 3. **Strategy Pattern**
- `RecognizeTextUseCase` interface allows different OCR strategies
- Easy to swap Tesseract for ML Kit

### 4. **Use Case Pattern**
- `ProcessImageUseCase` encapsulates business logic
- Single responsibility: coordinate image processing

### 5. **Interface Segregation**
- Small, focused interfaces
- `PreprocessImageUseCase`, `RecognizeTextUseCase`

## Benefits Summary

| Aspect | Before | After |
|--------|--------|-------|
| **Lines in MainActivity** | 417 | 152 |
| **Classes** | 1 | 10 |
| **Testability** | Hard | Easy |
| **Adding ML Kit** | Complex | Simple |
| **Understanding Code** | Difficult | Clear |
| **Separation of Concerns** | None | Complete |

## Testing Strategy

```
Unit Tests:
├── domain/
│   ├── ProcessImageUseCaseTest (with mocks)
│   └── OcrResultTest
├── data/
│   ├── ImagePreprocessorTest
│   ├── TesseractOcrEngineTest (with mock repository)
│   └── TesseractRepositoryTest
└── presentation/
    └── ImagePickerHandlerTest (with mock activity)

Integration Tests:
└── Full flow test: UI -> Use Case -> Data Layer

UI Tests:
└── MainActivity UI interaction tests
```

---

This architecture provides a solid foundation for:
- Maintaining existing features
- Adding new OCR engines (ML Kit, Cloud OCR, etc.)
- Testing individual components in isolation
- Scaling the application with new features
