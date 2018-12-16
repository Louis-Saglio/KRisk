package engine

import debug

fun <T> choose(message: String, ifDebug: () -> T, cast: (String?) -> T?, isValid: ((T) -> Boolean)? = null): T {
    var chosen: T?
    do {
        chosen = if (debug) {
            val debugInput = ifDebug()
            println("debug $debugInput")
            debugInput
        }
        else {
            println(message)
            val input = readLine()
            cast(input)
        }
    } while (chosen == null || if (isValid!=null) !isValid(chosen) else false)
    return chosen
}
