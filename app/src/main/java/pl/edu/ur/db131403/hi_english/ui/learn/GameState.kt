package pl.edu.ur.db131403.hi_english.ui.learn

sealed class GameState {
    data class MatchingGame(
        val pairs: Map<String, String>, // English -> Polish
        val selectedLeft: String? = null,
        val selectedRight: String? = null,
        val matchedKeys: Set<String> = emptySet()
    ) : GameState()

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