# Project Overview: Tesseract OCR Android App

## Objetivo del Proyecto

Crear una aplicación Android mínima que demuestre la integración de Tesseract OCR para reconocimiento de texto en imágenes, procesado completamente en local sin enviar datos fuera del dispositivo.

## Arquitectura de la Aplicación

### Componentes Principales

1. **MainActivity.java**
   - Punto de entrada de la aplicación
   - Gestiona la selección de imágenes
   - Inicializa Tesseract
   - Realiza el procesamiento OCR

2. **activity_main.xml**
   - Layout simple con LinearLayout vertical
   - Botón de selección de imagen
   - ImageView para previsualizar la imagen
   - TextView para mostrar el estado
   - ScrollView con TextView para mostrar el texto extraído

3. **Tesseract Integration**
   - Librería: `tess-two:9.1.0`
   - Datos de entrenamiento: español (spa.traineddata)
   - Procesamiento en hilo secundario para no bloquear UI

## Flujo de la Aplicación

```
Usuario abre app
    ↓
Usuario toca "Seleccionar Imagen"
    ↓
Sistema solicita permisos (si es necesario)
    ↓
Usuario concede permisos
    ↓
Se abre el selector de imágenes
    ↓
Usuario selecciona una imagen
    ↓
La imagen se muestra en ImageView
    ↓
Tesseract procesa la imagen en segundo plano
    ↓
El texto extraído se muestra en TextView
```

## Características Técnicas

### Gestión de Permisos
- **Android 13+ (API 33+)**: Usa `READ_MEDIA_IMAGES`
- **Android 12 y anterior**: Usa `READ_EXTERNAL_STORAGE`
- Solicitud de permisos en tiempo de ejecución

### Procesamiento OCR
- **Inicialización**: Los datos de entrenamiento se copian de assets a almacenamiento interno
- **Ejecución**: El OCR se ejecuta en un hilo separado usando Thread
- **Actualización UI**: Los resultados se muestran usando `runOnUiThread()`

### Gestión de Recursos
- TessBaseAPI se libera en `onDestroy()` para evitar fugas de memoria
- Los archivos de datos se mantienen en el directorio privado de la app

## Mejoras Futuras Potenciales

1. **Preprocesamiento de Imágenes**
   - Conversión a escala de grises
   - Ajuste de contraste y brillo
   - Binarización

2. **Soporte Multi-idioma**
   - Permitir al usuario seleccionar el idioma
   - Descargar modelos de lenguaje bajo demanda

3. **Captura de Cámara**
   - Permitir tomar foto directamente con la cámara
   - Vista previa en tiempo real

4. **Exportación de Resultados**
   - Copiar al portapapeles
   - Compartir con otras apps
   - Guardar como archivo de texto

5. **Optimización de Rendimiento**
   - Usar ProcessPoolExecutor para mejor gestión de hilos
   - Implementar caché de resultados
   - Comprimir imágenes grandes antes de procesar

6. **UI/UX Mejorada**
   - Indicador de progreso animado
   - Resaltado del texto en la imagen
   - Historial de procesamiento

## Requisitos de Sistema

- **Dispositivo**: Android 7.0 (API 24) o superior
- **Espacio**: ~25MB (app + datos de entrenamiento)
- **Permisos**: Acceso a almacenamiento/galería

## Notas de Desarrollo

- El proyecto usa AndroidX para mejor compatibilidad
- Gradle 8.2 y Android Gradle Plugin 8.1.0
- Compilado con SDK 34 (Android 14)
- Compatible con Java 8
