package pl.edu.ur.db131403.hi_english.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1, // Zawsze jeden profil
    val points: Int = 0
)