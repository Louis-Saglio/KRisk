package engine

import engine.game.Players
import engine.game.world.World


class RiskEngine(val world: World, vararg playerNames: String) {

    private val players = Players(initialArmyNumberByPlayerNumber.getValue(playerNames.size), *playerNames)

    fun setupTerritories() {
        players.forEachClaimTerritory(world.getTerritories().shuffled())
    }

    fun placeInitialArmies() {
        players.setToFirst()
        while (players.count { it.getArmyToPlaceNumber() > 0 } > 0) {
            players.getActual().takeIf { it.getArmyToPlaceNumber() > 0 }?.placeOneArmy()
            players.passToNext()
        }
    }

    companion object {
        val initialArmyNumberByPlayerNumber = mapOf(
            Pair(3, 35),
            Pair(4, 30),
            Pair(5, 25),
            Pair(6, 20)
        )
    }

    internal fun getPlayersForTest() = players

    internal fun getTerritoriesForTest() = world.getTerritories()

}
