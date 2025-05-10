swift
import Foundation
import CoreData
import SwiftUI

class QuoteRepository {
    private let context: NSManagedObjectContext
    private let generatedImageDao: GeneratedImageDao

    init(context: NSManagedObjectContext) {
        self.context = context
        self.generatedImageDao = GeneratedImageDao(context: context)
    }

    // --- Generated Image Methods ---
    func getAllGeneratedImages() -> AnyPublisher<[GeneratedImage], Never> {
        return generatedImageDao.getAllImages()
    }

    func getSelectedImagesForRotation() -> AnyPublisher<[GeneratedImage], Never> {
        return generatedImageDao.getSelectedImagesForRotation()
    }

    func insertGeneratedImage(image: GeneratedImage) throws {
         try generatedImageDao.insertImage(image: image)
    }

    func deleteGeneratedImageById(id: UUID) throws {
         try generatedImageDao.deleteImageById(id: id)
    }

    func updateGeneratedImageSelection(id: UUID, isSelected: Bool) throws {
        try generatedImageDao.updateSelectionStatus(id: id, isSelected: isSelected)
    }

    func getRandomSelectedImage() throws -> GeneratedImage? {
        return try generatedImageDao.getRandomSelectedImage()
    }
    
    func getFirstSelectedImage() throws -> GeneratedImage? {
        return try generatedImageDao.getFirstSelectedImage()
    }

    func getSelectedImagesOrderedById() throws -> [GeneratedImage] {
        return try generatedImageDao.getSelectedImagesOrderedById()
    }

    func deleteGeneratedImageFile(image: GeneratedImage) throws {
        // Delete the actual file from internal storage using filePath
        guard let fileURL = image.filePath else {
            print("Error: filePath is nil for image with ID: \(image.id)")
            throw NSError(domain: "com.example.app", code: -1, userInfo: [NSLocalizedDescriptionKey: "filePath is nil"])
        }
        
        do {
            if FileManager.default.fileExists(atPath: fileURL.path) {
                try FileManager.default.removeItem(at: fileURL)
                print("Deleted image file: \(fileURL)")
            } else {
                print("Image file not found, skipping deletion: \(fileURL)")
            }
        } catch {
            print("Error deleting image file \(fileURL): \(error.localizedDescription)")
            throw error
        }
       try generatedImageDao.deleteImage(image: image)
    }

    // Add function to delete multiple images (files and DB records)
    func deleteGeneratedImages(images: [GeneratedImage]) throws {
        // Delete files first
        for image in images {
           try deleteGeneratedImageFile(image: image)
        }
        
        // Then delete all database records at once
        try generatedImageDao.deleteImages(images: images)
    }
}