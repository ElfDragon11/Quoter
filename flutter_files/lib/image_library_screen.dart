import 'package:flutter/material.dart';
import 'package:path_provider/path_provider.dart';
import 'package:flutter_image_compress/flutter_image_compress.dart';
import 'dart:io';
import 'package:image/image.dart' as img;
import 'package:wallpaper/wallpaper.dart';
import 'package:path/path.dart' as path;

class ImageLibraryScreen extends StatefulWidget {
  const ImageLibraryScreen({Key? key}) : super(key: key);

  @override
  _ImageLibraryScreenState createState() => _ImageLibraryScreenState();
}

class _ImageLibraryScreenState extends State<ImageLibraryScreen> {
  List<String> _imagePaths = [];
  List<String> _selectedImagePaths = [];
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _loadImages();
  }

  Future<void> _loadImages() async {
    setState(() {
      _isLoading = true;
    });
    final directory = await getApplicationDocumentsDirectory();
    final imageDirectory = Directory('${directory.path}/images');
    if (!await imageDirectory.exists()) {
      await imageDirectory.create(recursive: true);
    }
      final files = imageDirectory.listSync();
      List<String> paths = [];
      for (var file in files) {
        if (file is File && ['.jpg', '.jpeg', '.png'].contains(path.extension(file.path).toLowerCase())) {
          paths.add(file.path);
        }
      }
    setState(() {
      _imagePaths = paths;
      _isLoading = false;
    });
  }

  void _toggleImageSelection(String imagePath) {
    setState(() {
      if (_selectedImagePaths.contains(imagePath)) {
        _selectedImagePaths.remove(imagePath);
      } else {
        _selectedImagePaths.add(imagePath);
      }
    });
  }

  Future<void> _deleteSelectedImages() async {
    if (_selectedImagePaths.isEmpty) return;

    setState(() {
      _isLoading = true;
    });

    final directory = await getApplicationDocumentsDirectory();
    final imageDirectory = Directory('${directory.path}/images');

    for (var imagePath in _selectedImagePaths) {
      final file = File(imagePath);
      if (await file.exists()) {
        await file.delete();
      }
    }
    setState(() {
      _selectedImagePaths.clear();
    });

    await _loadImages();
  }

  Future<void> _setWallpaper(String imagePath) async {
    setState(() {
      _isLoading = true;
    });
    String result;
    try{
        var file = File(imagePath);
        var byteData = file.readAsBytesSync();
        var img = img.decodeImage(byteData);
        var resizedImg = img.copyResize(img, width: 1024, height: 1792);
        var compressedData = img.encodePng(resizedImg);

        result = await Wallpaper.homeScreen(imageData: compressedData);
    }catch(e){
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text("Failed to set wallpaper: $e")));
      result = "";
    }
    if(result != "success"){
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text("Failed to set wallpaper")));
    }else{
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text("Wallpaper Set!")));
    }

    setState(() {
      _isLoading = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: _isLoading
          ? Center(child: CircularProgressIndicator())
          : _imagePaths.isEmpty
              ? Center(child: Text('No images saved.'))
              : Column(
                  children: [
                    Padding(
                      padding: const EdgeInsets.all(8.0),
                      child: Row(
                        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                        children: [
                          ElevatedButton(
                            onPressed: _selectedImagePaths.isNotEmpty ? _deleteSelectedImages : null,
                            child: Text('Delete Selected'),
                          ),
                          ElevatedButton(
                            onPressed: _selectedImagePaths.length == 1 ? () => _setWallpaper(_selectedImagePaths.first) : null,
                            child: Text('Set as Wallpaper'),
                          ),
                        ],
                      ),
                    ),
                    Expanded(
                      child: ListView.builder(
                        itemCount: _imagePaths.length,
                        itemBuilder: (context, index) {
                          final imagePath = _imagePaths[index];
                          return ListTile(
                            leading: Checkbox(
                              value: _selectedImagePaths.contains(imagePath),
                              onChanged: (bool? value) {
                                _toggleImageSelection(imagePath);
                              },
                            ),
                            title: Image.file(File(imagePath)),
                          );
                        },
                      ),
                    ),
                  ],
                ),
    );
  }
}