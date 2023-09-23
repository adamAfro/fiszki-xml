package pl.devadam.fiszki

import android.content.Context
import androidx.room.Room

object DatabaseManager {

    private var appDatabase: AppDatabase? = null

    fun getAppDatabase(context: Context): AppDatabase {
        if (appDatabase == null) {
            appDatabase = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java, "database-name"
            ).build()
        }
        return appDatabase!!
    }
}

