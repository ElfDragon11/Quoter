class GeneratedImage {
  final int? id;
  final String path;
  bool isSelected;

  GeneratedImage({
    this.id,
    required this.path,
    this.isSelected = false,
  });

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'path': path,
      'isSelected': isSelected,
    };
  }

  factory GeneratedImage.fromJson(Map<String, dynamic> json) {
    return GeneratedImage(
      id: json['id'],
      path: json['path'],
      isSelected: json['isSelected'] ?? false,
    );
  }
}