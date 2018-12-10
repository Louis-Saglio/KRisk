package engine.game

import engine.game.world.Territory

internal class Player(val name: String, armyToPlaceNumber: Int) {

    private var armyToPlaceNumber = armyToPlaceNumber
        set(value) {
            if (value >= 0) field = value
            else throw RuntimeException("Can't set negative armyToPlaceNumber")
        }

    fun getRemainingArmyToPlaceNumber() = armyToPlaceNumber

    private val territories = mutableListOf<Territory>()

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

    fun getArmyToPlaceForTest() = armyToPlaceNumber
    fun getTerritoriesForTest() = territories
}
