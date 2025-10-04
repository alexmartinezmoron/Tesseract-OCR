# Guía de Inicio Rápido

Esta guía te ayudará a poner en marcha la aplicación Tesseract OCR en menos de 10 minutos.

## Prerrequisitos

- **Android Studio** instalado ([Descargar aquí](https://developer.android.com/studio))
- Un dispositivo Android (API 24+) o emulador configurado

## Pasos de Instalación

### 1. Clonar el Repositorio

```bash
git clone https://github.com/alexmartinezmoron/Tesseract-OCR.git
cd Tesseract-OCR
```

### 2. Abrir en Android Studio

1. Lanza Android Studio
2. Selecciona **File > Open**
3. Navega a la carpeta `Tesseract-OCR` que acabas de clonar
4. Haz clic en **OK**

### 3. Sincronizar Gradle

Android Studio automáticamente sincronizará las dependencias de Gradle. Si no lo hace:

1. Ve a **File > Sync Project with Gradle Files**
2. Espera a que la sincronización termine (puede tomar unos minutos la primera vez)

### 4. Configurar un Dispositivo

#### Opción A: Usar un Dispositivo Físico

1. Habilita **Opciones de desarrollador** en tu dispositivo Android:
   - Ve a **Ajustes > Acerca del teléfono**
   - Toca **Número de compilación** 7 veces
   - Vuelve a Ajustes y busca **Opciones de desarrollador**
   
2. Habilita **Depuración USB**:
   - En **Opciones de desarrollador**, activa **Depuración USB**
   
3. Conecta tu dispositivo por USB
4. Acepta la solicitud de depuración USB en el dispositivo

#### Opción B: Usar un Emulador

1. En Android Studio, ve a **Tools > Device Manager**
2. Haz clic en **Create Device**
3. Selecciona un dispositivo (recomendado: Pixel 5)
4. Selecciona una imagen del sistema (API 24 o superior)
5. Haz clic en **Finish**

### 5. Ejecutar la Aplicación

1. Haz clic en el botón **Run** (▶️) en la barra de herramientas
2. Selecciona tu dispositivo o emulador
3. Espera a que la app se compile e instale
4. La app se abrirá automáticamente

## Uso de la Aplicación

### Primera Ejecución

1. La app se iniciará mostrando:
   - Un botón **"Seleccionar Imagen"**
   - Un área gris para previsualizar imágenes
   - Un texto de estado
   - Un área para mostrar resultados

2. Toca el botón **"Seleccionar Imagen"**

3. **Primera vez**: Se te pedirá permiso para acceder a las imágenes
   - Toca **"Permitir"** o **"Allow"**

4. Selecciona una imagen con texto de tu galería
   - Recomendación: Usa una imagen con texto claro y legible en español

5. Espera unos segundos mientras se procesa la imagen
   - Verás el mensaje **"Procesando imagen…"**

6. El texto extraído aparecerá en la parte inferior de la pantalla

## Consejos para Mejores Resultados

### Calidad de la Imagen

- Usa imágenes con **buena iluminación**
- Evita imágenes borrosas
- El texto debe ser **claro y de buen tamaño**
- Fondos simples funcionan mejor

### Tipos de Texto Compatibles

✅ **Funciona bien con:**
- Texto impreso en documentos
- Capturas de pantalla de texto
- Fotos de libros o revistas
- Carteles y letreros

❌ **Puede tener problemas con:**
- Texto manuscrito
- Fuentes muy decorativas
- Texto en ángulos extremos
- Imágenes de baja resolución

## Solución Rápida de Problemas

### La app no compila

**Problema**: Errores de sincronización de Gradle

**Solución**:
```bash
# Limpiar el proyecto
./gradlew clean

# Sincronizar nuevamente
./gradlew build
```

### No se puede seleccionar imágenes

**Problema**: Permisos no concedidos

**Solución**:
1. Ve a Ajustes del dispositivo
2. Busca la app "Tesseract OCR"
3. Ve a Permisos
4. Activa el permiso de Almacenamiento/Fotos

### El OCR no detecta texto

**Problema**: Imagen de baja calidad o idioma incorrecto

**Solución**:
- Prueba con una imagen de mejor calidad
- Asegúrate que el texto esté en **español**
- Verifica que el archivo `spa.traineddata` esté en `app/src/main/assets/tessdata/`
- Para tickets fotografiados: asegúrate de tomar la foto con buena iluminación y sin sombras

**Mejoras recientes para tickets fotografiados**:
- La app ahora preprocesa automáticamente las imágenes
- Convierte a escala de grises y ajusta el contraste
- Aplica umbral adaptativo para manejar iluminación irregular
- Usa modo de segmentación optimizado para tickets

### La app se cierra inesperadamente

**Problema**: Posible error de inicialización

**Solución**:
1. Desinstala la app
2. Vuelve a instalarla desde Android Studio
3. Revisa los logs en **Logcat** para más detalles

## Próximos Pasos

Una vez que la app funcione correctamente:

1. Prueba con diferentes tipos de imágenes
2. Lee el archivo **PROJECT_OVERVIEW.md** para entender la arquitectura
3. Consulta **CONTRIBUTING.md** si quieres agregar funcionalidades
4. Experimenta modificando el código

## ¿Necesitas Ayuda?

Si tienes problemas:

1. Revisa el archivo **README.md** para información detallada
2. Busca en los **Issues** del repositorio
3. Abre un nuevo **Issue** describiendo tu problema

¡Disfruta usando Tesseract OCR! 📸📝
