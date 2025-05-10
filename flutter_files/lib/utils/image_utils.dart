import 'dart:io';
import 'dart:typed_data';
import 'package:path_provider/path_provider.dart';
import 'package:flutter_image_compress/flutter_image_compress.dart';
import 'package:image/image.dart' as img;
import 'package:flutter_cache_manager/flutter_cache_manager.dart';
import 'package:flutter_wallpaper_manager/flutter_wallpaper_manager.dart';

class ImageUtils {
  /// Saves an image to the device's local storage.
  ///
  /// [imageBytes]: The image data as a list of bytes.
  /// [filename]: The desired filename for the saved image.
  static Future<String?> saveImageToDevice(Uint8List imageBytes, String filename) async {
    try {
      final directory = await getApplicationDocumentsDirectory();
      final file = File('${directory.path}/$filename');

      // Compress the image before saving
      final compressedBytes = await FlutterImageCompress.compressWithList(
        imageBytes,
        minHeight: 1024,
        minWidth: 512,
        quality: 90,
      );

      await file.writeAsBytes(compressedBytes);
      return file.path;
    } catch (e) {
      print('Error saving image: $e');
      return null;
    }
  }

  /// Deletes an image from the device's local storage.
  ///
  /// [imagePath]: The path to the image to be deleted.
  static Future<void> deleteImage(String imagePath) async {
    try {
      final file = File(imagePath);
      if (await file.exists()) {
        await file.delete();
      }
    } catch (e) {
      print('Error deleting image: $e');
    }
  }
  
  static Future<void> setWallpaperFromPath(String path) async {
    try {
      int location = WallpaperManager.BOTH_SCREENS;
      var file = File(path);
      if (file.existsSync()){
        await WallpaperManager.setWallpaperFromFile(file.path, location);
      }
    } catch (e) {
      print('Error setting wallpaper: $e');
    }
  }
}