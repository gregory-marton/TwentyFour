package com.example.twentyfour

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.CheckBox
import android.widget.Toast
import android.util.Log
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {
    private lateinit var digitsInput: EditText
    private lateinit var solveButton: Button
    private lateinit var solutionsText: TextView
    private lateinit var factorialsCheck: CheckBox
    private lateinit var trianglesCheck: CheckBox
    private lateinit var squaresCheck: CheckBox

    private val solver = NumberSolver()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try {
            // Initialize views
            digitsInput = findViewById(R.id.digitsInput)
            solveButton = findViewById(R.id.solveButton)
            solutionsText = findViewById(R.id.solutionsText)
            factorialsCheck = findViewById(R.id.factorialsCheck)
            trianglesCheck = findViewById(R.id.trianglesCheck)
            squaresCheck = findViewById(R.id.squaresCheck)

            // Set up click listener
            solveButton.setOnClickListener {
                val digits = digitsInput.text.toString()
                if (digits.length != 4) {
                    Toast.makeText(this, "Please enter exactly 4 digits", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                solve(digits)
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error in onCreate", e)
            Toast.makeText(this, "Error initializing app: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun solve(digits: String) {
        coroutineScope.launch(Dispatchers.Main + CoroutineExceptionHandler { _, throwable ->
            Log.e("MainActivity", "Error in solve coroutine", throwable)
            solutionsText.text = "Error: ${throwable.message}"
            Toast.makeText(this, "Error solving: ${throwable.message}", Toast.LENGTH_LONG).show()
            solveButton.isEnabled = true
        }) {
            try {
                solveButton.isEnabled = false
                solutionsText.text = "Calculating..."

                // Convert settings
                solver.useFactorials = factorialsCheck.isChecked
                solver.useTriangles = trianglesCheck.isChecked
                solver.useSquares = squaresCheck.isChecked

                // Log the input and settings
                Log.d("MainActivity", "Solving for digits: $digits")
                Log.d("MainActivity", "Settings - Factorials: ${solver.useFactorials}, " +
                        "Triangles: ${solver.useTriangles}, Squares: ${solver.useSquares}")

                // Solve in background
                val solutions = withContext(Dispatchers.Default) {
                    solver.findSolutions(digits.map {
                        it.toString().toIntOrNull() ?: throw IllegalArgumentException("Invalid digit: $it")
                    })
                }

                // Log solutions
                Log.d("MainActivity", "Found ${solutions.size} solutions")

                // Update UI
                withContext(Dispatchers.Main) {
                    solutionsText.text = if (solutions.isEmpty()) {
                        "No solutions found"
                    } else {
                        solutions.joinToString("\n")
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error in solve function", e)
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                solveButton.isEnabled = true
            }
        }
    }

    override fun onDestroy() {
        try {
            coroutineScope.cancel()
            super.onDestroy()
        } catch (e: Exception) {
            Log.e("MainActivity", "Error in onDestroy", e)
        }
    }
}