package com.example.quoter.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GeneratedImageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: GeneratedImage): Long

    @Query("SELECT * FROM generated_images ORDER BY createdAt DESC")
    fun getAllImages(): Flow<List<GeneratedImage>>

    @Query("SELECT * FROM generated_images WHERE isSelectedForRotation = 1 ORDER BY RANDOM()")
    fun getSelectedImagesForRotation(): Flow<List<GeneratedImage>>

    @Query("SELECT * FROM generated_images WHERE id = :id")
    suspend fun getImageById(id: Int): GeneratedImage?

    @Update
    suspend fun updateImage(image: GeneratedImage)

    @Query("UPDATE generated_images SET isSelectedForRotation = :isSelected WHERE id = :id")
    suspend fun updateSelectionStatus(id: Int, isSelected: Boolean)

    @Delete
    suspend fun deleteImage(image: GeneratedImage)

    @Query("DELETE FROM generated_images WHERE id = :id")
    suspend fun deleteImageById(id: Int)

    // Add function to delete multiple images
    @Delete
    suspend fun deleteImages(images: List<GeneratedImage>)

    @Query("SELECT * FROM generated_images WHERE isSelectedForRotation = 1 ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomSelectedImage(): GeneratedImage?

    // Add new query to get the first selected image ordered by ID
    @Query("SELECT * FROM generated_images WHERE isSelectedForRotation = 1 ORDER BY id ASC LIMIT 1")
    suspend fun getFirstSelectedImage(): GeneratedImage?

    // Add new query to get selected images ordered by ID for sequential rotation
    @Query("SELECT * FROM generated_images WHERE isSelectedForRotation = 1 ORDER BY id ASC")
    suspend fun getSelectedImagesOrderedById(): List<GeneratedImage>
}
