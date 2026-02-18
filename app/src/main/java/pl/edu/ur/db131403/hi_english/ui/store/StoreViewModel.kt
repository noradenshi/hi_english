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

    // Łączymy Flow z przedmiotami i Flow z ekwipunkiem
    val storeItems = storeDao.getAllStoreItems()
    val equippedItems = storeDao.getEquippedItems().map { list ->
        list.associate { it.category to it.itemId }
    }

    init {
        checkAndSeedDatabase()
    }

    private fun checkAndSeedDatabase() {
        viewModelScope.launch {
            // Pobieramy listę raz, żeby sprawdzić czy jest pusta
            val currentItems = storeDao.getAllStoreItems().firstOrNull() ?: emptyList()

            if (currentItems.isEmpty()) {
                seedDatabase()
            }
        }
    }

    suspend fun seedDatabase() {
        val initialItems = listOf(
            // CZAPKI
            StoreItem(
                name = "Czerwona Czapka",
                category = ItemCategories.HAT,
                price = 50,
                imageResName = "hat_red"
            ),
            StoreItem(
                name = "Czapka Zimowa",
                category = ItemCategories.HAT,
                price = 120,
                imageResName = "hat_winter"
            ),

            // OKULARY
            StoreItem(
                name = "Okulary Przeciwsłoneczne",
                category = ItemCategories.GLASSES,
                price = 80,
                imageResName = "glasses_sun"
            ),
            StoreItem(
                name = "Monokl",
                category = ItemCategories.GLASSES,
                price = 300,
                imageResName = "glasses_monocle"
            ),

            // SKÓRKI ZWIERZAKA
            StoreItem(
                name = "Złoty Futro",
                category = ItemCategories.PET_SKIN,
                price = 1000,
                imageResName = "skin_gold"
            ),
            StoreItem(
                name = "Panda",
                category = ItemCategories.PET_SKIN,
                price = 500,
                imageResName = "skin_panda"
            ),

            // ŚCIANY / TŁO
            StoreItem(
                name = "Tapeta w Kwiaty",
                category = ItemCategories.WALL_SKIN,
                price = 200,
                imageResName = "wall_flowers"
            ),
            StoreItem(
                name = "Ciemny Las",
                category = ItemCategories.WALL_SKIN,
                price = 250,
                imageResName = "wall_forest"
            )
        )
        storeDao.insertInitialItems(initialItems)
    }

    fun handleItemClick(item: StoreItem, isEquipped: Boolean) {
        viewModelScope.launch {
            if (!item.isPurchased) {
                buyItem(item)
            } else {
                if (isEquipped) {
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
            storeDao.updateItemPurchase(item.copy(isPurchased = true))
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