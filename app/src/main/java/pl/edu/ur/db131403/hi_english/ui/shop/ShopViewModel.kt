package pl.edu.ur.db131403.hi_english.ui.shop

import androidx.lifecycle.ViewModel
import pl.edu.ur.db131403.hi_english.data.repository.ShopRepository

class ShopViewModel(
    private val repo: ShopRepository = ShopRepository()
) : ViewModel() {

    val items = repo.getShopItems()
}