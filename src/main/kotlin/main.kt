import engine.RiskEngine
import engine.game.world.buildSimpleWorld
import engine.game.world.buildWorld

var debug = false

fun main(args: Array<String>) {
    val (p1, p2, p3) = listOf("p1", "p2", "p3")
    val engine = RiskEngine(buildSimpleWorld(), p1, p2, p3)
    engine.setupTerritories()
    engine.placeInitialArmies()
    engine.playTurns()
}
