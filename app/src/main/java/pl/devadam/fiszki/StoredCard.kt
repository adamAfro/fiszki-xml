package pl.devadam.fiszki

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "stored_cards",
    foreignKeys = [ForeignKey(
        entity = StoredDeck::class,
        parentColumns = ["id"],
        childColumns = ["deck_id"],
        onDelete = ForeignKey.CASCADE
    )]
)

data class StoredCard(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id: Long = 0,
    @ColumnInfo(name = "term") val term: String,
    @ColumnInfo(name = "definition") val definition: String,
    @ColumnInfo(name = "deck_id") val deckId: Long
)
