import io.ktor.application.Application
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.ws
import io.ktor.http.HttpMethod
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.filterNotNull
import kotlinx.coroutines.channels.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import server.games

var debug = false

@KtorExperimentalAPI
@ObsoleteCoroutinesApi
fun main(args: Array<String>) {
    embeddedServer(
        factory = Netty,
        watchPaths = listOf("server"),
        module = Application::games,
        host = "127.0.0.1",
        port = 8080
    ).start(false)

    runBlocking {
        val client = HttpClient(CIO).config { install(WebSockets) }

        client.ws(method = HttpMethod.Get, host = "127.0.0.1", port = 8080, path = "/games/newgame/input") {
            val session = this
            GlobalScope.launch {
                for (message in session.incoming.map { it as Frame.Text }.filterNotNull()) {
                    println("client: server sent ${message.readText()}")
                }
            }
            println("websocket client >>> ")
            while (true) {
                val input = readLine()!!
                send(Frame.Text(input))
            }
        }
    }
}
