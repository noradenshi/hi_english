package pl.edu.ur.db131403.hi_english.data.repository

import pl.edu.ur.db131403.hi_english.data.local.WordDao
import pl.edu.ur.db131403.hi_english.data.model.WordEntity

class WordRepository(
    private val wordDao: WordDao,
    private val context: android.content.Context
) {
    fun getAllWords() = wordDao.getAllWords()

    fun getWordsByCefr(levels: List<String>) = wordDao.getWordsByCefr(levels)

    fun getVisibleWordsByCefr(levels: List<String>) = wordDao.getVisibleWordsByCefr(levels)

    fun searchWordsInLevels(query: String, levels: List<String>) =
        wordDao.searchWordsInLevels(query, levels)

    fun getAllA1Words() = wordDao.getAllA1Words()
    fun getVisibleA1Words() = wordDao.getVisibleA1Words()

    fun searchWords(query: String) = wordDao.searchWords(query)

    suspend fun checkAndInitializeDatabase() {
        val count = wordDao.getWordCount()
        if (count == 0) {
            importFromCsv()
        }
    }

    private suspend fun importFromCsv() {
        val words = mutableListOf<WordEntity>()
        context.assets.open("databases/words.csv").bufferedReader().useLines { lines ->
            lines.forEach { line ->
                val parts = line.split(";")
                if (parts.size >= 2) {
                    words.add(WordEntity(
                        id = null,
                        word = parts[1],
                        pos = parts.getOrNull(2),
                        cefr = parts.getOrNull(3),
                        translationPl = parts[4],
                        description = parts.getOrNull(5),
                        imageRes = null,
                        isVisible = true
                    ))
                }
            }
        }
        wordDao.insertAll(words)
    }

    suspend fun updateWord(word: WordEntity) = wordDao.update(word)
    suspend fun toggleVisibility(word: WordEntity) {
        println("DEBUG: Zmieniam widoczność słowa ${word.word} z ${word.isVisible} na ${!word.isVisible}")
        wordDao.update(word.copy(isVisible = !word.isVisible))
    }

    suspend fun deleteWord(word: WordEntity) {
        wordDao.delete(word)
    }

    suspend fun insertWord(word: WordEntity) {
        wordDao.insert(word)
    }
}