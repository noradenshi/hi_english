package pl.edu.ur.db131403.hi_english.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import pl.edu.ur.db131403.hi_english.data.model.UserProfile

@Database(entities = [WordEntity::class, UserProfile::class], version = 4)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao
    abstract fun profileDao(): ProfileDao

    companion object {
        fun getDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "dictionary_database"
            )
                .createFromAsset("databases/dictionary.db") // Loads my generated file
                .fallbackToDestructiveMigration(true)
                .build()
        }
    }
}