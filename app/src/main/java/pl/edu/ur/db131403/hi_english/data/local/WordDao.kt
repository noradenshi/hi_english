package pl.edu.ur.db131403.hi_english.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import pl.edu.ur.db131403.hi_english.data.model.WordEntity

@Dao
interface WordDao {
    @Query("SELECT * FROM words ORDER BY word ASC")
    fun getAllWords(): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE cefr IN (:levels) ORDER BY word ASC")
    fun getWordsByCefr(levels: List<String>): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE cefr IN (:levels) AND is_visible = 1 ORDER BY word ASC")
    fun getVisibleWordsByCefr(levels: List<String>): Flow<List<WordEntity>>

    @Query("""
        SELECT * FROM words 
        WHERE cefr IN (:levels) 
        AND (word LIKE :query OR translation_pl LIKE :query) 
        ORDER BY word ASC
    """)
    fun searchWordsInLevels(query: String, levels: List<String>): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE cefr = 'A1' ORDER BY word ASC")
    fun getAllA1Words(): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE cefr = 'A1' AND is_visible = 1 ORDER BY word ASC")
    fun getVisibleA1Words(): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE word LIKE :query OR translation_pl LIKE :query ORDER BY word ASC")
    fun searchWords(query: String): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE cefr = 'A1' AND word LIKE :query OR translation_pl LIKE :query ORDER BY word ASC")
    fun searchA1Words(query: String): Flow<List<WordEntity>>

    @Query("SELECT COUNT(*) FROM words")
    suspend fun getWordCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(words: List<WordEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(word: WordEntity)

    @Update
    suspend fun update(word: WordEntity)

    @Delete
    suspend fun delete(word: WordEntity)
}