package engine

import engine.game.Players
import engine.game.world.Territory
import engine.game.world.World

abstract class Action(val playerName: String) {

    abstract fun matches(action: Action): Boolean
}

enum class Result {

    ACTION_NOT_EXPECTED,
    ACTION_PROCESSED,
    ERROR_WHILE_PROCESSING_ACTION;

    private var message: String? = null

    override fun toString(): String{
        return super.toString() + if (message == null) "" else " : $message"
    }

    operator fun invoke(message: String?): Result {
        this.message = message
        return this
    }


}

class PlaceOneArmy(val territory: Territory? = null, playerName: String): Action(playerName) {
    override fun matches(action: Action): Boolean {
        return action is PlaceOneArmy && playerName == action.playerName && action.territory != null
    }
}


enum class Phase {
    INITIAL_PLACEMENT,
    MAIN
}


enum class Verb


class Input(val subject: String, val verb: Verb)


// Quel input déclanche l'action ?
// Quelle méthode execute l'action ?
// l'action est elle attendue ?

// L'action est elle valide ?
// oui -> execute sa méthode


class RiskEngine(val world: World, vararg playerNames: String): Engine<Action, Result>() {

    private val players = Players(initialArmyNumberByPlayerNumber.getValue(playerNames.size), *playerNames)
    private var expectedAction: Action? = null
    private var phase = Phase.INITIAL_PLACEMENT

    init {
        setupTerritories()
        players.setToFirst()
        expectedAction = PlaceOneArmy(playerName = players.getActual().name)
    }

    override suspend fun handle(input: Action): Result {
        if (expectedAction != null && expectedAction!!.matches(input)) {
            try {
                when (input) {
                    is PlaceOneArmy -> execute(input)
                    else -> throw NotImplementedError("$input")
                }
            } catch (error: RuntimeException) {
                return Result.ERROR_WHILE_PROCESSING_ACTION(error.message)
            }
            players.passToNext()
            setExpectedAction()
            return Result.ACTION_PROCESSED
        }
        return Result.ACTION_NOT_EXPECTED
    }

    private fun execute(input: PlaceOneArmy) {
        players.getActual().placeOneArmyOn(input.territory!!)
    }

    private fun setExpectedAction() {
        if (phase == Phase.INITIAL_PLACEMENT) {
            if (players.count { it.getRemainingArmyToPlaceNumber() >= 0 } >= 0)
                expectedAction = PlaceOneArmy(playerName = players.getNext().name)
            else {
                phase = Phase.MAIN
            }
        }
    }

    private fun setupTerritories() {
        players.forEachClaimTerritory(world.getTerritories().shuffled())
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
