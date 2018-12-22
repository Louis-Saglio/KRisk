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
