
import android.content.Context
import androidx.room.Room
import pl.devadam.fiszki.database.Abstraction
import pl.devadam.fiszki.database.Dao

object Manager {

    private var database: Abstraction? = null

    fun accessData(context: Context): Dao {
        if (database == null) {
            database = Room.databaseBuilder(
                context.applicationContext,
                Abstraction::class.java, "database-name"
            ).build()
        }
        return database!!.getDao()
    }
}