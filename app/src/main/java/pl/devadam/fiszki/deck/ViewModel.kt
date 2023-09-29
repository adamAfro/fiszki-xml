package pl.devadam.fiszki.deck

import Manager
import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import pl.devadam.fiszki.TTSViewModel
import pl.devadam.fiszki.card.Entity as CardEntity

class ViewModel(application: Application) : TTSViewModel(application) {

    private val _cards = MutableLiveData<MutableList<CardEntity>>()
    val cards = _cards as LiveData<MutableList<CardEntity>>

    private val _name = MutableLiveData<String>()
    val name = _name as LiveData<String>

    private val _voiceName = MutableLiveData<String>()
    val voiceName = _voiceName as LiveData<String>

    private var _id: Long? = null
    val id: Long
        get() {
            return _id ?: throw IllegalStateException("ID is null")
        }

    init {
        initializeTTS(getApplication())
    }

    private fun addRelatedEntity(): RelatedEntity {

        val createdDeck = Entity(name = "New Deck")
        val createdDeckId = Manager.accessData(getApplication())
            .insertDeck(createdDeck)

        return RelatedEntity(createdDeck.copy(id = createdDeckId), emptyList())
    }

    fun loadRelatedEntity(id: Long? = null) {

        val relatedEntity = if (id == null)
            Manager.accessData(getApplication()).getLastAccessedDeckWithCards() ?: addRelatedEntity()
        else
            Manager.accessData(getApplication()).getDeckWithCards(id) ?: addRelatedEntity()

        _id = relatedEntity.deck.id
        _name.postValue(relatedEntity.deck.name)
        _voiceName.postValue(relatedEntity.deck.voiceName)
        _cards.postValue(relatedEntity.cards.toMutableList())
    }

    fun removeRelatedEntity() {

        val dao = Manager.accessData(getApplication())

        dao.deleteCardsInDeck(id)
        dao.deleteDeck(id)

        _name.postValue(null)
        _voiceName.postValue(null)
        _cards.postValue(null)
        _id = null
    }

    fun updateName(name: String) {

        _name.postValue(name)
        Manager.accessData(getApplication())
            .updateDeckName(id, name)
    }

    fun updateVoice(name: String?) {

        _voiceName.postValue(name)
        Manager.accessData(getApplication())
            .updateDeckVoiceName(id, name)
    }

    fun addCard(): CardEntity {

        val card = CardEntity(term = "term", definition = "definition", deckId = id).let {
            it.copy(id = Manager.accessData(getApplication()).insertCard(it))
        }

        _cards.value!!.add(card)
        return card
    }

    fun updateCardTerm(id: Long, term: String) {

        _cards.value?.find { it.id == id }?.let { it.term = term }
        Manager.accessData(getApplication())
            .updateTerm(id, term)
    }

    fun updateCardDefinition(id: Long, definition: String) {

        _cards.value?.find { it.id == id }?.let { it.definition = definition }
        Manager.accessData(getApplication())
            .updateDefinition(id, definition)
    }

    fun removeCard(id: Long) {

        _cards.value?.removeIf { it.id == id }
        Manager.accessData(getApplication())
            .deleteCard(id)
    }
}
