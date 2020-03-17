package calculator

import java.lang.IllegalArgumentException
import java.math.BigInteger
import java.util.Scanner

private fun String.isNumber() = this.matches(Regex("[0-9]+"))
private fun String.isVariable() = this.matches(Regex("[A-Za-z]+"))
private fun String.isPlus() = this == "+"
private fun String.isMinus() = this == "-"
private fun String.isMultiply() = this == "*"
private fun String.isDivide() = this == "/"
private fun String.isOperator() = this.isPlus() || this.isMinus() || this.isMultiply() || this.isDivide()
private fun String.isLeftParenthesis() = this == "("
private fun String.isRightParenthesis() = this == ")"

val variables = mutableMapOf<String, String>()

fun main() {
    val scanner = Scanner(System.`in`)
    var input = scanner.nextLine()

    while (input != "/exit") {
        if (input.isNotEmpty()) {
            when {
                input == "/help" -> {
                    println("The program calculates the sum of numbers")
                }
                input[0] == '/' -> {
                    println("Unknown command")
                }
                else -> {
                    runOperation(input)
                }
            }
        }

        input = scanner.nextLine()
    }

    println("Bye!")
}

private fun runOperation(input: String) {
    val isCalculateExpression = input.contains('+') || input.contains('-')

    if (isCalculateExpression)
        calculateExpression(input)
    else
        operateVariables(input)
}

private fun validateMathOperationExpression(expression: String): Boolean {
    val expressionParts = getExpressionParts(expression)

    return isLastSymbolValid(expression) && isOperandsAndOperatorsValid(expressionParts)
}

private fun getExpressionParts(expression: String): MutableList<String> {
    val whitespacelessExpression = expression.filter { !it.isWhitespace() }
    val expressionParts = mutableListOf<String>()
    var previousChar: Char = expression[0]
    var expressionPart = String()

    for (char in whitespacelessExpression) {
        if (Character.isLetter(char) && Character.isLetter(previousChar) ||
                Character.isDigit(char) && Character.isDigit(previousChar)) {
            expressionPart = expressionPart.plus(char)
        } else if (char == '-' && previousChar == '-') {
            expressionPart = "+"
            previousChar = '+'
        } else if (char == '-' && previousChar == '+') {
            expressionPart = "-"
            previousChar = '-'
        } else if (!(char == '+' && previousChar == '+')){
            expressionParts.add(expressionPart)
            expressionPart = "$char"
            previousChar = char
        }
    }
    expressionParts.add(expressionPart) //add last

    return expressionParts
}

private fun isLastSymbolValid(expression: String): Boolean =
        expression[expression.lastIndex] != '+' && expression[expression.lastIndex] != '-' &&
        expression[expression.lastIndex] != '*' && expression[expression.lastIndex] != '/'

private fun isOperandsAndOperatorsValid(expressionParts: List<String>): Boolean {
    if (expressionParts.count { it.isLeftParenthesis() } != expressionParts.count { it.isRightParenthesis() })
        return false

    val parentesislessExpressionPart = expressionParts.filter { !it.isLeftParenthesis() && !it.isRightParenthesis() }

    for (i in 0..parentesislessExpressionPart.lastIndex) {
        if (i % 2 == 0) {
            if (!isOperandValid(parentesislessExpressionPart[i]))
                return false
        } else {
            if (!isOperatorValid(parentesislessExpressionPart[i]))
                return false
        }
    }

    return true
}

private fun isOperandValid(operand: String) = operand.isNumber() || operand.isVariable() && variables.containsKey(operand)

private fun isOperatorValid(operator: String) = operator.isPlus() || operator.isMinus()|| operator.isMultiply()|| operator.isDivide()

private fun calculateExpression(expression: String) {
    if (validateMathOperationExpression(expression)) {
        val expressionParts = getExpressionParts(expression)

        val sum = when (expressionParts.size) {
            1 -> getValue(expressionParts[0])
            else -> {
                val postfixNotationExpressionParts = convertToPostfixNotation(expressionParts)
                calculateExpressionFromPostfixNotation(postfixNotationExpressionParts)
            }
        }

        println("$sum")
    } else {
        println("Invalid expression")
    }
}

private fun convertToPostfixNotation(expressionParts: MutableList<String>): MutableList<String> {
    val operatorOrderMap = mutableMapOf("*" to 2, "/" to 2, "+" to 1, "-" to 1)
    val postfixStack = mutableListOf<String>()
    val result = mutableListOf<String>()

    for (expressionPart in expressionParts) {
        if (expressionPart.isNumber() || expressionPart.isVariable()) {
            result.add(expressionPart)
        } else if(postfixStack.isEmpty() || postfixStack.peek().isLeftParenthesis() ||
                expressionPart.isOperator() && operatorOrderMap[expressionPart]!! > operatorOrderMap[postfixStack.peek()]!! ||
                expressionPart.isLeftParenthesis()) {
            postfixStack.push(expressionPart)
        } else if (expressionPart.isOperator() && operatorOrderMap[expressionPart]!! <= operatorOrderMap[postfixStack.peek()]!!) {
            var isPushed = false
            while (!isPushed) {
                if (postfixStack.isNotEmpty()) {
                    val postfixStackPeek = postfixStack.peek()
                    if (postfixStackPeek.isOperator() && operatorOrderMap[expressionPart]!! > operatorOrderMap[postfixStackPeek]!! ||
                            postfixStackPeek.isLeftParenthesis()) {
                        postfixStack.push(expressionPart)
                        isPushed = true
                    } else {
                        result.add(postfixStack.pop())
                    }
                } else {
                    postfixStack.push(expressionPart)
                    isPushed = true
                }
            }
        } else if (expressionPart.isRightParenthesis()) {
            var isDiscarded = false
            while (!isDiscarded) {
                val postfixStackPeek = postfixStack.peek()
                if (postfixStackPeek.isLeftParenthesis()) {
                    postfixStack.pop()
                    isDiscarded = true
                } else {
                    result.add(postfixStack.pop())
                }
            }
        }
    }
    val stackSize = postfixStack.size

    repeat(stackSize) {
        result.add(postfixStack.pop())
    }

    return result
}

private fun calculateExpressionFromPostfixNotation(postfixNotationExpression: MutableList<String>): BigInteger {
    val finalResultStack = mutableListOf<BigInteger>()

    for (expressionPart in postfixNotationExpression) {
        when {
            expressionPart.isNumber() -> {
                finalResultStack.push(BigInteger(expressionPart))
            }
            expressionPart.isVariable() -> {
                finalResultStack.push(getValue(expressionPart))
            }
            expressionPart.isOperator() -> {
                val secondOperand = finalResultStack.pop()
                val firstOperand = finalResultStack.pop()
                val operationResult =
                        when (expressionPart) {
                            "+" -> firstOperand + secondOperand
                            "-" -> firstOperand - secondOperand
                            "*" -> firstOperand * secondOperand
                            "/" -> firstOperand / secondOperand
                            else -> throw IllegalArgumentException()
                        }
                finalResultStack.push(operationResult)
            }
        }
    }

    return finalResultStack.peek()
}

private fun getValue(operand: String): BigInteger = if (operand.isNumber()) BigInteger(operand) else BigInteger(variables[operand]!!)

private fun operateVariables(expression: String) {
    val pureExpression = expression.replace(" ", "")
    val isVariableCall = pureExpression.replace(Regex("[A-Za-z0-9]"), "").isEmpty()

    if (isVariableCall) {
        printVariableValueOrErrorMessage(pureExpression)
    } else {
        assignValueToIdentifier(pureExpression)
    }
}

private fun printVariableValueOrErrorMessage(identifier: String) {
    if (variables.containsKey(identifier)) {
        println(variables[identifier])
    } else {
        println("Unknown variable")
    }
}

private fun assignValueToIdentifier(assigningExpression: String) {
    val operands = assigningExpression.split("=")
    val isOperandsCountValid = operands.size == 2 && operands[0].isNotEmpty() && operands[1].isNotEmpty()

    if (isOperandsCountValid) {
        val isValidIdentifier = operands[0].replace(Regex("[A-Za-z]"), "").isEmpty()

        if (isValidIdentifier) {
            if (operands[1].isNumber()) {
                variables[operands[0]] = operands[1]
            } else if (operands[1].isVariable()) {
                if (variables.containsKey(operands[1])) {
                    variables[operands[0]] = variables[operands[1]]!!
                } else {
                    println("Unknown variable")
                }
            } else {
                println("Invalid assignment")
            }
        } else {
            println("Invalid identifier")
        }
    } else {
        println("Invalid assignment")
    }
}

fun  <T> MutableList<T>.push(element: T) = this.add(this.count(), element)

fun <T> MutableList<T>.pop(): T {
    val element = this[this.lastIndex]
    this.removeAt(this.lastIndex)

    return element
}

fun <T> MutableList<T>.peek() = this[this.lastIndex]
