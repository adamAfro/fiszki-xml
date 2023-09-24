package pl.devadam.fiszki

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Timer
import java.util.TimerTask

class Deck : AppCompatActivity() {

    private var deckId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deck)

        deckId = intent.getLongExtra("deck_id", -1)
        if (deckId!! < 0)
            deckId = null

        val addButton = findViewById<ImageButton>(R.id.addCardButton)
        val menuButton = findViewById<ImageButton>(R.id.menuButton)
        val nameText = findViewById<TextView>(R.id.deckName)

        setupTextWatcher(nameText) {

            val dao = DatabaseManager
                .getAppDatabase(applicationContext)
                .cardsDao()

            if (deckId != null)
                dao.updateDeckName(deckId!!, it)
        }

        menuButton.setOnClickListener {
            startActivity(Intent(this, Pocket::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NO_HISTORY
            })
        }

        addButton.setOnClickListener {

            CoroutineScope(Dispatchers.IO).launch {

                val card = addNewCard()

                withContext(Dispatchers.Main) {
                    renderCard(card)
                }
            }
        }

        CoroutineScope(Dispatchers.IO).launch {

            val deckWithCards = reloadDeckWithCards()

            withContext(Dispatchers.Main) {
                deckId = deckWithCards.deck.id
                renderCards(deckWithCards.cards)
                rename(deckWithCards.deck.name)
            }
        }
    }

    private fun reloadDeckWithCards(): DeckWithCards {

        val dao = DatabaseManager.getAppDatabase(applicationContext).cardsDao()

        val deckWithCards = if (deckId != null)
            dao.getDeckWithCards(deckId!!)
        else
            dao.getLastAccessedDeckWithCards()

        if (deckWithCards != null)
            return deckWithCards

        val createdDeck = StoredDeck(name = "New Deck")
        val createdDeckId = dao.insertDeck(createdDeck)

        return DeckWithCards(createdDeck.copy(id = createdDeckId), listOf())
    }

    private fun addNewCard(): StoredCard {

        if (deckId == null)
            throw Exception("Deck ID is not set")

        val dao = DatabaseManager
            .getAppDatabase(applicationContext)
            .cardsDao()

        val createdCard = StoredCard(term = "term", definition = "definition", deckId = deckId!!)
        val createdCardId = dao.insertCard(createdCard)

        return createdCard.copy(id = createdCardId)
    }

    private fun rename(name: String) {

        val nameText = findViewById<TextView>(R.id.deckName)

        nameText.text = name
    }

    private fun renderCards(cards: List<StoredCard>) {

        val manager: FragmentManager = supportFragmentManager
        val additions: FragmentTransaction = manager.beginTransaction()

        for (card in cards) additions.add(
            R.id.cardsList,
            EditableCard.newInstance(card.id, card.term, card.definition)
        )

        additions.commit()
    }

    private fun renderCard(card: StoredCard) {

        val manager: FragmentManager = supportFragmentManager
        val addition: FragmentTransaction = manager.beginTransaction()

        addition.add(
            R.id.cardsList,
            EditableCard.newInstance(card.id, card.term, card.definition)
        )

        addition.commit()
    }

    private fun setupTextWatcher(textView: TextView, updateAction: (String) -> Unit) {

        textView.addTextChangedListener(object : TextWatcher {

            private var timer: Timer? = null

            override fun afterTextChanged(arg0: Editable?) {
                timer = Timer()
                timer!!.schedule(object : TimerTask() {
                    override fun run() {
                        updateAction(textView.text.toString())
                    }
                }, 600)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (timer != null) timer?.cancel()
            }
        })
    }
}
