package pl.edu.ur.db131403.hi_english.ui.dictionary

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import pl.edu.ur.db131403.hi_english.data.local.DictionaryDatabase
import pl.edu.ur.db131403.hi_english.data.local.WordEntity

class WordViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = DictionaryDatabase.getDatabase(application).wordDao()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val words: StateFlow<List<WordEntity>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                dao.getAllA1Words() // Fetch all from DB
            } else {
                dao.searchWords("%$query%") // Search in DB (using SQL LIKE syntax)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun onSearchChange(newQuery: String) {
        _searchQuery.value = newQuery
    }
}