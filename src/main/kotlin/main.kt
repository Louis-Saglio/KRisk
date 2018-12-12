import engine.RiskEngine
import engine.PlaceOneArmy
import engine.game.world.buildWorld

suspend fun main(args: Array<String>) {
    val (p1, p2, p3) = listOf("p1", "p2", "p3")
    val engine = RiskEngine(buildWorld(), p1, p2, p3)
    engine.start()
    val territory = engine.world.getTerritoryByName("Chine")
    val result = engine.processAndReturn(PlaceOneArmy(territory, p1))
    println(result)
}
