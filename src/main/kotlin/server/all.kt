package server

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.ws
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
val client = HttpClient(CIO).config { install(WebSockets) }

@KtorExperimentalAPI
suspend fun main(args: Array<String>) {
    client.ws(port = 8080, path = "/games") {
        for (message in incoming) {
            println("Server sent to all : ${when (message) {
                is Frame.Text -> message.readText()
                else -> message.toString()
            }}")
        }
    }
}
