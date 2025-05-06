package com.example.quoter

import android.app.Application
import android.app.WallpaperManager
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.provider.MediaStore
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.quoter.data.GeneratedImage
import com.example.quoter.data.QuoteRepository
import com.example.quoter.network.OpenAiService
import com.example.quoter.network.OpenAiServiceImpl
import com.example.quoter.utils.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.OutputStream
import com.example.quoter.data.UserPreferencesRepository
import android.util.Base64
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

// Add fontStyle and fontSize to CustomizationState
data class CustomizationState(
    val location: String = "Nature",
    val scene: String = "Landscape",
    val style: String = "Photorealistic",
    val fontStyle: String = "Bold", // New field for font style
    val fontSize: Float = 16f // New field for font size
)

class QuoteViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = QuoteRepository(application)
    private val userPreferencesRepository = UserPreferencesRepository(application)
    private val openAiService: OpenAiService = OpenAiServiceImpl()

    // --- State Variables ---
    var isGeneratingImage by mutableStateOf(false)
        private set
    var generationError by mutableStateOf<String?>(null)
        private set

    // SharedFlow for Snackbar messages
    private val _snackbarMessages = MutableSharedFlow<String>()
    val snackbarMessages = _snackbarMessages.asSharedFlow()

    // Update customizationState Flow to include fontStyle and fontSize
    val customizationState: StateFlow<CustomizationState> = userPreferencesRepository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CustomizationState() // Use updated initial value
        )

    val generatedImages: StateFlow<List<GeneratedImage>> = repository.getAllGeneratedImages()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Remove TODO related to autoRotate state
    }

    // --- Customization Update Functions (Font removed) ---
    fun updateLocation(location: String) {
        viewModelScope.launch {
            userPreferencesRepository.updateLocation(location)
        }
    }

    fun updateScene(scene: String) {
        viewModelScope.launch {
            userPreferencesRepository.updateScene(scene)
        }
    }

    fun updateStyle(style: String) {
        viewModelScope.launch {
            userPreferencesRepository.updateStyle(style)
        }
    }

    // Add functions to update fontStyle and fontSize
    fun updateFontStyle(fontStyle: String) {
        viewModelScope.launch {
            userPreferencesRepository.updateFontStyle(fontStyle)
        }
    }

    fun updateFontSize(fontSize: Float) {
        viewModelScope.launch {
            userPreferencesRepository.updateFontSize(fontSize)
        }
    }

    // Update generateAndSaveImage to include fontStyle and fontSize
    fun generateAndSaveImage(quoteText: String, customizationState: CustomizationState, fontStyle: String, fontSize: Float) {
        if (quoteText.isBlank()) {
            // Use _snackbarMessages for user feedback
            viewModelScope.launch { _snackbarMessages.emit("Please enter a quote.") }
            return
        }
        viewModelScope.launch {
            isGeneratingImage = true
            generationError = null
            // Use the passed customizationState directly
            val currentState = customizationState // Already collected

            // Include fontStyle and fontSize in the prompt
            val fullPrompt = "Generate a vertical 9:16 image. Background should show ${currentState.location} as the location, ${currentState.scene} as the scene, and follow ${currentState.style} styling. Center the quote \"${quoteText}\" within a safe 9:16 frame, using a ${fontStyle} font style at roughly ${fontSize.toInt()}pt. Add a subtle shadow or outline if needed to keep the words crisp against the background. Ensure the text fits comfortably and completely in the 9:16 center, avoiding the top/bottom 15%. Balance color, lighting, and depth. Return only the image."
            println("Generating image with prompt: $fullPrompt")

            var successMessage: String? = null
            var errorMessage: String? = null

            try {
                val imageResult = openAiService.generateImage(fullPrompt)
                imageResult.onSuccess { imageBase64 ->
                    val decodedBytes = Base64.decode(imageBase64, Base64.DEFAULT)
                    val initialBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                    if (initialBitmap != null) {
                        val croppedBitmap = ImageUtils.cropTo9x16(initialBitmap)
                        if (croppedBitmap != null) {
                            val filePath = ImageUtils.saveBitmapToInternalStorage(getApplication(), croppedBitmap)
                            if (filePath != null) {
                                val generatedImage = GeneratedImage(filePath = filePath, prompt = fullPrompt)
                                repository.insertGeneratedImage(generatedImage)
                                println("Image generated, cropped, and saved to internal storage: $filePath")
                                successMessage = "Wallpaper generated successfully!" // Set success message
                                generationError = null
                            } else {
                                errorMessage = "Failed to save cropped image."
                            }
                        } else {
                            errorMessage = "Failed to crop image."
                            println("Failed to crop bitmap. Original: ${initialBitmap.width}x${initialBitmap.height}")
                        }
                    } else {
                        errorMessage = "Failed to decode image data."
                    }
                }.onFailure { apiError ->
                    errorMessage = apiError.message ?: "Unknown API error"
                    println("API Error: ${apiError.message}")
                    apiError.printStackTrace()
                }
            } catch (e: Exception) {
                errorMessage = "Unexpected error: ${e.message}"
                println("Unexpected error: ${e.message}")
                e.printStackTrace()
            } finally {
                isGeneratingImage = false
                // Emit message to Snackbar flow
                errorMessage?.let {
                    generationError = it // Keep error state updated if needed
                    _snackbarMessages.emit(it)
                 }
                successMessage?.let { _snackbarMessages.emit(it) }
            }
        }
    }

    // --- Generated Image Library Management (Keep) ---
    fun deleteGeneratedImage(image: GeneratedImage) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteGeneratedImageFile(getApplication(), image)
        }
    }

    fun toggleImageSelection(image: GeneratedImage) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateGeneratedImageSelection(image.id, !image.isSelectedForRotation)
        }
    }

    // Add function to delete all selected images
    fun deleteSelectedImages() {
        viewModelScope.launch(Dispatchers.IO) {
            val imagesToDelete = generatedImages.value.filter { it.isSelectedForRotation }
            if (imagesToDelete.isNotEmpty()) {
                repository.deleteGeneratedImages(getApplication(), imagesToDelete)
                println("Deleted ${imagesToDelete.size} selected images.")
            }
        }
    }

    // --- Wallpaper Setting Logic ---

    // New function to set wallpaper from the first selected image
    @androidx.annotation.RequiresPermission(android.Manifest.permission.SET_WALLPAPER)
    fun setWallpaperFromSelection(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val wallpaperManager = WallpaperManager.getInstance(context)
                // Get the first image marked for rotation
                val firstSelectedImage = repository.getFirstSelectedImage()

                if (firstSelectedImage != null) {
                    val bitmap = loadBitmapFromFile(firstSelectedImage.filePath)
                    if (bitmap != null) {
                        wallpaperManager.setBitmap(bitmap)
                        println("Set wallpaper from selected file: ${firstSelectedImage.filePath}")
                    } else {
                        println("Failed to load bitmap for selected wallpaper: ${firstSelectedImage.filePath}")
                    }
                } else {
                    println("No selected images available to set as wallpaper.")
                }
            } catch (e: Exception) {
                 println("Error setting wallpaper from selection: ${e.message}")
                 e.printStackTrace()
            }
        }
    }

    // --- Export Logic ---
    fun exportSelectedImagesToGallery(context: Context) {
        viewModelScope.launch {
            val selectedImages = generatedImages.value.filter { it.isSelectedForRotation }
            if (selectedImages.isEmpty()) {
                _snackbarMessages.emit("No images selected for export.")
                return@launch
            }

            var successCount = 0
            var errorCount = 0

            withContext(Dispatchers.IO) { // Perform file operations on IO dispatcher
                selectedImages.forEach { image ->
                    try {
                        val bitmap = loadBitmapFromFile(image.filePath)
                        if (bitmap != null) {
                            saveBitmapToGallery(context, bitmap, "Quoter_${System.currentTimeMillis()}.jpg")
                            successCount++
                        } else {
                            errorCount++
                        }
                    } catch (e: Exception) {
                        println("Error exporting image ${image.filePath}: ${e.message}")
                        errorCount++
                    }
                }
            }

            // Report results via Snackbar
            val message = when {
                successCount > 0 && errorCount == 0 -> "Exported $successCount image(s) to gallery."
                successCount > 0 && errorCount > 0 -> "Exported $successCount image(s), failed for $errorCount."
                else -> "Failed to export selected image(s)."
            }
            _snackbarMessages.emit(message)
        }
    }

    // Helper function to save Bitmap using MediaStore
    private fun saveBitmapToGallery(context: Context, bitmap: Bitmap, displayName: String): Boolean {
        val imageCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.IS_PENDING, 1)
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Quoter") // Save in Pictures/Quoter folder
            }
        }

        val resolver = context.contentResolver
        var uri = resolver.insert(imageCollection, contentValues)
        var outputStream: OutputStream? = null
        var success = false

        try {
            if (uri != null) {
                outputStream = resolver.openOutputStream(uri)
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
                    success = true
                }
            }
        } catch (e: Exception) {
            println("Error saving bitmap to gallery: ${e.message}")
            // Clean up partially created entry if possible
            uri?.let { resolver.delete(it, null, null) }
            uri = null // Ensure we don't mark it as non-pending
        } finally {
            outputStream?.close()
            // Mark as not pending if saved successfully on Q+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && uri != null && success) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }
        }
        return success
    }

    // --- Bitmap Loading (Reverted to filePath) ---
    private fun loadBitmapFromFile(filePath: String): Bitmap? {
        return try {
            val file = File(filePath)
            if (file.exists()) {
                BitmapFactory.decodeFile(filePath)
            } else {
                null
            }
        } catch (e: Exception) {
            println("Error loading bitmap from file: ${e.message}")
            null
        }
    }

    // --- Snackbar Helper ---
    fun showSnackbar(message: String) {
        viewModelScope.launch {
            _snackbarMessages.emit(message)
        }
    }
}