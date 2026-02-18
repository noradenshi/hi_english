package pl.edu.ur.db131403.hi_english.ui.store

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pl.edu.ur.db131403.hi_english.data.model.StoreItem
import androidx.lifecycle.viewmodel.compose.viewModel
import pl.edu.ur.db131403.hi_english.data.model.ItemCategories

@Composable
fun StorePage(
    items: List<StoreItem>,
    points: Int,
    onBuy: (StoreItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf("all") }

    val categories = remember {
        listOf("all" to "Wszystko") + ItemCategories.all
    }

    Column(modifier = modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = categories.indexOfFirst { it.first == selectedTab }.coerceAtLeast(0),
            edgePadding = 8.dp,
            containerColor = Color.Transparent,
            divider = {},
            indicator = {}
        ) {
            categories.forEach { (id, label) ->
                val isSelected = selectedTab == id

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(horizontal = 4.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable { selectedTab = id }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        val filteredItems = if (selectedTab == "all") items else items.filter { it.category == selectedTab }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(items = filteredItems, key = { it.id }) { item ->
                StoreItemCard(
                    item = item,
                    isOwned = item.isPurchased,
                    canAfford = points >= item.price,
                    onBuy = { onBuy(item) }
                )
            }
        }
    }
}

@Composable
fun StoreItemCard(
    item: StoreItem,
    isOwned: Boolean,
    canAfford: Boolean,
    onBuy: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    // Dynamiczne pobieranie ID zasobu na podstawie nazwy String z bazy
    val imageResId = remember(item.imageResName) {
        val resId = context.resources.getIdentifier(
            item.imageResName,
            "drawable",
            context.packageName
        )
        if (resId != 0) resId else android.R.drawable.ic_menu_report_image // Fallback na ikonę systemową
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOwned) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = item.name,
                    modifier = Modifier.fillMaxSize(0.6f),
                    contentScale = ContentScale.Fit
                )

                if (isOwned) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Owned",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).size(24.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(item.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isOwned) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.MonetizationOn, null, Modifier.size(16.dp), MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(4.dp))
                        Text(item.price.toString(), fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = onBuy,
                    enabled = !isOwned && canAfford,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Text(if (isOwned) "POSIADANE" else "KUP")
                }
            }
        }
    }
}