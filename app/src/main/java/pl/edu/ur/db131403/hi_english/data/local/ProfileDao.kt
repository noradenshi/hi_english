package pl.edu.ur.db131403.hi_english.data.local

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT points FROM user_profile WHERE id = 1")
    fun getUserPoints(): Flow<Int>

    @Query("UPDATE user_profile SET points = points + :addedPoints WHERE id = 1")
    suspend fun updatePoints(addedPoints: Int)
}