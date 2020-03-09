import java.util.Scanner

private var upperCaseCount: Int = 0
private var lowerCaseCount: Int = 0
private var digitsCount: Int = 0
private var symbolsCount: Int = 0

fun main() {
    val scanner = Scanner(System.`in`)
    upperCaseCount = scanner.nextInt()
    lowerCaseCount = scanner.nextInt()
    digitsCount = scanner.nextInt()
    symbolsCount = scanner.nextInt()
    val passwordChars = CharArray(symbolsCount)

    repeat(symbolsCount) {
        val randomIndex = getRandomIndex(passwordChars)
        val previousChar = if (randomIndex > 0) passwordChars[randomIndex - 1] else Char.MIN_VALUE
        val nextChar = if (randomIndex < passwordChars.lastIndex) passwordChars[randomIndex + 1] else Char.MIN_VALUE

        passwordChars[randomIndex] =
                if (upperCaseCount != 0 || lowerCaseCount != 0 || digitsCount != 0)
                    getRequiredResultChar(previousChar, nextChar)
                else
                    getResultChar(previousChar, nextChar)
    }

    println(String(passwordChars))
}

private fun getRequiredResultChar(previousChar: Char, nextChar: Char): Char {
    val rand = Math.random()

    return when {
        rand in 0.0..0.33 -> getResultUpperCaseChar(previousChar, nextChar, rand)
        rand > 0.33 && rand <= 0.66 -> getResultLowerCaseChar(previousChar, nextChar, rand)
        else -> getResultDigitChar(previousChar, nextChar, rand)
    }
}

private fun getResultUpperCaseChar(previousChar: Char, nextChar: Char, rand: Double): Char {
    val upperCaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"

    return if (upperCaseCount == 0) {
        getRequiredResultChar(previousChar, nextChar)
    } else {
        val currentChar = upperCaseLetters[(rand * upperCaseLetters.length).toInt()]
        if (currentChar == previousChar || currentChar == nextChar) {
            getRequiredResultChar(previousChar, nextChar)
        } else {
            upperCaseCount--
            currentChar
        }
    }
}

private fun getResultLowerCaseChar(previousChar: Char, nextChar: Char, rand: Double): Char {
    val lowerCaseLetters = "abcdefghijklmnopqrstuvwxyz"

    return if (lowerCaseCount == 0) {
        getRequiredResultChar(previousChar, nextChar)
    } else {
        val currentChar = lowerCaseLetters[(rand * lowerCaseLetters.length).toInt()]
        if (currentChar == previousChar || currentChar == nextChar) {
            getRequiredResultChar(previousChar, nextChar)
        } else {
            lowerCaseCount--
            currentChar
        }
    }
}

private fun getResultDigitChar(previousChar: Char, nextChar: Char, rand: Double): Char {
    val digits = "0123456789"

    return if (digitsCount == 0) {
        getRequiredResultChar(previousChar, nextChar)
    } else {
        val currentChar = digits[(rand * digits.length).toInt()]
        if (currentChar == previousChar || currentChar == nextChar) {
            getRequiredResultChar(previousChar, nextChar)
        } else {
            digitsCount--
            currentChar
        }
    }
}

private fun getResultChar(previousChar: Char, nextChar: Char): Char {
    val allowedSymbols = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    val rand = Math.random()
    val currentChar = allowedSymbols[(rand * allowedSymbols.length).toInt()]

    return if (currentChar == previousChar || currentChar == nextChar)
        getResultChar(previousChar, nextChar)
    else
        currentChar
}

private fun getRandomIndex(passwordChars: CharArray): Int {
    val randomIndex = (Math.random() * symbolsCount).toInt()

    return if (passwordChars[randomIndex] == Char.MIN_VALUE) randomIndex else getRandomIndex(passwordChars)
}
