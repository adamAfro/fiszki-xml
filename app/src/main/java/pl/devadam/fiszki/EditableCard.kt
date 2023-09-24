package pl.devadam.fiszki

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask

class EditableCard private constructor(
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
            R.layout.fragment_editable_card,
            container, false
        )

        val voiceButton = view.findViewById<ImageButton>(R.id.voiceButton)

        val termView = view.findViewById<TextView>(R.id.term)
        val removeButton = view.findViewById<ImageButton>(R.id.removeCardButton)

        termView.text = term
        setupTextWatcher(termView) {

            term = it
            val dao = DatabaseManager
                .getAppDatabase(requireContext())
                .cardsDao()

            dao.updateTerm(id, term)
        }

        val definitionView = view.findViewById<TextView>(R.id.definition)

        definitionView.text = definition
        setupTextWatcher(definitionView) {

            definition = it
            val dao = DatabaseManager
                .getAppDatabase(requireContext())
                .cardsDao()

            dao.updateDefinition(id, definition)
        }

        removeButton.setOnClickListener {

            CoroutineScope(Dispatchers.IO).launch { remove() }

            (view?.parent as? ViewGroup)?.removeView(view)
        }

        voiceButton.setOnClickListener {
            
            (activity as? Deck)?.speak(term)
        }

        return view
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

    private fun remove() {

        val dao = DatabaseManager
            .getAppDatabase(requireContext())
            .cardsDao()

        dao.deleteCard(id)
    }

    companion object {

        private const val ARG_ID = "arg_id"
        private const val ARG_TERM = "arg_term"
        private const val ARG_DEFINITION = "arg_definition"

        fun newInstance(id: Long, term: String, definition: String): EditableCard {
            return EditableCard(id, term, definition).apply {
                arguments = Bundle().apply {
                    putLong(ARG_ID, id)
                    putString(ARG_TERM, term)
                    putString(ARG_DEFINITION, definition)
                }
            }
        }
    }
}