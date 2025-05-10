swift
import SwiftUI
import Combine

struct GenerationScreen: View {
    @ObservedObject var viewModel: QuoteViewModel
    @State private var quoteText: String = ""
    @State private var expandedLocation: Bool = false
    @State private var expandedScene: Bool = false
    @State private var expandedStyle: Bool = false
    @State private var expandedFontStyle: Bool = false
    @State private var editingDropdown: String? = nil
    @State private var customInputValue: String = ""
    @State private var fontSize: CGFloat = 16
    
    let locations: [String] = ["Mountains", "Forest", "Ocean", "Space", "Beach", "City", "Custom..."]
    let scenes: [String] = ["Natural", "Sunset", "Futuristic", "Modern", "Cozy", "Minimalist", "Custom..."]
    let styles: [String] = ["Photorealistic", "Oil Painting", "Watercolor", "Cartoon", "Cyberpunk", "Custom..."]
    let fontStyles: [String] = ["Bold", "Artistic", "Cursive", "Custom..."]
    
    private func handleDropdownSelection(type: String, value: String, setExpanded: (Bool) -> Void) {
        if value == "Custom..." {
            editingDropdown = type
            switch type {
            case "location":
                customInputValue = viewModel.customizationSettings.location
            case "scene":
                customInputValue = viewModel.customizationSettings.scene
            case "style":
                customInputValue = viewModel.customizationSettings.style
            case "fontStyle":
                customInputValue = viewModel.customizationSettings.fontStyle
            default:
                customInputValue = ""
            }
            setExpanded(false)
        } else {
            switch type {
            case "location":
                viewModel.updateLocation(location: value)
            case "scene":
                viewModel.updateScene(scene: value)
            case "style":
                viewModel.updateStyle(style: value)
            case "fontStyle":
                viewModel.updateFontStyle(fontStyle: value)
            default:
                break
            }
            setExpanded(false)
        }
    }
    
    @ViewBuilder
    func DropdownOrCustomInput(type: String, label: String, currentValue: String, expanded: Bool, onExpandedChange: @escaping (Bool) -> Void, options: [String], onOptionSelected: @escaping (String) -> Void) -> some View {
        if editingDropdown == type {
            HStack {
                TextField("Enter Custom \(label)", text: $customInputValue)
                    .padding(.trailing, 8)
                    .textFieldStyle(.roundedBorder)
                Button(action: {
                    switch type {
                    case "location":
                        viewModel.updateLocation(location: customInputValue)
                    case "scene":
                        viewModel.updateScene(scene: customInputValue)
                    case "style":
                        viewModel.updateStyle(style: customInputValue)
                    case "fontStyle":
                        viewModel.updateFontStyle(fontStyle: customInputValue)
                    default:
                        break
                    }
                    editingDropdown = nil
                }) {
                    Image(systemName: "checkmark")
                }
                Button(action: {
                    editingDropdown = nil
                }) {
                    Image(systemName: "xmark")
                }
            }
            .padding(EdgeInsets(top: 4, leading: 8, bottom: 4, trailing: 8))
            .background(Color.blue.opacity(0.8))
            .cornerRadius(8)
        } else {
            Menu {
                ForEach(options, id: \.self) { option in
                    Button(action: {
                        onOptionSelected(option)
                    }) {
                        Text(option)
                    }
                }
            } label: {
                TextField(label, text: .constant(currentValue))
                    .disabled(true)
            }
            .padding(EdgeInsets(top: 0, leading: 0, bottom: 0, trailing: 0))
            .background(Color.blue.opacity(0.8))
            .cornerRadius(8)
        }
    }
    
    var body: some View {
        VStack(spacing: 16) {
            TextField("Enter a quote", text: $quoteText)
                .padding(.top, 8)
                .textFieldStyle(.roundedBorder)
                .background(Color.gray.opacity(0.2))
                .cornerRadius(8)
                .disabled(viewModel.isGeneratingImage)
            
            DropdownOrCustomInput(
                type: "location",
                label: "Location",
                currentValue: viewModel.customizationSettings.location,
                expanded: expandedLocation,
                onExpandedChange: { expandedLocation = $0 },
                options: locations,
                onOptionSelected: { handleDropdownSelection(type: "location", value: $0, setExpanded: { expandedLocation = $0 }) }
            )
            
            DropdownOrCustomInput(
                type: "scene",
                label: "Scene",
                currentValue: viewModel.customizationSettings.scene,
                expanded: expandedScene,
                onExpandedChange: { expandedScene = $0 },
                options: scenes,
                onOptionSelected: { handleDropdownSelection(type: "scene", value: $0, setExpanded: { expandedScene = $0 }) }
            )
            
            DropdownOrCustomInput(
                type: "style",
                label: "Style",
                currentValue: viewModel.customizationSettings.style,
                expanded: expandedStyle,
                onExpandedChange: { expandedStyle = $0 },
                options: styles,
                onOptionSelected: { handleDropdownSelection(type: "style", value: $0, setExpanded: { expandedStyle = $0 }) }
            )
            
            DropdownOrCustomInput(
                type: "fontStyle",
                label: "Font Style",
                currentValue: viewModel.customizationSettings.fontStyle,
                expanded: expandedFontStyle,
                onExpandedChange: { expandedFontStyle = $0 },
                options: fontStyles,
                onOptionSelected: { handleDropdownSelection(type: "fontStyle", value: $0, setExpanded: { expandedFontStyle = $0 }) }
            )
            
            VStack(alignment: .leading) {
                Text("Font Size: \(Int(fontSize)) pt")
                    .font(.body)
                Slider(value: $fontSize, in: 8...72, step: 1)
                    .onChange(of: fontSize) { newSize in
                        viewModel.updateFontSize(fontSize: newSize)
                    }
            }
            
            Button(action: {
                viewModel.generateAndSaveImage(quoteText: quoteText, customizationSettings: viewModel.customizationSettings, fontStyle: viewModel.customizationSettings.fontStyle, fontSize: fontSize)
            }) {
                if viewModel.isGeneratingImage {
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle())
                        .frame(width: 24, height: 24)
                    Text("Generating...")
                } else {
                    Text("Generate Image")
                }
            }
            .disabled(quoteText.isEmpty || viewModel.isGeneratingImage)
            .frame(maxWidth: .infinity)
            
            if let error = viewModel.generationError {
                HStack {
                    Image(systemName: "exclamationmark.circle.fill")
                        .foregroundColor(.red)
                    Text(error)
                        .foregroundColor(.red)
                }
                .padding(.top, 8)
            }
        }
        .padding(16)
        .onAppear{
            fontSize = viewModel.customizationSettings.fontSize
        }
        .onReceive(viewModel.$snackbarMessage){ message in
            if let message = message {
                print("snackbar: \(message)")
            }
        }
    }
}