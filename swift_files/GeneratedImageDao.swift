swift
import CoreData
import Combine

class GeneratedImageDao {
    private let context: NSManagedObjectContext

    init(context: NSManagedObjectContext) {
        self.context = context
    }

    func insertImage(filePath: String, prompt: String?, isSelectedForRotation: Bool) -> AnyPublisher<GeneratedImage, Error> {
        return Future<GeneratedImage, Error> { promise in
            let image = GeneratedImage(context: self.context)
            image.id = UUID()
            image.filePath = filePath
            image.prompt = prompt
            image.createdAt = Date()
            image.isSelectedForRotation = isSelectedForRotation
            do {
                try self.context.save()
                promise(.success(image))
            } catch {
                promise(.failure(error))
            }
        }.eraseToAnyPublisher()
    }

    func getAllImages() -> AnyPublisher<[GeneratedImage], Error> {
        return Future<[GeneratedImage], Error> { promise in
            let fetchRequest: NSFetchRequest<GeneratedImage> = GeneratedImage.fetchRequest()
            let sortDescriptor = NSSortDescriptor(key: "createdAt", ascending: false)
            fetchRequest.sortDescriptors = [sortDescriptor]
            do {
                let images = try self.context.fetch(fetchRequest)
                promise(.success(images))
            } catch {
                promise(.failure(error))
            }
        }.eraseToAnyPublisher()
    }

    func getSelectedImagesForRotation() -> AnyPublisher<[GeneratedImage], Error> {
        return Future<[GeneratedImage], Error> { promise in
            let fetchRequest: NSFetchRequest<GeneratedImage> = GeneratedImage.fetchRequest()
            fetchRequest.predicate = NSPredicate(format: "isSelectedForRotation == %@", NSNumber(value: true))
            let sortDescriptor = NSSortDescriptor(key: "createdAt", ascending: false)
            fetchRequest.sortDescriptors = [sortDescriptor]
            do {
                let images = try self.context.fetch(fetchRequest)
                promise(.success(images))
            } catch {
                promise(.failure(error))
            }
        }.eraseToAnyPublisher()
    }

    func getImageById(id: UUID) -> AnyPublisher<GeneratedImage?, Error> {
        return Future<GeneratedImage?, Error> { promise in
            let fetchRequest: NSFetchRequest<GeneratedImage> = GeneratedImage.fetchRequest()
            fetchRequest.predicate = NSPredicate(format: "id == %@", id as CVarArg)
            fetchRequest.fetchLimit = 1
            do {
                let images = try self.context.fetch(fetchRequest)
                promise(.success(images.first))
            } catch {
                promise(.failure(error))
            }
        }.eraseToAnyPublisher()
    }

    func updateImage(image: GeneratedImage) -> AnyPublisher<Void, Error> {
        return Future<Void, Error> { promise in
            do {
                try self.context.save()
                promise(.success(()))
            } catch {
                promise(.failure(error))
            }
        }.eraseToAnyPublisher()
    }

    func updateSelectionStatus(id: UUID, isSelected: Bool) -> AnyPublisher<Void, Error> {
        return Future<Void, Error> { promise in
            let fetchRequest: NSFetchRequest<GeneratedImage> = GeneratedImage.fetchRequest()
            fetchRequest.predicate = NSPredicate(format: "id == %@", id as CVarArg)
            do {
                if let image = try self.context.fetch(fetchRequest).first {
                    image.isSelectedForRotation = isSelected
                    try self.context.save()
                    promise(.success(()))
                } else {
                    promise(.failure(NSError(domain: "com.example", code: -1, userInfo: [NSLocalizedDescriptionKey: "Image not found"])))
                }
            } catch {
                promise(.failure(error))
            }
        }.eraseToAnyPublisher()
    }

    func deleteImage(image: GeneratedImage) -> AnyPublisher<Void, Error> {
        return Future<Void, Error> { promise in
            self.context.delete(image)
            do {
                try self.context.save()
                promise(.success(()))
            } catch {
                promise(.failure(error))
            }
        }.eraseToAnyPublisher()
    }

    func deleteImageById(id: UUID) -> AnyPublisher<Void, Error> {
        return Future<Void, Error> { promise in
            let fetchRequest: NSFetchRequest<GeneratedImage> = GeneratedImage.fetchRequest()
            fetchRequest.predicate = NSPredicate(format: "id == %@", id as CVarArg)
            do {
                if let image = try self.context.fetch(fetchRequest).first {
                    self.context.delete(image)
                    try self.context.save()
                    promise(.success(()))
                } else {
                    promise(.failure(NSError(domain: "com.example", code: -1, userInfo: [NSLocalizedDescriptionKey: "Image not found"])))
                }
            } catch {
                promise(.failure(error))
            }
        }.eraseToAnyPublisher()
    }

    func deleteImages(images: [GeneratedImage]) -> AnyPublisher<Void, Error> {
           return Future<Void, Error> { promise in
               for image in images {
                   self.context.delete(image)
               }
               do {
                   try self.context.save()
                   promise(.success(()))
               } catch {
                   promise(.failure(error))
               }
           }.eraseToAnyPublisher()
       }

    func getRandomSelectedImage() -> AnyPublisher<GeneratedImage?, Error> {
        return Future<GeneratedImage?, Error> { promise in
            let fetchRequest: NSFetchRequest<GeneratedImage> = GeneratedImage.fetchRequest()
            fetchRequest.predicate = NSPredicate(format: "isSelectedForRotation == %@", NSNumber(value: true))
            do {
                let images = try self.context.fetch(fetchRequest)
                if let randomImage = images.randomElement() {
                    promise(.success(randomImage))
                } else {
                    promise(.success(nil))
                }
            } catch {
                promise(.failure(error))
            }
        }.eraseToAnyPublisher()
    }

    func getFirstSelectedImage() -> AnyPublisher<GeneratedImage?, Error> {
        return Future<GeneratedImage?, Error> { promise in
            let fetchRequest: NSFetchRequest<GeneratedImage> = GeneratedImage.fetchRequest()
            fetchRequest.predicate = NSPredicate(format: "isSelectedForRotation == %@", NSNumber(value: true))
            let sortDescriptor = NSSortDescriptor(key: "id", ascending: true)
            fetchRequest.sortDescriptors = [sortDescriptor]
            fetchRequest.fetchLimit = 1
            do {
                let images = try self.context.fetch(fetchRequest)
                promise(.success(images.first))
            } catch {
                promise(.failure(error))
            }
        }.eraseToAnyPublisher()
    }
    
    func getSelectedImagesOrderedById() -> AnyPublisher<[GeneratedImage], Error> {
            return Future<[GeneratedImage], Error> { promise in
                let fetchRequest: NSFetchRequest<GeneratedImage> = GeneratedImage.fetchRequest()
                fetchRequest.predicate = NSPredicate(format: "isSelectedForRotation == %@", NSNumber(value: true))
                let sortDescriptor = NSSortDescriptor(key: "id", ascending: true)
                fetchRequest.sortDescriptors = [sortDescriptor]
                do {
                    let images = try self.context.fetch(fetchRequest)
                    promise(.success(images))
                } catch {
                    promise(.failure(error))
                }
            }.eraseToAnyPublisher()
        }
}