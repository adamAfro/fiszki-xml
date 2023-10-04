package pl.devadam.fiszki.pocket

import Manager
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import pl.devadam.fiszki.deck.Entity

// TODO (for later) allow user to store files on the device as files instead of using database
class ViewModel(application: Application) : AndroidViewModel(application) {

    fun getDecks(): List<Entity> {

        return Manager.accessData(getApplication())
            .getAllDecks()
    }

    fun addDeck(): Long {

        val createdDeck = Entity(name = "New Deck")
        return Manager.accessData(getApplication())
            .insertDeck(createdDeck)
    }
}