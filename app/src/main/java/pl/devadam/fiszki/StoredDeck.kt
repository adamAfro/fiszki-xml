package pl.devadam.fiszki

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.sql.Date

object Converters {
    @TypeConverter
    @JvmStatic
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    @JvmStatic
    fun timestampToDate(timestamp: Long?): Date? {
        return if (timestamp == null) null else Date(timestamp)
    }
}

@Entity(tableName = "stored_decks")
@TypeConverters(Converters::class)
data class StoredDeck(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id: Long = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "preferred_voice") val preferredVoice: String? = null,
    @ColumnInfo(name = "preferred_recognition") val preferredRecognition: String? = null,
    @ColumnInfo(name = "last_access") val lastAccess: Date = Date(System.currentTimeMillis())
)