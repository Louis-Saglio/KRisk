package server

import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
suspend fun main(args: Array<String>) {
    communicateWithServer("Dolor", "newgame", "dolor")
}
