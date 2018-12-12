package engine.game

import engine.game.world.Territory
import kotlinx.coroutines.channels.Channel

private class Consumer<T>(private val player: Player, private val value: T) {
    operator fun invoke(function: (T) -> Unit) {
        function(value)
    }
}

internal class Player(val name: String, armyToPlaceNumber: Int) {

    private val inputStream = Channel<Any>(1)
    private val outputStream = Channel<Unit>(1)

    private val territories = mutableListOf<Territory>()
    private var armyToPlaceNumber = armyToPlaceNumber
        set(value) {
            if (value >= 0) field = value
            else throw RuntimeException("Can't set negative armyToPlaceNumber")
        }

    suspend fun process(input: Any) {
        // todo multiple process simultaneously
        inputStream.send(input)
        outputStream.receive()
    }

    private suspend fun done() {
        outputStream.send(Unit)
    }

    fun claimTerritory(territory: Territory) {
        territories.add(territory)
        placeOneArmyOn(territory)
    }

    fun placeOneArmyOn(territory: Territory) {
        if (!territories.contains(territory)) throw RuntimeException("Can't add army to not owned territory")
        armyToPlaceNumber -= 1
        territory.increaseArmyNumber(1)
    }

    override fun toString(): String {
        return "Player($name)"
    }

    suspend fun playTurn(reinforcementNumber: Int) {
        placeReinforcements(reinforcementNumber)
        attack()
        fortifyPosition()
    }

    private fun fortifyPosition() {

    }

    private fun attack() {

    }

    private suspend fun placeReinforcements(reinforcementNumber: Int) {
        val territory = waitForWhile(territories::contains)
        repeat(reinforcementNumber) {
            placeOneArmyOn(territory)
            done()
        }
    }

    private suspend inline fun <reified T> waitForWhile(isValid: (T) -> Boolean): T {
        var input: T
        var notTypedInput: Any
        do {
            do {
                notTypedInput = inputStream.receive()
            } while (notTypedInput !is T)
            input = notTypedInput
        } while (!isValid(input))
        return input
    }

    fun isEliminated() = territories.size > 0

    fun getArmyToPlaceForTest() = armyToPlaceNumber
    fun getTerritoriesForTest() = territories
}
