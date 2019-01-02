import io.ktor.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import server.games

var debug = false

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
@ObsoleteCoroutinesApi
fun main(args: Array<String>) {
    embeddedServer(
        factory = Netty,
        watchPaths = listOf("server"),
        module = Application::games,
        host = "127.0.0.1",
        port = 8080
    ).start(true)
}
