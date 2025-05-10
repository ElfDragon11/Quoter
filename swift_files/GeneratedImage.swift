swift
import Foundation
import CoreData

@objc(GeneratedImage)
public class GeneratedImage: NSManagedObject {
    @NSManaged public var id: UUID
    @NSManaged public var filePath: String
    @NSManaged public var prompt: String?
    @NSManaged public var createdAt: Date
    @NSManaged public var isSelectedForRotation: Bool

    convenience init(context: NSManagedObjectContext, filePath: String, prompt: String? = nil, createdAt: Date = Date(), isSelectedForRotation: Bool = true) {
        self.init(context: context)
        self.id = UUID()
        self.filePath = filePath
        self.prompt = prompt
        self.createdAt = createdAt
        self.isSelectedForRotation = isSelectedForRotation
    }
}