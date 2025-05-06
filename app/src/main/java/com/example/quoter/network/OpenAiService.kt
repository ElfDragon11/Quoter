package com.example.quoter.network

// Imports for official com.openai:openai-java SDK (e.g., 0.21.0+)
import com.example.quoter.BuildConfig
import com.openai.client.OpenAIClient
import com.openai.client.okhttp.OpenAIOkHttpClient
import com.openai.models.images.ImageGenerateParams
import com.openai.models.images.ImageModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException


// --- OpenAI Service Interface and Implementation using SDK ---

interface OpenAiService {
    suspend fun generateImage(prompt: String): Result<String>
}

class OpenAiServiceImpl : OpenAiService {

    private val apiKey = BuildConfig.OPENAI_API_KEY

    // Initialize the official OpenAIClient using the API key
    private val client: OpenAIClient = OpenAIOkHttpClient.builder()
        .apiKey(apiKey)
        // Add timeouts if needed
        .build()

    override suspend fun generateImage(prompt: String): Result<String> {
        if (apiKey.isBlank()) {
            return Result.failure(Exception("OpenAI API key is missing. Please add it to local.properties."))
        }
        // Build the request parameters based on the provided example structure
        val imageGenerateParams = ImageGenerateParams.builder()
            .prompt(prompt)
            .model(ImageModel.GPT_IMAGE_1) // Ensure this model supports B64_JSON
            .size(ImageGenerateParams.Size._1024X1536)
            .quality(ImageGenerateParams.Quality.MEDIUM)
            .n(1)
            .build()

        return try {
            // Execute the request using the client.images().generate() method
            val response = withContext(Dispatchers.IO) {
                // Use the class member client instance
                client.images().generate(imageGenerateParams)
            }

            // Extract the Base64 string from the response data
            val imageList = response.data().orElse(null) // Get List<Image> or null
            val imageBase64 = imageList?.firstOrNull() // Get the first image object
                ?.b64Json() // Get the Optional<String> containing Base64
                ?.orElse(null) // Get String or null if Optional is empty

            if (imageBase64 != null) {
                Result.success(imageBase64) // Return the Base64 string
            } else {
                // Handle cases where data list is null/empty or b64_json is missing
                Result.failure(Exception("No Base64 image data received from API response."))
            }
        } catch (e: SocketTimeoutException) {
            println("OpenAI SDK Timeout: ${e.message}")
            Result.failure(Exception("Request timed out. Please try again.", e))
        } catch (e: Exception) {
            // Catch other SDK or network exceptions (e.g., OpenAIException)
            println("Error generating image using OpenAI SDK: ${e.message}")
            e.printStackTrace()
            Result.failure(Exception("API Error: ${e.message}", e))
        }
    }
}
