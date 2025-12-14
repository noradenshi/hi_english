package pl.edu.ur.db131403.hi_english.ui.shop

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import pl.edu.ur.db131403.hi_english.data.model.ShopItem
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ShopPage(modifier: Modifier = Modifier, viewModel: ShopViewModel = viewModel(), onItemClick: (ShopItem) -> Unit = {}) {
    val items = viewModel.items

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items) { item ->
            ShopItemCard(item = item, onClick = { onItemClick(item) })
        }
    }
}

@Composable
fun ShopItemCard(item: ShopItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onClick() },
        colors = CardDefaults.cardColors()
    ) {
        Column(
            modifier = Modifier.Companion
                .fillMaxSize()
                .padding(4.dp),
            horizontalAlignment = Alignment.Companion.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Image(
                painter = painterResource(id = item.imageRes),
                contentDescription = item.name,
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .weight(1f),
                contentScale = ContentScale.Companion.Crop
            )
            Spacer(modifier = Modifier.Companion.height(4.dp))
            Text(text = item.name, style = MaterialTheme.typography.bodyMedium)
            Text(text = item.price, style = MaterialTheme.typography.labelMedium)
        }
    }
}