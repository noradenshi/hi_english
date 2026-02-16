package pl.edu.ur.db131403.hi_english.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {
    // Get all A1 words
    @Query("SELECT * FROM words WHERE cefr = 'A1' ORDER BY word ASC")
    fun getAllA1Words(): Flow<List<WordEntity>>

    // Get only words ready for the "Kids Mode" (those with translations)
    @Query("SELECT * FROM words WHERE translation_pl != '' ORDER BY word ASC")
    fun getKidsDictionary(): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE word LIKE :query OR translation_pl LIKE :query ORDER BY word ASC")
    fun searchWords(query: String): Flow<List<WordEntity>>
}