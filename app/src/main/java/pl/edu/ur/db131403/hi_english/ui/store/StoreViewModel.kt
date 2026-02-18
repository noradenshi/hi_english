package pl.edu.ur.db131403.hi_english.ui.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import pl.edu.ur.db131403.hi_english.data.local.StoreDao
import pl.edu.ur.db131403.hi_english.data.model.EquippedItem
import pl.edu.ur.db131403.hi_english.data.model.ItemCategories
import pl.edu.ur.db131403.hi_english.data.model.StoreItem
import pl.edu.ur.db131403.hi_english.data.repository.ProfileRepository

class StoreViewModel(
    private val storeDao: StoreDao,
    private val profileRepository: ProfileRepository
) : ViewModel() {
    val storeItems = storeDao.getAllStoreItems()
    val equippedItems = storeDao.getEquippedItems().map { list ->
        list.associate { it.category to it.itemId }
    }

    init {
        checkAndSeedDatabase()
    }

    private fun checkAndSeedDatabase() {
        viewModelScope.launch {
            val currentItems = storeDao.getAllStoreItems().firstOrNull() ?: emptyList()

            if (currentItems.isEmpty()) {
                seedDatabase()
            }
        }
    }

    suspend fun seedDatabase() {
        val freePet = StoreItem(
            name = "Brązowy Pies",
            category = ItemCategories.PET_SKIN,
            price = 0,
            isPurchased = true,
            imageResName = "dog_brown"
        )

        val initialItems = listOf(
            freePet,
            StoreItem(
                name = "Łaciaty Pies",
                category = ItemCategories.PET_SKIN,
                price = 400,
                imageResName = "dog_dotted"
            ),
            StoreItem(
                name = "Szary Kot",
                category = ItemCategories.PET_SKIN,
                price = 300,
                imageResName = "cat_gray"
            ),
            StoreItem(
                name = "Rudy Kot",
                category = ItemCategories.PET_SKIN,
                price = 300,
                imageResName = "cat_orange"
            ),

            StoreItem(
                name = "Żółta Czapka",
                category = ItemCategories.HAT,
                price = 100,
                imageResName = "hat_yellow"
            ),
            StoreItem(
                name = "Okulary",
                category = ItemCategories.GLASSES,
                price = 150,
                imageResName = "sunglasses"
            ),

            StoreItem(
                name = "Niebieski Szalik",
                category = ItemCategories.SCARF,
                price = 80,
                imageResName = "scarf_blue"
            ),
            StoreItem(
                name = "Zielony Szalik",
                category = ItemCategories.SCARF,
                price = 80,
                imageResName = "scarf_green"
            ),
            StoreItem(
                name = "Fioletowy Szalik",
                category = ItemCategories.SCARF,
                price = 80,
                imageResName = "scarf_purple"
            ),

            StoreItem(
                name = "Niebieski Sweter",
                category = ItemCategories.SWEATER,
                price = 200,
                imageResName = "sweater_blue"
            ),
            StoreItem(
                name = "Zielony Sweter",
                category = ItemCategories.SWEATER,
                price = 200,
                imageResName = "sweater_green"
            ),
            StoreItem(
                name = "Fioletowy Sweter",
                category = ItemCategories.SWEATER,
                price = 200,
                imageResName = "sweater_purple"
            ),

            // TODO WALL_SKIN

        )
        storeDao.insertInitialItems(initialItems)

        storeDao.equipItem(EquippedItem(freePet.category, 1))
    }

    fun handleItemClick(item: StoreItem, isEquipped: Boolean) {
        viewModelScope.launch {
            if (!item.isPurchased) {
                buyItem(item)
            } else {
                if (isEquipped) {
                    // Cannot unequip PET_SKIN
                    if (item.category == ItemCategories.PET_SKIN) {
                        return@launch
                    }

                    storeDao.unequipCategory(item.category)
                } else {
                    storeDao.equipItem(EquippedItem(item.category, item.id))
                }
            }
        }
    }

    private suspend fun buyItem(item: StoreItem) {
        val currentPoints = profileRepository.getCurrentPoints()
        if (currentPoints >= item.price) {
            profileRepository.addPoints(-item.price)
            storeDao.updateItem(item.copy(isPurchased = true))
        }
    }

    fun sellItem(item: StoreItem) {
        viewModelScope.launch {
            if (!item.isPurchased) return@launch

            storeDao.updateItem(item.copy(isPurchased = false))
            storeDao.unequipCategory(item.category)
            profileRepository.debugUpdatePoints(profileRepository.getCurrentPoints() + item.price)
        }
    }
}

class StoreViewModelFactory(
    private val storeDao: StoreDao,
    private val profileRepository: ProfileRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StoreViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StoreViewModel(storeDao, profileRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}