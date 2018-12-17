package engine

import debug

const val maxInputTryNumber = 100
fun <T> choose(message: String, ifDebug: () -> String?, cast: (String?) -> T?, isValid: ((T) -> Boolean)? = null): T {
    var chosen: T?
    var triedInputNumber = 0
    do {
        println(message)
        val input = if (debug) ifDebug() else readLine()
        chosen = cast(input)
        triedInputNumber++
        if (debug && triedInputNumber > maxInputTryNumber)
            throw RuntimeException("Too many input tried")
    } while (chosen == null || if (isValid!=null) !isValid(chosen) else false)
    return chosen
}
