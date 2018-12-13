package engine

import debug

fun <T> choose(message: String, ifDebug: () -> T, cast: (String?) -> T?, isValid: (T) -> Boolean): T {
    if (debug) return ifDebug()
    var chosen: T?
    do {
        println(message)
        val input = readLine()
        chosen = cast(input)
    } while (chosen == null || !isValid(chosen))
    return chosen
}
