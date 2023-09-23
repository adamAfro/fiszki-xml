package pl.devadam.fiszki

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class EditableCard : Fragment() {

    private var term: String? = null
    private var definition: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            term = it.getString(ARG_TERM)
            definition = it.getString(ARG_DEFINITION)
        }
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

        val termTextView = view.findViewById<TextView>(R.id.term)
        val definitionTextView = view.findViewById<TextView>(R.id.definition)

        termTextView.text = term
        definitionTextView.text = definition

        return view
    }

    companion object {
        private const val ARG_TERM = "arg_term"
        private const val ARG_DEFINITION = "arg_definition"

        fun newInstance(term: String, definition: String): EditableCard {
            val fragment = EditableCard()
            val args = Bundle()
            args.putString(ARG_TERM, term)
            args.putString(ARG_DEFINITION, definition)
            fragment.arguments = args
            return fragment
        }
    }
}