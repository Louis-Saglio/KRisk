package engine

import engine.game.Player
import engine.game.Players
import engine.game.world.Territory
import engine.game.world.World

abstract class Input(val playerName: String)

class TerritoryInput(playerName: String, val territory: Territory): Input(playerName)

class RiskEngine(val world: World, vararg playerNames: String): Engine<Input, Unit>() {

    private val players = Players(initialArmyNumberByPlayerNumber.getValue(playerNames.size), *playerNames)

    init {
        setupTerritories()
        players.setToFirst()
    }

    override suspend fun handle(input: Input) {
        val player = players.find { it.name == input.playerName }
        if (player != null) {
            when (input) {
                is TerritoryInput -> player.process(input.territory)
            }
        }
    }

    private fun setupTerritories() {
        players.forEachClaimTerritory(world.getTerritories().shuffled())
    }

    suspend fun playTurns() {
        while (true) {
            for (player in players.filter { !it.isEliminated() }) {
                player.playTurn(computeReinforcementNumberFor(player))
            }
        }
    }

    private fun computeReinforcementNumberFor(player: Player): Int {
        return 0
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
