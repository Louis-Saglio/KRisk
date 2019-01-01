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
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

data class CreateGameData(val code: String, val playerNumber: Int)


data class PlayerJoinData(val playerName: String, val wishedCode: String)


@Serializable
data class GameInputData(val playerCode: String, val input: String)


@Suppress("unused")
@Serializable
internal class GameState(val world: World, val players: List<PlayerPublicData>) {
    constructor(playerName: String, engine: RiskEngine) : this(engine.world, engine.getPlayersPublicData(playerName))
}


private class AddPlayerResult(val gameStarted: Boolean, val playerCode: String)


private class HighLevelPlayer(val code: String, val name: String) {
    private val sockets = mutableListOf<DefaultWebSocketServerSession>()
    private val socketsLock = ReentrantLock()

    suspend fun send(gameState: GameState) {
        val jsonGameState = JSON.stringify(GameState.serializer(), gameState)
        socketsLock.withLock {
            for (socket in sockets) {
                socket.send(Frame.Text(jsonGameState))
            }
        }
    }

    fun addSocket(socket: DefaultWebSocketServerSession) {
        socketsLock.withLock { sockets.add(socket) }
    }

    fun removeSocket(socket: DefaultWebSocketServerSession) {
        socketsLock.withLock { sockets.remove(socket) }
    }
}


private class HighLevelEngine(val code: String, val playerNumber: Int) {

    private val players = ConcurrentHashMap<String, HighLevelPlayer>()
    private var engine: RiskEngine? = null

    private fun getPlayerNameByCode(code: String): String {
        return (players[code] ?: throw BadPlayerException("No player found for code $code")).name
    }

    internal fun addPlayer(playerName: String, playerCode: String): AddPlayerResult {
        if (players.size < playerNumber) {
            players[playerCode] = HighLevelPlayer(playerCode, playerName)
        } else throw RuntimeException("Too much players")
        if (players.size == playerNumber) {
            engine = RiskEngine(buildSimpleWorld(), players.map { it.value.name }).apply {
                GlobalScope.launch { this@apply.start() }
            }
            return AddPlayerResult(true, playerCode)
        }
        return AddPlayerResult(false, playerCode)
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
        for (player in players.values) {
            player.send(toGameState(player.code))
        }
        return result
    }

    fun addSocketToPlayer(playerCode: String, socket: DefaultWebSocketServerSession) {
        val player = players[playerCode]
            ?: throw BadPlayerException("No player with code $playerCode found for $code")
        player.addSocket(socket)
    }

    fun removeSocketFromPlayer(playerCode: String, socket: DefaultWebSocketServerSession) {
        val player = players[playerCode]
            ?: throw BadPlayerException("No player with code $playerCode found for $code")
        player.removeSocket(socket)
    }

}

class EngineNotStarted(message: String) : Throwable(message)

class BadPlayerException(message: String) : Throwable(message)


private val engines = ConcurrentHashMap<String, HighLevelEngine>()

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
                engines[post.code] = engine
                call.respond(engine)
            }
            route("{code}") {
                post("players") {
                    val engine = engines[call.parameters["code"]]
                    if (engine == null) {
                        call.respond(HttpStatusCode.NotFound)
                    } else {
                        val joinData = call.receive<PlayerJoinData>()
                        val result = engine.addPlayer(joinData.playerName, joinData.wishedCode)
                        call.respond(HttpStatusCode.OK, result.playerCode)
                    }
                }
                post("inputs") {
                    println("post input ${call.parameters}")
                    // todo send game state when game begins
                    // todo check with multiple engines
                    val engine = engines[call.parameters["code"]]
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
                    val engine = engines[call.parameters["code"]]
                    if (engine == null) {
                        close(reason = CloseReason(HttpStatusCode.NotFound.value.toShort(), "No game with code $gameCode found"))
                    } else {
                        val playerCode = call.parameters["playerCode"]!!
                        try {
                            engine.addSocketToPlayer(playerCode, this)
                            try {
                                incoming.receive()
                            } catch (e: ClosedReceiveChannelException) {
                                engine.removeSocketFromPlayer(playerCode, this)
                            }
                        } catch (e: BadPlayerException) {
                            println(e.message)
                            close(e)
                        } catch (e: EngineNotStarted) {
                            println(e.message)
                            close(e)
                        }
                    }
                }
            }
        }
    }
}

// todo : use custom exceptions
// todo : handle game end
// todo : add logs
// todo : send state when rejoining
