package com.example.teqgastos.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max

object ImageCompressor {
    private const val MAX_WIDTH = 1600
    private const val MAX_HEIGHT = 1200
    private const val JPEG_QUALITY = 80

    suspend fun compressAndSaveImage(context: Context, sourceUri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(sourceUri) ?: return@withContext null
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (originalBitmap == null) return@withContext null

            // Calcular escala proporcional
            val width = originalBitmap.width
            val height = originalBitmap.height
            val scale = max(width.toFloat() / MAX_WIDTH, height.toFloat() / MAX_HEIGHT)

            val scaledBitmap = if (scale > 1f) {
                val newWidth = (width / scale).toInt()
                val newHeight = (height / scale).toInt()
                Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
            } else {
                originalBitmap
            }

            // Carpeta interna protegida
            val dir = File(context.filesDir, "comprobantes")
            if (!dir.exists()) dir.mkdirs()

            val compressedFile = File(dir, "boleta_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(compressedFile)
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, outputStream)
            outputStream.flush()
            outputStream.close()

            return@withContext Uri.fromFile(compressedFile).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext sourceUri.toString()
        }
    }
}
