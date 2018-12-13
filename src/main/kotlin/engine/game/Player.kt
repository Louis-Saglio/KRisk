package engine.game

import engine.RiskEngine
import engine.choose
import engine.game.world.Territory

internal class Player(private val engine: RiskEngine, val name: String, armyToPlaceNumber: Int) {

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
        if (territory !in territories) throw RuntimeException("Can't add army to not owned territory")
        armyToPlaceNumber -= 1
        territory.increaseArmyNumber(1)
    }

    override fun toString(): String {
        return "Player($name)"
    }

    fun placeOneArmy() {
        val territory = chooseOwnedTerritory()
        placeOneArmyOn(territory)
    }

    fun getTerritoriesForTest() = territories

    fun hasWon(): Boolean {
        return false
    }

    fun playTurn() {
        manageReinforcement()
//      todo   manageAttacks()
        fortifyPosition()
    }

    private fun fortifyPosition() {
        val firstTerritory = chooseOwnedTerritory()
        val secondTerritory = chooseOwnedTerritory {
            engine.world.borders.any {
                    border -> setOf(border.territory1, border.territory2) == setOf(firstTerritory, it)
            }
        }
        val armyNumber = chooseInt { it in 0 until firstTerritory.armyNumber }
        firstTerritory.armyNumber -= armyNumber
        secondTerritory.armyNumber += armyNumber
    }

    private fun manageReinforcement() {
        computeReinforcement()
        repeat(armyToPlaceNumber) { placeOneArmy() }
    }

    private fun computeReinforcement() {
        computeContinentalReinforcement()
        computeTerritorialReinforcement()
        getCombinationReinforcement()
    }

    private fun getCombinationReinforcement() {
        // todo
    }

    private fun computeTerritorialReinforcement() {
        val i = territories.size / 3
        armyToPlaceNumber += if (i > 3) i else 3
    }

    private fun computeContinentalReinforcement() {
        for (continent in engine.world.continents) {
            if (territories.containsAll(continent.territories)) {
                armyToPlaceNumber += continent.reinforcements
            }
        }
    }

    private fun chooseTerritory(isValid: (Territory) -> Boolean): Territory {
        return choose(
            message = "Choose territory for $this : ",
            ifDebug = { territories.random() },
            cast = { engine.world.getTerritories().find { territory -> territory.name == it } },
            isValid = isValid
        )
    }

    private fun chooseOwnedTerritory(isValid: ((Territory) -> Boolean)? = null): Territory {
        return chooseTerritory { it in territories && if (isValid == null) true else isValid(it) }
    }

    private fun chooseInt(isValid: (Int) -> Boolean): Int {
        return choose(
            message = "Choose Int",
            ifDebug = (0..30)::random,
            cast = { it?.toInt() },
            isValid = isValid
        )
    }
}
