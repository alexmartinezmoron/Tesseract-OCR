# Diseño de Interfaz de Usuario

## Vista Principal de la Aplicación

La aplicación presenta una interfaz simple y funcional con los siguientes elementos:

```
┌───────────────────────────────────────────────────────────────┐
│  Tesseract OCR                                          ☰     │ ← ActionBar
├───────────────────────────────────────────────────────────────┤
│                                                               │
│   ┌─────────────────────────────────────────────────────┐    │
│   │                                                      │    │
│   │        [ Seleccionar Imagen ]                       │    │ ← Botón Principal
│   │                                                      │    │
│   └─────────────────────────────────────────────────────┘    │
│                                                               │
│   ┌─────────────────────────────────────────────────────┐    │
│   │                                                      │    │
│   │                                                      │    │
│   │                                                      │    │
│   │              [  IMAGEN SELECCIONADA  ]              │    │ ← ImageView
│   │              (Vista previa aparece aquí)            │    │   (Fondo gris)
│   │                                                      │    │
│   │                                                      │    │
│   │                                                      │    │
│   └─────────────────────────────────────────────────────┘    │
│                                                               │
│   Listo para procesar imagen                                 │ ← Estado
│   (Italic, 14sp)                                             │
│                                                               │
│   ┌─────────────────────────────────────────────────────┐    │
│   │                                                      │    │
│   │  El texto extraído aparecerá aquí...                │    │ ← Resultados
│   │                                                      │    │   (ScrollView)
│   │                                                      │    │
│   └─────────────────────────────────────────────────────┘    │
│                                                               │
└───────────────────────────────────────────────────────────────┘
```

## Estados de la Interfaz

### Estado 1: Inicial (Sin Imagen)

```
┌────────────────────────────────┐
│   [ Seleccionar Imagen ]      │  ← Button habilitado
├────────────────────────────────┤
│        ░░░░░░░░░░░░            │  ← ImageView vacío (gris)
│        ░░░░░░░░░░░░            │
│        ░░░░░░░░░░░░            │
├────────────────────────────────┤
│ Listo para procesar imagen     │  ← Estado inicial
├────────────────────────────────┤
│ El texto extraído              │  ← Placeholder
│ aparecerá aquí...              │
└────────────────────────────────┘
```

### Estado 2: Procesando

```
┌────────────────────────────────┐
│   [ Seleccionar Imagen ]      │  ← Button habilitado
├────────────────────────────────┤
│   ┌──────────────────────┐    │  ← ImageView con imagen
│   │   [Foto del libro]    │    │
│   │    "Lorem ipsum..."   │    │
│   └──────────────────────┘    │
├────────────────────────────────┤
│ Procesando imagen...           │  ← Estado procesando
├────────────────────────────────┤
│ El texto extraído              │  ← Esperando resultado
│ aparecerá aquí...              │
└────────────────────────────────┘
```

### Estado 3: Completado

```
┌────────────────────────────────┐
│   [ Seleccionar Imagen ]      │  ← Button habilitado
├────────────────────────────────┤
│   ┌──────────────────────┐    │  ← ImageView con imagen
│   │   [Foto del libro]    │    │
│   │    "Lorem ipsum..."   │    │
│   └──────────────────────┘    │
├────────────────────────────────┤
│ Procesamiento completado       │  ← Estado completado
├────────────────────────────────┤
│ Lorem ipsum dolor sit amet,    │  ← Texto extraído
│ consectetur adipiscing elit.   │    (Scrollable)
│ Sed do eiusmod tempor          │
│ incididunt ut labore et...     │
└────────────────────────────────┘
```

## Especificaciones de Diseño

### Colores
- **Fondo Principal**: Blanco (`#FFFFFF`)
- **ImageView Vacío**: Gris oscuro (`@android:color/darker_gray`)
- **Texto Principal**: Negro (`@android:color/black`)
- **Texto Estado**: Gris (color por defecto del sistema)
- **ActionBar**: Theme.AppCompat.Light.DarkActionBar

### Tipografía
- **Botón**: 16sp, normal weight
- **Estado**: 14sp, italic
- **Resultados**: 14sp, normal weight

### Espaciado
- **Padding General**: 16dp
- **Margin Top ImageView**: 16dp
- **Margin Bottom ImageView**: 16dp
- **Margin Bottom Estado**: 8dp
- **Padding ScrollView**: 8dp

### Dimensiones
- **Botón**: match_parent × wrap_content
- **ImageView**: match_parent × 0dp (weight=1)
- **ScrollView**: match_parent × 0dp (weight=1)
- **TextViews**: match_parent × wrap_content

## Interacciones del Usuario

### 1. Tocar Botón "Seleccionar Imagen"
```
Usuario toca botón
    ↓
¿Tiene permisos?
    ↓ No         ↓ Sí
Dialog de    Abrir selector
permisos     de imágenes
    ↓ Concede
    ↓
Abrir selector
de imágenes
```

### 2. Seleccionar Imagen
```
Usuario selecciona imagen
    ↓
Mostrar en ImageView
    ↓
Cambiar estado a "Procesando..."
    ↓
Procesar OCR (background)
    ↓
Mostrar resultado en TextView
    ↓
Cambiar estado a "Completado"
```

### 3. Procesar Nueva Imagen
```
Usuario toca botón nuevamente
    ↓
Abrir selector (permisos ya concedidos)
    ↓
Reemplazar imagen anterior
    ↓
Procesar nueva imagen
```

## Comportamiento Responsive

### Portrait (Vertical)
- Layout vertical (LinearLayout)
- ImageView ocupa 50% del espacio disponible
- ScrollView ocupa los otros 50%

### Landscape (Horizontal)
- Mismo layout (funciona en ambas orientaciones)
- Componentes se ajustan automáticamente

## Accesibilidad

### Content Descriptions
- **ImageView**: "Imagen seleccionada"
- **Button**: Texto descriptivo "Seleccionar Imagen"

### Contraste
- Texto negro sobre fondo blanco (ratio 21:1)
- Cumple con WCAG AA para accesibilidad

### Tamaño de Toque
- Botón tiene altura suficiente (wrap_content con padding)
- Áreas táctiles cumplen con mínimo de 48dp

## Feedback Visual

### Estados del Botón
- **Normal**: Estilo Material Button
- **Pressed**: Ripple effect (Material Design)
- **Disabled**: No usado (siempre habilitado)

### Indicadores de Progreso
- **Textual**: "Procesando imagen..." en TextView
- **No visual**: Sin spinner (procesamiento rápido)

## Manejo de Errores - UI

### Error de Permisos
```
Toast: "Se requiere permiso para acceder a imágenes"
Estado: Permanece en "Listo para procesar imagen"
```

### Error al Cargar Imagen
```
Toast: "No se pudo cargar la imagen"
Estado: Vuelve a "Listo para procesar imagen"
ImageView: Mantiene imagen anterior (si existe)
```

### Error en OCR
```
Toast: "Error al procesar la imagen"
Estado: Vuelve a "Listo para procesar imagen"
Resultado: "No se detectó texto en la imagen"
```

## Mejoras Futuras de UI

1. **ProgressBar circular** durante procesamiento
2. **Botón de compartir** resultados
3. **Botón de copiar** al portapapeles
4. **Fab button** para captura rápida
5. **Material 3** upgrade
6. **Dark mode** support
7. **Animaciones** de transición
8. **Chips** para selección de idioma
9. **Bottom sheet** para opciones
10. **Snackbar** en lugar de Toast

## Principios de Diseño Aplicados

1. **Simplicidad**: Interface minimalista, fácil de entender
2. **Claridad**: Cada elemento tiene un propósito claro
3. **Feedback**: Usuario siempre sabe qué está pasando
4. **Eficiencia**: Mínimo número de toques para completar tarea
5. **Consistencia**: Usa componentes Material Design estándar
6. **Accesibilidad**: Contraste adecuado y content descriptions

---

**Diseño creado para maximizar usabilidad con implementación mínima.**
