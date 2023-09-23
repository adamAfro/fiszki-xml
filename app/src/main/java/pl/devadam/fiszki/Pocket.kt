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

        val addButton = findViewById<Button>(R.id.addDeckButton)
        val menuButton = findViewById<ImageButton>(R.id.menuButton)

        menuButton.setOnClickListener {
            startActivity(Intent(this, Deck::class.java))
        }

        loadDecks()

        addButton.setOnClickListener { addDeck() }
    }

    private fun loadDecks() { CoroutineScope(Dispatchers.IO).launch {

        val dao = DatabaseManager
            .getAppDatabase(applicationContext)
            .decksDao()

        val decks = dao.getAllDecks()

        renderDecks(decks)
    }}

    private fun addDeck() { CoroutineScope(Dispatchers.IO).launch {

        val dao = DatabaseManager
            .getAppDatabase(applicationContext)
            .decksDao()

        val createdDeck = StoredDeck(name = "New Deck")
        val createdDeckId = dao.insertDeck(createdDeck)

        CurrentDeck.instance.setId(createdDeckId)

        withContext(Dispatchers.Main) {
            startActivity(Intent(this@Pocket, Deck::class.java))
        }
    }}

    private fun renderDecks(decks: List<StoredDeck>) {

        val container = findViewById<LinearLayout>(R.id.decksList)

        for (deck in decks) {

            val button = Button(this)

            button.text = deck.name
            button.setOnClickListener {

                CurrentDeck.instance.setId(deck.id)

                startActivity(Intent(this, Deck::class.java))
            }

            container.addView(button)
        }
    }
}