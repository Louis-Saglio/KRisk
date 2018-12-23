package engine

import debug
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
        println(world)
        players.forEachClaimTerritory(world.getTerritories().shuffled())
    }

    fun placeInitialArmies() {
        println("RiskEngine.placeInitialArmies")
        debug = true
        players.setToFirst()
        while (players.any { it.getArmyToPlaceNumber() > 0 }) {
            players.getActual().takeIf { it.getArmyToPlaceNumber() > 0 }?.placeOneArmy()
            players.passToNext()
        }
        resumePlayerWorldSituation()
//        debug = false
    }

    private fun resumePlayerWorldSituation() {
        println("World situation :")
        for (player in players) {
            for (territory in player.getTerritories()) {
                println("$player $territory")
            }
        }
    }

    fun playTurns() {
        var somebodyHasWon: Boolean
        do {
            val playingPlayer = players.getActual()
            playingPlayer.manageReinforcement()

            var attackIsPossible: Boolean
            do {
                attackIsPossible = playerAttacksOneTerritory(playingPlayer)
            } while (attackIsPossible && chooseYesOrNo("Continue attack ?"))

            playingPlayer.fortifyPosition()
            somebodyHasWon = players.any(Player::hasWon)
            if (somebodyHasWon) break
            players.passToNext()
        } while (true)
        println("${players.getActual()} wins the game")
    }

    private fun playerAttacksOneTerritory(player: Player): Boolean {
        println("RiskEngine.playerAttacksOneTerritory")
        val from = player.chooseTerritoryToAttackFrom()
        if (from == null) {
            println("No attack possible")
            return false
        }
        val target = player.chooseTargetToAttackFrom(from)
        val attacker = PlayerTerritory(player, from)
        val defender = PlayerTerritory(
            players.getPlayerByTerritory(target) ?: throw RuntimeException("Nobody owns $target"),
            target
        )
        println("$player attacks $target of ${defender.player} with $from")
        BattleManager(attacker, defender).fight()
        return true
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
// todo 2 reinfo combi
// todo make initialArmyNumberByPlayerNumber parametrable
// todo do not fortify if no border
// todo get card when win a new territory once by turn
// todo manage first input suggestion taking in account possible input existence for second input
