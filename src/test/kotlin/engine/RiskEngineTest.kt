package engine

import debug
import engine.game.world.Continent
import engine.game.world.Territory
import engine.game.world.World
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.RepeatedTest
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class RiskEngineTest {

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

    private var engine = RiskEngine(buildWorld(), "p1", "p2", "p3")
    private var players = engine.getPlayersForTest()

    @BeforeEach
    internal fun setUp() {
        engine = RiskEngine(buildWorld(), "p1", "p2", "p3")
        players = engine.getPlayersForTest()
    }

    @RepeatedTest(9)
    fun setupTerritories() {
        engine.setupTerritories()
        players.forEach {
            assertTrue(it.getArmyToPlaceNumber() in 33..34)
            assertTrue(it.getTerritories().size in 1..2)
        }
        engine.getTerritoriesForTest().forEach { kotlin.test.assertEquals(1, it.armyNumber, it.name) }
    }

    @RepeatedTest(9)
    fun placeInitialArmies() {
        debug = true
        engine.setupTerritories()
        engine.placeInitialArmies()
        players.forEach { assertEquals(0, it.getArmyToPlaceNumber()) }
        players.forEach {
            assertEquals(it.getTerritories().sumBy { territory -> territory.armyNumber }, 35)
        }
    }
}
