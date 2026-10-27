---
name: teqgastos-glassmorphism
description: >-
  Guía y especificación técnica para la implementación del sistema de diseño Glassmorphism (Frosted Glass / Vidrio Esmerilado)
  en la aplicación Android TeqGastos (TEQMED SpA). Usar esta skill al diseñar o transformar componentes visuales
  con translucidez, bordes especulares de luz lineal, fondos con gradientes orgánicos de fondo, chips vítreos,
  FAB flotante de cristal y botones de vidrio esmerilado en Jetpack Compose.
---

# Glassmorphism Design System · TeqGastos (TEQMED SpA)

Esta skill define la arquitectura visual y técnica del estilo **Glassmorphism (Frosted Glass / Vidrio Esmerilado)** en **Jetpack Compose** para la aplicación **TeqGastos** de **TEQMED SpA**.

---

## 1. Principios Fundamentales del Glassmorphism

El efecto de cristal esmerilado se basa en cuatro pilares visuales inseparables:

1. **Translucidez Multicapa (Multi-layered Translucency):**
   - Los contenedores no son opacos; permiten vislumbrar los colores y formas que yacen en el fondo mediante canales alfa (`Color.White.copy(alpha = 0.65f - 0.85f)` para tarjetas claras, o degradados translúcidos al 80%-90% para botones primarios).
2. **Borde Especular Iluminado (Specular Light Border):**
   - Una línea perimetral de `1.dp` o `1.2.dp` trazada con un `Brush.linearGradient` diagonal:
     - Esquina superior izquierda: blanco con alta opacidad (`Color.White.copy(alpha = 0.80f)`), simulando el filo donde refracta la luz.
     - Esquina inferior derecha: blanco con baja opacidad (`Color.White.copy(alpha = 0.15f)`).
3. **Fondo con Gradiente u Orbes Ambientales (Ambient Backdrop):**
   - El vidrio esmerilado solo es visible si existe contraste dinámico detrás. Se utiliza un lienzo con gradiente suave médico (azul profundo a cian / aguamarina) u orbes ambientales desenfocados en el fondo.
4. **Sombra Flotante Suave (Floating Drop Shadow):**
   - Sombras difusas coloreadas (`spotColor = Color(0x33003258)`) que transmiten suspensión física sobre el plano base.

---

## 2. Paleta de Tokens Glassmorphic

| Token | Valor / Composición | Propósito |
| :--- | :--- | :--- |
| `GlassSurfaceLight` | `Color(0xCCFFFFFF)` (80% blanco) | Superficie de tarjetas y modales vítreos |
| `GlassSurfaceSubtle`| `Color(0x99FFFFFF)` (60% blanco) | Fondos de chips inactivos y paneles secundarios |
| `GlassBorderBrush`  | `linearGradient(White 80% → White 15%)` | Reflejo en el borde de refracción del cristal |
| `GlassPrimaryGradient` | `linearGradient(#0061A4 90% → #00838F 85%)` | Botón principal y FAB de vidrio esmerilado |
| `GlassActiveChip`   | `Color(0xCC97F0FF)` (80% cian) | Estado seleccionado de filtros de rendición |
| `GlassShadow`       | `Color(0x33002544)` | Sombra ambiental de cristal flotante |

---

## 3. Componentes Glassmorphic en Jetpack Compose

### A. Botón Principal de Cristal (`GlassmorphicButton`)
Sustituye botones planos opacos por una superficie vítrea con borde reflectante:
```kotlin
GlassmorphicButton(
    onClick = { /* Acción */ },
    modifier = Modifier.fillMaxWidth()
) {
    Text("Ingresar como Técnico de Terreno", fontWeight = FontWeight.Bold, color = Color.White)
}
```

### B. Botón de Acción Flotante (`GlassmorphicExtendedFab`)
FAB translúcido con gradiente azul-cian y sombra etérea:
```kotlin
GlassmorphicExtendedFab(
    onClick = { /* Abrir nueva rendición */ },
    text = "Nueva Rendición",
    icon = Icons.Default.Add
)
```

### C. Filtros Tipo Cápsula de Vidrio (`GlassmorphicFilterChip`)
Pills translúcidos con microborde y retroalimentación de estado:
```kotlin
GlassmorphicFilterChip(
    selected = isSelected,
    onClick = { /* Filtrar */ },
    label = "Borrador (1)"
)
```

### D. Tarjeta de Vidrio (`GlassmorphicCard`)
Para rendiciones, gastos y métricas con profundidad visual.

---

## 4. Checklist de Validación Glassmorphism
- [ ] ¿El fondo de la pantalla contiene un gradiente ambiental o lienzo enriquecido que dé vida a la refracción?
- [ ] ¿Las tarjetas poseen el borde lineal diagonal (`White 80%` a `White 15%`) de 1dp?
- [ ] ¿El texto sobre el vidrio mantiene un contraste accesible según norma WCAG?
- [ ] ¿Los botones clave (Login, FAB, Chips) lucen el brillo característico de vidrio esmerilado?
