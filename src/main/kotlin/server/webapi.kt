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
// POST /games/<code>/players : (playerName) -> <playerCode>

data class CreateGameData(val code: String, val playerNumber: Int)


data class PlayerJoinData(val playerName: String)


@Serializable
data class GameInputData(val playerCode: String, val input: String)


@Suppress("unused")
@Serializable
internal class GameState(val world: World, val players: List<PlayerPublicData>) {
    constructor(playerName: String, engine: RiskEngine) : this(engine.world, engine.getPlayersPublicData(playerName))
}


private class AddPlayerResult(val gameStarted: Boolean, val playerCode: String)


private class HighLevelEngine(val code: String, val playerNumber: Int) {

    private val playerNames = mutableMapOf<String, String>()
    private var engine: RiskEngine? = null

    internal fun addPlayer(playerName: String): AddPlayerResult {
        val randomWord: String
        if (playerNames.size < playerNumber) {
            randomWord = "abcdefghijklmnopqrstuvwxyz".randomWord()
            playerNames.safePut(randomWord, playerName)
        } else throw RuntimeException("Too much players")
        if (playerNames.size == playerNumber) {
            engine = RiskEngine(buildSimpleWorld(), playerNames.values).apply {
                GlobalScope.launch { this@apply.start() }
            }
            return AddPlayerResult(true, randomWord)
        }
        return AddPlayerResult(false, randomWord)
    }

    internal fun toGameState(playerName: String): GameState {
        return engine.run {
            if (this != null) GameState(playerName, this) else throw RuntimeException("Engine not started")
        }
    }

    internal fun processInputFrom(playerCode: String, input: String): String {
        val playerName = playerNames[playerCode] ?: throw BadPlayerException("No player found for code $playerCode")
        return engine.run { this?.processInputFrom(playerName, input) ?: throw RuntimeException("Engine not started") }
    }

}

class BadPlayerException(message: String) : Throwable(message)

private fun String.randomWord(): String {
    return this.map { this.random() }.joinToString(separator = "")
}

private val engines = mutableListOf<HighLevelEngine>()

@ObsoleteCoroutinesApi
fun Application.games() {
    install(ContentNegotiation) {
        jackson { }
    }
    install(WebSockets)
    routing {
        route("games") {
            post("") {
                val post = call.receive<CreateGameData>()
                val engine = HighLevelEngine(post.code, post.playerNumber)
                engines.safeAdd(engine)
                call.respond(engine)
            }
            route("{code}") {
                post("") {
                    val engine = engines.find { riskEngine -> riskEngine.code == call.parameters["code"] }
                    if (engine == null) {
                        call.respond(HttpStatusCode.NotFound)
                    } else {
                        val player = call.receive<PlayerJoinData>()
                        val result = engine.addPlayer(player.playerName)
                        call.respond(HttpStatusCode.OK, result.playerCode)
                    }
                }
                webSocket("input") {
                    // todo send game state when game begins
                    // todo check with multiple engines
                    incoming.mapNotNull { it as Frame.Text }.consumeEach { frame ->
                        // todo game input data must contains player code
                        val gameInputData = try {
                            JSON.parse(GameInputData.serializer(), frame.readText())
                        } catch (e: Exception) {
                            null
                        }
                        if (gameInputData == null) {
                            send(Frame.Text("${HttpStatusCode.BadRequest}}"))
                        } else {
                            val engine = engines.find { it.code == call.parameters["code"] }
                            if (engine == null) {
                                send(Frame.Text("${HttpStatusCode.NotFound}"))
                            } else {
                                engine.processInputFrom(gameInputData.playerCode, gameInputData.input)
                                send(Frame.Text(JSON.stringify(GameState.serializer(), engine.toGameState(gameInputData.playerCode))))
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

@Synchronized
private fun <E> MutableCollection<E>.safeRemove(item: E) {
    this.remove(item)
}

// todo : use custom exceptions
