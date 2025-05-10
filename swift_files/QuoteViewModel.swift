swift
import UIKit
import Combine
import PhotosUI

struct CustomizationSettings {
    var location: String = "Nature"
    var scene: String = "Landscape"
    var style: String = "Photorealistic"
    var fontStyle: String = "Bold"
    var fontSize: CGFloat = 16
}

class QuoteViewModel: ObservableObject {
    private let repository: QuoteRepository
    private let userPreferencesRepository: UserPreferencesRepository
    private let openAiService: OpenAiService
    private var cancellables = Set<AnyCancellable>()

    @Published var isGeneratingImage: Bool = false
    @Published var generationError: String?
    @Published var snackbarMessage: String?
    @Published var generatedImages: [GeneratedImage] = []

    let snackbarMessages = PassthroughSubject<String, Never>()
    let customizationSettings = CurrentValueSubject<CustomizationSettings, Never>(CustomizationSettings())
    let imageSubject = CurrentValueSubject<[GeneratedImage], Never>([])

    init(repository: QuoteRepository, userPreferencesRepository: UserPreferencesRepository, openAiService: OpenAiService) {
        self.repository = repository
        self.userPreferencesRepository = userPreferencesRepository
        self.openAiService = openAiService

        userPreferencesRepository.userPreferencesPublisher
            .sink { [weak self] settings in
                self?.customizationSettings.send(settings)
            }
            .store(in: &cancellables)
        
        repository.getAllGeneratedImages()
            .sink { [weak self] images in
                self?.generatedImages = images
            }
            .store(in: &cancellables)

        imageSubject.send(generatedImages)
    }

    func updateLocation(location: String) {
        Task {
            await userPreferencesRepository.updateLocation(location: location)
        }
    }

    func updateScene(scene: String) {
        Task {
            await userPreferencesRepository.updateScene(scene: scene)
        }
    }

    func updateStyle(style: String) {
        Task {
            await userPreferencesRepository.updateStyle(style: style)
        }
    }

    func updateFontStyle(fontStyle: String) {
        Task {
            await userPreferencesRepository.updateFontStyle(fontStyle: fontStyle)
        }
    }

    func updateFontSize(fontSize: CGFloat) {
        Task {
            await userPreferencesRepository.updateFontSize(fontSize: fontSize)
        }
    }

    func generateAndSaveImage(quoteText: String, customizationSettings: CustomizationSettings, fontStyle: String, fontSize: CGFloat) {
        guard !quoteText.isEmpty else {
            snackbarMessages.send("Please enter a quote.")
            return
        }

        Task {
            await MainActor.run {
                isGeneratingImage = true
                generationError = nil
            }

            let fullPrompt = "Generate a vertical 9:16 image. Background should show \(customizationSettings.location) as the location, \(customizationSettings.scene) as the scene, and follow \(customizationSettings.style) styling. Center the quote \"\(quoteText)\" within a safe 9:16 frame, using a \(fontStyle) font style at roughly \(Int(fontSize))pt. Add a subtle shadow or outline if needed to keep the words crisp against the background. Ensure the text fits comfortably and completely in the 9:16 center, avoiding the top/bottom 15%. Balance color, lighting, and depth. Return only the image."
            print("Generating image with prompt: \(fullPrompt)")

            var successMessage: String? = nil
            var errorMessage: String? = nil

            do {
                let imageBase64 = try await openAiService.generateImage(prompt: fullPrompt)
                guard let decodedData = Data(base64Encoded: imageBase64) else {
                    errorMessage = "Failed to decode image data."
                    return
                }

                guard let initialImage = UIImage(data: decodedData) else {
                    errorMessage = "Failed to create UIImage from decoded data."
                    return
                }

                guard let croppedImage = ImageUtils.cropTo9x20(source: initialImage) else {
                    errorMessage = "Failed to crop image."
                    print("Failed to crop image.")
                    return
                }

                guard let filePath = ImageUtils.saveUIImageToInternalStorage(image: croppedImage) else {
                    errorMessage = "Failed to save cropped image."
                    return
                }

                let generatedImage = GeneratedImage(filePath: filePath, prompt: fullPrompt, createdAt: Date(), isSelectedForRotation: true)
                await repository.insertGeneratedImage(image: generatedImage)
                print("Image generated, cropped, and saved to internal storage: \(filePath)")
                successMessage = "Wallpaper generated successfully!"
                errorMessage = nil
            } catch {
                errorMessage = error.localizedDescription
                print("Error: \(error.localizedDescription)")
            }

            await MainActor.run {
                isGeneratingImage = false
                if let errorMessage = errorMessage {
                    generationError = errorMessage
                    snackbarMessages.send(errorMessage)
                }
                if let successMessage = successMessage {
                    snackbarMessages.send(successMessage)
                }
            }
        }
    }

    func deleteGeneratedImage(image: GeneratedImage) {
        Task {
            await repository.deleteGeneratedImageFile(image: image)
        }
    }

    func toggleImageSelection(image: GeneratedImage) {
        Task {
            await repository.updateGeneratedImageSelection(id: image.id, isSelected: !image.isSelectedForRotation)
        }
    }
    
    func deleteSelectedImages() {
        Task {
            let imagesToDelete = generatedImages.filter { $0.isSelectedForRotation }
            if !imagesToDelete.isEmpty {
                await repository.deleteGeneratedImages(images: imagesToDelete)
                print("Deleted \(imagesToDelete.count) selected images.")
            }
        }
    }

    func setWallpaperFromSelection() {
        Task {
            do {
                guard let firstSelectedImage = await repository.getFirstSelectedImage() else {
                    print("No selected images available to set as wallpaper.")
                    return
                }
                guard let uiImage = loadUIImageFromFile(filePath: firstSelectedImage.filePath) else {
                    print("Failed to load image")
                    return
                }
                guard let imageData = uiImage.pngData() else{
                    print("Failed to get data from image")
                    return
                }
                
                try await setWallpaper(imageData: imageData)
                print("Set wallpaper from selected file: \(firstSelectedImage.filePath)")
            } catch {
                print("Error setting wallpaper from selection: \(error.localizedDescription)")
            }
        }
    }
    
    func setWallpaper(imageData: Data) async throws {
        
        if let uiImage = UIImage(data: imageData) {
            
            guard let cgImage = uiImage.cgImage else {
                throw NSError(domain: "WallpaperErrorDomain", code: -1, userInfo: [NSLocalizedDescriptionKey: "Could not get CGImage from UIImage"])
            }
            
            try await withCheckedThrowingContinuation { continuation in
                
                do {
                    try  NSWorkspace.shared.setDesktopImage(cgImage, for: NSScreen.main!)
                    continuation.resume()
                } catch {
                    continuation.resume(throwing: error)
                }
            }
        } else {
            throw NSError(domain: "WallpaperErrorDomain", code: -1, userInfo: [NSLocalizedDescriptionKey: "Could not create UIImage from data"])
        }
        
    }

    func exportSelectedImagesToGallery() {
        Task {
            let selectedImages = generatedImages.filter { $0.isSelectedForRotation }
            guard !selectedImages.isEmpty else {
                snackbarMessages.send("No images selected for export.")
                return
            }

            var successCount = 0
            var errorCount = 0
            
            for image in selectedImages {
                do {
                    if let uiImage = loadUIImageFromFile(filePath: image.filePath) {
                        try await saveUIImageToGallery(uiImage: uiImage)
                        successCount += 1
                    } else {
                        errorCount += 1
                    }
                } catch {
                    print("Error exporting image \(image.filePath): \(error.localizedDescription)")
                    errorCount += 1
                }
            }

            let message: String
            switch (successCount, errorCount) {
            case (let s, 0) where s > 0:
                message = "Exported \(successCount) image(s) to gallery."
            case (let s, let e) where s > 0 && e > 0:
                message = "Exported \(successCount) image(s), failed for \(errorCount)."
            default:
                message = "Failed to export selected image(s)."
            }
            snackbarMessages.send(message)
        }
    }

    private func saveUIImageToGallery(uiImage: UIImage) async throws {
        guard let imageData = uiImage.jpegData(compressionQuality: 1.0) else {
            throw NSError(domain: "ImageExportError", code: -1, userInfo: [NSLocalizedDescriptionKey: "Could not convert UIImage to JPEG data."])
        }
        
        let creationRequest = PHAssetCreationRequest.forAsset()
        let options = PHAssetResourceCreationOptions()
        
        creationRequest.addResource(with: .photo, data: imageData, options: options)
    }

    private func loadUIImageFromFile(filePath: String) -> UIImage? {
        return UIImage(contentsOfFile: filePath)
    }

    func showSnackbar(message: String) {
        snackbarMessages.send(message)
    }
}