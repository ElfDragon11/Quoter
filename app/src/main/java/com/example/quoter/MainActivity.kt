package com.example.quoter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi // For Pager
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GridView // Use GridView for Library
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope // Add this import
import androidx.compose.runtime.setValue // For mutable state
import androidx.compose.runtime.mutableStateOf // Add this import
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp // Import dp unit
import com.example.quoter.ui.screens.GenerationScreen
import com.example.quoter.ui.screens.ImageLibraryScreen
import com.example.quoter.ui.screens.SettingsScreen // Import SettingsScreen
import com.example.quoter.ui.theme.QuoterTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class) // Add OptIn for TopAppBar
@Composable
fun QuoterApp(viewModel: QuoteViewModel) {
    // val navController = rememberNavController() // NavController might not be needed for top-level anymore
    val snackbarHostState = remember { SnackbarHostState() }

    // Pager state for swipe navigation
    // Explicitly name the pageCount parameter
    val pagerState = rememberPagerState(pageCount = { items.size })
    val coroutineScope = rememberCoroutineScope()

    // State to control pager swipeability based on ImageLibrary's fullscreen mode
    var isImageLibraryFullscreen by remember { mutableStateOf(false) }

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
                items.forEachIndexed { index, screen -> // Use forEachIndexed for page index
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        // Selected state now based on pagerState.currentPage
                        selected = pagerState.currentPage == index,
                        onClick = {
                            // Animate to the selected page
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize() // Pager should fill the content area
                .padding(innerPadding),
            userScrollEnabled = !isImageLibraryFullscreen // Disable swipe if image library is fullscreen
        ) { pageIndex ->
            // Content for each page
            when (pageIndex) {
                0 -> GenerationScreen(
                    // navController, // Pass NavController if GenerationScreen has internal navigation
                    viewModel = viewModel
                )
                1 -> ImageLibraryScreen(
                    // navController, // Pass NavController if ImageLibraryScreen has internal navigation
                    viewModel = viewModel,
                    onFullscreenToggle = { isFullscreen ->
                        isImageLibraryFullscreen = isFullscreen
                    }
                )
            }
        }
    }
}