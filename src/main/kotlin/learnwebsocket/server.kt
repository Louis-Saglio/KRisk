package learnwebsocket

import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.http.cio.websocket.Frame
import io.ktor.jackson.jackson
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.websocket.DefaultWebSocketServerSession
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.launch


class Server {
    val sockets = mutableListOf<DefaultWebSocketServerSession>()

    suspend fun broadcast(message: String) {
        for (socket in sockets) {
            socket.outgoing.send(Frame.Text(message))
        }
    }
}

val server = Server()


fun Application.stream() {
    install(ContentNegotiation) {
        jackson { }
    }
    install(WebSockets)
    routing {
        webSocket("/stream") {
            val id = "S${"0123".randomWord()}"
            outgoing.send(Frame.Text("welcome from $id"))
            server.sockets.add(this)
            println("$id onconnect")
//            for (frame in incoming.map { it as Frame.Text }) {
//                println("$id onMessage : ${frame.readText()}")
//            }
            try {
                incoming.receive()
            } catch (e: ClosedReceiveChannelException) {

            }
            println("$id onclose")
            server.sockets.remove(this)
        }
    }
}

suspend fun main(args: Array<String>) {
    GlobalScope.launch {
        embeddedServer(
            factory = Netty,
            module = Application::stream,
            host = "127.0.0.1",
            port = 8080
        ).start(true)
    }
    while (true) {
        println("Server broadcast (${server.sockets.size}) >>> ")
        server.broadcast("@everyone ${readLine()}")
    }
}
