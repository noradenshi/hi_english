package pl.edu.ur.db131403.hi_english.data.repository

import kotlinx.coroutines.flow.Flow
import pl.edu.ur.db131403.hi_english.data.local.ProfileDao

class ProfileRepository(
    private val dao: ProfileDao
) {
    val userPoints: Flow<Int> = dao.getUserPoints()

    suspend fun addPoints(amount: Int) {
        dao.updatePoints(amount)
    }
}