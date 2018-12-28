import engine.RiskEngine
import engine.world.buildSimpleWorld
import engine.world.buildWorld
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

var debug = false

fun main(args: Array<String>) {
    val (p1, p2, p3, p4, p5) = listOf("p1", "p2", "p3", "p4", "p5")
    val engine = RiskEngine(buildSimpleWorld(), p1, p2, p3, p4, p5)
    GlobalScope.launch {
        engine.start()
    }
    while (true) {
        var playerName: String?
        do {
            println("Choose player name")
            playerName = readLine()
        } while (playerName == null)
        var input: String?
        do {
            println("Choose input")
            input = readLine()
        } while (input == null)
        println(engine.processInputFrom(playerName, input))
    }
}
