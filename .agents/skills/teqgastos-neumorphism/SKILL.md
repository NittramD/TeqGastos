---
name: teqgastos-neumorphism
description: >-
  Guía y especificación técnica para la implementación del sistema de diseño Neumorphism (Soft UI)
  en la aplicación Android TeqGastos (TEQMED SpA). Usar esta skill cuando se diseñen, modifiquen o auditen
  componentes visuales con relieve 3D táctil, sombras dobles suaves y tarjetas extruidas en Jetpack Compose.
---

# Neumorphism Design System · TeqGastos (TEQMED SpA)

Esta skill documenta las reglas de diseño y patrones de implementación para el estilo visual **Neumorphism (Soft UI)** adaptado al contexto de equipamiento médico y personal en terreno de **TEQMED SpA**.

---

## 1. Principios Fundamentales del Neumorfismo

1. **Monocromatismo de Superficie:** La superficie de los elementos (tarjetas, botones, contenedores) debe compartir la misma tonalidad base del fondo (`#E8EEF5`), creando la ilusión de que el elemento surge físicamente de la pantalla.
2. **Sistema de Doble Sombra (Dual Shadow):**
   - **Luz Superior Izquierda (Highlight):** Simula luz blanca cenital (`#FFFFFF` con alpha entre 0.7f y 0.9f) con desplazamiento negativo (`-dx, -dy`).
   - **Sombra Inferior Derecha (Shadow):** Simula la oclusión ambiental suave (`#A6B7CD` con alpha entre 0.45f y 0.65f) con desplazamiento positivo (`+dx, +dy`).
3. **Radio de Curvatura Generoso:** Esquinas redondeadas (`16.dp` a `24.dp`) para enfatizar la suavidad plástica de los paneles.
4. **Accesibilidad y Contraste Médico:**
   - La estructura táctil es neumórfica, pero el texto y los íconos de estado **deben mantener un contraste alto (WCAG AAA)** utilizando el Azul Quirúrgico (`#0061A4`) y tipografía en semibold/bold.

---

## 2. Paleta de Tokens Neumórficos

| Token | Código HEX / Color | Propósito |
| :--- | :--- | :--- |
| `NeuBackground` | `#E8EEF5` | Fondo general de las pantallas |
| `NeuSurface` | `#E8EEF5` | Color base de tarjetas y botones extruidos |
| `NeuLightShadow` | `#FFFFFF` (85% alpha) | Reflejo de luz superior izquierda |
| `NeuDarkShadow` | `#A6B7CD` (60% alpha) | Sombra difusa inferior derecha |
| `NeuPrimary` | `#0061A4` | Acentos primarios de acción y textos |
| `NeuPressed` | `#DEE5EF` | Superficie hundida (estado presionado / input) |

---

## 3. Componentes Neumórficos Reutilizables

### A. Tarjeta Extruida (`NeumorphicCard`)
Contenedor con relieve suave para rendiciones, gastos y métricas:
```kotlin
NeumorphicCard(
    modifier = Modifier.fillMaxWidth(),
    cornerRadius = 16.dp,
    elevation = 6.dp
) {
    // Contenido
}
```

### B. Botón Táctil Neumórfico (`NeumorphicButton`)
Botón con microinteracción de hundimiento al pulsar:
```kotlin
NeumorphicButton(
    onClick = { /* Acción */ },
    modifier = Modifier.fillMaxWidth()
) {
    Text("Registrar Gasto", fontWeight = FontWeight.Bold)
}
```

### C. Contenedor de Formulario Hundido (`NeumorphicInset`)
Para campos de texto y recuadros de captura que simulan un espacio bajo relieve.

---

## 4. Checklist de Validación Neumórfica
- [ ] ¿El fondo de la pantalla y la tarjeta tienen el mismo valor cromático base (`#E8EEF5`)?
- [ ] ¿Se aprecian con claridad tanto el brillo blanco superior izquierdo como la sombra gris azulada inferior derecha?
- [ ] ¿Los textos y cifras mantienen legibilidad perfecta bajo luz exterior en terreno?
- [ ] ¿El APK compila sin sobrecargar la GPU ni causar recomposiciones innecesarias?
