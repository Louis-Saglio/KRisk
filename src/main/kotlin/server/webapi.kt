package server

import engine.PlayerPublicData
import engine.RiskEngine
import engine.world.World
import engine.world.buildSimpleWorld
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.jackson.jackson
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JSON

// POST /games : (code, playerNumber) -> <gameState>
// POST /games/<code>/players : (playerName) -> <gameState>

data class CreateGameData(val code: String, val playerNumber: Int)


data class PlayerJoinData(val playerName: String)


@Serializable
data class GameInputData(val playerName: String, val input: String)


@Suppress("unused")
@Serializable
internal class GameState(val world: World, val players: List<PlayerPublicData>) {
    constructor(playerName: String, engine: RiskEngine): this(engine.world, engine.getPlayersPublicData(playerName))
}


private class PreEngine(val code: String, val playerNumber: Int) {

    internal val players = mutableListOf<String>()

    internal fun addPlayer(player: String) {
        if (players.size < playerNumber) {
            players.safeAdd(player)
        } else throw RuntimeException("Too much players")
    }
}

private val engines = mutableMapOf<String, RiskEngine>()
private val preEngines = mutableListOf<PreEngine>()

@ObsoleteCoroutinesApi
fun Application.games() {
    install(ContentNegotiation) {
        jackson {  }
    }
    install(WebSockets)
    routing {
        route("games") {
            post("") {
                val post = call.receive<CreateGameData>()
                val preEngine = PreEngine(post.code, post.playerNumber)
                preEngines.safeAdd(preEngine)
                call.respond(preEngine)
            }
            route("{code}") {
                post("") {
                    val preEngine = preEngines.find { preEngine -> preEngine.code == call.parameters["code"] }
                    if (preEngine == null) {
                        call.respond(HttpStatusCode.NotFound)
                    } else {
                        val player = call.receive<PlayerJoinData>()
                        preEngine.addPlayer(player.playerName)
                        if (preEngine.players.size == preEngine.playerNumber) {
                            val riskEngine = RiskEngine(buildSimpleWorld(), preEngine.players)
                            GlobalScope.launch { riskEngine.start() }
                            engines.safePut(preEngine.code, riskEngine)
                            call.respond(GameState(player.playerName, riskEngine))
                        } else {
                            call.respond(HttpStatusCode.OK)
                        }
                    }
                }
                webSocket("input") {
                    incoming.mapNotNull { it as Frame.Text }.consumeEach { frame ->
                        val gameInputData = try {
                            JSON.parse(GameInputData.serializer(), frame.readText())
                        } catch (e: Exception) {
                            null
                        }
                        if (gameInputData == null) {
                            send(Frame.Text("${HttpStatusCode.BadRequest}}"))
                        } else {
                            val engine = engines[call.parameters["code"]]
                            if (engine == null) {
                                send(Frame.Text("${HttpStatusCode.NotFound}"))
                            } else {
                                engine.processInputFrom(gameInputData.playerName, gameInputData.input)
                                val message = GameState(gameInputData.playerName, engine)
                                send(Frame.Text(JSON.stringify(GameState.serializer(), message)))
                            }
                        }
                    }
                }
            }
        }
    }

}

@Synchronized
private fun <K, V> MutableMap<K, V>.safePut(key: K, value: V) {
    this[key] = value
}

@Synchronized
private fun <E> MutableCollection<E>.safeAdd(item: E) {
    this.add(item)
}
