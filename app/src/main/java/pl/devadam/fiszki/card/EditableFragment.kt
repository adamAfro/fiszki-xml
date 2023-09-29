package pl.devadam.fiszki.card

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.devadam.fiszki.R
import pl.devadam.fiszki.deck.Activity
import pl.devadam.fiszki.deck.ViewModel
import java.util.Timer
import java.util.TimerTask

class EditableFragment: Fragment() {

    private var id: Long = -1

    private lateinit var viewModel: ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        id = arguments?.getLong(ARG_ID, -1) ?: -1
        viewModel = ViewModelProvider(requireActivity()).get(ViewModel::class.java)
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

        val termView = view.findViewById<TextView>(R.id.term)
        val definitionView = view.findViewById<TextView>(R.id.definition)
        val entity = getEntity()

        termView.text = entity.term
        definitionView.text = entity.definition

        setupTextWatcher(termView) { applyTerm(it) }
        setupTextWatcher(definitionView) { applyDefinition(it) }

        val voiceButton = view.findViewById<ImageButton>(R.id.voiceButton)
        val removeButton = view.findViewById<ImageButton>(R.id.removeCardButton)

        voiceButton.setOnClickListener { synthesizeTerm() }
        removeButton.setOnClickListener { applyRemoval() }

        return view
    }

    private fun getEntity(): Entity {

        return viewModel.cards.value!!.find { it.id == id }
            ?: throw ExceptionInInitializerError("Nonexistent card in EditableFragment")
    }

    private fun applyTerm(term: String) = CoroutineScope(Dispatchers.IO).launch {

        viewModel.updateCardTerm(id, term)
    }

    private fun applyDefinition(definition: String) = CoroutineScope(Dispatchers.IO).launch {

        viewModel.updateCardDefinition(id, definition)
    }

    private fun applyRemoval() = CoroutineScope(Dispatchers.IO).launch {

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

    private fun synthesizeTerm() {

        val term = viewModel.cards.value!!.find { it.id == id }?.term ?: return

        println("speak ${term}")
        (activity as? Activity)?.speak(term)
    }

    companion object {
        private const val ARG_ID = "card_id"

        fun newInstance(id: Long): EditableFragment {
            val fragment = EditableFragment()
            val args = Bundle()
            args.putLong(ARG_ID, id)
            fragment.arguments = args
            return fragment
        }
    }
}