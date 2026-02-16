package pl.edu.ur.db131403.hi_english.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int?,

    val word: String,

    val pos: String?,

    val cefr: String?,

    @ColumnInfo(name = "translation_pl")
    val translationPl: String?,

    val description: String?,

    @ColumnInfo(name = "image_res")
    val imageRes: String?
)