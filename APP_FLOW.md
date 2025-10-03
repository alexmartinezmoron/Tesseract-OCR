# Flujo de la Aplicación

## Diagrama de Flujo Simplificado

```
┌─────────────────────────────────────────────────────────────┐
│                    INICIO DE LA APP                         │
│                   (MainActivity.onCreate)                    │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│              INICIALIZACIÓN DE TESSERACT                    │
│  - Crear directorio tessdata/                               │
│  - Copiar spa.traineddata desde assets                      │
│  - Inicializar TessBaseAPI                                  │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                  PANTALLA PRINCIPAL                         │
│  ┌─────────────────────────────────────────┐               │
│  │    [Seleccionar Imagen]  (Button)       │               │
│  └─────────────────────────────────────────┘               │
│  ┌─────────────────────────────────────────┐               │
│  │                                          │               │
│  │     (ImageView - Preview)                │               │
│  │                                          │               │
│  └─────────────────────────────────────────┘               │
│  Status: Listo para procesar imagen                        │
│  ┌─────────────────────────────────────────┐               │
│  │ El texto extraído aparecerá aquí...     │               │
│  │ (TextView - Resultados)                  │               │
│  └─────────────────────────────────────────┘               │
└────────────────────────┬────────────────────────────────────┘
                         │
                         │ Usuario toca botón
                         ▼
┌─────────────────────────────────────────────────────────────┐
│           VERIFICAR PERMISOS                                │
│  ¿Tiene permiso de lectura de imágenes?                     │
└─────┬──────────────────────────────────────┬────────────────┘
      │                                       │
      │ NO                                    │ SÍ
      ▼                                       ▼
┌──────────────────────┐         ┌──────────────────────────┐
│ SOLICITAR PERMISOS   │         │  ABRIR SELECTOR          │
│ (requestPermissions) │         │  DE IMÁGENES             │
└──────┬───────────────┘         │  (Image Picker Intent)   │
       │                         └──────────┬───────────────┘
       │ Concedido                          │
       └─────────────────┬──────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│              USUARIO SELECCIONA IMAGEN                      │
│              (onActivityResult)                             │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                CARGAR IMAGEN                                │
│  - Obtener URI de la imagen                                 │
│  - Decodificar a Bitmap                                     │
│  - Mostrar en ImageView                                     │
│  Status: "Procesando imagen..."                             │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│            PROCESAMIENTO OCR (Thread Separado)              │
│  1. tessBaseAPI.setImage(bitmap)                            │
│  2. String text = tessBaseAPI.getUTF8Text()                 │
│  3. Retornar resultado                                      │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│         ACTUALIZAR UI (runOnUiThread)                       │
│  - Mostrar texto extraído en TextView                       │
│  - Status: "Procesamiento completado"                       │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                  PANTALLA CON RESULTADO                     │
│  ┌─────────────────────────────────────────┐               │
│  │    [Seleccionar Imagen]                 │               │
│  └─────────────────────────────────────────┘               │
│  ┌─────────────────────────────────────────┐               │
│  │  [Imagen seleccionada mostrada aquí]    │               │
│  └─────────────────────────────────────────┘               │
│  Status: Procesamiento completado                          │
│  ┌─────────────────────────────────────────┐               │
│  │ Texto extraído:                          │               │
│  │ Lorem ipsum dolor sit amet...            │               │
│  │ consectetur adipiscing elit...           │               │
│  └─────────────────────────────────────────┘               │
└─────────────────────────────────────────────────────────────┘
```

## Componentes Clave

### 1. Activity Lifecycle
```
onCreate() → initializeTesseract() → setupUI() → Ready
                                                    ↓
                                              User Action
                                                    ↓
                                          onDestroy() → cleanup
```

### 2. Permission Flow
```
checkPermission() → requestPermissions() → onRequestPermissionsResult()
                                                      ↓
                                            granted / denied
```

### 3. OCR Processing
```
selectImage() → loadBitmap() → new Thread() → tessBaseAPI.setImage()
                                                      ↓
                                            tessBaseAPI.getUTF8Text()
                                                      ↓
                                            runOnUiThread() → updateUI()
```

## Gestión de Hilos

```
┌──────────────┐
│  Main Thread │ ← UI Operations
│   (UI)       │
└──────┬───────┘
       │
       │ new Thread()
       ▼
┌──────────────┐
│ Worker Thread│ ← OCR Processing
│   (OCR)      │
└──────┬───────┘
       │
       │ runOnUiThread()
       ▼
┌──────────────┐
│  Main Thread │ ← Update Results
│   (UI)       │
└──────────────┘
```

## Estados de la Aplicación

1. **INICIALIZADO**: App lista, Tesseract configurado
2. **ESPERANDO**: Esperando acción del usuario
3. **SOLICITANDO_PERMISOS**: Esperando decisión del usuario
4. **SELECCIONANDO**: Selector de imágenes abierto
5. **PROCESANDO**: OCR en ejecución
6. **COMPLETADO**: Resultados mostrados
7. **ERROR**: Algo falló, mostrar mensaje

## Manejo de Errores

```
Error en cualquier paso
        ↓
Toast.makeText() → Mostrar mensaje de error
        ↓
Actualizar TextView con estado de error
        ↓
Volver al estado ESPERANDO
```
