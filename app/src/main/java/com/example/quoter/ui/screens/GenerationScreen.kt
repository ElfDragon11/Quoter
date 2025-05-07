package com.example.quoter.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape // Import for rounded corners
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check // Import Check icon
import androidx.compose.material.icons.filled.Close // Import Close icon
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip // Import for clipping shape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.quoter.QuoteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerationScreen(
    viewModel: QuoteViewModel
) {
    val customizationState by viewModel.customizationState.collectAsState()
    var quoteText by remember { mutableStateOf("") }
    val isGenerating = viewModel.isGeneratingImage
    val error = viewModel.generationError

    // State for dropdown expansion
    var expandedLocation by remember { mutableStateOf(false) }
    var expandedScene by remember { mutableStateOf(false) }
    var expandedStyle by remember { mutableStateOf(false) }
    var expandedFontStyle by remember { mutableStateOf(false) } // Updated for font styles

    // State for custom input
    var editingDropdown by remember { mutableStateOf<String?>(null) } // "location", "scene", "style", or null
    var customInputValue by remember { mutableStateOf("") }

    // Initialize local states from ViewModel state
    var fontSize by remember { mutableStateOf(customizationState.fontSize) }

    LaunchedEffect(customizationState.fontSize) {
        fontSize = customizationState.fontSize
    }

    val locations = remember { listOf("Mountains", "Forest", "Ocean", "Space", "Beach", "City", "Custom...") }
    val scenes = remember { listOf("Natural", "Sunset", "Futuristic", "Modern", "Cozy", "Minimalist", "Custom...") }
    val styles = remember { listOf("Photorealistic", "Oil Painting", "Watercolor", "Cartoon", "Cyberpunk", "Custom...") }
    val fontStyles = remember { listOf("Bold", "Artistic", "Cursive", "Custom...") } // Updated font styles

    val dropdownBackgroundColor = MaterialTheme.colorScheme.primary
    val dropdownShape = RoundedCornerShape(8.dp)

    // Helper function to handle dropdown item clicks (ensure fontStyle updates local state)
    val handleDropdownSelection = { type: String, value: String, setExpanded: (Boolean) -> Unit ->
        if (value == "Custom...") {
            editingDropdown = type
            customInputValue = when (type) {
                "location" -> customizationState.location
                "scene" -> customizationState.scene
                "style" -> customizationState.style
                "fontStyle" -> customizationState.fontStyle // Use ViewModel state here
                else -> ""
            }
            setExpanded(false)
        } else {
            when (type) {
                "location" -> viewModel.updateLocation(value)
                "scene" -> viewModel.updateScene(value)
                "style" -> viewModel.updateStyle(value)
                "fontStyle" -> viewModel.updateFontStyle(value) // Just update ViewModel
            }
            setExpanded(false)
        }
    }

    // Helper function to render dropdown or custom input
    @Composable
    fun DropdownOrCustomInput(
        type: String,
        label: String,
        currentValue: String,
        expanded: Boolean,
        onExpandedChange: (Boolean) -> Unit,
        options: List<String>,
        onOptionSelected: (String) -> Unit
    ) {
        if (editingDropdown == type) {
            // Custom Input View
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(dropdownShape)
                    .background(dropdownBackgroundColor)
                    .padding(horizontal = 8.dp, vertical = 4.dp), // Padding inside the row
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween // Space out elements
            ) {
                TextField(
                    value = customInputValue,
                    onValueChange = { customInputValue = it },
                    modifier = Modifier.weight(1f).padding(end = 8.dp), // Take available space, add padding
                    label = { Text("Enter Custom $label") },
                    singleLine = true,
                    colors = ExposedDropdownMenuDefaults.textFieldColors( // Reuse dropdown colors for consistency
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = MaterialTheme.colorScheme.onPrimary, // Underline when focused
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedLabelColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                        unfocusedLabelColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                        focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                        unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                        cursorColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
                // Confirm Button (ensure fontStyle updates local state)
                IconButton(onClick = {
                    when (type) {
                        "location" -> viewModel.updateLocation(customInputValue)
                        "scene" -> viewModel.updateScene(customInputValue)
                        "style" -> viewModel.updateStyle(customInputValue)
                        "fontStyle" -> viewModel.updateFontStyle(customInputValue) // Just update ViewModel
                    }
                    editingDropdown = null // Exit editing mode
                }) {
                    Icon(Icons.Filled.Check, contentDescription = "Confirm Custom $label", tint = MaterialTheme.colorScheme.onPrimary)
                }
                // Cancel Button
                IconButton(onClick = {
                    editingDropdown = null // Exit editing mode without saving
                }) {
                    Icon(Icons.Filled.Close, contentDescription = "Cancel Custom $label", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        } else {
            // Standard Dropdown View
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = onExpandedChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(dropdownShape)
                    .background(dropdownBackgroundColor)
            ) {
                TextField(
                    value = currentValue,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(label) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = dropdownShape,
                    colors = ExposedDropdownMenuDefaults.textFieldColors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTrailingIconColor = MaterialTheme.colorScheme.onPrimary,
                        unfocusedTrailingIconColor = MaterialTheme.colorScheme.onPrimary,
                        focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                        unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                        focusedLabelColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f), // Dim label slightly
                        unfocusedLabelColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                    )
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { onExpandedChange(false) }
                ) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = { onOptionSelected(option) }
                        )
                    }
                }
            }
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Use theme background
            .padding(16.dp), // Apply overall padding
        verticalArrangement = Arrangement.spacedBy(16.dp) // Space between elements
    ) {

        // Restyled Quote Text Field
        TextField(
            value = quoteText,
            onValueChange = { quoteText = it },
            placeholder = { Text("Enter a quote") }, // Use placeholder
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp) // Add top padding here
                .clip(RoundedCornerShape(8.dp)), // Rounded corners
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant, // Match image background
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor = Color.Transparent, // No indicator line
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            maxLines = 5,
            enabled = !isGenerating
        )

        // Location Dropdown or Custom Input
        DropdownOrCustomInput(
            type = "location",
            label = "Location",
            currentValue = customizationState.location,
            expanded = expandedLocation,
            onExpandedChange = { expandedLocation = it },
            options = locations,
            onOptionSelected = { handleDropdownSelection("location", it) { expandedLocation = false } }
        )

        // Scene Dropdown or Custom Input
        DropdownOrCustomInput(
            type = "scene",
            label = "Scene",
            currentValue = customizationState.scene,
            expanded = expandedScene,
            onExpandedChange = { expandedScene = it },
            options = scenes,
            onOptionSelected = { handleDropdownSelection("scene", it) { expandedScene = false } }
        )

        // Style Dropdown or Custom Input
        DropdownOrCustomInput(
            type = "style",
            label = "Style",
            currentValue = customizationState.style,
            expanded = expandedStyle,
            onExpandedChange = { expandedStyle = it },
            options = styles,
            onOptionSelected = { handleDropdownSelection("style", it) { expandedStyle = false } }
        )

        // Font Style Dropdown or Custom Input
        DropdownOrCustomInput(
            type = "fontStyle",
            label = "Font Style",
            currentValue = customizationState.fontStyle, // Use ViewModel state directly
            expanded = expandedFontStyle,
            onExpandedChange = { expandedFontStyle = it },
            options = fontStyles,
            onOptionSelected = { handleDropdownSelection("fontStyle", it) { expandedFontStyle = false } }
        )

        // Font Size Slider (Updates local state, saves on finish)
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Font Size: ${fontSize.toInt()} pt", style = MaterialTheme.typography.bodyMedium)
            Slider(
                value = fontSize,
                onValueChange = { fontSize = it }, // Update local state continuously
                onValueChangeFinished = { // Save when slider interaction finishes
                    viewModel.updateFontSize(fontSize)
                },
                valueRange = 8f..72f,
                steps = 0,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Keep Generate Button (Update onClick)
        Button(
            onClick = {
                // Pass selectedFontStyle and fontSize to the ViewModel function
                viewModel.generateAndSaveImage(quoteText, customizationState, customizationState.fontStyle, fontSize)
            },
            enabled = quoteText.isNotBlank() && !isGenerating,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isGenerating) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
                Text("Generating...")
            } else {
                Text("Generate Image")
            }
        }

        // Keep Error Display
        if (error != null) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                Icon(Icons.Filled.Error, contentDescription = "Error", tint = MaterialTheme.colorScheme.error)
                Spacer(Modifier.width(4.dp))
                Text(error, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}