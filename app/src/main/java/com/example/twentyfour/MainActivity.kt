package com.example.twentyfour

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var digitsInput: EditText
    private lateinit var solveButton: Button
    private lateinit var solutionsText: TextView
    private lateinit var factorialsCheck: CheckBox
    private lateinit var trianglesCheck: CheckBox
    private lateinit var squaresCheck: CheckBox

    private val solver = NumberSolver()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())
    private var calculationJob: Job? = null // Keep track of the calculation job

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

            // Make solutionsText clickable
            solutionsText.isClickable = true
            solutionsText.setOnClickListener {
                if (calculationJob?.isActive == true) {
                    showCancelDialog()
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error in onCreate", e)
            Toast.makeText(this, "Error initializing app: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun solve(digits: String) {
        // Cancel any existing job
        calculationJob?.cancel()

        calculationJob = coroutineScope.launch(Dispatchers.Main + CoroutineExceptionHandler { _, throwable ->
            Log.e("MainActivity", "Error in solve coroutine", throwable)
            solutionsText.text = "Error: ${throwable.message}"
            Toast.makeText(this, "Error solving: ${throwable.message}", Toast.LENGTH_LONG).show()
            resetUI() // Reset UI on error
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
                if (e is CancellationException) {
                    Log.i("MainActivity", "Calculation cancelled")
                } else {
                    Log.e("MainActivity", "Error in solve function", e)
                    Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } finally {
                resetUI() // Reset UI after calculation (success or cancellation)
            }
        }
    }

    private fun showCancelDialog() {
        AlertDialog.Builder(this)
            .setTitle("Cancel Calculation")
            .setMessage("Are you sure you want to cancel the current calculation?")
            .setPositiveButton("Yes") { _, _ ->
                calculationJob?.cancel()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun resetUI() {
        solveButton.isEnabled = true
        solutionsText.text = "" // Clear the "Calculating..." or "Calculation cancelled" message
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