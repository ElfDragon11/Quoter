# Quoter v2 - AI-Powered Wallpaper Generator

This document outlines the goals and development plan for enhancing the Quoter Android application. The primary goal is to integrate AI-powered image generation for dynamic wallpapers and provide users with greater customization options.

## Project Goals

1.  **AI Background Generation**: Integrate with the OpenAI API (specifically, an image generation model like DALL-E) to create unique background images based on user prompts or selections.
2.  **Image Cropping**: Automatically crop the generated 1024x1536 images to a 9:16 aspect ratio suitable for phone wallpapers, focusing on the central portion.
3.  **Enhanced Customization**: Introduce new settings for wallpaper generation:
    *   Location (e.g., City, Nature)
    *   Scene (e.g., Abstract, Landscape)
    *   Style (e.g., Photorealistic, Cartoon, Painting)
    *   Font (for the quote text)
4.  **Image Library**: Implement a gallery or library where users can view, select, and deselect the AI-generated images they want to include in the automatic wallpaper rotation.
5.  **Local Image Storage**: Save generated images to the device's local storage for offline access and use in the image library.
6.  **Refined User Experience**: Ensure the new features are seamlessly integrated into the existing app structure and UI (built with Jetpack Compose).

## Development Outline

1.  **Setup OpenAI Integration**:
    *   Add necessary dependencies (e.g., Retrofit/Ktor for API calls, JSON parsing library).
    *   Securely manage the OpenAI API key (avoid hardcoding).
    *   Implement API service calls to the image generation endpoint.
2.  **Image Generation & Cropping**:
    *   Develop logic to construct prompts for the AI based on user customizations (Location, Scene, Style).
    *   Handle the API response containing the image URL or data.
    *   Download the generated image.
    *   Implement image cropping logic (calculate center 9:16 portion of 1024x1536).
3.  **Local Storage**:
    *   Choose a storage strategy (Internal vs. External storage).
    *   Implement functions to save cropped images locally, potentially naming them uniquely.
    *   Consider storage management (e.g., capping the number of saved images or total size).
4.  **Database/State Management**:
    *   Update the Room database or app state management to store references (e.g., file paths) to the saved images.
    *   Associate saved images with user selections (which ones are active for rotation).
5.  **UI Development**:
    *   **Customization**: Add dropdowns/selectors for Location, Scene, Style, and Font to the Settings or a dedicated customization screen.
    *   **Image Library**: Create a new screen or section displaying thumbnails of saved images. Allow users to select/deselect images for rotation. Add delete functionality.
    *   **Preview**: Update the `PreviewScreen` to reflect the new customization options and potentially show previews with generated images.
6.  **Wallpaper Service Update**:
    *   Modify the `QuoteWallpaperWorker` (or relevant service) to:
        *   Fetch user customization preferences.
        *   Select an image from the user's active library (either randomly or sequentially).
        *   Load the selected local image file.
        *   Combine the loaded image with the selected quote and font.
        *   Set the combined image as the wallpaper.
7.  **Refactoring & Integration**:
    *   Update ViewModels and Repositories to handle new data and logic.
    *   Ensure smooth navigation between existing and new screens.
8.  **Permissions**:
    *   Verify necessary permissions (Internet for API calls, potentially Storage permissions depending on the chosen strategy).
9.  **Testing**:
    *   Unit tests for API calls, cropping logic, database operations.
    *   UI tests for new screens and interactions.
    *   Manual testing of the end-to-end flow (customization -> generation -> saving -> library management -> wallpaper setting).

