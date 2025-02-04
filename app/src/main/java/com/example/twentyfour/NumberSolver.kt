package com.example.twentyfour

//import kotlinx.coroutines.yield

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlin.math.floor
import kotlin.math.sqrt

class NumberSolver {
    var useFactorials = true
    var useTriangles = false
    var useSquares = false

    private val target = 24
    private lateinit var job: Job

    private fun factorial(n: Int): Int? {
        return when {
            n < 0 -> null
            n == 0 -> 1
            n >= 10 -> null  // Prevent overflow
            else -> {
                var result = 1
                for (i in 1..n) {
                    result *= i
                }
                result
            }
        }
    }

    private fun triangular(n: Int): Int? {
        return when {
            n < 0 -> null
            n >= 100 -> null  // Prevent overflow
            else -> n * (n + 1) / 2
        }
    }

    private fun triangularRoot(n: Int): Int? {
        val discriminant = 8 * n + 1
        val root = sqrt(discriminant.toDouble())
        if (root != floor(root)) return null
        val result = (root - 1) / 2
        if (result != floor(result)) return null
        return result.toInt()
    }

    private fun square(n: Int): Int? {
        return when {
            n < 0 -> null
            n >= 100 -> null  // Prevent overflow
            else -> n * n
        }
    }

    private fun squareRoot(n: Int): Int? {
        val root = sqrt(n.toDouble())
        if (root != floor(root)) return null
        return root.toInt()
    }

    private fun applyOperation(a: Int, b: Int, op: String): Int? {
        return when (op) {
            "+" -> a + b
            "-" -> a - b
            "*" -> if (a * b <= 10000) a * b else null
            "/" -> if (b != 0 && a % b == 0) a / b else null
            else -> null
        }
    }

    private fun applyUnaryOperation(n: Int, op: String): Int? {
        return when (op) {
            "!" -> if (useFactorials) factorial(n) else null
            "Δ" -> if (useTriangles) triangular(n) else null
            "Δ√" -> if (useTriangles) triangularRoot(n) else null
            "□" -> if (useSquares) square(n) else null
            "□√" -> if (useSquares) squareRoot(n) else null
            else -> null
        }
    }

    fun findSolutions(numbers: List<Int>): List<String> {
        val solutions = mutableListOf<String>()
        this.job = CoroutineScope(Dispatchers.Default).launch {
            findSolutionsRecursive(numbers.toMutableList(), "", solutions, 0)
        }
        return solutions.take(3)  // Return top 3 solutions
    }

    private val MAX_DEPTH = 10  // Limit recursion depth

    private fun findSolutionsRecursive(
        numbers: MutableList<Int>,
        steps: String,
        solutions: MutableList<String>,
        depth: Int
    ) {
        this.job.ensureActive()
        if (depth > MAX_DEPTH) return  // Prevent stack overflow
        if (solutions.size >= 3) return  // Limit to 3 solutions

        // Check if we've found a solution
        if (numbers.size == 1 && numbers[0] == target) {
            solutions.add(steps.trim())
            return
        }

        // Try binary operations
        for (i in numbers.indices) {
            for (j in i + 1 until numbers.size) {
                val a = numbers[i]
                val b = numbers[j]

                // Create new list without used numbers
                val remainingNumbers = numbers.toMutableList()
                remainingNumbers.removeAt(j)
                remainingNumbers.removeAt(i)

                // Try each operation
                for (op in listOf("+", "-", "*", "/")) {
                    val result = applyOperation(a, b, op)
                    if (result != null) {
                        remainingNumbers.add(result)
                        findSolutionsRecursive(
                            remainingNumbers,
                            "$steps $a$op$b=>$result",
                            solutions,
                            depth + 1
                        )
                        remainingNumbers.removeAt(remainingNumbers.lastIndex)
                    }

                    // Try reverse order for non-commutative operations
                    if (op in listOf("-", "/")) {
                        val nonCommutativeResult = applyOperation(b, a, op)
                        if (nonCommutativeResult != null) {
                            remainingNumbers.add(nonCommutativeResult)
                            findSolutionsRecursive(
                                remainingNumbers,
                                "$steps $b$op$a=>$nonCommutativeResult",
                                solutions,
                                30)
                            remainingNumbers.removeAt(remainingNumbers.lastIndex)
                        }
                    }
                }
            }
        }

        // Try unary operations
        if (useFactorials || useTriangles || useSquares) {
            for (i in numbers.indices) {
                val n = numbers[i]
                val remainingNumbers = numbers.toMutableList()
                remainingNumbers.removeAt(i)

                for (op in listOf("!", "Δ", "Δ√", "□", "□√")) {
                    val result = applyUnaryOperation(n, op)
                    if (result != null) {
                        remainingNumbers.add(result)
                        findSolutionsRecursive(
                            remainingNumbers,
                            "$steps $op$n=>$result",
                            solutions,
                            depth + 1
                        )
                        remainingNumbers.removeAt(remainingNumbers.lastIndex)
                    }
                }
            }
        }
    }
}