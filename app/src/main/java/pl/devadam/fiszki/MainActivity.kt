package pl.devadam.fiszki

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val addButton = findViewById<ImageButton>(R.id.addButton)

        addCards()

        addButton.setOnClickListener { addCard() }
    }

    private fun addCards() {

        val manager: FragmentManager = supportFragmentManager
        val additions: FragmentTransaction = manager.beginTransaction()

        listOf(
            Pair("Hello", "World"),
            Pair("Example", "Definition"),
            Pair("3rd Card", "for scrolling"),
        ).forEach { (term, definition) ->
            additions.add(
                R.id.cardsList,
                EditableCard.newInstance(term, definition)
            )
        }

        additions.commit()
    }

    private fun addCard() {

        val manager: FragmentManager = supportFragmentManager
        val additions: FragmentTransaction = manager.beginTransaction()

        additions.add(
            R.id.cardsList,
            EditableCard.newInstance("New Term", "New Definition")
        )

        additions.commit()
    }
}