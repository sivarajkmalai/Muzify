package com.muzify.app.ui.util

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object PaletteHelper {
    suspend fun getGradientColors(imagePath: String?): List<Color> = withContext(Dispatchers.IO) {
        if (imagePath == null || !File(imagePath).exists()) {
            return@withContext listOf(
                Color(0xFF1DB954.toInt()),
                Color(0xFF191414.toInt())
            )
        }

        try {
            val bitmap = BitmapFactory.decodeFile(imagePath)
            val palette = Palette.from(bitmap).generate()

            val vibrant = palette.vibrantSwatch?.rgb ?: palette.dominantSwatch?.rgb
            val darkVibrant = palette.darkVibrantSwatch?.rgb ?: palette.darkMutedSwatch?.rgb

            listOf(
                Color(vibrant ?: 0xFF1DB954.toInt()),
                Color(darkVibrant ?: 0xFF191414.toInt())
            )
        } catch (e: Exception) {
            listOf(
                Color(0xFF1DB954.toInt()),
                Color(0xFF191414.toInt())
            )
        }
    }
}
