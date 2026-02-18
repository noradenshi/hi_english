package pl.edu.ur.db131403.hi_english.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import pl.edu.ur.db131403.hi_english.data.model.EquippedItem
import pl.edu.ur.db131403.hi_english.data.model.ItemCategories
import pl.edu.ur.db131403.hi_english.data.model.StoreItem

// Rejestrujemy obie encje: StoreItem oraz EquippedItem
@Database(entities = [StoreItem::class, EquippedItem::class], version = 1, exportSchema = false)
abstract class InventoryDatabase : RoomDatabase() {

    abstract fun storeDao(): StoreDao

    companion object {
        @Volatile
        private var INSTANCE: InventoryDatabase? = null

        fun getDatabase(context: Context): InventoryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    InventoryDatabase::class.java,
                    "inventory_database" // To jest Twoja nowa, bezpieczna baza
                )
                    // Opcjonalnie: .addCallback(MIGRATION_CALLBACK)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}