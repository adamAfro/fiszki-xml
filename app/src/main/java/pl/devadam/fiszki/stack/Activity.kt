package pl.devadam.fiszki.stack

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.devadam.fiszki.R
import pl.devadam.fiszki.card.Entity
import pl.devadam.fiszki.card.StaticFragment
import pl.devadam.fiszki.deck.Activity as DeckActivity

class Activity : AppCompatActivity() {

    private lateinit var viewModel: ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stack)

        val putBackButton = findViewById<ImageButton>(R.id.putBackButton)
        val removeButton = findViewById<ImageButton>(R.id.removeButton)

        viewModel = ViewModelProvider(this).get(ViewModel::class.java)

        initRenderCard()

        load()

        putBackButton.setOnClickListener() { renderNextCard() }
        removeButton.setOnClickListener() { renderNextCard(true) }
    }

    private fun initRenderCard() = viewModel.cards.observe(this, Observer {

        if (it.isNullOrEmpty()) return@Observer

        viewModel.cards.removeObservers(this)
        renderCard(viewModel.getNextCard())
    })

    private fun renderNextCard(remove: Boolean? = false) {

        if (!viewModel.hasNextCard()) {
            redirectToDeck(viewModel.deckId)
            return
        }

        renderCard(viewModel.getNextCard(remove))
    }

    private fun renderCard(card: Entity) {

        vanishCard()

        val manager: FragmentManager = supportFragmentManager
        val addition: FragmentTransaction = manager.beginTransaction()

        addition.add(
            R.id.cardContainer,
            StaticFragment.newInstance(card.id, card.term, card.definition)
        )

        addition.commit()
    }

    private fun vanishCard() {

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        for (fragment in fragmentManager.fragments) {
            fragmentTransaction.remove(fragment)
        }

        fragmentTransaction.commit()
    }

    private fun load() = CoroutineScope(Dispatchers.IO).launch {

        val id = getPassedDeckId() ?: throw IllegalStateException("ID is null")

        viewModel.loadCardEntities(id)
    }

    fun speak(text: String) {

        viewModel.textToSpeech.value?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun getPassedDeckId(): Long? {

        val id = intent.getLongExtra("deck_id", -1)
        return if (id >= 0) id else null
    }
    
    private fun redirectToDeck(deckId: Long? = null) {

        startActivity(Intent(this@Activity, DeckActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NO_HISTORY
            if (deckId != null)
                putExtra("deck_id", deckId)
        })
    }
}