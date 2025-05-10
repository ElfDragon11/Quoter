swift
import Foundation
import OpenAI

protocol OpenAIService {
    func generateImage(prompt: String) async -> Result<String, Error>
}

class OpenAIServiceImpl: OpenAIService {
    private let apiKey = "YOUR_OPENAI_API_KEY"
    private let openAI: OpenAIProtocol
    
    init() {
      openAI = OpenAI(apiToken: apiKey)
    }

    func generateImage(prompt: String) async -> Result<String, Error> {
        if apiKey.isEmpty {
            return .failure(NSError(domain: "OpenAIServiceError", code: -1, userInfo: [NSLocalizedDescriptionKey: "OpenAI API key is missing."]))
        }

        let imageParameters = ImageParameters(prompt: prompt, resolution: ._1024x1536, responseFormat: .base64Json)

        do {
            let response = try await openAI.createImage(parameters: imageParameters)
            
            guard let imageData = response.data.first?.b64_json else {
                return .failure(NSError(domain: "OpenAIServiceError", code: -2, userInfo: [NSLocalizedDescriptionKey: "No Base64 image data received from API response."]))
            }
            
            return .success(imageData)
        } catch let error as APIError {
            print("OpenAI API Error: \(error)")
            return .failure(error)
        } catch {
            print("Error generating image using OpenAI SDK: \(error.localizedDescription)")
            return .failure(error)
        }
    }
}