package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CalculationHistory
import com.example.data.ExpressionEvaluator
import com.example.data.HistoryDatabase
import com.example.data.HistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.util.Locale

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: HistoryRepository

    init {
        val database = HistoryDatabase.getDatabase(application)
        repository = HistoryRepository(database.calculationHistoryDao())
    }

    val historyState: StateFlow<List<CalculationHistory>> = repository.allHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _expression = MutableStateFlow("")
    val expression = _expression.asStateFlow()

    private val _realTimeResult = MutableStateFlow("")
    val realTimeResult = _realTimeResult.asStateFlow()

    private val _useRadians = MutableStateFlow(false)
    val useRadians = _useRadians.asStateFlow()

    private var isResultFinal = false
    private val decimalFormat = DecimalFormat("#.##########", java.text.DecimalFormatSymbols(Locale.US))

    fun toggleAngleUnit() {
        _useRadians.value = !_useRadians.value
        evaluateRealTime()
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    fun selectHistoryItem(item: CalculationHistory) {
        _expression.value = item.expression
        _realTimeResult.value = item.result
        isResultFinal = true
    }

    fun handleKeyPress(key: String) {
        when (key) {
            "C" -> {
                _expression.value = ""
                _realTimeResult.value = ""
                isResultFinal = false
            }
            "⌫" -> {
                if (isResultFinal) {
                    _expression.value = ""
                    _realTimeResult.value = ""
                    isResultFinal = false
                } else if (_expression.value.isNotEmpty()) {
                    val exp = _expression.value
                    val newExp = when {
                        exp.endsWith("sin(") || exp.endsWith("cos(") || exp.endsWith("tan(") || exp.endsWith("log(") -> exp.dropLast(4)
                        exp.endsWith("sqrt(") -> exp.dropLast(5)
                        exp.endsWith("ln(") -> exp.dropLast(3)
                        else -> exp.dropLast(1)
                    }
                    _expression.value = newExp
                    evaluateRealTime()
                }
            }
            "=" -> {
                val exp = _expression.value
                if (exp.isBlank()) return
                try {
                    val doubleValue = ExpressionEvaluator.evaluate(exp, _useRadians.value)
                    if (doubleValue.isNaN()) {
                        _realTimeResult.value = "Error"
                    } else if (doubleValue.isInfinite()) {
                        _realTimeResult.value = "Infinity"
                    } else {
                        val formatted = formatResult(doubleValue)
                        _realTimeResult.value = formatted
                        saveCalculation(exp, formatted)
                        isResultFinal = true
                    }
                } catch (e: Exception) {
                    _realTimeResult.value = "Error"
                }
            }
            "+/-" -> {
                if (isResultFinal) {
                    isResultFinal = false
                }
                val exp = _expression.value
                if (exp.isEmpty()) {
                    _expression.value = "-"
                } else if (exp.startsWith("-") && exp.length == 1) {
                    _expression.value = ""
                } else if (exp.startsWith("-")) {
                    _expression.value = exp.substring(1)
                } else {
                    _expression.value = "-$exp"
                }
                evaluateRealTime()
            }
            "( )" -> {
                if (isResultFinal) {
                    _expression.value = ""
                    isResultFinal = false
                }
                val exp = _expression.value
                val openBracketCount = exp.count { char -> char == '(' }
                val closeBracketCount = exp.count { char -> char == ')' }
                val lastChar = exp.lastOrNull()

                if (lastChar == null) {
                    _expression.value += "("
                } else {
                    val lastCharChar = lastChar
                    val isOp = lastCharChar == '+' || lastCharChar == '−' || lastCharChar == '×' || lastCharChar == '÷' || lastCharChar == '(' || lastCharChar == '^' || lastCharChar == '√'
                    if (isOp) {
                        _expression.value += "("
                    } else if (openBracketCount > closeBracketCount && (lastCharChar.isDigit() || lastCharChar == ')' || lastCharChar == 'π' || lastCharChar == 'e' || lastCharChar == '%')) {
                        _expression.value += ")"
                    } else {
                        _expression.value += "("
                    }
                }
                evaluateRealTime()
            }
            else -> {
                if (isResultFinal) {
                    if (isOperator(key)) {
                        _expression.value = _realTimeResult.value + key
                    } else {
                        _expression.value = key
                    }
                    isResultFinal = false
                } else {
                    _expression.value += key
                }
                evaluateRealTime()
            }
        }
    }

    private fun isOperator(key: String): Boolean {
        return key in setOf("+", "−", "×", "÷", "^")
    }

    private fun evaluateRealTime() {
        val exp = _expression.value
        if (exp.isBlank()) {
            _realTimeResult.value = ""
            return
        }
        var cleanExp = exp
        while (cleanExp.isNotEmpty() && isOperator(cleanExp.last().toString())) {
            cleanExp = cleanExp.dropLast(1)
        }
        if (cleanExp.isEmpty()) {
            _realTimeResult.value = ""
            return
        }

        val openCount = cleanExp.count { char -> char == '(' }
        val closeCount = cleanExp.count { char -> char == ')' }
        if (openCount > closeCount) {
            cleanExp += ")".repeat(openCount - closeCount)
        }

        try {
            val doubleValue = ExpressionEvaluator.evaluate(cleanExp, _useRadians.value)
            if (!doubleValue.isNaN() && !doubleValue.isInfinite()) {
                _realTimeResult.value = formatResult(doubleValue)
            }
        } catch (e: Exception) {
            // Ignore syntax transient states
        }
    }

    private fun formatResult(resultValue: Double): String {
        return if (resultValue == resultValue.toLong().toDouble()) {
            resultValue.toLong().toString()
        } else {
            decimalFormat.format(resultValue)
        }
    }

    private fun saveCalculation(expr: String, res: String) {
        viewModelScope.launch {
            repository.insert(CalculationHistory(expression = expr, result = res))
        }
    }
}
