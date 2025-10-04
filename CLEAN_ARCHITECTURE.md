# Clean Architecture Implementation

## Overview

This project has been refactored to follow **Clean Architecture** principles, separating the code into distinct layers with clear responsibilities. This structure makes the code more maintainable, testable, and extensible.

## Architecture Layers

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                        │
│  (UI, Activities, Handlers)                                  │
│  - MainActivity.java                                         │
│  - ImagePickerHandler.java                                   │
└────────────────────┬────────────────────────────────────────┘
                     │ depends on
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                     DOMAIN LAYER                             │
│  (Business Logic, Use Cases, Models)                         │
│  - ProcessImageUseCase.java                                  │
│  - PreprocessImageUseCase.java (interface)                   │
│  - RecognizeTextUseCase.java (interface)                     │
│  - OcrResult.java (model)                                    │
└────────────────────┬────────────────────────────────────────┘
                     │ depends on
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                      DATA LAYER                              │
│  (Implementations, Repositories)                             │
│  - TesseractRepository.java                                  │
│  - ImagePreprocessor.java                                    │
│  - TesseractOcrEngine.java                                   │
│  - ImageLoader.java                                          │
└─────────────────────────────────────────────────────────────┘
```

## Directory Structure

```
app/src/main/java/com/example/tesseractocr/
├── MainActivity.java                      # Main Activity (Presentation)
│
├── presentation/                          # Presentation Layer
│   └── ImagePickerHandler.java           # Handles image selection and permissions
│
├── domain/                                # Domain Layer (Business Logic)
│   ├── models/
│   │   └── OcrResult.java                # Data model for OCR results
│   └── usecases/
│       ├── ProcessImageUseCase.java      # Coordinates preprocessing and OCR
│       ├── PreprocessImageUseCase.java   # Interface for image preprocessing
│       └── RecognizeTextUseCase.java     # Interface for text recognition
│
└── data/                                  # Data Layer (Implementations)
    ├── repository/
    │   └── TesseractRepository.java      # Manages Tesseract lifecycle
    ├── preprocessing/
    │   ├── ImagePreprocessor.java        # Image preprocessing implementation
    │   └── ImageLoader.java              # Loads images with orientation
    └── ocr/
        └── TesseractOcrEngine.java       # Tesseract OCR implementation
```

## Layer Responsibilities

### 1. Presentation Layer (`presentation/` + `MainActivity`)

**Responsibility**: Handle UI interactions and display data to the user.

- `MainActivity`: Displays UI, handles user input, coordinates view updates
- `ImagePickerHandler`: Manages image selection and permissions

**Key Principles**:
- No business logic
- Only UI and Android framework code
- Delegates all work to use cases

### 2. Domain Layer (`domain/`)

**Responsibility**: Contains business logic and defines contracts (interfaces).

- `ProcessImageUseCase`: Orchestrates the image processing workflow
- `PreprocessImageUseCase`: Interface defining image preprocessing contract
- `RecognizeTextUseCase`: Interface defining text recognition contract
- `OcrResult`: Domain model for OCR results

**Key Principles**:
- Platform-independent (no Android dependencies)
- Contains only business rules
- Defines interfaces that data layer implements

### 3. Data Layer (`data/`)

**Responsibility**: Implements domain interfaces and manages data sources.

- `TesseractRepository`: Manages Tesseract initialization and lifecycle
- `ImagePreprocessor`: Implements image preprocessing algorithms
- `TesseractOcrEngine`: Implements text recognition using Tesseract
- `ImageLoader`: Handles image loading with proper orientation

**Key Principles**:
- Implements domain interfaces
- Handles external dependencies (Tesseract, file system)
- Can be easily swapped with different implementations

## Benefits of This Architecture

### 1. Separation of Concerns
Each layer has a single, well-defined responsibility:
- **Presentation**: UI and user interactions
- **Domain**: Business logic
- **Data**: Implementation details

### 2. Testability
- Each layer can be tested independently
- Business logic (domain) has no Android dependencies
- Easy to mock interfaces for unit testing

### 3. Maintainability
- Changes in one layer don't affect others
- Clear boundaries between components
- Easy to understand and navigate

### 4. Extensibility
- Easy to add new OCR engines (e.g., ML Kit)
- Simple to modify preprocessing algorithms
- Can add new use cases without touching existing code

## How to Add ML Kit (Future Enhancement)

Thanks to Clean Architecture, adding ML Kit is straightforward:

1. Create `MLKitOcrEngine.java` implementing `RecognizeTextUseCase`
2. Update dependency injection in `MainActivity`:
   ```java
   // Instead of:
   RecognizeTextUseCase recognizeTextUseCase = new TesseractOcrEngine(tesseractRepository);
   
   // Use:
   RecognizeTextUseCase recognizeTextUseCase = new MLKitOcrEngine();
   ```
3. No changes needed to UI or use cases!

## Dependency Flow

```
MainActivity 
    ↓ creates
TesseractRepository ──→ TesseractOcrEngine
ImagePreprocessor   ──→ (implements PreprocessImageUseCase)
TesseractOcrEngine  ──→ (implements RecognizeTextUseCase)
    ↓ injects into
ProcessImageUseCase
    ↓ uses
MainActivity (calls use case)
```

## Key Concepts

### Dependency Inversion
- High-level modules (domain) don't depend on low-level modules (data)
- Both depend on abstractions (interfaces)
- Example: `ProcessImageUseCase` depends on `RecognizeTextUseCase` interface, not `TesseractOcrEngine`

### Single Responsibility
- Each class has one reason to change
- `ImagePreprocessor`: only handles image preprocessing
- `TesseractOcrEngine`: only handles text recognition
- `MainActivity`: only handles UI

### Open/Closed Principle
- Open for extension (add new OCR engines)
- Closed for modification (existing code doesn't change)

## Usage Example

```java
// In MainActivity.initializeDependencies()

// 1. Create data layer implementations
TesseractRepository repository = new TesseractRepository(context);
repository.initialize();

ImagePreprocessor preprocessor = new ImagePreprocessor();
TesseractOcrEngine ocrEngine = new TesseractOcrEngine(repository);

// 2. Create domain use case
ProcessImageUseCase processImageUseCase = new ProcessImageUseCase(
    preprocessor,  // PreprocessImageUseCase interface
    ocrEngine      // RecognizeTextUseCase interface
);

// 3. Use in presentation layer
OcrResult result = processImageUseCase.execute(bitmap);
if (result.isSuccess()) {
    display(result.getText());
}
```

## Migration Notes

### Before (Monolithic)
All code was in `MainActivity.java`:
- UI logic
- Permission handling
- Image loading
- Image preprocessing
- Tesseract initialization
- OCR processing

### After (Clean Architecture)
Code is separated by responsibility:
- **Presentation**: `MainActivity`, `ImagePickerHandler`
- **Domain**: Use cases and models
- **Data**: Implementations and repositories

### Breaking Changes
None! The external behavior is identical. This is a pure refactoring.

## Testing Strategy

### Unit Tests
- **Domain Layer**: Test use cases with mock implementations
- **Data Layer**: Test each implementation independently

### Integration Tests
- Test complete flow from presentation to data layer

### UI Tests
- Test user interactions through `MainActivity`

## Future Enhancements

1. **Add ML Kit Support**
   - Create `MLKitOcrEngine` implementing `RecognizeTextUseCase`
   - Allow user to choose OCR engine

2. **Enhanced Preprocessing**
   - Add more algorithms to `ImagePreprocessor`
   - Create strategy pattern for different preprocessing pipelines

3. **Offline/Online Processing**
   - Add cloud OCR option
   - Create `CloudOcrEngine` implementing `RecognizeTextUseCase`

4. **Caching**
   - Add caching layer in data layer
   - Cache OCR results for processed images

## Conclusion

This Clean Architecture implementation provides a solid foundation for:
- Maintaining the current codebase
- Adding new features (like ML Kit)
- Testing individual components
- Scaling the application

The architecture follows SOLID principles and industry best practices, making the code professional and production-ready.
