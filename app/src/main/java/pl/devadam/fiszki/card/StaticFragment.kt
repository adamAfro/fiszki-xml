package pl.devadam.fiszki.card

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import pl.devadam.fiszki.R
import pl.devadam.fiszki.deck.Activity as DeckActivity

open class StaticFragment(
    private val id: Long,
    private var term: String,
    private var definition: String
): Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(
            R.layout.fragment_card,
            container, false
        )

        val termView = view.findViewById<TextView>(R.id.term)
        val definitionView = view.findViewById<TextView>(R.id.definition)
        val voiceButton = view.findViewById<ImageButton>(R.id.voiceButton)

        termView.text = term
        definitionView.text = definition
        voiceButton.setOnClickListener { synthetizeTerm() }

        return view
    }

    protected fun synthetizeTerm() {

        println("speak ${term}")
        (activity as? DeckActivity)?.speak(term)
    }

    companion object {

        fun newInstance(id: Long, term: String, definition: String): StaticFragment {
            return StaticFragment(id, term, definition)
        }
    }
}