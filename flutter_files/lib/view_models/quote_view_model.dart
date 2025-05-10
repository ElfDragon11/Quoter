import 'dart:convert';
import 'dart:io';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'package:path_provider/path_provider.dart';
import 'package:quoter/models/generated_image.dart';
import 'package:quoter/utils/image_utils.dart';
import 'package:shared_preferences/shared_preferences.dart';

class QuoteViewModel extends ChangeNotifier {
  String _location = "Nature";
  String get location => _location;
  String _scene = "Landscape";
  String get scene => _scene;
  String _style = "Photorealistic";
  String get style => _style;
  double _fontSize = 16.0;
  double get fontSize => _fontSize;
  bool _isGenerating = false;
  bool get isGenerating => _isGenerating;
  String? _generationError;
  String? get generationError => _generationError;
  List<GeneratedImage> _generatedImages = [];
  List<GeneratedImage> get generatedImages => _generatedImages;

  final String _openAiApiKey = "";

  QuoteViewModel() {
    _loadPreferences();
    _loadGeneratedImages();
  }

  Future<void> updateLocation(String location) async {
    _location = location;
    notifyListeners();
    await _savePreferences();
  }

  Future<void> updateScene(String scene) async {
    _scene = scene;
    notifyListeners();
    await _savePreferences();
  }

  Future<void> updateStyle(String style) async {
    _style = style;
    notifyListeners();
    await _savePreferences();
  }

  Future<void> updateFontSize(double fontSize) async {
    _fontSize = fontSize;
    notifyListeners();
    await _savePreferences();
  }

  Future<void> generateImage(String quoteText) async {
    if (quoteText.isEmpty) {
      _generationError = "Please enter a quote.";
      notifyListeners();
      return;
    }

    _isGenerating = true;
    _generationError = null;
    notifyListeners();

    final fullPrompt =
        "Generate a vertical 9:16 image. Background should show $_location as the location, $_scene as the scene, and follow $_style styling. Center the quote \"$quoteText\" within a safe 9:16 frame, using a Bold font style at roughly ${_fontSize.toInt()}pt. Add a subtle shadow or outline if needed to keep the words crisp against the background. Ensure the text fits comfortably and completely in the 9:16 center, avoiding the top/bottom 15%. Balance color, lighting, and depth. Return only the image.";

    try {
      final response = await http.post(
        Uri.parse('https://api.openai.com/v1/images/generations'),
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer $_openAiApiKey',
        },
        body: jsonEncode({
          'model': 'dall-e-3',
          'prompt': fullPrompt,
          'n': 1,
          'size': '1024x1792',
          'response_format': 'b64_json'
        }),
      );

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        final base64Image = data['data'][0]['b64_json'];
        final decodedBytes = base64.decode(base64Image);
        final directory = await getApplicationDocumentsDirectory();
        final path = '${directory.path}/quoter_${DateTime.now().millisecondsSinceEpoch}.png';
        final file = File(path);
        await file.writeAsBytes(decodedBytes);

        final newImage = GeneratedImage(
          id: DateTime.now().millisecondsSinceEpoch,
          path: path,
          isSelected: false,
        );
        _generatedImages.add(newImage);
        await _saveGeneratedImages();
        notifyListeners();
      } else {
        _generationError = "Failed to generate image: ${response.body}";
        notifyListeners();
      }
    } catch (e) {
      _generationError = "Unexpected error: $e";
      notifyListeners();
    } finally {
      _isGenerating = false;
      notifyListeners();
    }
  }

  Future<void> deleteImage(GeneratedImage image) async {
    await ImageUtils.deleteImage(image.path);
    _generatedImages.removeWhere((element) => element.id == image.id);
    await _saveGeneratedImages();
    notifyListeners();
  }

  Future<void> deleteSelectedImages() async {
    final selectedImages = _generatedImages.where((image) => image.isSelected).toList();
    for (final image in selectedImages) {
      await ImageUtils.deleteImage(image.path);
      _generatedImages.removeWhere((element) => element.id == image.id);
    }
    await _saveGeneratedImages();
    notifyListeners();
  }

  Future<void> setWallpaper(GeneratedImage image) async {
    await ImageUtils.setWallpaper(image.path);
    notifyListeners();
  }

  void toggleImageSelection(GeneratedImage image) {
    final index = _generatedImages.indexOf(image);
    if (index != -1) {
      _generatedImages[index].isSelected = !_generatedImages[index].isSelected;
      notifyListeners();
    }
  }

  Future<void> _savePreferences() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString('location', _location);
    await prefs.setString('scene', _scene);
    await prefs.setString('style', _style);
    await prefs.setDouble('fontSize', _fontSize);
  }

  Future<void> _loadPreferences() async {
    final prefs = await SharedPreferences.getInstance();
    _location = prefs.getString('location') ?? "Nature";
    _scene = prefs.getString('scene') ?? "Landscape";
    _style = prefs.getString('style') ?? "Photorealistic";
    _fontSize = prefs.getDouble('fontSize') ?? 16.0;
    notifyListeners();
  }

  Future<void> _saveGeneratedImages() async {
    final prefs = await SharedPreferences.getInstance();
    final imagesJson = _generatedImages.map((image) => image.toJson()).toList();
    await prefs.setString('generatedImages', jsonEncode(imagesJson));
  }

  Future<void> _loadGeneratedImages() async {
    final prefs = await SharedPreferences.getInstance();
    final imagesJson = prefs.getString('generatedImages');
    if (imagesJson != null) {
      final List<dynamic> decodedJson = jsonDecode(imagesJson);
      _generatedImages = decodedJson.map((json) => GeneratedImage.fromJson(json)).toList();
      notifyListeners();
    }
  }
}