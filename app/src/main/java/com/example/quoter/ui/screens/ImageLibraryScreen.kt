package com.example.quoter.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi // Import for Pager
import androidx.compose.foundation.border // Import for border
import androidx.compose.foundation.gestures.detectDragGestures // Import for swipe down
import androidx.compose.foundation.gestures.detectTapGestures // Import for tap gestures
import androidx.compose.foundation.layout.offset // Import for offset modifier
import androidx.compose.foundation.pager.HorizontalPager // Import Pager
import androidx.compose.foundation.pager.rememberPagerState // Import Pager state
import androidx.compose.material3.AlertDialog // Keep AlertDialog import
import androidx.compose.material3.Button // Keep Button import
import androidx.compose.material3.Card // Keep Card import
import androidx.compose.material3.ExperimentalMaterial3Api // Keep Material3 import
import androidx.compose.material3.Icon // Keep Icon import
import androidx.compose.material3.IconButton // Keep IconButton import
import androidx.compose.material3.MaterialTheme // Keep MaterialTheme import
import androidx.compose.material3.Scaffold // Keep Scaffold import
import androidx.compose.material3.Text // Keep Text import
import androidx.compose.material3.TopAppBar // Keep TopAppBar import
import androidx.compose.runtime.Composable // Keep Composable import
import androidx.compose.runtime.LaunchedEffect // Import LaunchedEffect
import androidx.compose.runtime.collectAsState // Keep collectAsState import
import androidx.compose.runtime.getValue // Keep getValue import
import androidx.compose.runtime.mutableStateOf // Keep mutableStateOf import
import androidx.compose.runtime.remember // Keep remember import
import androidx.compose.runtime.setValue // Keep setValue import
import androidx.compose.ui.input.pointer.pointerInput // Import for pointer input
import androidx.compose.ui.unit.IntOffset // Import for offset
import kotlin.math.roundToInt // Import for rounding offset
import androidx.compose.ui.window.Dialog // Import Dialog
import androidx.compose.ui.window.DialogProperties // Import DialogProperties
import androidx.compose.foundation.combinedClickable // Import combinedClickable
import android.util.Log // Import Log
import androidx.compose.material3.ExtendedFloatingActionButton // Import Extended FAB
import androidx.compose.material3.FloatingActionButton // Import standard FAB
import androidx.compose.material3.FloatingActionButtonDefaults // Import FAB defaults for shape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.FabPosition

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.Download // Ensure this import exists or add it
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.quoter.QuoteViewModel
import com.example.quoter.data.GeneratedImage
import java.io.File
import android.content.Intent
import android.app.WallpaperManager
import android.content.ComponentName
import com.example.quoter.QuoteWallpaperService
import com.example.quoter.ui.theme.PrimaryBlue // Import the color

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class) // Add ExperimentalFoundationApi
@Composable
fun ImageLibraryScreen(
    navController: NavController,
    viewModel: QuoteViewModel
) {
    val generatedImages by viewModel.generatedImages.collectAsState()
    val context = LocalContext.current
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var isInSelectionMode by remember { mutableStateOf(false) }
    val selectedImagesCount = generatedImages.count { it.isSelectedForRotation }

    LaunchedEffect(selectedImagesCount) {
        if (selectedImagesCount == 0 && isInSelectionMode) { // Only exit if count becomes 0 *while* in selection mode
            isInSelectionMode = false
        }
    }

    var fullscreenImageIndex by remember { mutableStateOf<Int?>(null) }

    // Use Box to allow overlaying the FAB
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            // No topBar
            // Define the FAB slot and position
            floatingActionButton = {
                // Use standard FloatingActionButton for "Set Live Wallpaper"
                FloatingActionButton(
                    onClick = {
                        // Check enabled condition inside onClick
                        if (generatedImages.any { it.isSelectedForRotation }) {
                            val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
                            intent.putExtra(
                                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                                ComponentName(context, QuoteWallpaperService::class.java)
                            )
                            if (intent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(intent)
                            } else {
                                println("Live Wallpaper Chooser not found.")
                            }
                        } else {
                            // Show snackbar if no image is selected
                            viewModel.showSnackbar("Please select an image to set as wallpaper.")
                        }
                    },
                    shape = FloatingActionButtonDefaults.shape, // Default FAB shape
                    containerColor = PrimaryBlue // Set background color to dark blue
                ) {
                    // Manually create the Row content inside the FAB
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp) // Padding inside FAB
                    ) {
                        Icon(Icons.Filled.Wallpaper, contentDescription = null)
                        Spacer(Modifier.width(8.dp)) // Space between icon and text
                        Text("Set Live Wallpaper")
                    }
                }
            },
            floatingActionButtonPosition = FabPosition.Center // Center the FAB
        ) { paddingValues -> // This padding now accounts for the FAB and bottom nav
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    // Apply padding calculated by Scaffold
                    .padding(paddingValues)
                    // Add horizontal padding
                    .padding(horizontal = 8.dp)
            ) {
                if (generatedImages.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                        Text("No images generated yet.")
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        // Adjust top padding since TopAppBar is removed
                        contentPadding = PaddingValues(top = 16.dp, bottom = 8.dp) // Increased top padding
                    ) {
                        items(generatedImages.size, key = { index -> generatedImages[index].id }) { index ->
                            val image = generatedImages[index]
                            ImageLibraryItem(
                                image = image,
                                viewModel = viewModel,
                                isInSelectionMode = isInSelectionMode,
                                onToggleSelection = {
                                    viewModel.toggleImageSelection(image)
                                },
                                onStartSelectionMode = {
                                    if (!isInSelectionMode) { // Only start selection mode if not already in it
                                        isInSelectionMode = true
                                        viewModel.toggleImageSelection(image) // Select the long-pressed item
                                    }
                                },
                                onClick = {
                                    if (!isInSelectionMode) {
                                        fullscreenImageIndex = index
                                    } else {
                                        // If in selection mode, treat tap as toggle
                                        viewModel.toggleImageSelection(image)
                                    }
                                }
                            )
                        }
                    }
                }
                // Remove the FAB/Box from here
                // Box( ... ) { FloatingActionButton(...) }
            } // End Column

            if (showDeleteConfirmationDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmationDialog = false },
                    title = { Text("Confirm Deletion") },
                    text = { Text("Are you sure you want to delete the $selectedImagesCount selected image(s)? This action cannot be undone.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.deleteSelectedImages()
                                showDeleteConfirmationDialog = false
                            }
                        ) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showDeleteConfirmationDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

        } // End Scaffold

        // Conditionally display Export/Delete buttons (remain overlaid via Box)
        if (isInSelectionMode && selectedImagesCount > 0) {
            // --- Add Logging ---
            // Log whenever this block is composed (condition is true)
            Log.d("ImageLibraryScreen", "Selection mode active, count=$selectedImagesCount. Displaying FABs.")
            // --- End Logging ---

            // Export Floating Action Button (Top Left) - Add Border
            FloatingActionButton(
                onClick = {
                    viewModel.exportSelectedImagesToGallery(context) // Call ViewModel function
                },
                modifier = Modifier
                    .align(Alignment.TopStart) // Align to top left
                    .padding(16.dp) // Padding from the edges
                    // Add a white border
                    .border(BorderStroke(2.dp, Color.White), CircleShape),
                containerColor = MaterialTheme.colorScheme.primary, // Use primary color
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(
                    Icons.Filled.Download, // Change from Default to Filled
                    contentDescription = "Export Selected Images"
                )
            }

            // Delete Floating Action Button (Top Right)
            FloatingActionButton(
                onClick = { showDeleteConfirmationDialog = true },
                modifier = Modifier
                    .align(Alignment.TopEnd) // Align to top right
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
                shape = CircleShape
            ) {
                Icon(
                    Icons.Filled.DeleteSweep, // Ensure this is also Filled if needed
                    contentDescription = "Delete Selected Images"
                )
            }
        } // End conditional button display

    } // End Box

    fullscreenImageIndex?.let { startIndex ->
        FullscreenImageViewer(
            images = generatedImages,
            startIndex = startIndex,
            onDismiss = { fullscreenImageIndex = null }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FullscreenImageViewer(
    images: List<GeneratedImage>,
    startIndex: Int,
    onDismiss: () -> Unit
) {
    val pagerState = rememberPagerState(initialPage = startIndex) {
        images.size
    }
    val context = LocalContext.current

    // State for swipe-to-dismiss gesture
    var offsetY by remember { mutableStateOf(0f) }
    var initialOffsetY by remember { mutableStateOf(0f) }
    val dismissThreshold = 300f // Pixels to swipe down to dismiss

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false, // Allow dialog to fill screen
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f)) // Semi-transparent background
                .offset { IntOffset(0, offsetY.roundToInt()) } // Apply vertical offset for swipe
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { initialOffsetY = offsetY },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            offsetY += dragAmount.y
                        },
                        onDragEnd = {
                            if (offsetY > dismissThreshold) {
                                onDismiss() // Dismiss if swiped down enough
                            } else {
                                // Animate back to original position if not dismissed (optional)
                                // For simplicity, just reset offset here
                                offsetY = 0f
                            }
                        }
                    )
                }
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { pageIndex ->
                val image = images[pageIndex]
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(File(image.filePath))
                        .crossfade(true)
                        .build(),
                    contentDescription = "Fullscreen image: ${image.prompt ?: ""}",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp), // Add some padding around the image
                    contentScale = ContentScale.Fit // Fit the image within the bounds
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class) // Keep OptIns
@Composable
fun ImageLibraryItem(
    image: GeneratedImage,
    viewModel: QuoteViewModel,
    isInSelectionMode: Boolean,
    onToggleSelection: () -> Unit,
    onStartSelectionMode: () -> Unit,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val isSelected = image.isSelectedForRotation

    Card(
        modifier = Modifier
            .aspectRatio(9f / 16f)
            // Replace pointerInput with combinedClickable
            .combinedClickable(
                onClick = {
                    if (isInSelectionMode) {
                        // In selection mode, tap toggles the item
                        onToggleSelection()
                    } else {
                        // Not in selection mode, tap opens fullscreen
                        onClick()
                    }
                },
                onLongClick = {
                    if (!isInSelectionMode) {
                        // Long press only starts selection mode if not already in it
                        onStartSelectionMode()
                    }
                    // Optional: Define behavior for long press *during* selection mode.
                    // Currently does nothing, which seems reasonable.
                }
                // No need for onPress, onDoubleTap for this use case
            )
            .then(
                // Apply border modifier (unchanged)
                if (isSelected && isInSelectionMode) {
                    Modifier.border(
                        width = 3.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.medium
                    )
                } else {
                    Modifier
                }
            )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current) // Use LocalContext.current
                    .data(File(image.filePath))
                    .crossfade(true)
                    .build(),
                contentDescription = "Generated background: ${image.prompt ?: ""}", // Add comma here
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                onError = { /* Optional: Show placeholder */ },
                onLoading = { /* Optional: Show placeholder */ }
            )
            // Old selection icon box remains removed
        }
    }
}
