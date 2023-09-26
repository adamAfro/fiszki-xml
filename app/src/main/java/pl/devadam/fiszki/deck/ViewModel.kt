package pl.devadam.fiszki.deck

import Manager
import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import pl.devadam.fiszki.TTSViewModel
import pl.devadam.fiszki.card.Entity as CardEntity

class ViewModel(application: Application) : AndroidViewModel(application) {

    private val _textToSpeech = MutableLiveData<TextToSpeech>()
    val textToSpeech: LiveData<TextToSpeech> get() = _textToSpeech

    private val _voices = MutableLiveData<Set<Voice>>()
    val voices: LiveData<Set<Voice>> get() = _voices

    private val _deckData = MutableLiveData<RelatedEntity>()
    val deckData: LiveData<RelatedEntity> get() = _deckData

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

        dao.deleteDeck(id)
        _id = null
        _name.postValue(null)
        _voiceName.postValue(null)

        dao.deleteCardsInDeck(id)
        _cards.postValue(null)
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

    fun getTTSVoicesNames(): List<String> {

        if (_textToSpeech.value == null || _textToSpeech.value!!.voices == null)
            return emptyList()

        val localVoices = _textToSpeech.value!!.voices!!.filter { it.name.endsWith("-language") }
        // TODO: fallback if voices are not ending with -language

        return localVoices.map { it.name }
    }

    private fun initializeTTS(application: Application) {
        _textToSpeech.value = TextToSpeech(application) { status ->
            if (status != TextToSpeech.SUCCESS) {
                // Handle TTS initialization failure
                // Log or show an error message
                println("TTS failed")
                return@TextToSpeech
            }

            _voices.value = _textToSpeech.value?.voices
        }
    }

    override fun onCleared() {

        _textToSpeech.value?.shutdown()

        super.onCleared()
    }
}
