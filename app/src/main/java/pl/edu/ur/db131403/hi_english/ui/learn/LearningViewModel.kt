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
    // Stan postępu w aktualnej lekcji (0 do 20)
    var tasksCompletedInSession by mutableIntStateOf(0)
        private set

    private val _currentGameState = mutableStateOf<GameState?>(null)
    // Teraz State<GameState?> będzie rozpoznawane poprawnie
    val currentGameState: State<GameState?> = _currentGameState

    fun startMatchGame(category: String? = null) {
        viewModelScope.launch {
            // 1. Użyj .first(), aby pobrać aktualną listę z Flow (wymaga importu)
            // Jeśli repozytorium zwraca Flow, musimy go "zebrać"
            val allWords = wordRepository.getAllA1Words().first()

            // 2. Teraz allWords to zwykła List<WordEntity>, więc te funkcje działają:
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

    fun startSpellGame() {
        viewModelScope.launch {
            // Pobieramy słowa z bazy (używając .first() jak wcześniej)
            val allWords = wordRepository.getAllA1Words().first()

            if (allWords.isNotEmpty()) {
                val target = allWords.random()
                val wordToSpell = target.word.uppercase()

                // Tworzymy listę slotów z literami
                val slots = wordToSpell.mapIndexed { index, c ->
                    LetterSlot(id = index, char = c)
                }.shuffled()

                _currentGameState.value = GameState.ScrambledLetters(
                    word = wordToSpell,
                    translation = target.translationPl ?: "???",
                    scrambledLetters = slots
                )
            }
        }
    }

    // Funkcja do obsługi kliknięcia litery
    fun onLetterClicked(slot: LetterSlot) {
        // Musimy rzutować stan na ScrambledLetters, aby mieć dostęp do jego pól
        val state = _currentGameState.value as? GameState.ScrambledLetters ?: return

        if (!slot.isUsed) {
            val newGuess = state.currentGuess + slot.char

            // Tworzymy nową listę slotów, oznaczając kliknięty jako zużyty
            val newSlots = state.scrambledLetters.map {
                if (it.id == slot.id) it.copy(isUsed = true) else it
            }

            // Aktualizujemy stan całego obiektu
            _currentGameState.value = state.copy(
                currentGuess = newGuess,
                scrambledLetters = newSlots
            )
        }
    }

    // Funkcja wywoływana, gdy użytkownik poprawnie rozwiąże zadanie (np. ułoży słowo)
    fun onTaskSuccess() {
        viewModelScope.launch {
            tasksCompletedInSession++

            if (tasksCompletedInSession >= 20) {
                // Bonus: 1 pkt za zadanie + 5 pkt za ukończenie zestawu = 6
                profileRepository.addPoints(6)
                tasksCompletedInSession = 0 // Resetujemy pasek postępu dla nowej lekcji
            } else {
                // Standardowa nagroda: 1 pkt za zadanie
                profileRepository.addPoints(1)
            }
        }
    }

    // Dodaj funkcję resetu, o którą prosiło UI
    fun resetSpellGame() {
        val state = _currentGameState.value as? GameState.ScrambledLetters ?: return
        _currentGameState.value = state.copy(
            currentGuess = "",
            scrambledLetters = state.scrambledLetters.map { it.copy(isUsed = false) }
        )
    }
}