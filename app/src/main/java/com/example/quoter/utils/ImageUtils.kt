package com.example.quoter.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ImageUtils {

    /**
     * Crops a bitmap to a 9:20 aspect ratio, keeping the center.
     * Assumes the input bitmap is 1024x1792 (or similar aspect ratio from DALL-E 3).
     */
    fun cropTo9x16(source: Bitmap): Bitmap? { // Keep name for compatibility, but ratio is 9:20
        val sourceWidth = source.width
        val sourceHeight = source.height

        // Target aspect ratio (9:20)
        val targetAspectRatio = 9.0 / 20.0 // New ratio

        // Calculate the target width based on source height to maintain 9:20
        var targetWidth = (sourceHeight * targetAspectRatio).toInt()
        var targetHeight = sourceHeight

        // If the calculated width is wider than the source, calculate target height based on source width
        if (targetWidth > sourceWidth) {
            targetWidth = sourceWidth
            targetHeight = (sourceWidth / targetAspectRatio).toInt()
            // Ensure targetHeight doesn't exceed sourceHeight
            if (targetHeight > sourceHeight) targetHeight = sourceHeight
            println("Cropping: Using full width ($sourceWidth), calculated height ($targetHeight) for 9:20 ratio.")
        } else {
             println("Cropping: Using full height ($sourceHeight), calculated width ($targetWidth) for 9:20 ratio.")
        }

        // Calculate cropping bounds (centered)
        val left = (sourceWidth - targetWidth) / 2
        val top = (sourceHeight - targetHeight) / 2
        val right = left + targetWidth
        val bottom = top + targetHeight

        // Ensure bounds are within the source bitmap dimensions
        if (left < 0 || top < 0 || right > sourceWidth || bottom > sourceHeight || targetWidth <= 0 || targetHeight <= 0) {
            println("Error: Calculated crop bounds are invalid.")
            return null // Or return the original bitmap if cropping fails
        }

        return try {
            Bitmap.createBitmap(source, left, top, targetWidth, targetHeight)
        } catch (e: Exception) {
            println("Error cropping bitmap: ${e.message}")
            null
        }
    }

    /**
     * Saves a bitmap to the app's internal files directory.
     * Returns the absolute path of the saved file, or null on failure.
     */
    fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap, filenamePrefix: String = "wallpaper_"): String? {
        val directory = context.filesDir // App's internal files directory
        // Create a unique filename
        val filename = "${filenamePrefix}${UUID.randomUUID()}.png"
        val file = File(directory, filename)

        var fileOutputStream: FileOutputStream? = null
        return try {
            fileOutputStream = FileOutputStream(file)
            // Compress the bitmap to the output stream (PNG format)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            file.absolutePath // Return the full path to the saved file
        } catch (e: IOException) {
            println("Error saving bitmap: ${e.message}")
            e.printStackTrace()
            null
        } finally {
            try {
                fileOutputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}
