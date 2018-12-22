package engine.game

import engine.RiskEngine
import engine.choose
import engine.game.world.Territory
import engine.game.world.combinations
import org.jetbrains.annotations.TestOnly
import kotlin.math.min

internal class Player(private val engine: RiskEngine, val name: String, armyToPlaceNumber: Int) {

    private var armyToPlaceNumber = armyToPlaceNumber
        set(value) {
            println("armyToPlaceNumber : from $field to $value")
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
        println("$this.claimTerritory $territory")
        if (territory.armyNumber > 0)
            throw RuntimeException("$this can't claim terrytory $territory because it is already owned")
        territories.add(territory)
        placeOneArmyOn(territory)
    }

    /**
     * Take an army from the armyToPlaceNumber reserve and place it on territory
     */
    private fun placeOneArmyOn(territory: Territory) {
        println("$this.placeOneArmyOn $territory")
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
        println("$this.placeOneArmy")
        println("Armée restante : $armyToPlaceNumber")
        val territory = chooseOwnedTerritory()
        placeOneArmyOn(territory)
    }

    fun hasWon(): Boolean {
        val hasConqueredWorld = hasConqueredWorld()
        println("$this hasWon : $hasConqueredWorld")
        return hasConqueredWorld
    }

    private fun hasConqueredWorld() = territories.containsAll(engine.world.getTerritories())

    internal fun fortifyPosition() {
        println("$this.fortifyPosition")
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

    internal fun manageReinforcement() {
        println("$this.manageReinforcement")
        computeReinforcement()
        repeat(armyToPlaceNumber) { placeOneArmy() }
    }

    private fun computeReinforcement() {
        println("$this.computeReinforcement")
        computeContinentalReinforcement()
        computeTerritorialReinforcement()
        getCombinationReinforcement()
        println("$armyToPlaceNumber renforts")
    }

    private fun getCombinationReinforcement() {
        println("$this.getCombinationReinforcement")
        if (cards.getBestCombination() != null)
            armyToPlaceNumber += (1..3).map { chooseOwnedCard() }.getBestCombination()?.reinforcement ?: 0
    }

    private fun computeTerritorialReinforcement() {
        println("$this.computeTerritorialReinforcement")
        val i = territories.size / 3
        armyToPlaceNumber += if (i > 3) i else 3
    }

    private fun computeContinentalReinforcement() {
        println("$this.computeContinentalReinforcement")
        for (continent in engine.world.continents) {
            if (territories.containsAll(continent.territories)) {
                armyToPlaceNumber += continent.reinforcements
            }
        }
    }

    private fun chooseTerritory(isValid: (Territory) -> Boolean): Territory {
        println("$this.chooseTerritory")
        return choose(
            message = "Choose territory for $this : ",
            ifDebug = { territories.map { it.name }.random() },
            cast = { engine.world.getTerritoryByName(it) },
            isValid = isValid
        )
    }

    internal fun chooseOwnedTerritory(isValid: ((Territory) -> Boolean)? = null): Territory {
        println("$this.chooseOwnedTerritory between : $territories")
        // todo: optimize by searching only in this.territories in the cast function
        return chooseTerritory { it in territories && if (isValid == null) true else isValid(it) }
    }

    fun chooseTargetToAttackFrom(from: Territory): Territory {
        val possibleTargets = engine.world.getTerritories().filter {
            engine.world.borders.any {
                    border -> setOf(border.territory1, border.territory2) == setOf(from, it)
            } && it !in territories
        }
        println("$this.chooseTargetToAttackFrom $from between $possibleTargets")
        return chooseTerritory {
            engine.world.borders.any {
                    border -> setOf(border.territory1, border.territory2) == setOf(from, it)
            } && it !in territories
        }
    }

    private fun chooseArmyNumberToTransferFrom(origin: Territory): Int {
        println("$this.chooseArmyNumberToTransferFrom")
        return choose(
            message = "Choose a number between 0 and ${origin.armyNumber - 1}",
            ifDebug = { "5" },
            cast = { it?.toInt() },
            isValid = { it in 0 until origin.armyNumber }
        )
    }

    private fun chooseOwnedCard(): Card {
        println("$this.chooseOwnedCard")
        return choose(
            message = "Choose a card between ${cards.joinToString()}",
            ifDebug = { cards.map { it.territory.name }.random() },
            cast = { cards.find { card -> card.territory.name == it } }
        )
    }

    fun owns(target: Territory): Boolean {
        println("$this.owns")
        return territories.contains(target)
    }

    fun chooseDiceNumberForAttackFrom(from: Territory): Int {
        println("$this.chooseDiceNumberForAttackFrom $from")
        return choose(
            message = "Choose dice number. max :  ${min(from.armyNumber - 1, 3)}",
            ifDebug = { (1..min(from.armyNumber - 1, 3)).random().toString() },
            cast = { it?.toInt() },
            isValid = { it in 1..min(from.armyNumber - 1, 3) }
        )
    }

    fun chooseDiceNumberForDefenceOf(territory: Territory): Int {
        println("$this.chooseDiceNumberForDefenceOf")
        return choose(
            message = "Choose dice number. max :  ${min(territory.armyNumber, 2)}",
            ifDebug = { (1..min(territory.armyNumber, 2)).random().toString() },
            cast = { it?.toInt() },
            isValid = { it in 1..min(territory.armyNumber, 2) }
        )
    }

    fun getTerritories() = territories

    //<editor-fold desc="TestOnly">
    @TestOnly
    fun addTerritoryForTest(territory: Territory) {
        territories.add(territory)
    }

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

private fun List<Card>.getBestCombination(): Combination? {
    println("$this.getBestCombination")
    val combination = combinations.filter { it.matches(this) }.maxBy { it.reinforcement }
    println("Best combination : $combination")
    return combination
}
