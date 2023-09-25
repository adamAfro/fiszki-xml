package pl.devadam.fiszki.deck

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.devadam.fiszki.R
import pl.devadam.fiszki.card.EditableFragment
import java.util.Timer
import java.util.TimerTask
import pl.devadam.fiszki.card.Entity as CardEntity
import pl.devadam.fiszki.pocket.Activity as PocketActivity

class Activity : AppCompatActivity() {

    private lateinit var viewModel: ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deck)

        val addButton = findViewById<ImageButton>(R.id.addCardButton)
        val menuButton = findViewById<ImageButton>(R.id.menuButton)
        val nameText = findViewById<TextView>(R.id.deckName)
        val voiceSpinner = findViewById<Spinner>(R.id.spinnerVoice)
        val removeButton = findViewById<ImageButton>(R.id.removeDeckButton)

        viewModel = ViewModelProvider(this).get(ViewModel::class.java)

        observeRelatedEntity()
        observeVoices()

        load()

        menuButton.setOnClickListener { redirectToPocket() }
        addButton.setOnClickListener { addCard() }
        removeButton.setOnClickListener { remove() }

        setupTextWatcher(nameText) { viewModel.updateName(it) }
    }

    private fun observeRelatedEntity() = viewModel.deckData.observe(this, Observer { deckWithCards ->

        if (deckWithCards == null)
            return@Observer

        renderCards(deckWithCards.cards)
        rename(deckWithCards.deck.name)
        if (deckWithCards.deck.preferredVoice != null)
            selectVoice(deckWithCards.deck.preferredVoice)
    })

    private fun observeVoices() = viewModel.voices.observe(this, Observer {

        val voiceSpinner = findViewById<Spinner>(R.id.spinnerVoice)

        insertVoices(viewModel.getTTSVoicesNames())

        setupVoiceChangeWatcher(voiceSpinner, it) { changeVoice(it) }
    })

    private fun load() = CoroutineScope(Dispatchers.IO).launch {

        viewModel.loadRelatedEntity(getPassedId())
    }

    private fun getPassedId(): Long? {

        val id = intent.getLongExtra("deck_id", -1)
        if (id >= 0)
            return id
        else
            return null
    }

    private fun addCard() = CoroutineScope(Dispatchers.IO).launch {

        val card = viewModel.addCard()

        withContext(Dispatchers.Main) { renderCard(card) }
    }

    private fun remove() = CoroutineScope(Dispatchers.IO).launch {

        viewModel.removeRelatedEntity()
        redirectToPocket()
    }

    private fun changeVoice(voice: Voice?) = CoroutineScope(Dispatchers.IO).launch {

        viewModel.textToSpeech.value?.voice = voice
        viewModel.updateVoice(voice?.name)
    }

    fun speak(text: String) {

        viewModel.textToSpeech.value?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun insertVoices(names: List<String>) {

        val voiceSpinner = findViewById<Spinner>(R.id.spinnerVoice)

        voiceSpinner.adapter = ArrayAdapter(this@Activity,
            android.R.layout.simple_spinner_item, names
        )

        if (viewModel.deckData.value != null)
            selectVoice(viewModel.deckData.value!!.deck.preferredVoice)
    }

    private fun selectVoice(name: String?) {

        if (viewModel.deckData.value == null)
            throw Exception("Deck not loaded yet")

        //viewModel.deckData.value!!.deck.preferredVoice = name
        if (viewModel.textToSpeech.value?.voice == null)
            return

        val voiceSpinner = findViewById<Spinner>(R.id.spinnerVoice)
        val adapter = voiceSpinner.adapter

        val pos = (0 until adapter.count)
            .firstOrNull { adapter.getItem(it) == name } ?: 0

        voiceSpinner.setSelection(pos)
    }

    private fun rename(name: String) {

        val nameText = findViewById<TextView>(R.id.deckName)

        nameText.text = name
    }

    private fun renderCards(cards: List<CardEntity>) {

        val manager: FragmentManager = supportFragmentManager
        val additions: FragmentTransaction = manager.beginTransaction()

        for (card in cards) additions.add(
            R.id.cardsList,
            EditableFragment.newInstance(card.id, card.term, card.definition)
        )

        additions.commit()
    }

    private fun renderCard(card: CardEntity) {

        val manager: FragmentManager = supportFragmentManager
        val addition: FragmentTransaction = manager.beginTransaction()

        addition.add(
            R.id.cardsList,
            EditableFragment.newInstance(card.id, card.term, card.definition)
        )

        addition.commit()
    }

    private fun setupVoiceChangeWatcher(voiceSpinner: Spinner, voices: Set<Voice>, changeVoice: (Voice?) -> Unit) {
        voiceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedVoiceName = parent?.getItemAtPosition(position).toString()
                val voice = voices.firstOrNull { it.name == selectedVoiceName }
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

        startActivity(Intent(this, PocketActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NO_HISTORY
        })
    }
}
