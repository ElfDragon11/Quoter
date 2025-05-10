swift
import Foundation

struct CustomizationSettings {
    var location: String
    var scene: String
    var style: String
    var fontStyle: String
    var fontSize: Float
}

class UserPreferencesRepository {
    
    private let userDefaults: UserDefaults
    
    private struct PreferencesKeys {
        static let location = "pref_location"
        static let scene = "pref_scene"
        static let style = "pref_style"
        static let fontStyle = "pref_font_style"
        static let fontSize = "pref_font_size"
    }
    
    private let defaultLocation = "Mountains"
    private let defaultScene = "Natural"
    private let defaultStyle = "Photorealistic"
    
    init(userDefaults: UserDefaults = .standard) {
        self.userDefaults = userDefaults
    }
    
    var userPreferences: CustomizationSettings {
        get {
            return CustomizationSettings(
                location: userDefaults.string(forKey: PreferencesKeys.location) ?? defaultLocation,
                scene: userDefaults.string(forKey: PreferencesKeys.scene) ?? defaultScene,
                style: userDefaults.string(forKey: PreferencesKeys.style) ?? defaultStyle,
                fontStyle: userDefaults.string(forKey: PreferencesKeys.fontStyle) ?? "Bold",
                fontSize: userDefaults.float(forKey: PreferencesKeys.fontSize) == 0 ? 16.0 : userDefaults.float(forKey: PreferencesKeys.fontSize)
            )
        }
        set {
            userDefaults.set(newValue.location, forKey: PreferencesKeys.location)
            userDefaults.set(newValue.scene, forKey: PreferencesKeys.scene)
            userDefaults.set(newValue.style, forKey: PreferencesKeys.style)
            userDefaults.set(newValue.fontStyle, forKey: PreferencesKeys.fontStyle)
            userDefaults.set(newValue.fontSize, forKey: PreferencesKeys.fontSize)
        }
    }
    
    func updateLocation(location: String) {
        userDefaults.set(location, forKey: PreferencesKeys.location)
    }
    
    func updateScene(scene: String) {
        userDefaults.set(scene, forKey: PreferencesKeys.scene)
    }
    
    func updateStyle(style: String) {
        userDefaults.set(style, forKey: PreferencesKeys.style)
    }
    
    func updateFontStyle(fontStyle: String) {
        userDefaults.set(fontStyle, forKey: PreferencesKeys.fontStyle)
    }
    
    func updateFontSize(fontSize: Float) {
        userDefaults.set(fontSize, forKey: PreferencesKeys.fontSize)
    }
}