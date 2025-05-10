swift
import UIKit

class ImageUtils {

    /**
     Crops a UIImage to a 9:20 aspect ratio, keeping the center.
     Assumes the input image is 1024x1792 (or similar aspect ratio from DALL-E 3).
     */
    class func cropTo9x20(source: UIImage) -> UIImage? {
        let sourceWidth = source.size.width
        let sourceHeight = source.size.height

        // Target aspect ratio (9:20)
        let targetAspectRatio = 9.0 / 20.0

        // Calculate the target width based on source height to maintain 9:20
        var targetWidth = Int(sourceHeight * targetAspectRatio)
        var targetHeight = Int(sourceHeight)

        // If the calculated width is wider than the source, calculate target height based on source width
        if CGFloat(targetWidth) > sourceWidth {
            targetWidth = Int(sourceWidth)
            targetHeight = Int(sourceWidth / targetAspectRatio)
            // Ensure targetHeight doesn't exceed sourceHeight
            if CGFloat(targetHeight) > sourceHeight { targetHeight = Int(sourceHeight) }
            print("Cropping: Using full width (\(sourceWidth)), calculated height (\(targetHeight)) for 9:20 ratio.")
        } else {
            print("Cropping: Using full height (\(sourceHeight)), calculated width (\(targetWidth)) for 9:20 ratio.")
        }

        // Calculate cropping bounds (centered)
        let left = (Int(sourceWidth) - targetWidth) / 2
        let top = (Int(sourceHeight) - targetHeight) / 2
        let right = left + targetWidth
        let bottom = top + targetHeight

        // Ensure bounds are within the source image dimensions
        if left < 0 || top < 0 || right > Int(sourceWidth) || bottom > Int(sourceHeight) || targetWidth <= 0 || targetHeight <= 0 {
            print("Error: Calculated crop bounds are invalid.")
            return nil // Or return the original image if cropping fails
        }

        // Create the cropping rectangle
        let cropRect = CGRect(x: CGFloat(left), y: CGFloat(top), width: CGFloat(targetWidth), height: CGFloat(targetHeight))

        // Perform the cropping
        if let croppedCGImage = source.cgImage?.cropping(to: cropRect) {
            return UIImage(cgImage: croppedCGImage, scale: source.scale, orientation: source.imageOrientation)
        } else {
            print("Error cropping image.")
            return nil
        }
    }

    /**
     Saves a UIImage to the app's documents directory.
     Returns the URL of the saved file, or nil on failure.
     */
    class func saveImageToDocumentsDirectory(image: UIImage, filenamePrefix: String = "wallpaper_") -> URL? {
        guard let data = image.pngData() else {
            print("Error: Could not convert image to PNG data.")
            return nil
        }

        // Get the documents directory URL
        guard let documentsDirectory = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first else {
            print("Error: Could not access documents directory.")
            return nil
        }

        // Create a unique filename
        let filename = "\(filenamePrefix)\(UUID().uuidString).png"
        let fileURL = documentsDirectory.appendingPathComponent(filename)

        do {
            try data.write(to: fileURL)
            return fileURL
        } catch {
            print("Error saving image: \(error.localizedDescription)")
            return nil
        }
    }
}