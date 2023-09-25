package pl.devadam.fiszki.deck

import androidx.room.Embedded
import androidx.room.Relation

data class RelatedEntity(
    @Embedded val deck: Entity,
    @Relation(
        parentColumn = "id",
        entityColumn = "deck_id"
    )
    val cards: List<pl.devadam.fiszki.card.Entity>
)