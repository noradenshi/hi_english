package pl.edu.ur.db131403.hi_english.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pl.edu.ur.db131403.hi_english.data.model.EquippedItems

@Composable
fun HouseScreen(
    inventory: List<String>,
    equipped: EquippedItems,
    onEquip: (String, String) -> Unit // itemId, category
) {
    var showCustomizer by remember { mutableStateOf(false) }

    // Map your category IDs to actual Icons for visualization
    val petIcon = Icons.Outlined.Face // The "Pet"
    val hatIcon = Icons.Outlined.School // Represents the "Czapka"
    val roomColor = if (equipped.background == "item_modern_room")
        MaterialTheme.colorScheme.tertiaryContainer
    else
        MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(roomColor) // Background color changes based on "Room" selection
    ) {
        // --- The "Pet" Layers ---
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            // Base Pet
            Icon(
                imageVector = petIcon,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                tint = MaterialTheme.colorScheme.primary
            )

            // Layered Hat
            if (equipped.hat == "item_blue_cap") {
                Icon(
                    imageVector = hatIcon,
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .align(Alignment.TopCenter)
                        .offset(y = (-20).dp), // Position it on the "head"
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        }

        // --- UI Overlay ---
        Column(modifier = Modifier.padding(24.dp).align(Alignment.TopStart)) {
            Text("Twój Dom", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            StatusBar(label = "Szczęście", value = 0.8f, color = Color(0xFF4CAF50))
            StatusBar(label = "Energia", value = 0.4f, color = Color(0xFFFF9800))
        }

        FloatingActionButton(
            onClick = { showCustomizer = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp)
        ) {
            Icon(Icons.Outlined.Checkroom, "Customize")
        }

        if (showCustomizer) {
            CustomizerSheet(
                inventory = inventory,
                onEquip = onEquip,
                onDismiss = { showCustomizer = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizerSheet(
    inventory: List<String>,
    onEquip: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp).fillMaxHeight(0.4f)) {
            Text("Twoje przedmioty", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(inventory) { itemId ->
                    // Determine category based on ID (simple logic for now)
                    val category = if (itemId.contains("cap")) "hats" else "rooms"

                    Surface(
                        onClick = { onEquip(itemId, category) },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.size(80.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(itemId.takeLast(3).uppercase()) // Show short ID for now
                        }
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun StatusBar(label: String, value: Float, color: Color) {
    Column(modifier = Modifier.width(120.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        LinearProgressIndicator(
            progress = { value },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
            color = color,
            trackColor = color.copy(alpha = 0.2f),
        )
    }
}
