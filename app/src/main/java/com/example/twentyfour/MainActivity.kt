package com.example.twentyfour

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.util.Log
import androidx.appcompat.app.AppCompatActivity


val DIGIT_IDS = arrayOf(R.id.digit1, R.id.digit2, R.id.digit3, R.id.digit4)

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private val digitValues = mutableMapOf<Int, Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        DIGIT_IDS.forEach { spinner_id ->
            digitValues[spinner_id] = 0
            val spinner: Spinner = findViewById(spinner_id)
            // Create an ArrayAdapter using the string array and a default spinner layout
            ArrayAdapter.createFromResource(
                this,
                R.array.digits,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                // Specify the layout to use when the list of choices appears
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                // Apply the adapter to the spinner
                spinner.adapter = adapter
                spinner.onItemSelectedListener = this
            }
        }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        val item = parent.getItemAtPosition(pos)
        val itemstr = item as String
        digitValues[parent.id] = itemstr.toInt()

        findViewById<TextView>(R.id.solution_content).apply {
            this.text = digitValues.values.sum().toString()
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        // Another interface callback
    }

    fun solve() {

    }
}
