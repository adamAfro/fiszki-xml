package pl.devadam.fiszki.deck

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.sql.Date

@Entity(tableName = "stored_decks")
@TypeConverters(EntityConverters::class)
data class Entity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id: Long = 0,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "voice_name") var voiceName: String? = null,
    @ColumnInfo(name = "last_access") var lastAccess: Date = Date(System.currentTimeMillis())
)