package engine

import engine.world.World
import org.jetbrains.annotations.TestOnly


class RiskEngine(val world: World, vararg playerNames: String) {

    private val players = Players(this, initialArmyNumberByPlayerNumber.getValue(playerNames.size), *playerNames)
    val cards = world.getTerritories().map { Card(it, Symbol.values().random()) }.shuffled().toMutableList()

    fun setupTerritories() {
        println("RiskEngine.setupTerritories")
        println(world)
        players.forEachClaimTerritory(world.getTerritories().shuffled())
    }

    fun placeInitialArmies() {
        println("RiskEngine.placeInitialArmies")
        players.setToFirst()
        while (players.any { it.getArmyToPlaceNumber() > 0 }) {
            players.getActual().takeIf { it.getArmyToPlaceNumber() > 0 }?.placeOneArmy()
            players.passToNext()
        }
        resumePlayerWorldSituation()
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
        var turnNumber = 0
        var somebodyHasWon: Boolean
        do {
            turnNumber++
            println("Turn $turnNumber start")
            val playingPlayer = players.getActual()
            if (!playingPlayer.isDefeated()) {
                println("#&1 $playingPlayer ${playingPlayer.getTerritories()}")
                playingPlayer.manageReinforcement()

                playingPlayer.hasConqueredTerritory = false
                var attackIsPossible: Boolean
                do {
                    attackIsPossible = playerAttacksOneTerritory(playingPlayer)
                } while (attackIsPossible && chooseYesOrNo("Continue attack ?"))

                if (playingPlayer.hasConqueredTerritory && cards.isNotEmpty()) {
                    val card = cards[cards.size - 1]
                    cards.remove(card)
                    playingPlayer.addCard(card)
                } else if (!playingPlayer.hasConqueredTerritory) {
                    println("$playingPlayer didn't conquer a territory")
                } else if (cards.isEmpty()) {
                    println("No more card to give")
                }
                playingPlayer.fortifyPosition()
                somebodyHasWon = players.any(Player::hasWon)
                if (somebodyHasWon) break
            } else {
                println("Skip turn because defeated")
            }
            println("Turn $turnNumber end")
            players.passToNext()
        } while (true)
        println("${players.getActual()} wins the game")
        println(players.getActual().getTerritories())
        println("Total turn number : $turnNumber")
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
// todo make initialArmyNumberByPlayerNumber parametrable
// todo make card symbol equally distributed
