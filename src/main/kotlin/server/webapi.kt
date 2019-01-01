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
import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.close
import io.ktor.jackson.jackson
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.util.KtorExperimentalAPI
import io.ktor.websocket.DefaultWebSocketServerSession
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.ClosedReceiveChannelException
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


private class HighLevelPlayer(val code: String, val name: String) {
    val sockets = mutableListOf<DefaultWebSocketServerSession>()

    suspend fun send(gameState: GameState) {
        val jsonGameState = JSON.stringify(GameState.serializer(), gameState)
        for (socket in sockets) {
            socket.send(Frame.Text(jsonGameState))
        }
    }
}


private class HighLevelEngine(val code: String, val playerNumber: Int) {

    private val players = mutableListOf<HighLevelPlayer>()  // todo : use thread safe data structure
    private var engine: RiskEngine? = null

    private fun getPlayerNameByCode(code: String): String {
        return (players.find { it.code == code } ?: throw BadPlayerException("No player found for code $code")).name
    }

    internal fun addPlayer(playerName: String): AddPlayerResult {
        val randomWord: String
        if (players.size < playerNumber) {
            randomWord = "abcdefghijklmnopqrstuvwxyz".randomWord()
            players.add(HighLevelPlayer(randomWord, playerName))
        } else throw RuntimeException("Too much players")
        if (players.size == playerNumber) {
            engine = RiskEngine(buildSimpleWorld(), players.map { it.name }).apply {
                GlobalScope.launch { this@apply.start() }
            }
            return AddPlayerResult(true, randomWord)
        }
        return AddPlayerResult(false, randomWord)
    }

    internal fun toGameState(playerCode: String): GameState {
        return engine.run {
            if (this == null) {
                throw EngineNotStarted("Engine not started")
            }
            return@run GameState(getPlayerNameByCode(playerCode), this)
        }
    }

    internal suspend fun processInputFrom(playerCode: String, input: String): String {
        println("$this.processInputFrom $playerCode, $input")
        val result = engine.run {
            if (this == null) {
                throw EngineNotStarted("Engine not started")
            }
            return@run processInputFrom(getPlayerNameByCode(playerCode), input)
        }
        for (player in players) {
            player.send(toGameState(player.code))
        }
        return result
    }

    fun addSocketToPlayer(playerCode: String, socket: DefaultWebSocketServerSession) {
        val player = players.find { it.code == playerCode }
            ?: throw BadPlayerException("No player with code $playerCode found for $code")
        player.sockets.add(socket)
    }

    fun removeSocketFromPlayer(playerCode: String, socket: DefaultWebSocketServerSession) {
        val player = players.find { it.code == playerCode }
            ?: throw BadPlayerException("No player with code $playerCode found for $code")
        player.sockets.remove(socket)
    }

}

class EngineNotStarted(message: String) : Throwable(message)

class BadPlayerException(message: String) : Throwable(message)

// todo make private
internal fun String.randomWord(): String {
    return this.map { this.random() }.joinToString(separator = "")
}

private val engines = mutableListOf<HighLevelEngine>() // todo : use ConcurrentSkipListSet<HighLevelEngine>()

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
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
            route("{code}/players") {
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
                post("inputs") {
                    println("post input ${call.parameters}")
                    // todo send game state when game begins
                    // todo check with multiple engines
                    val engine = engines.find { engine -> engine.code == call.parameters["code"] }
                    if (engine == null) {
                        call.respond(HttpStatusCode.NotFound)
                    } else {
                        val gameInputData = call.receive<GameInputData>()
                        println("$gameInputData received by server")
                        try {
                            println("Try to process it")
                            engine.processInputFrom(gameInputData.playerCode, gameInputData.input)
                            println("Done")
                            call.respond(HttpStatusCode.OK)
                        } catch (e: BadPlayerException) {
                            call.respond(HttpStatusCode.NotFound, e.message ?: "")
                        } catch (e: EngineNotStarted) {
                            call.respond(HttpStatusCode.BadRequest, e.message ?: "")
                        }
                    }
                }
                webSocket(path = "/players/{playerCode}/state") {
                    val gameCode = call.parameters["code"]!!
                    val engine = engines.find { engine -> engine.code == gameCode }
                    if (engine == null) {
                        close(reason = CloseReason(HttpStatusCode.NotFound.value.toShort(), "No game with code $gameCode found"))
                    } else {
                        val playerCode = call.parameters["playerCode"]!!
                        try {
                            engine.addSocketToPlayer(playerCode, this)
                        } catch (e: BadPlayerException) {
                            println(e.message)
                            close(e)
                        } catch (e: EngineNotStarted) {
                            println(e.message)
                            close(e)
                        }
                        try {
                            incoming.receive()
                        } catch (e: ClosedReceiveChannelException) {
                            engine.removeSocketFromPlayer(playerCode, this)
                        }
                    }
                }
            }
        }
    }
}


@Synchronized
private fun <E> MutableCollection<E>.safeAdd(item: E) {
    this.add(item)
}

// todo : use custom exceptions
