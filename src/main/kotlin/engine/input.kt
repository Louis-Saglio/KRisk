package engine

import debug

class InputSuggestion(val value: String, toDisplay: String? = null) {
    private val toDisplay = toDisplay ?: value

    override fun toString(): String {
        return toDisplay
    }
}

const val maxInputTryNumber = 100
fun <T> choose(
    message: String? = null,
    ifDebug: () -> String?,
    cast: (String?) -> T?,
    inputSuggestions: List<InputSuggestion>? = null,
    isValid: ((T) -> Boolean)? = null
): T {
    if (inputSuggestions != null)
        println("Choose between ${inputSuggestions.filter {
            val suggestion = cast(it.value)
            suggestion != null && isValid?.invoke(suggestion) ?: true
        }}")
    var chosen: T?
    var triedInputNumber = 0
    do {
        if (debug && triedInputNumber > maxInputTryNumber)
            throw RuntimeException("Too many input tried")
        if (message != null) println(message)
        val input = if (debug) ifDebug() else readLine()
        if (debug) {
            println(">>> $input")
        }
        chosen = cast(input)
        triedInputNumber++
    } while (chosen == null || if (isValid!=null) !isValid(chosen) else false)
    println("chosen $chosen")
    return chosen
}

fun chooseYesOrNo(message: String): Boolean {
    println("chooseYesOrNo")
    return choose(
        "$message : yes/no",
        { setOf("yes", "no").random() },
        {
            when (it?.toLowerCase()) {
                "yes" -> true
                "no" -> false
                else -> null
            }
        }
    )
}
