package pl.devadam.fiszki.deck

import Manager
import android.app.Application
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    private fun getId(): Long {

        if (_deckData.value == null)
            throw Exception("Deck was not loaded yet")

        return _deckData.value!!.deck.id
    }

    private fun addRelatedEntity(): RelatedEntity {

        val createdDeck = Entity(name = "New Deck")
        val createdDeckId = Manager.accessData(getApplication())
            .insertDeck(createdDeck)

        return RelatedEntity(createdDeck.copy(id = createdDeckId), emptyList())
    }

    fun loadRelatedEntity(deckId: Long? = null) {

        _deckData.postValue(
            if (deckId == null)
                Manager.accessData(getApplication()).getLastAccessedDeckWithCards() ?: addRelatedEntity()
            else
                Manager.accessData(getApplication()).getDeckWithCards(deckId) ?: addRelatedEntity()
        )
    }

    suspend fun removeRelatedEntity() {

        val dao = Manager.accessData(getApplication())
        val id = getId()

        dao.deleteDeck(id)
        dao.deleteCardsInDeck(id)

        withContext(Dispatchers.Main) {
            _deckData.value = null
        }
    }

    fun updateName(name: String) {

        Manager.accessData(getApplication()).updateDeckName(getId(), name)
    }

    fun updateVoice(name: String?) {

        Manager.accessData(getApplication()).updateDeckPreferredVoice(getId(), name)
    }

    fun addCard(): CardEntity {

        val createdCard = CardEntity(term = "term", definition = "definition", deckId = getId())
        val createdCardId = Manager.accessData(getApplication())
            .insertCard(createdCard)

        return createdCard.copy(id = createdCardId)
    }

    fun updateCardTerm(id: Long, term: String) {

        Manager.accessData(getApplication())
            .updateTerm(id, term)
    }

    fun updateCardDefinition(id: Long, definition: String) {

        Manager.accessData(getApplication())
            .updateDefinition(id, definition)
    }

    fun removeCard(id: Long) {

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
