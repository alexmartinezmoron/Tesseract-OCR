# Tesseract-OCR

Proyecto Android mínimo que demuestra la integración de OCR (Reconocimiento Óptico de Caracteres) local usando Tesseract.

## Características

- **Procesamiento Local**: Todo el procesamiento OCR se realiza localmente en el dispositivo sin enviar datos externos
- **Interfaz Simple**: Una pantalla con botón para seleccionar imágenes y mostrar resultados
- **Tesseract OCR**: Utiliza la biblioteca tess-two para reconocimiento de texto en español
- **Permisos Modernos**: Compatible con Android 13+ (READ_MEDIA_IMAGES) y versiones anteriores (READ_EXTERNAL_STORAGE)

## Requisitos

- Android Studio (versión recomendada: Arctic Fox o superior)
- SDK mínimo: Android 7.0 (API 24)
- SDK objetivo: Android 14 (API 34)
- Java 8 o superior

## Estructura del Proyecto

```
Tesseract-OCR/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/example/tesseractocr/
│   │       │   └── MainActivity.java          # Actividad principal con lógica OCR
│   │       ├── res/
│   │       │   ├── layout/
│   │       │   │   └── activity_main.xml      # Diseño de la interfaz
│   │       │   └── values/
│   │       │       └── strings.xml            # Recursos de texto
│   │       ├── assets/
│   │       │   └── tessdata/
│   │       │       └── spa.traineddata        # Datos entrenados para español
│   │       └── AndroidManifest.xml            # Configuración de la app
│   └── build.gradle                           # Configuración de dependencias
├── build.gradle                               # Configuración del proyecto
├── settings.gradle                            # Configuración de módulos
└── README.md                                  # Este archivo
```

## Instalación

1. **Clonar el repositorio**:
   ```bash
   git clone https://github.com/alexmartinezmoron/Tesseract-OCR.git
   cd Tesseract-OCR
   ```

2. **Abrir en Android Studio**:
   - Abrir Android Studio
   - Seleccionar "Open an Existing Project"
   - Navegar a la carpeta del proyecto clonado
   - Esperar a que Gradle sincronice las dependencias

3. **Ejecutar la aplicación**:
   - Conectar un dispositivo Android o iniciar un emulador
   - Hacer clic en el botón "Run" (▶️) en Android Studio
   - Seleccionar el dispositivo destino

## Uso

1. **Lanzar la aplicación**: Abrir la app desde el dispositivo
2. **Seleccionar imagen**: Tocar el botón "Seleccionar Imagen"
3. **Conceder permisos**: Si es la primera vez, conceder permiso de acceso a imágenes
4. **Elegir imagen**: Seleccionar una imagen con texto de la galería del dispositivo
5. **Ver resultados**: El texto extraído aparecerá automáticamente en la pantalla

## Dependencias Principales

- **AndroidX AppCompat**: Compatibilidad con versiones anteriores de Android
- **Material Components**: Componentes de diseño Material
- **ConstraintLayout**: Sistema de diseño flexible
- **tess-two (9.1.0)**: Biblioteca Tesseract OCR para Android

## Características Técnicas

### Permisos
La aplicación solicita los permisos necesarios según la versión de Android:
- **Android 13+**: `READ_MEDIA_IMAGES`
- **Android 12 y anteriores**: `READ_EXTERNAL_STORAGE`

### Procesamiento OCR
- El procesamiento se ejecuta en un hilo secundario para no bloquear la UI
- Utiliza el modelo de lenguaje español (`spa.traineddata`)
- Los datos del modelo se copian desde assets al almacenamiento interno en el primer uso

### Gestión de Recursos
- La instancia de TessBaseAPI se libera correctamente en `onDestroy()`
- El modelo de lenguaje se almacena en el directorio privado de la app

## Modificaciones y Extensiones

### Agregar otros idiomas
1. Descargar el archivo `.traineddata` del idioma deseado de [tessdata](https://github.com/tesseract-ocr/tessdata)
2. Colocarlo en `app/src/main/assets/tessdata/`
3. Modificar el código en `MainActivity.java` para usar el nuevo idioma:
   ```java
   tessBaseAPI.init(dataPath, "eng"); // Para inglés
   ```

### Mejorar la precisión
- Usar imágenes de alta calidad con buen contraste
- Tomar fotos con buena iluminación, sin sombras
- La app ya incluye preprocesamiento automático (escala de grises, ajuste de contraste, umbral adaptativo)
- Usar modelos de lenguaje actualizados

### Optimizaciones para tickets fotografiados
La aplicación incluye preprocesamiento automático optimizado para tickets:
- **Conversión a escala de grises**: Normaliza variaciones de color
- **Ajuste de contraste**: Mejora la legibilidad del texto
- **Umbral adaptativo**: Maneja iluminación irregular en fotografías
- **Modo de segmentación PSM_SINGLE_BLOCK**: Optimizado para documentos como tickets

## Solución de Problemas

### Error al inicializar Tesseract
- Verificar que el archivo `spa.traineddata` esté en `assets/tessdata/`
- Comprobar que hay suficiente espacio de almacenamiento

### No se detecta texto o falta texto
- Asegurarse de que la imagen tiene texto legible
- Verificar que el texto está en español
- Intentar con una imagen de mayor calidad
- Para tickets fotografiados: tomar la foto con buena iluminación, sin sombras ni reflejos
- La aplicación ahora incluye preprocesamiento automático para mejorar el reconocimiento

### Problemas de permisos
- Verificar que los permisos están declarados en `AndroidManifest.xml`
- Asegurarse de conceder los permisos cuando la app los solicite

## Licencia

Este proyecto es un ejemplo educativo de código abierto.

## Contribuciones

Las contribuciones son bienvenidas. Por favor, abre un issue o pull request para sugerencias y mejoras.