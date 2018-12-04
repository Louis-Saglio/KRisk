package engine.game

import engine.game.world.Territory

class Player(val name: String, initialArmyNumber: Int) {
    private var initalArmyNumber = initialArmyNumber
        set(value) {
            if (value >= 0) field = value
            else throw RuntimeException("Can't set negative initialArmyNumber")
        }
    private val territories = mutableListOf<Territory>()

    fun addTerritory(territory: Territory) {
        initalArmyNumber -= 1
        territories.add(territory)
    }

    fun getInitialArmyNumberForTest() = initalArmyNumber
    fun getTerritoriesForTest() = territories
}
