package pl.edu.ur.db131403.hi_english.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class EquippedItems(
    val hat: String? = null,
    val skin: String? = "default_cat",
    val background: String = "item_default_walls"
) : Parcelable