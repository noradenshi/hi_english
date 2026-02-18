package pl.edu.ur.db131403.hi_english.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import pl.edu.ur.db131403.hi_english.data.model.EquippedItem
import pl.edu.ur.db131403.hi_english.data.model.StoreItem

@Dao
interface StoreDao {
    // --- Zarządzanie Sklepem ---
    @Query("SELECT * FROM store_items")
    fun getAllStoreItems(): Flow<List<StoreItem>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertInitialItems(items: List<StoreItem>)

    @Update
    suspend fun updateItemPurchase(item: StoreItem)

    // --- Zarządzanie Ekwipunkiem ---
    @Query("SELECT * FROM equipped_items")
    fun getEquippedItems(): Flow<List<EquippedItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun equipItem(equippedItem: EquippedItem)

    @Query("DELETE FROM equipped_items WHERE category = :category")
    suspend fun unequipCategory(category: String)

    @Query("SELECT COUNT(*) FROM store_items WHERE isPurchased = 1")
    fun getPurchasedItemsCount(): Flow<Int>
}