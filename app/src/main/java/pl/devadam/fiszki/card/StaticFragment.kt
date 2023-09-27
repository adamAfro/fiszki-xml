package pl.devadam.fiszki.card

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import pl.devadam.fiszki.R
import pl.devadam.fiszki.stack.Activity
import pl.devadam.fiszki.stack.ViewModel

class StaticFragment: Fragment() {

    private var id: Long = -1

    private lateinit var viewModel: ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        id = arguments?.getLong(ARG_ID, -1) ?: -1
        viewModel = ViewModelProvider(requireActivity())[ViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(
            R.layout.fragment_card,
            container, false
        )

        val termView = view.findViewById<TextView>(R.id.term)
        val definitionView = view.findViewById<TextView>(R.id.definition)

        val card = viewModel.cards.value!!
            .find { it.id == id } ?: throw Exception("No card found")

        termView.text = card.term
        definitionView.text = card.definition

        val voiceButton = view.findViewById<ImageButton>(R.id.voiceButton)

        voiceButton.setOnClickListener { synthesizeTerm() }

        return view
    }

    private fun synthesizeTerm() {

        val term = viewModel.cards.value!!.find { it.id == id }?.term ?: return

        println("speak ${term}")
        (activity as? Activity)?.speak(term)
    }

    companion object {
        private const val ARG_ID = "card_id"

        fun newInstance(id: Long): StaticFragment {
            val fragment = StaticFragment()
            val args = Bundle()
            args.putLong(ARG_ID, id)
            fragment.arguments = args
            return fragment
        }
    }
}