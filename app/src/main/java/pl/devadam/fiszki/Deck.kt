package pl.devadam.fiszki

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
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

// TODO: initial deck is saved in history so after deleting it it may be restored for a moment
class Deck : AppCompatActivity() {

    private var deckId: Long? = null
    private var prefferedVoiceName: String? = null

    private lateinit var textToSpeech: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deck)

        deckId = intent.getLongExtra("deck_id", -1)
        if (deckId!! < 0)
            deckId = null

        textToSpeech = initializeTTS()
        loadDeck()

        val addButton = findViewById<ImageButton>(R.id.addCardButton)
        val menuButton = findViewById<ImageButton>(R.id.menuButton)
        val nameText = findViewById<TextView>(R.id.deckName)
        val voiceSpinner = findViewById<Spinner>(R.id.spinnerVoice)
        val removeButton = findViewById<ImageButton>(R.id.removeDeckButton)

        menuButton.setOnClickListener { redirectToPocket() }
        addButton.setOnClickListener { addCard() }
        removeButton.setOnClickListener { remove() }

        setupTextWatcher(nameText) { updateName(it) }
        setupVoiceChangeWatcher(voiceSpinner, textToSpeech) { changeVoice(it) }
    }

    private fun updateName(name: String) {

        val dao = DatabaseManager
            .getAppDatabase(applicationContext)
            .cardsDao()

        if (deckId != null)
            dao.updateDeckName(deckId!!, name)
    }

    private fun loadDeck() = CoroutineScope(Dispatchers.IO).launch {

        val deckWithCards = loadDeckWithCardsFromDatabase()

        withContext(Dispatchers.Main) {

            deckId = deckWithCards.deck.id
            renderCards(deckWithCards.cards)
            rename(deckWithCards.deck.name)
            if (deckWithCards.deck.preferredVoice != null)
                selectVoice(deckWithCards.deck.preferredVoice)
        }
    }

    private fun addCard() = CoroutineScope(Dispatchers.IO).launch {

        val card = addNewCard()

        withContext(Dispatchers.Main) { renderCard(card) }
    }

    private fun remove() = CoroutineScope(Dispatchers.IO).launch {

        removeFromDatabase()
        redirectToPocket()
    }

    private fun changeVoice(voice: Voice?) = CoroutineScope(Dispatchers.IO).launch {

        textToSpeech.voice = voice
        changeVoiceInDatabase(voice?.name)
    }

    fun speak(text: String) {

        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun initializeTTS() = TextToSpeech(this) { status ->

        if (status != TextToSpeech.SUCCESS) {

            Log.w("TTS", "initialization failed")

            return@TextToSpeech
        }

        val voiceSpinner = findViewById<Spinner>(R.id.spinnerVoice)
        val localVoices = textToSpeech.voices.filter { it.name.endsWith("-language") }
        // TODO: fallback if voices are not ending with -language

        val names = localVoices.map { it.name }

        voiceSpinner.adapter = ArrayAdapter(this@Deck,
            android.R.layout.simple_spinner_item, names
        )

        if (prefferedVoiceName != null)
            selectVoice(prefferedVoiceName!!)
    }


    private fun removeFromDatabase() {

        if (deckId == null)
            throw Exception("Deck ID is not set")

        val dao = DatabaseManager
            .getAppDatabase(applicationContext)
            .cardsDao()

        dao.deleteDeck(deckId!!)
        dao.deleteCardsInDeck(deckId!!)
    }

    private fun selectVoice(name: String) {

        prefferedVoiceName = name
        if (textToSpeech.voices == null)
            return

        val voiceSpinner = findViewById<Spinner>(R.id.spinnerVoice)
        val adapter = voiceSpinner.adapter

        val pos = (0 until adapter.count)
            .firstOrNull { adapter.getItem(it) == name } ?: 0

        voiceSpinner.setSelection(pos)
    }

    private fun changeVoiceInDatabase(name: String?) {

        val dao = DatabaseManager
            .getAppDatabase(applicationContext)
            .cardsDao()

        if (deckId != null)
            dao.updateDeckPreferredVoice(deckId!!, name)
    }

    private fun loadDeckWithCardsFromDatabase(): DeckWithCards {

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

    private fun setupVoiceChangeWatcher(voiceSpinner: Spinner, textToSpeech: TextToSpeech, changeVoice: (Voice?) -> Unit) {
        voiceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedVoiceName = parent?.getItemAtPosition(position).toString()
                val voice = textToSpeech.voices.firstOrNull { it.name == selectedVoiceName }
                changeVoice(voice)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
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

    private fun redirectToPocket() {

        startActivity(Intent(this, Pocket::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NO_HISTORY
        })
    }

    override fun onDestroy() {

        textToSpeech.shutdown()
        super.onDestroy()
    }
}
