swift
import SwiftUI
import Combine

struct GeneratedImagesScreen: View {
    @ObservedObject var viewModel: QuoteViewModel
    @State private var showingDeleteConfirmation = false
    @State private var showingWallpaperConfirmation = false
    
    var body: some View {
        NavigationView {
            VStack {
                if viewModel.generatedImages.isEmpty {
                    Text("No generated images yet.")
                        .font(.headline)
                        .foregroundColor(.gray)
                        .padding()
                } else {
                    List {
                        ForEach(viewModel.generatedImages, id: \.id) { image in
                            HStack {
                                if let uiImage = viewModel.loadUIImageFromFile(filePath: image.filePath) {
                                    Image(uiImage: uiImage)
                                        .resizable()
                                        .scaledToFit()
                                        .frame(width: 100, height: 150)
                                        .cornerRadius(8)
                                } else {
                                    Image(systemName: "photo")
                                        .resizable()
                                        .scaledToFit()
                                        .frame(width: 100, height: 150)
                                        .cornerRadius(8)
                                }
                                
                                Button(action: {
                                    viewModel.toggleImageSelection(image: image)
                                }) {
                                    Image(systemName: image.isSelectedForRotation ? "checkmark.circle.fill" : "circle")
                                        .foregroundColor(image.isSelectedForRotation ? .blue : .gray)
                                }
                                .buttonStyle(PlainButtonStyle())
                                
                                Spacer()
                                
                                Button(action: {
                                    viewModel.deleteGeneratedImage(image: image)
                                }) {
                                    Image(systemName: "trash")
                                        .foregroundColor(.red)
                                }
                                .buttonStyle(PlainButtonStyle())
                            }
                        }
                    }
                }
            }
            .navigationTitle("Generated Images")
            .toolbar {
                ToolbarItemGroup(placement: .topBarTrailing) {
                    if viewModel.generatedImages.contains(where: { $0.isSelectedForRotation }) {
                        Button(action: {
                            showingDeleteConfirmation = true
                        }) {
                            Image(systemName: "trash.fill")
                        }
                        .confirmationDialog("Are you sure you want to delete the selected images?", isPresented: $showingDeleteConfirmation) {
                            Button("Delete", role: .destructive) {
                                viewModel.deleteSelectedImages()
                            }
                        }
                    }
                    
                    if viewModel.generatedImages.contains(where: { $0.isSelectedForRotation }) {
                        Button(action: {
                            showingWallpaperConfirmation = true
                        }) {
                            Image(systemName: "wallpaper")
                        }
                        .confirmationDialog("Are you sure you want to set the first selected image as the wallpaper?", isPresented: $showingWallpaperConfirmation) {
                            Button("Set as Wallpaper", role: .none) {
                                viewModel.setWallpaperFromSelection()
                            }
                        }
                    }
                }
            }
        }
    }
}