package engine.game

import debug
import engine.game.world.Territory

internal class Player(val name: String, armyToPlaceNumber: Int) {

    private var armyToPlaceNumber = armyToPlaceNumber
        set(value) {
            if (value >= 0) field = value
            else throw RuntimeException("Can't set negative armyToPlaceNumber")
        }

    fun getArmyToPlaceNumber(): Int {
        return armyToPlaceNumber
    }

    private val territories = mutableListOf<Territory>()

    fun claimTerritory(territory: Territory) {
        territories.add(territory)
        placeOneArmyOn(territory)
    }

    private fun placeOneArmyOn(territory: Territory) {
        if (!territories.contains(territory)) throw RuntimeException("Can't add army to not owned territory")
        armyToPlaceNumber -= 1
        territory.increaseArmyNumber(1)
    }

    override fun toString(): String {
        return "Player($name)"
    }

    fun placeOneArmy() {
        val territory = chooseTerritory()
        placeOneArmyOn(territory)
    }

    private fun chooseTerritory(): Territory {
        if (debug) return territories.random()
        var territory: Territory?
        do {
            println("Choose territory for $this : ")
            val input = readLine()
            territory = territories.find { it.name == input }
        } while (!territories.contains(territory) || territory == null)
        return territory
    }

    fun getTerritoriesForTest() = territories
}
