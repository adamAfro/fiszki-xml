package pl.devadam.fiszki

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment

open class Card(
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
        (activity as? Deck)?.speak(term)
    }

    companion object {

        private const val ARG_ID = "arg_id"
        private const val ARG_TERM = "arg_term"
        private const val ARG_DEFINITION = "arg_definition"

        fun newInstance(id: Long, term: String, definition: String): Card {
            return Card(id, term, definition).apply {
                arguments = Bundle().apply {
                    putLong(ARG_ID, id)
                    putString(ARG_TERM, term)
                    putString(ARG_DEFINITION, definition)
                }
            }
        }
    }
}