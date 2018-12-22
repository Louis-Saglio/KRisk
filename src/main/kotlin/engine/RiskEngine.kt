package engine

import engine.game.BattleManager
import engine.game.Player
import engine.game.PlayerTerritory
import engine.game.Players
import engine.game.world.World
import org.jetbrains.annotations.TestOnly


class RiskEngine(val world: World, vararg playerNames: String) {

    private val players = Players(this, initialArmyNumberByPlayerNumber.getValue(playerNames.size), *playerNames)

    fun setupTerritories() {
        println("RiskEngine.setupTerritories")
        players.forEachClaimTerritory(world.getTerritories().shuffled())
    }

    fun placeInitialArmies() {
        println("RiskEngine.placeInitialArmies")
        players.setToFirst()
        while (players.any { it.getArmyToPlaceNumber() > 0 }) {
            players.getActual().takeIf { it.getArmyToPlaceNumber() > 0 }?.placeOneArmy()
            players.passToNext()
        }
    }

    fun playTurns() {
        do {
            val playingPlayer = players.getActual()
            println("Au tour de $playingPlayer")
            playingPlayer.manageReinforcement()

            do {
                playerAttacksOneTerritory(playingPlayer)
            } while (chooseYesOrNo("Continue attack ?"))

            playingPlayer.fortifyPosition()
            players.passToNext()
        } while (players.none(Player::hasWon))
        println("${players.find(Player::hasWon)} wins the game")
    }

    private fun playerAttacksOneTerritory(player: Player) {
        println("RiskEngine.playerAttacksOneTerritory")
        val from = player.chooseOwnedTerritory { it.armyNumber >= 2 }
        val target = player.chooseTargetToAttackFrom(from)
        val attacker = PlayerTerritory(player, from)
        val defender = PlayerTerritory(
            players.getPlayerByTerritory(target) ?: throw RuntimeException("Nobody owns $target"),
            target
        )
        println("$player attacks $target of ${defender.player} with $from")
        BattleManager(attacker, defender).fight()

        player.fortifyPosition()
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
