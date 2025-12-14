package pl.edu.ur.db131403.hi_english.data.repository

import pl.edu.ur.db131403.hi_english.R
import pl.edu.ur.db131403.hi_english.data.model.ShopItem

class ShopRepository {

    // This returns static data for now.
    // Later, replace this function with a SQLite query.
    fun getShopItems(): List<ShopItem> = listOf(
        ShopItem(1, "Apple", "$2.99", R.drawable.rounded_logo_dev_24),
        ShopItem(2, "Banana", "$1.49", R.drawable.rounded_logo_dev_24),
        ShopItem(3, "Cookie", "$3.59", R.drawable.rounded_logo_dev_24),
        ShopItem(4, "Milk", "$2.19", R.drawable.rounded_logo_dev_24),
        ShopItem(5, "Eggs", "$4.99", R.drawable.rounded_logo_dev_24),
        ShopItem(6, "Juice", "$2.89", R.drawable.rounded_logo_dev_24),
    )
}