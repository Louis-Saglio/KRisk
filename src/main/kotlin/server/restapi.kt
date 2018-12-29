package server

import engine.RiskEngine
import engine.world.buildSimpleWorld
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.routing.routing
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

// POST /games : (code, playerNumber) -> <gameState>
// POST /games/<code>/players : (playerName) -> <gameState>
// POST /games/<code>/input (input, playerName)  -> <gameState>

data class CreateGameData(val code: String, val playerNumber: Int)


data class PlayerJoinData(val playerName: String)


data class GameInputData(val playerName: String, val input: String)


internal class GameState(playerName: String, engine: RiskEngine) {
    val world = engine.world
    @Suppress("unused") // Used to create a Json response
    val players = engine.getPlayersPublicData(playerName)
}


private class PreEngine(val code: String, val playerNumber: Int) {

    internal val players = mutableListOf<String>()

    internal fun addPlayer(player: String) {
        if (players.size < playerNumber) {
            players.add(player)
        } else throw RuntimeException("Too much players")
    }
}

private val engines = mutableMapOf<String, RiskEngine>()
private val preEngines = mutableListOf<PreEngine>()

fun Application.games() {
    install(ContentNegotiation) {
        jackson {  }
    }
    routing {
        route("/games") {
            post("") {
                val post = call.receive<CreateGameData>()
                val preEngine = PreEngine(post.code, post.playerNumber)
                preEngines.add(preEngine)
                call.respond(preEngine)
            }
            route("/{code}") {
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
                            engines[preEngine.code] = riskEngine
                            call.respond(riskEngine)
                        } else {
                            call.respond(preEngine)
                        }
                    }
                }
                post("/input") {
                    val gameInputData = call.receive<GameInputData>()
                    val engine = engines[call.parameters["code"]]
                    if (engine == null) {
                        call.respond(HttpStatusCode.NotFound)
                    } else {
                        engine.processInputFrom(gameInputData.playerName, gameInputData.input)
                        call.respond(GameState(gameInputData.playerName, engine))
                    }
                }
            }
        }
    }
}
