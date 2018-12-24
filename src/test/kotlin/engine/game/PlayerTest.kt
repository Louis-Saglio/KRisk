package engine.game

import debug
import engine.RiskEngine
import engine.game.world.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import kotlin.test.assertEquals
import kotlin.test.assertTrue


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
        player = Player(engine, "tester", 1) // todo : last arg nullable ?
        territory = Territory("motherland")
    }

    @Test
    fun claimTerritory() {
        player.claimTerritory(territory)
        assertEquals(1, territory.armyNumber)
        assertEquals(0, player.getArmyToPlaceNumber())
    }

    @Test
    fun placeOneArmy() {
        `when`(engine.world).thenReturn(mock(World::class.java))
        `when`(engine.world.getTerritoryByName("motherland")).thenReturn(territory)
        player.addTerritoryForTest(territory)
        player.placeOneArmy()
        assertEquals(1, territory.armyNumber)
        assertEquals(0, player.getArmyToPlaceNumber())
    }

    @Test
    fun hasWon() {
        `when`(engine.world).thenReturn(mock(World::class.java))
        `when`(engine.world.getTerritories()).thenReturn(listOf(territory))
        player.addTerritoryForTest(territory)
        assertTrue(player.hasWon())
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
        assertEquals(4, player.getArmyToPlaceNumber())
    }

    @Test
    fun computeTerritorialReinforcementLessThan3() {
        val continent = Continent("ctn", 3, listOf(territory))
        `when`(engine.world).thenReturn(mock(World::class.java))
        `when`(engine.world.continents).thenReturn(listOf(continent))
        player.addTerritoryForTest(territory)
        player.addTerritoryForTest(Territory("t1"))
        player.computeTerritorialReinforcementForTest()
        assertEquals(4, player.getArmyToPlaceNumber())
    }

    @Test
    fun computeTerritorialReinforcementMoreThan3() {
        val continent = Continent("ctn", 3, listOf(territory))
        `when`(engine.world).thenReturn(mock(World::class.java))
        `when`(engine.world.continents).thenReturn(listOf(continent))
        player.addTerritoryForTest(territory)
        player.addTerritoryForTest(Territory("t1"))
        player.addTerritoryForTest(Territory("t1"))
        player.addTerritoryForTest(Territory("t1"))
        player.addTerritoryForTest(Territory("t1"))
        player.addTerritoryForTest(Territory("t1"))
        player.addTerritoryForTest(Territory("t1"))
        player.addTerritoryForTest(Territory("t1"))
        player.addTerritoryForTest(Territory("t1"))
        player.addTerritoryForTest(Territory("t1"))
        player.addTerritoryForTest(Territory("t1"))
        player.addTerritoryForTest(Territory("t1"))
        player.addTerritoryForTest(Territory("t1"))
        player.computeTerritorialReinforcementForTest()
        assertEquals(5, player.getArmyToPlaceNumber())
    }

    @Test
    fun getCombinationReinforcement() {
        val territory1 = Territory("1")
        player.addTerritoryForTest(territory1)
        val territory2 = Territory("2")
        player.addTerritoryForTest(territory2)
        player.addCardForTest(Card(territory, Symbol.CAVALRY))
        player.addCardForTest(Card(territory1, Symbol.CAVALRY))
        player.addCardForTest(Card(territory2, Symbol.CAVALRY))
        player.getCombinationReinforcementForTest()
        assertEquals(7, player.getArmyToPlaceNumber())
    }

    @RepeatedTest(9)
    fun fortifyPosition() {
        player.addTerritoryForTest(territory)
        val territory1 = Territory("t1")
        `when`(engine.world).thenReturn(mock(World::class.java))
        `when`(engine.world.areNeighbours(territory, territory1)).thenReturn(true)
        `when`(engine.world.areNeighbours(territory1, territory)).thenReturn(true)
        `when`(engine.world.getTerritoryByName("motherland")).thenReturn(territory)
        `when`(engine.world.getTerritoryByName("t1")).thenReturn(territory1)
        player.addTerritoryForTest(territory1)
        territory.increaseArmyNumber(6)
        territory1.increaseArmyNumber(7)
        player.fortifyPositionForTest()
        assertTrue(territory.armyNumber in 1..12)
        assertTrue(territory1.armyNumber in 1..13)
    }

    @Test
    fun manageReinforcement() {
        val continent = Continent("ctn", 3, listOf(territory))
        `when`(engine.world).thenReturn(mock(World::class.java))
        `when`(engine.world.continents).thenReturn(listOf(continent))
        `when`(engine.world.getTerritoryByName("motherland")).thenReturn(territory)
        player.addCardForTest(Card(territory, Symbol.CAVALRY))
        val territory1 = Territory("1")
        val territory2 = Territory("2")
        player.addTerritoryForTest(territory1)
        player.addTerritoryForTest(territory2)
        player.addTerritoryForTest(territory)
        player.addCardForTest(Card(territory1, Symbol.CAVALRY))
        player.addCardForTest(Card(territory2, Symbol.CAVALRY))
        player.manageReinforcementForTest()
        assertEquals(0, player.getArmyToPlaceNumber())
        assertEquals(15, territory.armyNumber)
    }

    @Test
    fun getAllPossibleSetOfThreeOwnedCards() {
        val card1 = Card(territory, Symbol.INFANTRY)
        val card2 = Card(territory, Symbol.CAVALRY)
        val card3 = Card(territory, Symbol.ARTILLERY)
        val card4 = Card(territory, Symbol.INFANTRY)
        player.addCard(card1)
        player.addCard(card2)
        player.addCard(card3)
        player.addCard(card4)
        val solution = setOf(
            setOf(card1, card2, card3),
            setOf(card1, card2, card4),
            setOf(card1, card3, card4),
            setOf(card2, card3, card4)
        )
        player.getAllPossibleSetOfThreeOwnedCardsForTest().forEach {
            assertTrue(it in solution)
        }
    }
}
