package pl.edu.ur.db131403.hi_english.ui.learn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pl.edu.ur.db131403.hi_english.data.repository.ProfileRepository
import pl.edu.ur.db131403.hi_english.data.repository.WordRepository

class LearningViewModelFactory(
    private val wordRepository: WordRepository,
    private val profileRepository: ProfileRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LearningViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LearningViewModel(wordRepository, profileRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}