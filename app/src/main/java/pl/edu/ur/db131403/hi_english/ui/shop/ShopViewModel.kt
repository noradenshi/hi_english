package pl.edu.ur.db131403.hi_english.ui.shop

import androidx.lifecycle.ViewModel
import pl.edu.ur.db131403.hi_english.data.repository.ShopRepository
import pl.edu.ur.db131403.hi_english.data.model.ShopItem

class ShopViewModel : ViewModel() {
    private val repo = ShopRepository()
    val items: List<ShopItem> = repo.getShopItems()
}