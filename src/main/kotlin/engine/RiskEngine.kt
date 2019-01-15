package engine

import com.fasterxml.jackson.annotation.JsonIgnore
import engine.world.World
import engine.world.buildWorld
import org.jetbrains.annotations.TestOnly
import java.util.concurrent.LinkedBlockingQueue


class RiskEngine(val world: World = buildWorld(), playerNames: Collection<String>) {

    private val players = Players(this, initialArmyNumberByPlayerNumber.getValue(playerNames.size), playerNames)
    private val cards = world.getTerritories().map { Card(it, Symbol.values().random()) }.shuffled().toMutableList()

    private val inputQueue = LinkedBlockingQueue<String>()
    private val outputQueue = LinkedBlockingQueue<String>(1)
    private var waitInputFrom: Player? = null
    internal var lastPendingInput: String? = null
    private var isRunning = false

    fun getIsRunning() = isRunning

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

    private fun playTurns() {
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
                } while (attackIsPossible && playingPlayer.chooseYesOrNo("Continue attack ?"))

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

    internal fun addCards(cardCollection: Collection<Card>) {
        cards.addAll(cardCollection)
    }

    companion object {
        val initialArmyNumberByPlayerNumber = mapOf(
            3 to 35,
            4 to 30,
            5 to 25,
            6 to 20
        )
    }

    @JsonIgnore
    @TestOnly
    internal fun getPlayersForTest() = players

    @JsonIgnore
    @TestOnly
    internal fun getTerritoriesForTest() = world.getTerritories()

    internal fun readlineFor(player: Player): String {
        println("$this.readlineFor $player")
        waitInputFrom = player
        return inputQueue.take()
    }

    internal fun sendOutput(output: String) {
        if (outputQueue.isNotEmpty()) {
            println("output queue not clear")
            outputQueue.clear()
        }
        outputQueue.put(output)
    }

    fun processInputFrom(playerName: String, input: String): String {
        println("$this.processInputFrom $playerName >>> $input")
        return when {
            !isRunning -> "Engine not running"
            playerName != waitInputFrom?.name -> "Bad player"
            inputQueue.isNotEmpty() -> "An input is already waiting to be processed"
            else -> {
                inputQueue.add(input)
                outputQueue.take()
            }
        }
    }

    fun start() {
        isRunning = true
        setupTerritories()
        placeInitialArmies()
        playTurns()
    }

    internal fun getPlayersPublicData(playerName: String): List<PlayerPublicData> {
        return players.map { it.asPublicData(playerName == it.name) }
    }

}
// todo make initialArmyNumberByPlayerNumber parametrable
// todo make card symbol equally distributed
// todo ask if fortify position
// todo count actual turn number, not player turn number
// todo auto choose when only one choice possible
