package server

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.ws
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


@KtorExperimentalAPI
suspend fun communicateWithServer(name: String, gameCode: String, playerCode: String) {
    val client = HttpClient(CIO).config { install(WebSockets) }
    client.ws(port = 8080, path = "/games/$gameCode/input") {
        GlobalScope.launch {
            do {
                println("$name client >>> ")
                val text = readLine()!!
                send(Frame.Text("""{"playerCode": "$playerCode", "input": "$text"}"""))
            } while (true)
        }
        for (message in incoming) {
            val toSend = when (message) {
                is Frame.Text -> message.readText()
                else -> message.toString()
            }
            println("Server sent to $name : $toSend")
        }
    }
}
