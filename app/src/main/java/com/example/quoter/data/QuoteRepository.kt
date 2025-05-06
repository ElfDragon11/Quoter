package com.example.quoter.data

import android.content.Context
import java.io.File // Ensure File import is present
import kotlinx.coroutines.flow.Flow

class QuoteRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val generatedImageDao = db.generatedImageDao() // Get instance of the new DAO

    // --- Generated Image Methods ---
    fun getAllGeneratedImages(): Flow<List<GeneratedImage>> = generatedImageDao.getAllImages()

    fun getSelectedImagesForRotation(): Flow<List<GeneratedImage>> = generatedImageDao.getSelectedImagesForRotation()

    suspend fun insertGeneratedImage(image: GeneratedImage): Long = generatedImageDao.insertImage(image)

    suspend fun deleteGeneratedImageById(id: Int) = generatedImageDao.deleteImageById(id)

    suspend fun updateGeneratedImageSelection(id: Int, isSelected: Boolean) = generatedImageDao.updateSelectionStatus(id, isSelected)

    suspend fun getRandomSelectedImage(): GeneratedImage? = generatedImageDao.getRandomSelectedImage()

    // Add function to get the first selected image
    suspend fun getFirstSelectedImage(): GeneratedImage? = generatedImageDao.getFirstSelectedImage()

    // Add function to get all selected images ordered by ID
    suspend fun getSelectedImagesOrderedById(): List<GeneratedImage> = generatedImageDao.getSelectedImagesOrderedById()

    suspend fun deleteGeneratedImageFile(context: Context, image: GeneratedImage) {
        // Delete the actual file from internal storage using filePath
        try {
            val file = File(image.filePath) // Use filePath
            if (file.exists()) {
                if (file.delete()) {
                    println("Deleted image file: ${image.filePath}")
                } else {
                    println("Failed to delete image file: ${image.filePath}")
                }
            } else {
                println("Image file not found, skipping deletion: ${image.filePath}")
            }
        } catch (e: Exception) {
            println("Error deleting image file ${image.filePath}: ${e.message}")
            e.printStackTrace()
        }

        // Always delete the database record
        generatedImageDao.deleteImage(image)
    }

    // Add function to delete multiple images (files and DB records)
    suspend fun deleteGeneratedImages(context: Context, images: List<GeneratedImage>) {
        // Delete files first
        images.forEach { image ->
            try {
                val file = File(image.filePath)
                if (file.exists()) {
                    if (file.delete()) {
                        println("Deleted image file: ${image.filePath}")
                    } else {
                        println("Failed to delete image file: ${image.filePath}")
                    }
                } else {
                    println("Image file not found, skipping deletion: ${image.filePath}")
                }
            } catch (e: Exception) {
                println("Error deleting image file ${image.filePath}: ${e.message}")
                e.printStackTrace()
            }
        }
        // Then delete all database records at once
        generatedImageDao.deleteImages(images)
    }
}