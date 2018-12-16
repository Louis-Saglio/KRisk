package engine

import debug

fun <T> choose(message: String, ifDebug: () -> T, cast: (String?) -> T?, isValid: ((T) -> Boolean)? = null): T {
    if (debug) return ifDebug()
    var chosen: T?
    do {
        println(message)
        val input = readLine()
        chosen = cast(input)
    } while (chosen == null || if (isValid!=null) !isValid(chosen) else false)
    return chosen
}
