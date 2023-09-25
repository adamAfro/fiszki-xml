package pl.devadam.fiszki.card

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.devadam.fiszki.R
import pl.devadam.fiszki.deck.ViewModel
import java.util.Timer
import java.util.TimerTask

class EditableFragment constructor(
    private val id: Long,
    private var term: String,
    private var definition: String
): StaticFragment(id, term, definition) {

    private lateinit var viewModel: ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(
            R.layout.fragment_editable_card,
            container, false
        )

        viewModel = ViewModelProvider(requireActivity()).get(ViewModel::class.java)

        val termView = view.findViewById<TextView>(R.id.term)
        val definitionView = view.findViewById<TextView>(R.id.definition)
        val voiceButton = view.findViewById<ImageButton>(R.id.voiceButton)

        termView.text = term
        definitionView.text = definition
        voiceButton.setOnClickListener { synthetizeTerm() }

        val removeButton = view.findViewById<ImageButton>(R.id.removeCardButton)

        setupTextWatcher(termView) { viewModel.updateCardTerm(id, it) }
        setupTextWatcher(definitionView) { viewModel.updateCardDefinition(id, it) }
        removeButton.setOnClickListener { remove() }

        return view
    }

    private fun remove() = CoroutineScope(Dispatchers.IO).launch {

        viewModel.removeCard(id)

        withContext(Dispatchers.Main) {
            (view?.parent as? ViewGroup)?.removeView(view)
        }
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

    companion object {

        fun newInstance(id: Long, term: String, definition: String): EditableFragment {
            return EditableFragment(id, term, definition)
        }
    }
}