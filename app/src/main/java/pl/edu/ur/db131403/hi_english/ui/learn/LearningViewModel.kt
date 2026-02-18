package pl.edu.ur.db131403.hi_english.ui.learn

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import pl.edu.ur.db131403.hi_english.data.repository.WordRepository
import pl.edu.ur.db131403.hi_english.data.repository.ProfileRepository

class LearningViewModel(
    private val wordRepository: WordRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {
    var tasksCompletedInSession by mutableIntStateOf(0)
        private set

    private val _currentGameState = mutableStateOf<GameState?>(null)
    val currentGameState: State<GameState?> = _currentGameState

    fun startMatchGame(category: String? = null) {
        viewModelScope.launch {
            val allWords = wordRepository.getVisibleA1Words().first()

            val filtered = if (category != null) {
                allWords.filter { it.description?.contains(category, ignoreCase = true) == true }
            } else {
                allWords
            }

            if (filtered.isNotEmpty()) {
                val selection = filtered.shuffled().take(4)
                _currentGameState.value = GameState.MatchingGame(
                    pairs = selection.associate { it.word to (it.translationPl ?: "???") }
                )
            }
        }
    }

    fun onMatchSelected(word: String, isEnglish: Boolean) {
        val state = _currentGameState.value as? GameState.MatchingGame ?: return

        // Jeśli kliknięty element jest już dopasowany, ignoruj
        if (state.matchedKeys.contains(word) || state.pairs.values.contains(word) && state.matchedKeys.any { state.pairs[it] == word }) return

        val newState = if (isEnglish) {
            state.copy(selectedLeft = if (state.selectedLeft == word) null else word)
        } else {
            state.copy(selectedRight = if (state.selectedRight == word) null else word)
        }

        _currentGameState.value = newState
        checkMatch(newState)
    }

    private fun checkMatch(state: GameState.MatchingGame) {
        val left = state.selectedLeft
        val right = state.selectedRight

        if (left != null && right != null) {
            if (state.pairs[left] == right) {
                val newMatched = state.matchedKeys + left
                val updatedState = state.copy(
                    matchedKeys = newMatched,
                    selectedLeft = null,
                    selectedRight = null
                )
                _currentGameState.value = updatedState

                // Jeśli wszystkie 4 pary odnalezione -> koniec zestawu
                if (newMatched.size == state.pairs.size) {
                    onMatchSetComplete()
                }
            } else {
                viewModelScope.launch {
                    kotlinx.coroutines.delay(500)
                    _currentGameState.value = state.copy(selectedLeft = null, selectedRight = null)
                }
            }
        }
    }

    private fun onMatchSetComplete() {
        viewModelScope.launch {
            tasksCompletedInSession++

            // 4 punkty za każdy zestaw
            val pointsToAdd = if (tasksCompletedInSession >= 20) {
                4 + 5 // 4 za zestaw + 5 bonusu
            } else {
                4
            }

            profileRepository.addPoints(pointsToAdd)

            if (tasksCompletedInSession >= 20) {
                clearSession()
                profileRepository.incrementGamesCompleted()
            } else {
                startMatchGame()
            }
        }
    }

    fun startSpellGame() {
        viewModelScope.launch {
            val allWords = wordRepository.getVisibleA1Words().first()

            if (allWords.isNotEmpty()) {
                val target = allWords.random()
                val wordToSpell = target.word.uppercase()

                val slots = wordToSpell.mapIndexed { index, c ->
                    LetterSlot(id = index, char = c)
                }.shuffled()

                _currentGameState.value = GameState.ScrambledLetters(
                    word = wordToSpell,
                    translation = target.translationPl ?: "???",
                    scrambledLetters = slots,
                    guessedSlots = emptyMap()
                )
            }
        }
    }

    fun onLetterClicked(slot: LetterSlot) {
        // Rzutowanie na ScrambledLetters, aby mieć dostęp do jego pól
        val state = _currentGameState.value as? GameState.ScrambledLetters ?: return

        val isAlreadyInWord = state.guessedSlots.values.any { it.id == slot.id }

        if (isAlreadyInWord) {
            // --- LOGIKA USUWANIA ---
            // Znajdź pozycję, na której znajduje się ten slot w odgadniętym słowie
            val entryToRemove = state.guessedSlots.entries.find { it.value.id == slot.id }

            if (entryToRemove != null) {
                val newGuessedSlots = state.guessedSlots - entryToRemove.key
                val newScrambledLetters = state.scrambledLetters.map {
                    if (it.id == slot.id) it.copy(isUsed = false) else it
                }

                _currentGameState.value = state.copy(
                    guessedSlots = newGuessedSlots,
                    scrambledLetters = newScrambledLetters
                )
            }
        } else {
            // --- LOGIKA DODAWANIA ---
            // Znajdź pierwszy wolny indeks w słowie docelowym
            val firstEmptyIndex = (0 until state.word.length).firstOrNull {
                !state.guessedSlots.containsKey(it)
            }

            if (firstEmptyIndex != null) {
                val newGuessedSlots = state.guessedSlots + (firstEmptyIndex to slot)
                val newScrambledLetters = state.scrambledLetters.map {
                    if (it.id == slot.id) it.copy(isUsed = true) else it
                }

                val updatedState = state.copy(
                    guessedSlots = newGuessedSlots,
                    scrambledLetters = newScrambledLetters
                )
                _currentGameState.value = updatedState

                // Sprawdzenie wygranej
                if (newGuessedSlots.size == state.word.length) {
                    val finalWord = (0 until state.word.length)
                        .map { newGuessedSlots[it]?.char ?: "" }
                        .joinToString("")

                    if (finalWord.equals(state.word, ignoreCase = true)) {
                        onTaskSuccess()
                    }
                }
            }
        }
    }

    // Funkcja wywoływana, gdy użytkownik poprawnie rozwiąże zadanie
    fun onTaskSuccess() {
        viewModelScope.launch {
            tasksCompletedInSession++

            if (tasksCompletedInSession >= 20) {
                // Bonus: 1 pkt za zadanie + 5 pkt za ukończenie zestawu
                profileRepository.addPoints(1 + 5)
                clearSession()
                profileRepository.incrementGamesCompleted()
            } else {
                // Standardowa nagroda: 1 pkt za zadanie
                profileRepository.addPoints(1)
                startSpellGame()
            }
        }
    }

    fun clearSession() {
        tasksCompletedInSession = 0
        _currentGameState.value = null
    }

    fun resetSpellGame() {
        val state = _currentGameState.value as? GameState.ScrambledLetters ?: return
        _currentGameState.value = state.copy(
            guessedSlots = emptyMap(),
            scrambledLetters = state.scrambledLetters.map { it.copy(isUsed = false) }
        )
    }
}