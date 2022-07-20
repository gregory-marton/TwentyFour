package com.example.twentyfour

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.lang.Math.pow
import kotlin.math.abs
import kotlin.math.sqrt

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
            this.text = solve(digitValues.values)
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        // Another interface callback
    }

    fun ensureSmallInt (i: Number): Int? {
        if (abs(i.toDouble()) > 10000) return null
        return if (abs(i.toDouble() - i.toInt()) < 0.00001) i.toInt() else null
    }
    fun intsqrt (i: Int): Int? {
        val s = sqrt(i.toDouble()).toInt()
        return if (s * s == i) s else null
    }
    fun intdiv (a: Int, b:Int): Int? {
        if (b == 0) return null
        val quotient:Int = (a.toDouble() / b).toInt()
        if (abs(quotient * b - a) > 0) return null
        return quotient
    }
    fun intpow (a: Int, b:Int): Int? {
        return if ((a != 0 || b > 0) && abs(b)<100)
            ensureSmallInt(pow(a.toDouble(), b.toDouble()))
        else null
    }
    fun introot (a: Int, b:Int): Int? {
        if (a != 0 && b!= 0)
            return ensureSmallInt(pow(b.toDouble(), 1.0/a.toDouble()))
        else return null
    }
    private val unary_operations:Map<String, (Int)->Int?> = mapOf(
        "~" to {i:Int -> -i},
        "□" to {i:Int -> i * i},
        "□√" to ::intsqrt,
        "Δ" to {d:Int -> if (d > 0) d*(d+1)/2 else null},
        "Δ√" to {d:Int -> if (d > 0) (kotlin.math.sqrt(8.0 * d + 1).toInt() - 1)/2 else null},
    )

    private val binary_operations:Map<String, (Int, Int)->Int?> = mapOf(
        "+" to {a:Int, b:Int -> a + b},
        "-" to {a:Int, b:Int -> a - b},
        "*" to {a:Int, b:Int -> a * b},
        "/" to ::intdiv,
        "%" to {a:Int, b:Int -> if (b != 0) a % b else null},
        "^" to ::intpow,
        "`√" to ::introot,
    )

    private val costs:Map<String, Int> = mapOf(
        "STEP" to 15,
        "OP" to 5,
        "%%" to 3,
        "^" to 2,
        "`√" to 5,
        "~" to 1,
        "□" to 2,
        "□√" to 4,
        "Δ" to 2,
        "Δ√" to 4,
    )

    fun bfs(values: Collection<Int>, desired: Int = 5, maxdepth:Int = 5) {
        // Queue contains a Pair<history, values> in which
        //   history is the set of operations already applied, and
        //   values is the set of values remaining to be used.
        val queue = ArrayDeque<Pair<List<String>, Collection<Int>>>()
        val empty_history = List<String>(0)
        queue.add(Pair(empty_history, values))
        val limit = maxdepth
        val solutions = Array<List<String>>()
        while (queue.isNotEmpty()) {
            val (history, remaining) = queue.removeFirst()
            if ((history.size > limit) || (solutions.size >= desired)) break
            if (remaining.size > 1) {
                for ((i, a) in remaining.withIndex()) {
                    for ((j, b) in remaining.withIndex()) {
                        if (j != i) {
                            val rest : Collection<Int> = remaining.minus(a).minus(b)
                            for ((name, fn) in binary_operations.entries) {
                                val result = fn(a, b)
                                if (result)
                                    queue.addLast(Pair<List<String>,List<Int>>(
                                        history.plus("$a $name $b"), rest))
                            }
                        }
                    }
                }
            }
        }
    }

    fun solve(values: Collection<Int>):String {
        return values.sum().toString()
    }
}
