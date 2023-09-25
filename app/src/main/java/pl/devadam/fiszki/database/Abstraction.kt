package pl.devadam.fiszki.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [pl.devadam.fiszki.card.Entity::class, pl.devadam.fiszki.deck.Entity::class], version = 1)
abstract class Abstraction : RoomDatabase() {

    abstract fun getDao(): Dao
}