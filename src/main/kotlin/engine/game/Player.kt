package engine.game

import engine.RiskEngine
import engine.choose
import engine.game.world.Territory
import engine.game.world.combinations
import org.jetbrains.annotations.TestOnly

internal class Player(private val engine: RiskEngine, val name: String, armyToPlaceNumber: Int) {

    private var armyToPlaceNumber = armyToPlaceNumber
        set(value) {
            if (value >= 0) field = value
            else throw RuntimeException("Can't set negative armyToPlaceNumber")
        }

    fun getArmyToPlaceNumber() = armyToPlaceNumber  // todo : replace by backing field

    private val territories = mutableListOf<Territory>()
    private val cards = mutableListOf<Card>()

    /**
     * Add territory to the owned territories list and place one army on it
     */
    fun claimTerritory(territory: Territory) {
        if (territory.armyNumber > 0)
            throw RuntimeException("$this can't claim terrytory $territory because it is already owned")
        territories.add(territory)
        placeOneArmyOn(territory)
    }

    /**
     * Take an army from the armyToPlaceNumber reserve and place it on territory
     */
    private fun placeOneArmyOn(territory: Territory) {
        if (territory !in territories) throw RuntimeException("Can't add army to not owned territory")
        armyToPlaceNumber -= 1
        territory.increaseArmyNumber(1)
    }

    override fun toString(): String {
        return "Player($name)"
    }

    /**
     * Choose an owned territory and place one army on it
     */
    fun placeOneArmy() {
        val territory = chooseOwnedTerritory()
        placeOneArmyOn(territory)
    }

    fun hasWon(): Boolean {
        return hasConqueredWorld()
    }

    private fun hasConqueredWorld() = territories.containsAll(engine.world.getTerritories())

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
        val armyNumber = chooseArmyNumberToTransferFrom(firstTerritory)
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
        armyToPlaceNumber += (1..3).map { chooseOwnedCard() }.getBestCombination()?.reinforcement ?: 0
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
            ifDebug = { territories.map { it.name }.random() },
            cast = { engine.world.getTerritoryByName(it) },
            isValid = isValid
        )
    }

    private fun chooseOwnedTerritory(isValid: ((Territory) -> Boolean)? = null): Territory {
        // todo: optimize by searching only in this.territories in the cast function
        return chooseTerritory { it in territories && if (isValid == null) true else isValid(it) }
    }

    private fun chooseArmyNumberToTransferFrom(origin: Territory): Int {
        return choose(
            message = "Choose a number between 0 and ${origin.armyNumber - 1}",
            ifDebug = { "5" },
            cast = { it?.toInt() },
            isValid = { it in 0 until origin.armyNumber }
        )
    }

    private fun chooseOwnedCard(): Card {
        return choose(
            message = "Choose a card between ${cards.joinToString()}",
            ifDebug = { cards.map { it.territory.name }.random() },
            cast = { cards.find { card -> card.territory.name == it } }
        )
    }

    //<editor-fold desc="TestOnly">
    @TestOnly
    fun addTerritoryForTest(territory: Territory) {
        territories.add(territory)
    }

    @TestOnly
    fun getTerritoriesForTest() = territories

    @TestOnly
    fun computeContinentalReinforcementForTest() {
        computeContinentalReinforcement()
    }

    @TestOnly
    fun computeTerritorialReinforcementForTest() {
        computeTerritorialReinforcement()
    }

    @TestOnly
    fun getCombinationReinforcementForTest() {
        getCombinationReinforcement()
    }

    @TestOnly
    fun addCardForTest(card: Card) {
        cards.add(card)
    }

    @TestOnly
    fun fortifyPositionForTest() {
        fortifyPosition()
    }

    @TestOnly
    fun manageReinforcementForTest() {
        manageReinforcement()
    }
    //</editor-fold>
}

private fun List<Card>.getBestCombination() = combinations.filter { it.matches(this) }.maxBy { it.reinforcement }
