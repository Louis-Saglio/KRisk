package server

import engine.PlayerPublicData
import engine.RiskEngine
import engine.world.World
import engine.world.buildSimpleWorld
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CORS
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.close
import io.ktor.http.content.file
import io.ktor.http.content.static
import io.ktor.http.content.staticRootFolder
import io.ktor.jackson.jackson
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.get
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
import kotlinx.serialization.list
import java.io.File
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

data class CreateGameData(val code: String, val playerNumber: Int)


data class PlayerJoinData(val playerName: String, val wishedCode: String)


@Serializable
data class GameInputData(val input: String)


@Suppress("unused")
@Serializable
internal class GameState private constructor(val world: World, val players: List<PlayerPublicData>) {
    constructor(playerName: String, engine: RiskEngine) : this(engine.world, engine.getPlayersPublicData(playerName))
}


@Serializable
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

    override fun toString(): String {
        return "$name:$code"
    }
}

@Serializable
data class GameResume(val code: String, val playerNumber: Int, val actualPlayerNumber: Int)

private class HighLevelEngine(val code: String, val playerNumber: Int) {

    private val players = ConcurrentHashMap<String, HighLevelPlayer>()
    private var engine: RiskEngine? = null

    init {
        println("$this created")
    }

    val actualPlayerNumber: Int
        get() = players.size

    private fun getPlayerNameByCode(code: String): String {
        val name = (players[code] ?: throw BadPlayerException("No player found for code $code")).name
        println("$this.getPlayerNameByCode $code : $name")
        return name
    }

    internal fun addPlayer(playerName: String, playerCode: String): AddPlayerResult {
        println("$this.addPlayer $playerName, $playerCode")
        if (players.size < playerNumber) {
            players[playerCode] = HighLevelPlayer(playerCode, playerName)
        } else {
            println("Can't add because too much player in $this")
            throw TooMuchPlayerException()
        }
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

    internal fun toGameResume(): GameResume {
        return GameResume(code, playerNumber, actualPlayerNumber)
    }

    private suspend fun broadcastState() {
        for (player in players.values) {
            player.send(toGameState(player.code))
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
        broadcastState()
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

    override fun toString(): String {
        return "Game($code, $playerNumber)"
    }

}

class TooMuchPlayerException : Throwable()

class EngineNotStarted(message: String) : Throwable(message)

class BadPlayerException(message: String) : Throwable(message)


private val engines = ConcurrentHashMap<String, HighLevelEngine>()
private val engineListClientSockets = mutableListOf<DefaultWebSocketServerSession>()

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
fun Application.games() {
    install(CORS) {
        anyHost()
    }
    install(ContentNegotiation) {
        jackson { }
    }
    install(WebSockets)
    routing {
        get("favicon.ico") {
            call.respond(HttpStatusCode.OK)
        }
        route("games") {
            route("front") {
                static {
                    staticRootFolder = File("/home/louis/Projects/Kotlin/KRisk/src/main/kotlin/server/front")
                    file("index.html")
                }
            }
            webSocket {
                println("Connexion on ws /games")
                engineListClientSockets.safeAdd(this)
                this.send(Frame.Text(JSON.stringify(GameResume.serializer().list, engines.values.map { it1 -> it1.toGameResume() })))
                println(engineListClientSockets)
                try {
                    incoming.receive()
                } catch(e: ClosedReceiveChannelException) {
                    println("$this has been closed")
                } finally {
                    engineListClientSockets.safeRemove(this)
                }
            }
            post {
                println("/games")
                val post = try {
                    call.receive<CreateGameData>()
                } catch (e: IOException) {
                    println(e)
                    return@post call.respond(HttpStatusCode.BadRequest)
                }
                println("/games $post")
                val engine = HighLevelEngine(post.code, post.playerNumber)
                engines[post.code] = engine
                for (socket in engineListClientSockets) {
                    socket.send(Frame.Text(JSON.stringify(GameResume.serializer().list, engines.values.map { it1 -> it1.toGameResume() })))
                }
                return@post call.respond(HttpStatusCode.OK)
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
                            println(e)
                            return@post call.respond(HttpStatusCode.BadRequest)
                        }
                        println("received : $joinData")
                        val result = try {
                            engine.addPlayer(joinData.playerName, joinData.wishedCode)
                        } catch (e: TooMuchPlayerException) {
                            return@post call.respond(HttpStatusCode.Locked, "${engine.code} is full")
                        }
                        for (socket in engineListClientSockets) {
                            socket.send(Frame.Text(JSON.stringify(GameResume.serializer().list, engines.values.map { it1 -> it1.toGameResume() })))
                        }
                        return@post call.respond(JSON.stringify(AddPlayerResult.serializer(), result))
                    }
                }
                post("players/{playerCode}/inputs") {
                    println("/games/{code}/players/{playerCode}/inputs ${call.parameters}}")
                    val engine = engines[call.parameters["code"]]
                    if (engine == null) {
                        return@post call.respond(HttpStatusCode.NotFound)
                    } else {
                        val gameInputData = call.receive<GameInputData>()
                        println("$gameInputData received by server")
                        try {
                            println("Try to process it")
                            engine.processInputFrom(call.parameters["playerCode"]!!, gameInputData.input)
                            println("Done")
                            return@post call.respond(HttpStatusCode.OK)
                        } catch (e: BadPlayerException) {
                            return@post call.respond(HttpStatusCode.NotFound, e.message ?: "")
                        } catch (e: EngineNotStarted) {
                            return@post call.respond(HttpStatusCode.BadRequest, e.message ?: "")
                        }
                    }
                }
                webSocket("players/{playerCode}/state") {
                    println("/games/players/{playerCode}/state ${call.parameters}")
                    val gameCode = call.parameters["code"]!!
                    val engine = engines[call.parameters["code"]]
                    if (engine == null) {
                        close(reason = CloseReason(HttpStatusCode.NotFound.value.toShort(), "No game with code $gameCode found"))
                    } else {
                        val playerCode = call.parameters["playerCode"]!!
                        try {
                            engine.addSocketToPlayer(playerCode, this)
                            send(Frame.Text(JSON.stringify(GameState.serializer(), engine.toGameState(playerCode))))
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

@Synchronized
private fun <E> MutableCollection<E>.safeRemove(e: E) {
    this.remove(e)
}

@Synchronized
private fun <E> MutableCollection<E>.safeAdd(e: E) {
    this.add(e)
}

// todo : handle game end
// todo : notify client when game start
// todo : send state when rejoining
// todo : notify client when it is his turn
// todo : send into game state rolled dices (bundle choose as input information or with game state)
