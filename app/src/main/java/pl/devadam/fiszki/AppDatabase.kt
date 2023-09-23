package pl.devadam.fiszki

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [StoredCard::class, StoredDeck::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cardsDao(): CardsDao
}