swift
import CoreData
import UIKit

class CoreDataManager {
    static let shared = CoreDataManager()
    
    let persistentContainer: NSPersistentContainer
    
    private init() {
        persistentContainer = NSPersistentContainer(name: "GeneratedImageModel") // Name of your .xcdatamodeld file
        
        // Load persistent stores. If this is not successful the app will crash.
        persistentContainer.loadPersistentStores(completionHandler: { (storeDescription, error) in
            if let error = error as NSError? {
                fatalError("Unresolved error \(error), \(error.userInfo)")
            }
        })
        
        persistentContainer.viewContext.automaticallyMergesChangesFromParent = true
    }
    
    // Helper functions
    func saveContext() {
        let context = persistentContainer.viewContext
        if context.hasChanges {
            do {
                try context.save()
            } catch {
                let nserror = error as NSError
                fatalError("Unresolved error \(nserror), \(nserror.userInfo)")
            }
        }
    }
    
    func fetchAllGeneratedImages() -> [GeneratedImage] {
        let request: NSFetchRequest<GeneratedImage> = GeneratedImage.fetchRequest()
        do {
            return try persistentContainer.viewContext.fetch(request)
        } catch {
            print("Error fetching data: \(error)")
            return []
        }
    }
}

// Define the GeneratedImage entity (should match your Core Data model)
@objc(GeneratedImage)
public class GeneratedImage: NSManagedObject {
    @NSManaged public var id: Int64
    @NSManaged public var filePath: String
    @NSManaged public var prompt: String?
    @NSManaged public var createdAt: Date
    @NSManaged public var isSelectedForRotation: Bool
}

extension GeneratedImage {
    
    @nonobjc public class func fetchRequest() -> NSFetchRequest<GeneratedImage> {
        return NSFetchRequest<GeneratedImage>(entityName: "GeneratedImage")
    }
}