package learnwebsocket

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.ws
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

internal fun String.randomWord(): String {
    return this.map { this.random() }.joinToString(separator = "")
}

@KtorExperimentalAPI
suspend fun communicateWithServer() {
    val client = HttpClient(CIO).config { install(WebSockets) }
    client.ws(port = 8080, path = "/stream") {
        val id = "C${"0123".randomWord()}"
        println("$id starts")
        GlobalScope.launch {
            while (true) {
                println("$id >>> ")
                outgoing.send(Frame.Text("$id sends : " + readLine()!!))
            }
        }
        for (message in incoming) {
            val toSend = when (message) {
                is Frame.Text -> message.readText()
                else -> message.toString()
            }
            println("$id received : $toSend")
        }
        println("$id closes")
    }
    println("end")
}
