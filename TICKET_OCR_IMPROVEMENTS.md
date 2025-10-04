# Mejoras en el Reconocimiento de Tickets Fotografiados

## Problema Reportado

**Issue**: "tengo problemas con el reconocimiento de ticket, no me aparece todo el texto, los ticket son fotografiados y siempres son en español"

**Traducción**: Problemas con el reconocimiento de tickets donde no aparece todo el texto. Los tickets son fotografiados (no escaneados) y siempre están en español.

## Análisis del Problema

Los tickets fotografiados presentan desafíos específicos para OCR:

1. **Iluminación irregular**: Las fotos pueden tener sombras, reflejos o iluminación desigual
2. **Variaciones de color**: Dependiendo de la cámara y condiciones de luz
3. **Resolución variable**: Las fotos pueden tener diferentes calidades
4. **Ángulos y perspectiva**: Las fotos no siempre son perfectamente perpendiculares
5. **Texto pequeño**: Los tickets suelen tener texto muy pequeño

## Soluciones Implementadas

### 1. Cambio de Modo de Segmentación de Página

**Antes**:
```java
tessBaseAPI.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO);
```

**Después**:
```java
tessBaseAPI.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_BLOCK);
```

**Razón**: `PSM_SINGLE_BLOCK` trata la imagen como un único bloque de texto uniforme, lo cual es ideal para tickets y recibos. `PSM_AUTO` puede fallar al detectar el diseño complejo de un ticket.

### 2. Eliminación de Lista Blanca de Caracteres

**Antes**:
```java
tessBaseAPI.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST,
    "ABCDEFGHIJKLMNÑOPQRSTUVWXYZabcdefghijklmnñopqrstuvwxyzáéíóúÁÉÍÓÚ0123456789.,€$:-/ ");
```

**Después**: Removido completamente.

**Razón**: La lista blanca era demasiado restrictiva y podía impedir el reconocimiento de:
- Símbolos especiales comunes en tickets (%, #, @, *, etc.)
- Caracteres de formato
- Otros símbolos monetarios
- Caracteres especiales de productos o códigos

### 3. Activación de Preprocesamiento de Imagen

**Antes**: La mayoría del preprocesamiento estaba comentado
```java
//bitmap = toGrayscale(bitmap);
//bitmap = applyGaussianBlur(bitmap);
bitmap = adjustContrast(bitmap, 1f);
//bitmap = applyAdaptiveThreshold(bitmap);
```

**Después**: Preprocesamiento completo activado
```java
bitmap = toGrayscale(bitmap);
bitmap = adjustContrast(bitmap, 1.5f);
bitmap = applyAdaptiveThreshold(bitmap);
```

#### 3.1 Conversión a Escala de Grises
- Normaliza las variaciones de color
- Reduce el ruido de color de las fotografías
- Mejora la consistencia del reconocimiento

#### 3.2 Ajuste de Contraste (1.5f)
- Aumentado de 1.0 a 1.5
- Hace que el texto sea más legible
- Mejora la distinción entre texto y fondo

#### 3.3 Umbral Adaptativo
- Crucial para fotografías con iluminación irregular
- Calcula umbrales locales en lugar de uno global
- Maneja sombras y variaciones de iluminación
- Usa bloques de 15x15 píxeles para adaptarse localmente

### 4. Mantenimiento de Funcionalidades Existentes

Se mantienen las funcionalidades que ya estaban funcionando bien:
- Corrección de orientación EXIF
- Redimensionamiento inteligente (máximo 2000px)
- Procesamiento en hilo secundario
- Manejo robusto de errores

## Mejoras Técnicas Detalladas

### Umbral Adaptativo - Algoritmo

El algoritmo implementado calcula un umbral local para cada píxel:

1. Para cada píxel, calcula el promedio de los píxeles en un bloque de 15x15 alrededor
2. Resta una constante de ajuste (C=10)
3. Si el píxel es más claro que el umbral, se convierte en blanco; si no, en negro
4. Esto permite manejar diferentes condiciones de iluminación en diferentes partes de la imagen

```java
int blockSize = 15; // Tamaño del bloque
int C = 10; // Constante de ajuste
int threshold = sum / count - C;
int newColor = gray > threshold ? 0xFFFFFFFF : 0xFF000000;
```

### Ajuste de Contraste

El contraste se ajusta usando una matriz de color:

```java
float contrast = 1.5f;
cm.set(new float[] {
    contrast, 0, 0, 0, 0,
    0, contrast, 0, 0, 0,
    0, 0, contrast, 0, 0,
    0, 0, 0, 1, 0
});
```

Un valor de 1.5 aumenta el contraste en un 50%, haciendo que los textos oscuros sean más oscuros y los fondos claros más claros.

## Comparación: Antes vs Después

### Antes
- ✗ Modo PSM_AUTO podía fallar en tickets complejos
- ✗ Lista blanca bloqueaba caracteres especiales
- ✗ Preprocesamiento mínimo (solo contraste básico)
- ✗ No manejaba bien iluminación irregular
- ✗ Pérdida de texto en fotos con sombras

### Después
- ✓ PSM_SINGLE_BLOCK optimizado para tickets
- ✓ Sin restricciones en caracteres reconocidos
- ✓ Preprocesamiento completo activado
- ✓ Umbral adaptativo maneja iluminación irregular
- ✓ Mejor reconocimiento en condiciones variables

## Cómo Probar las Mejoras

### 1. Preparar Tickets de Prueba

Fotografiar tickets con diferentes condiciones:
- ✓ Con buena iluminación (luz natural uniforme)
- ✓ Con iluminación irregular (algunas sombras)
- ✓ Con texto pequeño
- ✓ Con diferentes fondos (blanco, colores)
- ✓ Con caracteres especiales (€, %, $, etc.)

### 2. Comparar Resultados

**Qué buscar**:
- Más líneas de texto reconocidas
- Reconocimiento de caracteres especiales
- Mejor manejo de áreas con sombras
- Textos más completos (menos palabras faltantes)

### 3. Casos de Prueba Específicos

| Característica | Qué Probar | Resultado Esperado |
|----------------|------------|-------------------|
| Precio | "15,50 €" | Reconoce el símbolo € |
| Descuento | "Descuento 10%" | Reconoce el símbolo % |
| Código | "REF#12345" | Reconoce el símbolo # |
| Sombras | Ticket con sombra parcial | Lee texto en área sombreada |
| Texto pequeño | Líneas de 6-8pt | Mejora legibilidad |

## Recomendaciones para el Usuario

Para obtener los mejores resultados al fotografiar tickets:

### Iluminación
- ✓ Usar luz natural o luz blanca uniforme
- ✓ Evitar sombras directas sobre el ticket
- ✓ Evitar reflejos (especialmente en tickets brillantes)

### Posicionamiento
- ✓ Tomar la foto lo más perpendicular posible al ticket
- ✓ Llenar el encuadre con el ticket (evitar mucho fondo)
- ✓ Mantener el ticket plano si es posible

### Calidad de la Imagen
- ✓ Enfocar bien el texto
- ✓ Usar la resolución más alta disponible
- ✓ Evitar mover la cámara durante la captura

### Condiciones del Ticket
- ✓ Estirar el ticket si está arrugado
- ✓ Limpiar manchas visibles si es posible
- ✓ Si el ticket está deteriorado, tomar múltiples fotos

## Limitaciones Conocidas

A pesar de las mejoras, algunas limitaciones persisten:

1. **Texto manuscrito**: Tesseract no está optimizado para escritura a mano
2. **Tickets muy deteriorados**: Texto borroso o ilegible no se puede recuperar
3. **Ángulos extremos**: Fotos muy inclinadas pueden afectar el reconocimiento
4. **Resolución muy baja**: Fotos de muy baja calidad seguirán dando malos resultados

## Código Técnico de Referencia

### Función de Preprocesamiento Completa

```java
private Bitmap preprocessTicketImage(Bitmap bitmap) {
    // Redimensionar si es muy grande para mejorar velocidad
    bitmap = resizeIfNeeded(bitmap);

    // Convertir a escala de grises para normalizar colores
    bitmap = toGrayscale(bitmap);

    // Aumentar contraste para mejorar legibilidad
    bitmap = adjustContrast(bitmap, 1.5f);

    // Aplicar umbral adaptativo para manejar iluminación irregular en fotos
    bitmap = applyAdaptiveThreshold(bitmap);

    return bitmap;
}
```

### Configuración de Tesseract

```java
// Initialize TessBaseAPI
tessBaseAPI = new TessBaseAPI();
tessBaseAPI.init(dataPath, "spa");

// Configuración optimizada para tickets fotografiados
// PSM_SINGLE_BLOCK funciona mejor para tickets que PSM_AUTO
tessBaseAPI.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_BLOCK);
```

## Próximos Pasos Potenciales (Fuera del Alcance Actual)

Si se necesitan mejoras adicionales en el futuro:

1. **Corrección de perspectiva**: Detectar y corregir tickets fotografiados en ángulo
2. **Mejora de resolución**: Usar técnicas de super-resolución para texto pequeño
3. **Detección de bordes**: Recortar automáticamente solo el área del ticket
4. **OCR con múltiples intentos**: Probar diferentes configuraciones y combinar resultados
5. **Post-procesamiento de texto**: Corrección ortográfica y validación de formatos comunes

## Conclusión

Las mejoras implementadas optimizan significativamente el reconocimiento de tickets fotografiados en español, específicamente:

1. ✅ Mejor modo de segmentación para documentos tipo ticket
2. ✅ Sin restricciones artificiales en caracteres
3. ✅ Preprocesamiento completo para fotos reales
4. ✅ Manejo robusto de iluminación irregular

Estos cambios deberían resolver el problema reportado de "no me aparece todo el texto" en tickets fotografiados.
