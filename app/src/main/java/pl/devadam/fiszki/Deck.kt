package pl.devadam.fiszki

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Deck : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deck)

        val addButton = findViewById<ImageButton>(R.id.addCardButton)
        val menuButton = findViewById<ImageButton>(R.id.menuButton)

        menuButton.setOnClickListener {
            startActivity(Intent(this, Pocket::class.java))
        }

        addButton.setOnClickListener { addCard() }

        loadCurrentDeck()
    }

    private fun loadCurrentDeck() { CoroutineScope(Dispatchers.IO).launch {

        val dao = DatabaseManager
            .getAppDatabase(applicationContext)
            .decksDao()

        val userSelectedDeckId = CurrentDeck.instance.getId()
        val deckWithCards =
            if (userSelectedDeckId != null)
                dao.getDeckWithCards(userSelectedDeckId)
            else
                dao.getLastAccessedDeckWithCards()

        if (deckWithCards != null) {

            renderCards(deckWithCards.cards)
            rename(deckWithCards.deck.name)

            CurrentDeck.instance.setId(deckWithCards.deck.id)

            return@launch
        }

        val createdDeck = StoredDeck(name = "New Deck")
        val createdDeckId = dao.insertDeck(createdDeck)

        CurrentDeck.instance.setId(createdDeckId)

        rename(createdDeck.name)
    }}

    private fun addCard() { CoroutineScope(Dispatchers.IO).launch {

        val currentDeckId = CurrentDeck.instance.getId() ?: return@launch
        val dao = DatabaseManager
            .getAppDatabase(applicationContext)
            .decksDao()

        val createdCard = StoredCard(term = "term", definition = "definition", deckId = currentDeckId)
        val createdCardId = dao.insertCard(createdCard)

        renderCard(createdCard.copy(id = createdCardId))
    }}

    private fun rename(name: String) {

        val nameText = findViewById<TextView>(R.id.deckName)

        nameText.text = name
    }

    private suspend fun renderCards(cards: List<StoredCard>) {

        val manager: FragmentManager = supportFragmentManager
        val additions: FragmentTransaction = manager.beginTransaction()

        withContext(Dispatchers.Main) {

            for (card in cards)
                additions.add(
                    R.id.cardsList,
                    EditableCard.newInstance(card.term, card.definition)
                )

            additions.commit()
        }
    }

    private fun renderCard(card: StoredCard) {

        val manager: FragmentManager = supportFragmentManager
        val addition: FragmentTransaction = manager.beginTransaction()

        addition.add(
            R.id.cardsList,
            EditableCard.newInstance(card.term, card.definition)
        )

        addition.commit()
    }
}
