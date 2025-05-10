import 'package:flutter/material.dart';
import 'package:quoter/screens/generate_screen.dart';
import 'package:quoter/screens/images_screen.dart';

void main() {
  runApp(const QuoterApp());
}

class QuoterApp extends StatelessWidget {
  const QuoterApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Quoter',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
        useMaterial3: true,
      ),
      home: const QuoterHome(),
    );
  }
}

class QuoterHome extends StatefulWidget {
  const QuoterHome({super.key});

  @override
  State<QuoterHome> createState() => _QuoterHomeState();
}

class _QuoterHomeState extends State<QuoterHome> {
  int _selectedIndex = 0;

  static const List<Widget> _widgetOptions = <Widget>[
    GenerateScreen(),
    ImagesScreen(),
  ];

  void _onItemTapped(int index) {
    setState(() {
      _selectedIndex = index;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Center(
        child: _widgetOptions.elementAt(_selectedIndex),
      ),
      bottomNavigationBar: BottomNavigationBar(
        items: const <BottomNavigationBarItem>[
          BottomNavigationBarItem(
            icon: Icon(Icons.add),
            label: 'Generate',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.image),
            label: 'Images',
          ),
        ],
        currentIndex: _selectedIndex,
        onTap: _onItemTapped,
      ),
    );
  }
}