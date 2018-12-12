package engine.game

import engine.game.world.World

class NaiveRiskEngine(val world: World, vararg playerNames: String) {
    private val players = Players(initialArmyNumberByPlayerNumber.getValue(playerNames.size), *playerNames)

    init {
        setupTerritories()
    }

    companion object {
        val initialArmyNumberByPlayerNumber = mapOf(
            Pair(3, 35),
            Pair(4, 30),
            Pair(5, 25),
            Pair(6, 20)
        )
    }

    private fun setupTerritories() {
        players.forEachClaimTerritory(world.getTerritories().shuffled())
    }

    fun placeInitialArmies() {
        while (players.count { it.getRemainingArmyToPlaceNumber() > 0 } > 0) {
            for (player in players) {
                if (player.getRemainingArmyToPlaceNumber() > 0) {
                    player.placeOneArmy()
                }
            }
        }
    }
}
