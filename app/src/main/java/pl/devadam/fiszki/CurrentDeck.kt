package pl.devadam.fiszki

class CurrentDeck private constructor() {

    private var id: Long? = null

    companion object {
        val instance = CurrentDeck()
    }

    fun setId(deckId: Long) {
        id = deckId
    }

    fun getId(): Long? {
        return id
    }
}
