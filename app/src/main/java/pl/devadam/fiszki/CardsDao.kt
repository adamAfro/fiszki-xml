package pl.devadam.fiszki

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction

data class DeckWithCards(
    @Embedded val deck: StoredDeck,
    @Relation(
        parentColumn = "id",
        entityColumn = "deck_id"
    )
    val cards: List<StoredCard>
)

@Dao
interface CardsDao {

    @Insert(entity = StoredCard::class)
    fun insertCard(card: StoredCard): Long

    @Query("UPDATE stored_cards SET term = :term WHERE id = :cardId")
    fun updateTerm(cardId: Long, term: String)

    @Query("UPDATE stored_cards SET definition = :definition WHERE id = :cardId")
    fun updateDefinition(cardId: Long, definition: String)


    @Insert(entity = StoredDeck::class)
    fun insertDeck(deck: StoredDeck): Long

    @Query("UPDATE stored_decks SET name = :name WHERE id = :deckId")
    fun updateDeckName(deckId: Long, name: String)


    @Query("SELECT * FROM stored_cards WHERE deck_id = :deckId")
    fun getCardsFromDeck(deckId: Long): List<StoredCard>

    @Transaction
    @Query("SELECT * FROM stored_decks WHERE id = (SELECT id FROM stored_decks ORDER BY last_access DESC LIMIT 1)")
    fun getLastAccessedDeckWithCards(): DeckWithCards?

    @Transaction
    @Query("SELECT * FROM stored_decks WHERE id = :deckId")
    fun getDeckWithCards(deckId: Long): DeckWithCards?

    @Query("SELECT * FROM stored_decks")
    fun getAllDecks(): List<StoredDeck>
}