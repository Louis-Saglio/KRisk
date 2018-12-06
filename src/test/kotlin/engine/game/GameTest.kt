package engine.game

import engine.game.world.Continent
import engine.game.world.Territory
import engine.game.world.World
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


internal class GameTest {

    private fun buildWorld(): World {
        val t1 = Territory("t1")
        val t2 = Territory("t2")
        val c1 = Continent("c1", 1, listOf(t1, t2))
        val t3 = Territory("t3")
        val t4 = Territory("t4")
        val t5 = Territory("t5")
        val c2 = Continent("c2", 1, listOf(t3, t4, t5))
        return World(listOf(c1, c2), listOf())
    }

    @Test
    fun setupTerritories() {
        val game = Game(buildWorld(), "p1", "p2", "p3")
        game.setupTerritories()
        val (p1, p2,p3) = game.getPlayersForTest()
        assertEquals(33, p1.getArmyToPlaceForTest())
        assertEquals(33, p2.getArmyToPlaceForTest())
        assertEquals(34, p3.getArmyToPlaceForTest())
        game.getTerritoriesForTest().forEach { assertEquals(1, it.getArmyNumberForTest(), it.name) }
        assertEquals(p1.getTerritoriesForTest().size, 2)
        assertEquals(p2.getTerritoriesForTest().size, 2)
        assertEquals(p3.getTerritoriesForTest().size, 1)
    }
}
