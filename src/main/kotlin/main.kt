import engine.RiskEngine
import engine.game.world.buildSimpleWorld
import engine.game.world.buildWorld

var debug = true

fun main(args: Array<String>) {
    val (p1, p2, p3, p4, p5) = listOf("p1", "p2", "p3", "p4", "p5")
    val engine = RiskEngine(buildSimpleWorld(), p1, p2, p3, p4, p5)
    engine.setupTerritories()
    engine.placeInitialArmies()
    engine.playTurns()
}
