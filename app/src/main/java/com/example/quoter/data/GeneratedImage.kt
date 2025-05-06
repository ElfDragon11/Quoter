package com.example.quoter.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "generated_images")
data class GeneratedImage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val filePath: String, // Change back to filePath (non-nullable)
    val prompt: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    var isSelectedForRotation: Boolean = true
)
