package com.example.quoter.network

import com.example.quoter.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlobPart
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

// --- Imagen Service Interface and Implementation using Generative AI Client Library ---

interface ImagenService {
    suspend fun generateImage(prompt: String): Result<ByteArray>
}

class ImagenServiceImpl : ImagenService {

    private val apiKey = BuildConfig.GOOGLE_CLOUD_API_KEY

    private val generativeModel = GenerativeModel(
        modelName = "imagen-3-generate-002",
        apiKey = apiKey
    )

    override suspend fun generateImage(prompt: String): Result<ByteArray> {
        if (apiKey.isBlank()) {
            return Result.failure(Exception("Google Cloud API key is missing. Please add it to local.properties."))
        }

        val content = content {
            text(prompt)
        }

        return try {
            val response = withContext(Dispatchers.IO) {
                generativeModel.generateContent(content)
            }

            val generatedContent = response.candidates.firstOrNull()?.content

            if (generatedContent != null && generatedContent.parts.isNotEmpty()) {
                val imagePart = generatedContent.parts.firstOrNull()
                if (imagePart is BlobPart) {
                    val imageData = imagePart.blob.data.array()
                    if (imageData.isNotEmpty()) {
                        Result.success(imageData)
                    } else {
                        Result.failure(Exception("Imagen API response contained empty image data."))
                    }
                } else if (imagePart != null) {
                    Result.failure(
                        Exception(
                            "Unexpected content type received: ${imagePart::class.java.name}"
                        )
                    )
                } else {
                    Result.failure(Exception("Imagen API response did not contain image part."))
                }
            } else {
                Result.failure(Exception("Imagen API response did not contain generated content."))
            }

        } catch (e: IOException) {
            println("Imagen API IOException: ${e.message}")
            Result.failure(Exception("Network error calling Imagen API.", e))
        } catch (e: Exception) {
            // Catch other potential exceptions from the client library
            println("Error generating image using Imagen API: ${e.message}")
            e.printStackTrace()
            Result.failure(Exception("API Error: ${e.message}", e))
        } finally {
        }
    }
}