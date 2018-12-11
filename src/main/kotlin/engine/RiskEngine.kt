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
        return super.toString() + message.let { " : $message" }
    }

    operator fun invoke(message: String?): Result {
        this.message = message
        return this
    }


}

class PlaceOneInitialArmyAction(val territory: Territory? = null, playerName: String): Action(playerName) {
    override fun matches(action: Action): Boolean {
        return action is PlaceOneInitialArmyAction && playerName == action.playerName && action.territory != null
    }
}


class RiskEngine(val world: World, vararg playerNames: String): Engine<Action, Result>() {

    private val players = Players(this, initialArmyNumberByPlayerNumber.getValue(playerNames.size), *playerNames)
    private var expectedAction: Action? = null

    init {
        setupTerritories()
        players.setToFirst()
        expectedAction = PlaceOneInitialArmyAction(playerName = players.getActual().name)
    }

    override suspend fun handle(input: Action): Result {
        if (expectedAction != null && expectedAction!!.matches(input)) {
            try {
                when (input) {
                    is PlaceOneInitialArmyAction -> execute(input)
                    else -> throw NotImplementedError("$input")
                }
            } catch (error: RuntimeException) {
                return Result.ERROR_WHILE_PROCESSING_ACTION(error.message)
            }
            players.passToNext()
            return Result.ACTION_PROCESSED
        }
        return Result.ACTION_NOT_EXPECTED
    }

    private fun execute(input: PlaceOneInitialArmyAction) {
        players.getActual().placeOneArmyOn(input.territory!!)
        if (players.getNext().getRemainingArmyToPlaceNumber() == 0) {
            expectedAction = PlaceOneInitialArmyAction(playerName = players.getNext().name)
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
