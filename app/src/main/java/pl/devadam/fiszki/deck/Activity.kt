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
import pl.devadam.fiszki.stack.Activity as StackActivity

class Activity : AppCompatActivity() {

    private lateinit var viewModel: ViewModel

    var name: String?

        get() {
            return findViewById<TextView>(R.id.deckName).text.toString()
        }

        set(value) {
            findViewById<TextView>(R.id.deckName).text = value
        }


    private var spinnerVoiceName: String? = ""
    private var voiceName: String?

        get() { return spinnerVoiceName }

        set(value) {

            if (viewModel.voiceName.value == null)
                throw Exception("Deck not loaded yet")

            val voiceSpinner = findViewById<Spinner>(R.id.spinnerVoice)
            val adapter = voiceSpinner.adapter ?: return
            val pos = (0 until adapter.count)
                .firstOrNull { adapter.getItem(it) == value } ?: 0

            spinnerVoiceName = value
            voiceSpinner.setSelection(pos)
        }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deck)

        val addButton = findViewById<ImageButton>(R.id.addCardButton)
        val playButton = findViewById<ImageButton>(R.id.playButton)
        val menuButton = findViewById<ImageButton>(R.id.menuButton)
        val nameText = findViewById<TextView>(R.id.deckName)
        val removeButton = findViewById<ImageButton>(R.id.removeDeckButton)

        noVoicesFallback()

        viewModel = ViewModelProvider(this).get(ViewModel::class.java)

        initRenderEntity()
        initRenderCards()
        initVoicesObserver()

        load()

        playButton.setOnClickListener { redirectToStack() }
        menuButton.setOnClickListener { redirectToPocket() }
        addButton.setOnClickListener { addCard() }
        removeButton.setOnClickListener { applyRemoval() }

        setupTextWatcher(nameText) { applyName(it) }
    }

    private fun initRenderEntity() {

        viewModel.name.observe(this, Observer {

            if (it == null) return@Observer

            viewModel.name.removeObservers(this)
            name = it
        })

        viewModel.voiceName.observe(this, Observer {

            if (it == null) return@Observer

            viewModel.voiceName.removeObservers(this)
            voiceName = it
        })
    }

    private fun initRenderCards() = viewModel.cards.observe(this, Observer {

        if (it == null) return@Observer

        viewModel.cards.removeObservers(this)
        vanishCards()
        renderCards(it)
    })

    private fun initVoicesObserver() = viewModel.voices.observe(this, Observer {

        viewModel.voices.removeObservers(this)

        insertVoices(viewModel.getTTSVoicesNames())
        setupVoiceChangeWatcher(findViewById<Spinner>(R.id.spinnerVoice), it) { applyVoice(it) }
    })

    private fun load() = CoroutineScope(Dispatchers.IO).launch {

        viewModel.loadRelatedEntity(getPassedId())
    }

    private fun getPassedId(): Long? {

        val id = intent.getLongExtra("deck_id", -1)

        return if (id >= 0) id else null
    }

    private fun addCard() = CoroutineScope(Dispatchers.IO).launch {

        val card = viewModel.addCard()

        withContext(Dispatchers.Main) { renderCard(card) }
    }

    private fun applyRemoval() = CoroutineScope(Dispatchers.IO).launch {

        viewModel.removeRelatedEntity()
        redirectToPocket()
    }

    private fun applyVoice(voice: Voice?) = CoroutineScope(Dispatchers.IO).launch {

        viewModel.textToSpeech.value?.voice = voice
        viewModel.updateVoice(voice?.name)
    }

    private fun applyName(name: String) = CoroutineScope(Dispatchers.IO).launch {

        viewModel.updateName(name)
    }

    fun speak(text: String) {

        viewModel.textToSpeech.value?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun insertVoices(names: List<String>) {

        val voiceSpinner = findViewById<Spinner>(R.id.spinnerVoice)
        voiceSpinner.adapter = ArrayAdapter(this@Activity,
            android.R.layout.simple_spinner_item, names.ifEmpty { listOf("No voices available") }
        )

        if (viewModel.voiceName.value != null)
            voiceName = viewModel.voiceName.value
    }

    private fun noVoicesFallback() {

        val voiceSpinner = findViewById<Spinner>(R.id.spinnerVoice)

        voiceSpinner.adapter = ArrayAdapter(this@Activity,
            android.R.layout.simple_spinner_item, listOf("No voices available")
        )
    }

    private fun renderCards(cards: List<CardEntity>) {

        val manager: FragmentManager = supportFragmentManager
        val additions: FragmentTransaction = manager.beginTransaction()

        for (card in cards) additions.add(
            R.id.cardsList,
            EditableFragment.newInstance(card.id)
        )

        additions.commit()
    }

    private fun renderCard(card: CardEntity) {

        val manager: FragmentManager = supportFragmentManager
        val addition: FragmentTransaction = manager.beginTransaction()

        addition.add(
            R.id.cardsList,
            EditableFragment.newInstance(card.id)
        )

        addition.commit()
    }

    private fun vanishCards() {

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        for (fragment in fragmentManager.fragments) {
            fragmentTransaction.remove(fragment)
        }

        fragmentTransaction.commit()
    }

    private fun setupVoiceChangeWatcher(voiceSpinner: Spinner, voices: Set<Voice>, voiceChange: (Voice?) -> Unit) {
        voiceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedVoiceName = parent?.getItemAtPosition(position).toString()
                val voice = voices.firstOrNull { it.name == selectedVoiceName }
                spinnerVoiceName = voice?.name
                voiceChange(voice)
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

    private fun redirectToStack() {

        startActivity(Intent(this, StackActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NO_HISTORY
            putExtra("deck_id", viewModel.id)
        })
    }
}
