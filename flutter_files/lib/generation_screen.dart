import 'package:flutter/material.dart';

class GenerationScreen extends StatefulWidget {
  const GenerationScreen({super.key});

  @override
  GenerationScreenState createState() => GenerationScreenState();
}

class GenerationScreenState extends State<GenerationScreen> {
  final TextEditingController _quoteController = TextEditingController();
  final TextEditingController _locationController = TextEditingController();
  final TextEditingController _sceneController = TextEditingController();
  final TextEditingController _styleController = TextEditingController();
  double _fontSize = 16.0;
  bool _isGenerating = false;

  @override
  void dispose() {
    _quoteController.dispose();
    _locationController.dispose();
    _sceneController.dispose();
    _styleController.dispose();
    super.dispose();
  }

  void _generateImage() {
    setState(() {
      _isGenerating = true;
    });
    // Here should be the code to call the api
    Future.delayed(const Duration(seconds: 3), () {
      setState(() {
        _isGenerating = false;
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: SingleChildScrollView(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              TextField(
                controller: _quoteController,
                decoration: const InputDecoration(
                  labelText: 'Enter a quote',
                  border: OutlineInputBorder(),
                ),
                maxLines: 5,
              ),
              const SizedBox(height: 16),
              TextField(
                controller: _locationController,
                decoration: const InputDecoration(
                  labelText: 'Location',
                  border: OutlineInputBorder(),
                ),
              ),
              const SizedBox(height: 16),
              TextField(
                controller: _sceneController,
                decoration: const InputDecoration(
                  labelText: 'Scene',
                  border: OutlineInputBorder(),
                ),
              ),
              const SizedBox(height: 16),
              TextField(
                controller: _styleController,
                decoration: const InputDecoration(
                  labelText: 'Style',
                  border: OutlineInputBorder(),
                ),
              ),
              const SizedBox(height: 16),
              Text('Font Size: ${_fontSize.toInt()}'),
              Slider(
                value: _fontSize,
                min: 8.0,
                max: 72.0,
                onChanged: (value) {
                  setState(() {
                    _fontSize = value;
                  });
                },
              ),
              const SizedBox(height: 24),
              ElevatedButton(
                onPressed: _isGenerating ? null : _generateImage,
                child: _isGenerating
                    ? const SizedBox(
                        height: 20,
                        width: 20,
                        child: CircularProgressIndicator(
                          color: Colors.white,
                          strokeWidth: 3,
                        ),
                      )
                    : const Text('Generate Image'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}