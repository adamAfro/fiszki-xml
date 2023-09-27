package pl.devadam.fiszki.stack

import Manager
import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import pl.devadam.fiszki.TTSViewModel
import pl.devadam.fiszki.card.Entity as CardEntity

class ViewModel(application: Application) : TTSViewModel(application) {

    private val _cards = MutableLiveData<List<CardEntity>>()
    val cards = _cards as LiveData<List<CardEntity>>

    private var leftIndices = mutableListOf<Int>()

    private var _cardIndex: Int = -1
    private var _deckId: Long? = null

    val deckId: Long
        get() {
            return _deckId ?: throw IllegalStateException("Deck ID is null")
        }

    init {
        initializeTTS(getApplication())
    }
    fun loadCardEntities(deckId: Long) {

        val cards = Manager.accessData(getApplication())
            .getCardsFromDeck(deckId)

        _deckId = deckId
        _cards.postValue(cards)

        leftIndices = cards.indices.toMutableList()
    }

    fun getNextCard(remove: Boolean? = false): CardEntity {

        if (leftIndices.isEmpty())
            throw IllegalStateException("No cards left")
        _cardIndex = (_cardIndex + 1) % _cards.value!!.size
        while (!leftIndices.contains(_cardIndex))
            _cardIndex = (_cardIndex + 1) % _cards.value!!.size
        if (remove == true)
            leftIndices.remove(_cardIndex)

        return _cards.value!![_cardIndex]
    }

    fun hasNextCard(): Boolean {

        return leftIndices.size > 1
    }
}