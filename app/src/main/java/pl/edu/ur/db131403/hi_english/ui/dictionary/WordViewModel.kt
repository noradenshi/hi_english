package pl.edu.ur.db131403.hi_english.ui.dictionary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pl.edu.ur.db131403.hi_english.data.model.WordEntity
import pl.edu.ur.db131403.hi_english.data.repository.WordRepository

class WordViewModel(private val wordRepository: WordRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _isRootMode = MutableStateFlow(false)

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val words: StateFlow<List<WordEntity>> =
        combine(_searchQuery.debounce(300), _isRootMode) { query, isRoot ->
            Pair(query, isRoot)
        }.flatMapLatest { (query, isRoot) ->
            if (query.isBlank()) {
                if (isRoot) wordRepository.getAllWords()
                else wordRepository.getVisibleWordsByCefr(listOf("A1"))
            } else {
                if (isRoot)
                    wordRepository.searchWords("%$query%").map { list ->
                        list.filter { it.isVisible }
                    }
                else
                    wordRepository.searchWordsInLevels("%$query%", listOf("A1"))
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun updateRootMode(isRoot: Boolean) {
        _isRootMode.value = isRoot
    }

    fun onSearchChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun toggleVisibility(wordFromUi: WordEntity) {
        viewModelScope.launch {
            val currentWord = words.value.find { it.id == wordFromUi.id } ?: wordFromUi
            val updated = currentWord.copy(isVisible = !currentWord.isVisible)

            wordRepository.updateWord(updated)
        }
    }

    fun updateWord(wordFromUi: WordEntity) {
        viewModelScope.launch {
            wordRepository.updateWord(wordFromUi)
        }
    }

    fun deleteWord(word: WordEntity) {
        viewModelScope.launch {
            wordRepository.deleteWord(word)
        }
    }

    fun insertWord(word: WordEntity) {
        viewModelScope.launch {
            wordRepository.insertWord(word)
        }
    }

    // Fabryka do ręcznego tworzenia ViewModelu z parametrem
    class Factory(private val repository: WordRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return WordViewModel(repository) as T
        }
    }
}