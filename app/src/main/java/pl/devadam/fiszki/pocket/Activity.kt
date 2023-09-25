package pl.devadam.fiszki.pocket

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.devadam.fiszki.R
import pl.devadam.fiszki.deck.Activity as DeckActivity
import pl.devadam.fiszki.deck.Entity as DeckEntity

class Activity : AppCompatActivity() {

    private lateinit var viewModel: ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pocket)

        viewModel = ViewModelProvider(this).get(ViewModel::class.java)

        val addButton = findViewById<Button>(R.id.addDeckButton)
        val menuButton = findViewById<ImageButton>(R.id.menuButton)

        loadDecks()

        menuButton.setOnClickListener { redirectToDeck() }
        addButton.setOnClickListener { addDeck() }
    }

    private fun addDeck() = CoroutineScope(Dispatchers.IO).launch {

        val deckId = viewModel.addDeck()

        withContext(Dispatchers.Main) { redirectToDeck(deckId) }
    }

    private fun loadDecks() = CoroutineScope(Dispatchers.IO).launch {

        val decks = viewModel.getDecks()

        withContext(Dispatchers.Main) { renderDecks(decks) }
    }

    private fun renderDecks(decks: List<DeckEntity>) {

        val container = findViewById<LinearLayout>(R.id.decksList)

        for (deck in decks) {

            val button = Button(this)

            button.text = deck.name
            button.setOnClickListener {redirectToDeck(deck.id) }

            container.addView(button)
        }
    }

    private fun redirectToDeck(deckId: Long? = null) {

        startActivity(Intent(this@Activity, DeckActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NO_HISTORY
            if (deckId != null)
                putExtra("deck_id", deckId)
        })
    }
}