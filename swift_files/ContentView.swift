swift
import SwiftUI
import Combine

enum Screen: String, CaseIterable {
    case generation = "Generate"
    case library = "Library"
    
    var icon: Image {
        switch self {
        case .generation:
            return Image(systemName: "plus")
        case .library:
            return Image(systemName: "photo.on.rectangle.angled")
        }
    }
}

class AppState: ObservableObject {
    @Published var selectedTab: Screen = .generation
    @Published var isImageLibraryFullscreen: Bool = false
    @Published var snackbarMessage: String?
}

class MainViewController: UIViewController {
    private var viewModel: QuoteViewModel
    private var appState: AppState
    
    init(viewModel: QuoteViewModel, appState: AppState) {
        self.viewModel = viewModel
        self.appState = appState
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        let contentView = QuoterApp(viewModel: viewModel, appState: appState)
        let hostingController = UIHostingController(rootView: contentView)
        addChild(hostingController)
        view.addSubview(hostingController.view)
        hostingController.view.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([
            hostingController.view.topAnchor.constraint(equalTo: view.topAnchor),
            hostingController.view.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            hostingController.view.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            hostingController.view.bottomAnchor.constraint(equalTo: view.bottomAnchor),
        ])
        hostingController.didMove(toParent: self)
        
        Task {
            for await message in viewModel.snackbarMessages.values {
                appState.snackbarMessage = message
                
                DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
                    self.appState.snackbarMessage = nil
                }
            }
        }
    }
}

struct QuoterApp: View {
    @ObservedObject var viewModel: QuoteViewModel
    @ObservedObject var appState: AppState
    
    var body: some View {
        ZStack {
            TabView(selection: $appState.selectedTab) {
                GenerationScreen(viewModel: viewModel)
                    .tabItem {
                        appState.selectedTab == .generation ? Screen.generation.icon.foregroundColor(Color.blue) : Screen.generation.icon.foregroundColor(Color.gray)
                        Text(Screen.generation.rawValue)
                    }
                    .tag(Screen.generation)
                
                GeneratedImagesScreen(viewModel: viewModel, isFullscreen: $appState.isImageLibraryFullscreen)
                    .tabItem {
                        appState.selectedTab == .library ? Screen.library.icon.foregroundColor(Color.blue) : Screen.library.icon.foregroundColor(Color.gray)
                        Text(Screen.library.rawValue)
                    }
                    .tag(Screen.library)
            }
            
            if let snackbarMessage = appState.snackbarMessage {
                VStack {
                    Spacer()
                    Text(snackbarMessage)
                        .padding()
                        .background(Color.gray)
                        .foregroundColor(.white)
                        .cornerRadius(10)
                        .padding(.bottom)
                }
            }
        }
    }
}