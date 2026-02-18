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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material3.BottomSheetDefaults
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

    val equippedHat = storeItems.find { it.id == equippedMap[ItemCategories.HAT] }
    val equippedGlasses = storeItems.find { it.id == equippedMap[ItemCategories.GLASSES] }
    val equippedWall = storeItems.find { it.id == equippedMap[ItemCategories.WALL_SKIN] }
    val equippedPetSkin = storeItems.find { it.id == equippedMap[ItemCategories.PET_SKIN] }
    val equippedScarf = storeItems.find { it.id == equippedMap[ItemCategories.SCARF] }
    val equippedSweater = storeItems.find { it.id == equippedMap[ItemCategories.SWEATER] }

    Box(modifier = Modifier.fillMaxSize()) {
        // --- WARSTWA 1: TŁO (WALL_SKIN) ---
        if (equippedWall != null) {
            coil.compose.AsyncImage(
                model = "file:///android_asset/images/${equippedWall.imageResName}.png",
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                filterQuality = androidx.compose.ui.graphics.FilterQuality.None
            )
        } else {
            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant))
        }

        // --- WARSTWA 2: ZWIERZAK I UBRANIA ---
        // Box nakłada elementy jeden na drugi w kolejności kodu
        Box(
            modifier = Modifier.align(Alignment.Center).size(400.dp),
            contentAlignment = Alignment.Center
        ) {
            // 1. Ciało (Baza)
            if (equippedPetSkin != null) {
                coil.compose.AsyncImage(
                    model = "file:///android_asset/images/${equippedPetSkin.imageResName}.png",
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    filterQuality = androidx.compose.ui.graphics.FilterQuality.None
                )
            } else {
                // Jeśli nie ma skina, pokazujemy ikonę lub domyślny plik
                Icon(Icons.Outlined.Pets, null, modifier = Modifier.size(100.dp), tint = MaterialTheme.colorScheme.outline)
            }

            // 2. Sweter (Pod szalikiem, nad ciałem)
            equippedSweater?.let { item ->
                coil.compose.AsyncImage(
                    model = "file:///android_asset/images/${item.imageResName}.png",
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    filterQuality = androidx.compose.ui.graphics.FilterQuality.None
                )
            }

            // 3. Szalik
            equippedScarf?.let { item ->
                coil.compose.AsyncImage(
                    model = "file:///android_asset/images/${item.imageResName}.png",
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    filterQuality = androidx.compose.ui.graphics.FilterQuality.None
                )
            }

            // 4. Okulary
            equippedGlasses?.let { item ->
                coil.compose.AsyncImage(
                    model = "file:///android_asset/images/${item.imageResName}.png",
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    filterQuality = androidx.compose.ui.graphics.FilterQuality.None
                )
            }

            // 5. Czapka (Na samym wierzchu)
            equippedHat?.let { item ->
                coil.compose.AsyncImage(
                    model = "file:///android_asset/images/${item.imageResName}.png",
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    filterQuality = androidx.compose.ui.graphics.FilterQuality.None
                )
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
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
                .fillMaxWidth()
        ) {
            Text(
                "Twoja Szafa",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(16.dp))

            if (ownedItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Nic tu jeszcze nie ma. Kup coś w sklepie!")
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 90.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
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
}

@Composable
fun WardrobeItemCard(item: StoreItem, isEquipped: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isEquipped) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        border = if (isEquipped) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = Modifier.size(90.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            coil.compose.AsyncImage(
                model = "file:///android_asset/images/${item.imageResName}.png",
                contentDescription = null,
                modifier = Modifier.size(70.dp).padding(8.dp),
                filterQuality = androidx.compose.ui.graphics.FilterQuality.None
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