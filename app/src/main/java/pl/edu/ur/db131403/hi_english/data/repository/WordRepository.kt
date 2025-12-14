package pl.edu.ur.db131403.hi_english.data.repository

import pl.edu.ur.db131403.hi_english.data.local.WordDao
import pl.edu.ur.db131403.hi_english.data.local.WordEntity

class WordRepository(
    private val dao: WordDao
) {
    fun getAllWords() = dao.getAllWords()

    fun getWordsByTag(tag: String) = dao.getWordsByTag(tag)

    fun searchWords(query: String) = dao.searchWords(query)

    suspend fun insert(word: WordEntity) {
        dao.insertWord(word)
    }
}

