package pl.devadam.fiszki.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

import pl.devadam.fiszki.card.Entity as CardEntity
import pl.devadam.fiszki.deck.Entity as DeckEntity
import pl.devadam.fiszki.deck.RelatedEntity as DeckWithCardEntities

@Dao
interface Dao {

    @Insert(entity = CardEntity::class)
    fun insertCard(card: CardEntity): Long

    @Query("DELETE FROM stored_cards WHERE id = :cardId")
    fun deleteCard(cardId: Long): Void

    @Query("UPDATE stored_cards SET term = :term WHERE id = :cardId")
    fun updateTerm(cardId: Long, term: String): Void

    @Query("UPDATE stored_cards SET definition = :definition WHERE id = :cardId")
    fun updateDefinition(cardId: Long, definition: String): Void


    @Insert(entity = DeckEntity::class)
    fun insertDeck(deck: DeckEntity): Long

    @Query("DELETE FROM stored_decks WHERE id = :deckId")
    fun deleteDeck(deckId: Long): Void

    @Query("SELECT * FROM stored_decks")
    fun getAllDecks(): List<DeckEntity>

    @Query("UPDATE stored_decks SET name = :name WHERE id = :deckId")
    fun updateDeckName(deckId: Long, name: String): Void

    @Query("UPDATE stored_decks SET voice_name = :voiceName WHERE id = :deckId")
    fun updateDeckVoiceName(deckId: Long, voiceName: String?): Void


    @Query("SELECT * FROM stored_cards WHERE deck_id = :deckId")
    fun getCardsFromDeck(deckId: Long): List<CardEntity>

    @Query("DELETE FROM stored_cards WHERE deck_id = :deckId")
    fun deleteCardsInDeck(deckId: Long): Void


    @Transaction
    @Query("SELECT * FROM stored_decks WHERE id = (SELECT id FROM stored_decks ORDER BY last_access DESC LIMIT 1)")
    fun getLastAccessedDeckWithCards(): DeckWithCardEntities?

    @Transaction
    @Query("SELECT * FROM stored_decks WHERE id = :deckId")
    fun getDeckWithCards(deckId: Long): DeckWithCardEntities?
}