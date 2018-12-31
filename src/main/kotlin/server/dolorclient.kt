package server

import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
suspend fun main(args: Array<String>) {
    communicateWithServer("Dolor", "newgame", "unvrhuwxyfrysjnkfdxhjavidh")
}
