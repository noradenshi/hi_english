package pl.edu.ur.db131403.hi_english.ui.learn

import pl.edu.ur.db131403.hi_english.data.model.WordEntity

sealed class GameState {
    // 1. Wybór słowa do obrazka
    data class PictureQuiz(
        val imageRes: String,
        val correctAnswer: String,
        val options: List<String>
    ) : GameState()

    // 2. Łączenie par
    data class MatchingGame(
        val pairs: Map<String, String>, // English -> Polish
        val selectedLeft: String? = null,
        val selectedRight: String? = null,
        val matchedKeys: Set<String> = emptySet()
    ) : GameState()

    // 3. Układanie z liter
    data class ScrambledLetters(
        val word: String,
        val translation: String,
        val scrambledLetters: List<LetterSlot>,
        val guessedSlots: Map<Int, LetterSlot> = emptyMap()
    ) : GameState()
}

data class LetterSlot(
    val id: Int,
    val char: Char,
    val isUsed: Boolean = false
)

fun generatePictureQuiz(allWords: List<WordEntity>): GameState.PictureQuiz {
    val correct = allWords.random()
    val options = (allWords - correct).shuffled().take(3) + correct
    return GameState.PictureQuiz(
        imageRes = correct.imageRes ?: "default_img",
        correctAnswer = correct.word,
        options = options.map { it.word }.shuffled()
    )
}

fun generateMatchingGame(allWords: List<WordEntity>): GameState.MatchingGame {
    val selected = allWords.shuffled().take(4)
    val pairs = selected.associate { it.word to (it.translationPl ?: "???") }
    return GameState.MatchingGame(pairs = pairs)
}