package engine.game

import engine.game.world.Territory

internal class Player(val name: String, armyToPlaceNumber: Int) {

    private var armyToPlaceNumber = armyToPlaceNumber
        set(value) {
            if (value >= 0) field = value
            else throw RuntimeException("Can't set negative armyToPlaceNumber")
        }

    private val territories = mutableListOf<Territory>()

    fun addTerritory(territory: Territory) {
        armyToPlaceNumber -= 1
        territories.add(territory)
        territory.increaseArmyNumber(1)
    }

    fun getArmyToPlaceForTest() = armyToPlaceNumber
    fun getTerritoriesForTest() = territories
}
