package server

import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun main(args: Array<String>) {
    GlobalScope.launch { communicateWithServer("Sit", "oldgame", "sit") }
    GlobalScope.launch { communicateWithServer("Amet", "oldgame", "amet") }
    GlobalScope.launch { communicateWithServer("Donec", "oldgame", "donec") }.join()
}
