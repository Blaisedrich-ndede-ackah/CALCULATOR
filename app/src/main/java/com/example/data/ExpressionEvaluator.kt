package com.example.data

import kotlin.math.*

object ExpressionEvaluator {
    fun evaluate(expression: String, useRadians: Boolean = false): Double {
        val sanitized = expression
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("π", PI.toString())
            .replace("e", E.toString())
            .replace(" ", "")

        return Parser(sanitized, useRadians).parse()
    }

    private class Parser(private val str: String, private val useRadians: Boolean) {
        private var pos = -1
        private var ch = 0

        private fun nextChar() {
            ch = if (++pos < str.length) str[pos].code else -1
        }

        private fun eat(charToEat: Int): Boolean {
            while (ch == ' '.code) nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            nextChar()
            val x = parseExpression()
            if (pos < str.length) throw IllegalArgumentException("Unexpected character: " + ch.toChar())
            return x
        }

        // expression = term | expression `+` term | expression `-` term
        private fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                if (eat('+'.code)) x += parseTerm() // addition
                else if (eat('-'.code)) x -= parseTerm() // subtraction
                else break
            }
            return x
        }

        // term = factor | term `*` factor | term `/` factor
        private fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                if (eat('*'.code)) x *= parseFactor() // multiplication
                else if (eat('/'.code)) {
                    val divisor = parseFactor()
                    if (divisor == 0.0) throw ArithmeticException("Division by zero")
                    x /= divisor // division
                } else break
            }
            return x
        }

        // factor = unary `^` factor | unary
        private fun parseFactor(): Double {
            var x = parseUnary()
            if (eat('^'.code)) {
                x = x.pow(parseFactor()) // power
            }
            return x
        }

        // unary = `+` unary | `-` unary | func unary | primary
        private fun parseUnary(): Double {
            if (eat('+'.code)) return parseUnary() 
            if (eat('-'.code)) return -parseUnary() 

            val startPos = this.pos
            var x: Double
            if (eat('('.code)) { 
                x = parseExpression()
                eat(')'.code)
                x = handlePostprimary(x)
            } else if (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) { 
                while (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) nextChar()
                val numStr = str.substring(startPos, this.pos)
                x = numStr.toDoubleOrNull() ?: throw IllegalArgumentException("Invalid number: $numStr")
                x = handlePostprimary(x)
            } else if (ch >= 'a'.code && ch <= 'z'.code || ch == '√'.code.toInt()) { 
                var isSqrt = false
                if (eat('√'.code)) {
                    isSqrt = true
                }
                while (ch >= 'a'.code && ch <= 'z'.code) nextChar()
                val funcName = if (isSqrt) "sqrt" else str.substring(startPos, this.pos)
                
                val arg = if (eat('('.code)) {
                    val res = parseExpression()
                    eat(')'.code)
                    res
                } else {
                    parseUnary()
                }

                x = when (funcName) {
                    "sqrt", "√" -> {
                        if (arg < 0) throw IllegalArgumentException("Square root of negative number")
                        sqrt(arg)
                    }
                    "sin" -> {
                        val angle = if (useRadians) arg else Math.toRadians(arg)
                        sin(angle)
                    }
                    "cos" -> {
                        val angle = if (useRadians) arg else Math.toRadians(arg)
                        cos(angle)
                    }
                    "tan" -> {
                        val angle = if (useRadians) arg else Math.toRadians(arg)
                        tan(angle)
                    }
                    "ln" -> {
                        if (arg <= 0) throw IllegalArgumentException("Log of non-positive number")
                        ln(arg)
                    }
                    "log" -> {
                        if (arg <= 0) throw IllegalArgumentException("Log of non-positive number")
                        log10(arg)
                    }
                    else -> throw IllegalArgumentException("Unknown function: $funcName")
                }
                x = handlePostprimary(x)
            } else {
                throw IllegalArgumentException("Unexpected character: " + ch.toChar())
            }

            return x
        }

        private fun handlePostprimary(value: Double): Double {
            var x = value
            while (true) {
                if (eat('%'.code)) {
                    x /= 100.0
                } else if (eat('!'.code)) {
                    x = factorial(x)
                } else {
                    break
                }
            }
            return x
        }

        private fun factorial(n: Double): Double {
            if (n < 0.0 || n != n.toInt().toDouble()) {
                throw IllegalArgumentException("Factorial of non-negative integer only")
            }
            val num = n.toInt()
            if (num > 100) throw IllegalArgumentException("Overflow for factorial")
            var result = 1.0
            for (i in 2..num) {
                result *= i
            }
            return result
        }
    }
}
