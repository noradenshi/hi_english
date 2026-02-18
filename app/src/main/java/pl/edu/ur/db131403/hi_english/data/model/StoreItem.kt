package pl.edu.ur.db131403.hi_english.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "store_items")
data class StoreItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String,
    val price: Int,
    val imageResName: String,
    val isPurchased: Boolean = false
)