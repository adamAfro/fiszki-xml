package pl.devadam.fiszki.card

import androidx.room.ColumnInfo
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import pl.devadam.fiszki.deck.Entity as DeckEntity

@androidx.room.Entity(
    tableName = "stored_cards",
    foreignKeys = [ForeignKey(
        entity = DeckEntity::class,
        parentColumns = ["id"],
        childColumns = ["deck_id"],
        onDelete = ForeignKey.CASCADE
    )]
)

data class Entity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id: Long = 0,
    @ColumnInfo(name = "term") val term: String,
    @ColumnInfo(name = "definition") val definition: String,
    @ColumnInfo(name = "deck_id") val deckId: Long
)
