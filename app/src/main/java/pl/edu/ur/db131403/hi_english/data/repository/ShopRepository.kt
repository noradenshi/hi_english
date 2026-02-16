package pl.edu.ur.db131403.hi_english.data.repository

import pl.edu.ur.db131403.hi_english.R
import pl.edu.ur.db131403.hi_english.data.model.ShopItem

class ShopRepository {
    fun getShopItems(): List<ShopItem> = listOf(
        ShopItem("item_blue_cap", "Czapka", 100, "hats", R.drawable.rounded_logo_dev_24),
        ShopItem("item_modern_room", "Nowoczesny Pokój", 500, "rooms", R.drawable.rounded_logo_dev_24),
        ShopItem("item_sunglasses", "Okulary", 150, "accessories", R.drawable.rounded_logo_dev_24)
    )
}