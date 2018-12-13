package engine

import engine.game.Player
import engine.game.Players
import engine.game.world.World
import org.jetbrains.annotations.TestOnly


class RiskEngine(val world: World, vararg playerNames: String) {

    private val players = Players(this, initialArmyNumberByPlayerNumber.getValue(playerNames.size), *playerNames)

    fun setupTerritories() {
        players.forEachClaimTerritory(world.getTerritories().shuffled())
    }

    fun placeInitialArmies() {
        players.setToFirst()
        while (players.any { it.getArmyToPlaceNumber() > 0 }) {
            players.getActual().takeIf { it.getArmyToPlaceNumber() > 0 }?.placeOneArmy()
            players.passToNext()
        }
    }

    fun playTurns() {
        do {
            players.getActual().playTurn()
            players.passToNext()
        } while (players.none(Player::hasWon))
    }

    companion object {
        val initialArmyNumberByPlayerNumber = mapOf(
            Pair(3, 35),
            Pair(4, 30),
            Pair(5, 25),
            Pair(6, 20)
        )
    }

    @TestOnly
    internal fun getPlayersForTest() = players

    @TestOnly
    internal fun getTerritoriesForTest() = world.getTerritories()

}
