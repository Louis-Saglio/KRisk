package engine.game

import engine.game.world.World
import engine.game.world.buildWorld

class Game(private val world: World = buildWorld(), vararg playerNames: String) {
    private val players = Players(initialArmyNumberByPlayerNumber.getValue(playerNames.size), *playerNames)

    internal fun setupTerritories() {
        for (territory in world.getTerritories().shuffled()) {
            players.getActual().addTerritory(territory)
            players.passToNext()
        }
    }

    companion object {
        val initialArmyNumberByPlayerNumber = mapOf(
            Pair(3, 35),
            Pair(4, 30),
            Pair(5, 25),
            Pair(4, 20)
        )
    }

    internal fun getPlayersForTest() = players

    internal fun getTerritoriesForTest() = world.getTerritories()
}
