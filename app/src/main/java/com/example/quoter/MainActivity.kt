package com.example.quoter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GridView // Use GridView for Library
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.* // Import compose navigation artifacts
import com.example.quoter.ui.screens.GenerationScreen
import com.example.quoter.ui.screens.ImageLibraryScreen
import com.example.quoter.ui.screens.SettingsScreen // Import SettingsScreen
import com.example.quoter.ui.theme.QuoterTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.ui.unit.dp // Import dp unit
import androidx.compose.foundation.layout.height // Add this import

// Define Navigation Routes
sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Generation : Screen("generation", "Generate", Icons.Default.Add)
    object Library : Screen("library", "Library", Icons.Default.GridView)
    // Remove Settings object
    // object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

// Update items list to remove Settings
val items = listOf(
    Screen.Generation,
    Screen.Library
    // Screen.Settings removed
)

class MainActivity : ComponentActivity() {
    private val viewModel: QuoteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuoterTheme {
                QuoterApp(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) // Add OptIn for TopAppBar
@Composable
fun QuoterApp(viewModel: QuoteViewModel) {
    val navController = rememberNavController()
    // Create SnackbarHostState
    val snackbarHostState = remember { SnackbarHostState() }

    // Listen for Snackbar messages from ViewModel
    LaunchedEffect(Unit) { // Launch once per composition
        viewModel.snackbarMessages.collectLatest { message ->
            snackbarHostState.showSnackbar(message = message)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        // Remove the topBar parameter entirely
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                // Add modifier to set the height
                modifier = Modifier.height(60.dp) // Try a smaller height
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen -> // This loop now only includes Generation and Library
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                // on the back stack as users select items
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = Screen.Generation.route, Modifier.padding(innerPadding)) {
            composable(Screen.Generation.route) { GenerationScreen(navController, viewModel) }
            composable(Screen.Library.route) { ImageLibraryScreen(navController, viewModel) }
            // Remove SettingsScreen route
            // composable(Screen.Settings.route) { SettingsScreen() }
        }
    }
}