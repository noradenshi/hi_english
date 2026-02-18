package pl.edu.ur.db131403.hi_english.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "equipped_items")
data class EquippedItem(
    @PrimaryKey val category: String, // "HAT", "PET_SKIN", "GLASSES", itd.
    val itemId: Int // ID przedmiotu z tabeli StoreItem
)