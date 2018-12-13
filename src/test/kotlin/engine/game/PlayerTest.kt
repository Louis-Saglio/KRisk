package engine.game

import debug
import engine.RiskEngine
import engine.game.world.Territory
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import kotlin.test.assertEquals
import engine.game.world.Continent
import engine.game.world.World


internal class PlayerTest {

    lateinit var engine: RiskEngine
    lateinit var player: Player
    lateinit var territory: Territory

    init {
        debug = true
    }

    @BeforeEach
    internal fun setUp() {
        engine = mock(RiskEngine::class.java)
        player = Player(engine, "tester", 42) // todo : last arg nullable ?
        territory = Territory("motherland")
    }

    @Test
    fun claimTerritory() {
        player.claimTerritory(territory)
        assertEquals(1, territory.armyNumber)
        assertEquals(41, player.getArmyToPlaceNumber())
    }

    @Test
    fun placeOneArmy() {
        player.addTerritoryForTest(territory)
        player.placeOneArmy()
        assertEquals(1, territory.armyNumber)
        assertEquals(41, player.getArmyToPlaceNumber())
    }

    @Disabled("Not yet implemented")
    @Test
    fun hasWon() {
    }

    @Disabled("Not yet implemented")
    @Test
    fun playTurn() {
    }

    @Test
    fun computeContinentalReinforcement() {
        val continent = Continent("ctn", 3, listOf(territory))
        `when`(engine.world).thenReturn(mock(World::class.java))
        `when`(engine.world.continents).thenReturn(listOf(continent))
        player.addTerritoryForTest(territory)
        player.computeContinentalReinforcementForTest()
        assertEquals(45, player.getArmyToPlaceNumber())
    }
}
