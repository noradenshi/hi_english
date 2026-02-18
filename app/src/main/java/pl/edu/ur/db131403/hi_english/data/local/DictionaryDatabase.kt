package pl.edu.ur.db131403.hi_english.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [WordEntity::class], version = 5)
abstract class DictionaryDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao

    companion object {
        fun getDatabase(context: Context): DictionaryDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                DictionaryDatabase::class.java,
                "dictionary_database"
            )
                .createFromAsset("databases/dictionary.db")
                .fallbackToDestructiveMigration(true)
                .build()

        }
    }
}