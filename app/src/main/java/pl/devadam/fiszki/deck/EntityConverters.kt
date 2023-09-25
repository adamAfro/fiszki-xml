package pl.devadam.fiszki.deck

import androidx.room.TypeConverter
import java.sql.Date

object EntityConverters {
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