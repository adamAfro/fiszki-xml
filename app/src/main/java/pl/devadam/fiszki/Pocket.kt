package pl.devadam.fiszki

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Pocket : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pocket)

        loadDecks()

        val addButton = findViewById<Button>(R.id.addDeckButton)
        val menuButton = findViewById<ImageButton>(R.id.menuButton)

        menuButton.setOnClickListener { redirectToDeck() }
        addButton.setOnClickListener { addDeck() }
    }

    private fun addDeck() = CoroutineScope(Dispatchers.IO).launch {

        val deckId = addDeckToDatabase()

        withContext(Dispatchers.Main) { redirectToDeck(deckId) }
    }

    private fun loadDecks() = CoroutineScope(Dispatchers.IO).launch {

        val decks = loadDecksFromDatabase()

        withContext(Dispatchers.Main) { renderDecks(decks) }
    }

    private fun loadDecksFromDatabase(): List<StoredDeck> {

        val dao = DatabaseManager
            .getAppDatabase(applicationContext)
            .cardsDao()

        return dao.getAllDecks()
    }

    private fun addDeckToDatabase(): Long {

        val dao = DatabaseManager
            .getAppDatabase(applicationContext)
            .cardsDao()

        val createdDeck = StoredDeck(name = "New Deck")
        return dao.insertDeck(createdDeck)
    }

    private fun renderDecks(decks: List<StoredDeck>) {

        val container = findViewById<LinearLayout>(R.id.decksList)

        for (deck in decks) {

            val button = Button(this)

            button.text = deck.name
            button.setOnClickListener {redirectToDeck(deck.id) }

            container.addView(button)
        }
    }

    private fun redirectToDeck(deckId: Long? = null) {

        startActivity(Intent(this@Pocket, Deck::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NO_HISTORY
            if (deckId != null)
                putExtra("deck_id", deckId)
        })
    }
}