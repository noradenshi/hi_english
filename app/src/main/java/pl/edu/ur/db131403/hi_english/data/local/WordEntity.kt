package pl.edu.ur.db131403.hi_english.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val word: String,
    val tag: String,
    val imageRes: Int?,          // drawable resource ID
    val polishTranslation: String,
    val descriptionPl: String,
    val exampleSentence: String
)