package com.example.teqgastos.ui.components

import java.text.NumberFormat
import java.util.Locale

// Formateador monetario chileno
fun Double.formatClp(): String {
    val format = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
    format.maximumFractionDigits = 0
    return format.format(this)
}
