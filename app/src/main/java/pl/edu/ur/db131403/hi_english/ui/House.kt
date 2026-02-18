package pl.edu.ur.db131403.hi_english.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.Pets
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pl.edu.ur.db131403.hi_english.data.model.ItemCategories
import pl.edu.ur.db131403.hi_english.data.model.StoreItem

@Composable
fun HouseScreen(
    equippedMap: Map<String, Int>, // [Kategoria -> ID]
    storeItems: List<StoreItem>,
    onEquipToggle: (StoreItem, Boolean) -> Unit // Akcja z ViewModelu
) {
    var showCustomizer by remember { mutableStateOf(false) }

    // Znajdujemy obiekty StoreItem na podstawie ID z mapy ekwipunku
    val equippedHat = storeItems.find { it.id == equippedMap[ItemCategories.HAT] }
    val equippedGlasses = storeItems.find { it.id == equippedMap[ItemCategories.GLASSES] }
    val equippedWall = storeItems.find { it.id == equippedMap[ItemCategories.WALL_SKIN] }
    val equippedPetSkin = storeItems.find { it.id == equippedMap[ItemCategories.PET_SKIN] }

    val context = androidx.compose.ui.platform.LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        // --- WARSTWA 1: TŁO (WALL_SKIN) ---
        if (equippedWall != null) {
            val wallResId = context.resources.getIdentifier(equippedWall.imageResName, "drawable", context.packageName)
            Image(
                painter = painterResource(id = if (wallResId != 0) wallResId else android.R.drawable.ic_menu_gallery),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant))
        }

        // --- WARSTWA 2: ZWIERZAK I AKCESORIA ---
        Box(
            modifier = Modifier.align(Alignment.Center).size(250.dp),
            contentAlignment = Alignment.Center
        ) {
            // Ciało zwierzaka (PET_SKIN lub domyślny)
            val petResId = if (equippedPetSkin != null) {
                context.resources.getIdentifier(equippedPetSkin.imageResName, "drawable", context.packageName)
            } else 0

            if (petResId != 0) {
                Image(painter = painterResource(id = petResId), contentDescription = null, modifier = Modifier.fillMaxSize())
            } else {
                Icon(Icons.Outlined.Pets, null, modifier = Modifier.fillMaxSize(0.7f), tint = MaterialTheme.colorScheme.primary)
            }

            // Okulary (Warstwa nad zwierzakiem)
            equippedGlasses?.let { glasses ->
                val resId = context.resources.getIdentifier(glasses.imageResName, "drawable", context.packageName)
                if (resId != 0) {
                    Image(
                        painter = painterResource(id = resId),
                        contentDescription = null,
                        modifier = Modifier.size(100.dp).offset(y = (-10).dp)
                    )
                }
            }

            // Czapka (Najwyższa warstwa)
            equippedHat?.let { hat ->
                val resId = context.resources.getIdentifier(hat.imageResName, "drawable", context.packageName)
                if (resId != 0) {
                    Image(
                        painter = painterResource(id = resId),
                        contentDescription = null,
                        modifier = Modifier.size(120.dp).align(Alignment.TopCenter).offset(y = (-30).dp)
                    )
                }
            }
        }

        // --- UI OVERLAY ---
        FloatingActionButton(
            onClick = { showCustomizer = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ) {
            Icon(Icons.Outlined.Checkroom, "Szafa")
        }

        if (showCustomizer) {
            CustomizerSheet(
                ownedItems = storeItems.filter { it.isPurchased },
                equippedMap = equippedMap,
                onEquipToggle = onEquipToggle,
                onDismiss = { showCustomizer = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizerSheet(
    ownedItems: List<StoreItem>,
    equippedMap: Map<String, Int>,
    onEquipToggle: (StoreItem, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Text("Twoja Szafa", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            if (ownedItems.isEmpty()) {
                Text("Nic tu jeszcze nie ma. Kup coś w sklepie!", modifier = Modifier.padding(vertical = 32.dp))
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(ownedItems) { item ->
                    val isEquipped = equippedMap[item.category] == item.id

                    WardrobeItemCard(
                        item = item,
                        isEquipped = isEquipped,
                        onClick = { onEquipToggle(item, isEquipped) }
                    )
                }
            }
        }
    }
}

@Composable
fun WardrobeItemCard(item: StoreItem, isEquipped: Boolean, onClick: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val resId = context.resources.getIdentifier(item.imageResName, "drawable", context.packageName)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isEquipped) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        border = if (isEquipped) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = Modifier.size(90.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(id = if (resId != 0) resId else android.R.drawable.ic_menu_report_image),
                contentDescription = null,
                modifier = Modifier.size(60.dp).padding(8.dp)
            )
            if (isEquipped) {
                Icon(
                    Icons.Default.CheckCircle,
                    null,
                    Modifier.align(Alignment.TopEnd).padding(4.dp).size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
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
