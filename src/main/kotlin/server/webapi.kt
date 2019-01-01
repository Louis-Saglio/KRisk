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
import java.io.IOException
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
        println("$this.send $gameState")
        val jsonGameState = JSON.stringify(GameState.serializer(), gameState)
        socketsLock.withLock {
            for (socket in sockets) {
                socket.send(Frame.Text(jsonGameState))
            }
        }
    }

    fun addSocket(socket: DefaultWebSocketServerSession) {
        println("$this.addSocket $socket")
        socketsLock.withLock { sockets.add(socket) }
    }

    fun removeSocket(socket: DefaultWebSocketServerSession) {
        println("$this.removeSocket $socket")
        socketsLock.withLock { sockets.remove(socket) }
    }
}


private class HighLevelEngine(val code: String, val playerNumber: Int) {

    private val players = ConcurrentHashMap<String, HighLevelPlayer>()
    private var engine: RiskEngine? = null

    private fun getPlayerNameByCode(code: String): String {
        val name = (players[code] ?: throw BadPlayerException("No player found for code $code")).name
        println("$this.getPlayerNameByCode $code : $name")
        return name
    }

    internal fun addPlayer(playerName: String, playerCode: String): AddPlayerResult {
        println("$this.addPlayer $playerName, $playerCode")
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
        println("$this.toGameState $playerCode")
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
        println("$this.addSocketToPlayer $playerCode, $socket")
        val player = players[playerCode]
            ?: throw BadPlayerException("No player with code $playerCode found for $code")
        player.addSocket(socket)
    }

    fun removeSocketFromPlayer(playerCode: String, socket: DefaultWebSocketServerSession) {
        println("$this.removeSocketFromPlayer $playerCode, $socket")
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
                val post = try {
                    call.receive<CreateGameData>()
                } catch (e: IOException) {
                    return@post call.respond(HttpStatusCode.BadRequest)
                }
                println("/games $post")
                val engine = HighLevelEngine(post.code, post.playerNumber)
                engines[post.code] = engine
                return@post call.respond(engine)
            }
            route("{code}") {
                post("players") {
                    println("/games/players/ ${call.parameters}")
                    val engine = engines[call.parameters["code"]]
                    if (engine == null) {
                        return@post call.respond(HttpStatusCode.NotFound)
                    } else {
                        val joinData = try {
                            call.receive<PlayerJoinData>()
                        } catch (e: IOException) {
                            return@post call.respond(HttpStatusCode.BadRequest)
                        }
                        println("received : $joinData")
                        val result = engine.addPlayer(joinData.playerName, joinData.wishedCode)
                        return@post call.respond(HttpStatusCode.OK, result.playerCode)
                    }
                }
                post("inputs") {
                    // todo playerCode in path
                    println("/games/{code}/players/{playerCode}/inputs ${call.parameters}}")
                    val engine = engines[call.parameters["code"]]
                    if (engine == null) {
                        return@post call.respond(HttpStatusCode.NotFound)
                    } else {
                        val gameInputData = call.receive<GameInputData>()
                        println("$gameInputData received by server")
                        try {
                            println("Try to process it")
                            engine.processInputFrom(gameInputData.playerCode, gameInputData.input)
                            println("Done")
                            return@post call.respond(HttpStatusCode.OK)
                        } catch (e: BadPlayerException) {
                            return@post call.respond(HttpStatusCode.NotFound, e.message ?: "")
                        } catch (e: EngineNotStarted) {
                            return@post call.respond(HttpStatusCode.BadRequest, e.message ?: "")
                        }
                    }
                }
                webSocket(path = "/players/{playerCode}/state") {
                    println("/games/players/{playerCode}/state ${call.parameters}")
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
