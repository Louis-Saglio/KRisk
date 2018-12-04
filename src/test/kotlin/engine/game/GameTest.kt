package engine.game

import engine.game.world.Continent
import engine.game.world.Territory
import engine.game.world.World
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


internal class GameTest {

    @Test
    fun setupTerritories() {
        val t1 = Territory("t1")
        val t2 = Territory("t2")
        val c1 = Continent("c1", 1, listOf(t1, t2))
        val t3 = Territory("t3")
        val c2 = Continent("c2", 1, listOf(t3))
        val world = World(listOf(c1, c2), listOf())
        val player = Player("p1", 20)
        val game = Game(Players(player), world)
        game.setupTerritories()
        assertEquals(17, player.getInitialArmyNumberForTest())
        listOf(t1, t2, t3).map { assertTrue(player.getTerritoriesForTest().contains(it)) }
    }
}
