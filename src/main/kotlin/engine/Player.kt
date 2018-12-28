package engine

import debug
import engine.world.Territory
import engine.world.combinations
import org.jetbrains.annotations.TestOnly
import kotlin.math.min

const val maxInputTryNumber = 20

private class InputSuggestion(val value: String, toDisplay: String? = null) {
    private val toDisplay = toDisplay ?: value

    override fun toString(): String {
        return toDisplay
    }
}

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
    var hasConqueredTerritory = false

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

    fun captureTerritory(from: Territory, to: Territory, minimum: Int = 0) {
        val nbr = choose(
            message = "Choose army number to move from $from to $to between $minimum and ${from.armyNumber - 1}",
            ifDebug = { (minimum until (from.armyNumber)).random().toString() },
            cast = { it?.toIntOrNull() },
            isValid = { it in (minimum..(from.armyNumber)) }
        )
        territories.add(to)
        hasConqueredTerritory = true
        println("$this.move $nbr armies from $from to $to")
        from.increaseArmyNumber(-nbr)
        to.increaseArmyNumber(nbr)
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
        val possibleSources = territories.filter {
            it.armyNumber > 1 && territories.any {territory -> engine.world.areNeighbours(it, territory) }
        }
        if (possibleSources.isEmpty()) {
            println("No fortification possible")
            return
        }
        val firstTerritory = choose(
            message = "Choose a territory to fortify from",
            ifDebug = { possibleSources.random().name },
            cast = { possibleSources.find { territory -> territory.name == it } },
            inputSuggestions = possibleSources.map { territory ->  InputSuggestion(territory.name, territory.toString()) }
        )
        val possibleDestinations = territories.filter { engine.world.areNeighbours(it, firstTerritory) }
        val secondTerritory = choose(
            message = "Choose territory to fortify",
            ifDebug = { possibleDestinations.random().name },
            cast = { possibleDestinations.find { territory -> territory.name == it }},
            isValid = { it in possibleDestinations },
            inputSuggestions = possibleDestinations.map { InputSuggestion(it.name, it.toString()) }
        )
        val armyNumber = chooseArmyNumberToTransferFrom(firstTerritory)
        firstTerritory.armyNumber -= armyNumber
        secondTerritory.armyNumber += armyNumber
    }

    internal fun manageReinforcement() {
        println("$this.manageReinforcement")
        computeReinforcement()
        repeat(armyToPlaceNumber) {
            placeOneArmy()
        }
    }

    private fun computeReinforcement() {
        println("$this.computeReinforcement")
        computeContinentalReinforcement()
        computeTerritorialReinforcement()
        getCombinationReinforcement()
        println("$armyToPlaceNumber reinforcement")
    }

    private fun getCombinationReinforcement() {
        println("$this.getCombinationReinforcement")
        val possibleSetsOfCard = getAllPossibleSetOfThreeOwnedCards().filter {
            combinations.any { combination -> combination.matches(it) }
        }
        if (possibleSetsOfCard.isEmpty()) {
            println("$this has no combination")
            println("cards : $cards")
            return
        }
        if (chooseYesOrNo("Use combination ?")) {
            val chosenCards = mutableListOf<Card>()
            repeat(3) {
                val card = choose(
                    "choose card $it",
                    { possibleSetsOfCard.filter { setOfCard -> setOfCard.containsAll(chosenCards) }.random().random().territory.name },
                    { input -> cards.find { card -> card.territory.name == input } },
                    possibleSetsOfCard.flatten().map { card -> InputSuggestion(card.territory.name, card.toString()) },
                    { card ->
                        possibleSetsOfCard.any { setOfCard ->
                            setOfCard.containsAll(chosenCards) && setOfCard.contains(card)
                        } && card !in chosenCards
                    }
                )
                chosenCards.add(card)
            }
            val combination =
                chosenCards.getBestCombination() ?: throw RuntimeException("No combination in a valid set of card")
            println("$this got $combination")
            chosenCards.forEach {
                it.territory.increaseArmyNumber(2)
            }
            engine.cards.addAll(chosenCards)
            cards.removeAll(chosenCards)
            armyToPlaceNumber += combination.reinforcement
        }
    }

    private fun getAllPossibleSetOfThreeOwnedCards(): Set<Set<Card>> {
        val answer = mutableSetOf<Set<Card>>()
        for (card1 in cards) {
            for (card2 in cards) {
                for (card3 in cards) {
                    if (card3 !in setOf(card1, card2)) {
                        val set = setOf(card1, card2, card3)
                        if (set.size == 3) {
                            answer.add(set)
                        }
                    }
                }
            }
        }
        return answer
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

    internal fun addCard(card: Card) {
        println("$this.addCard $card")
        cards.add(card)
    }

    private fun chooseTerritory(inputSuggestions: List<Territory>? = null, isValid: (Territory) -> Boolean): Territory {
        println("$this.chooseTerritory")
        return choose(
            message = "Choose territory for $this : ",
            ifDebug = { territories.map { it.name }.random() },
            cast = { engine.world.getTerritoryByName(it) },
            isValid = isValid,
            inputSuggestions = inputSuggestions?.map { InputSuggestion(it.name, it.toString()) }
        )
    }

    private fun chooseOwnedTerritory(isValid: ((Territory) -> Boolean)? = null): Territory {
        return chooseTerritory(
            isValid = { it in territories && if (isValid == null) true else isValid(it) },
            inputSuggestions = territories
        )
    }

    fun chooseTerritoryToAttackFrom(): Territory? {
        println("$this.chooseTerritoryToAttackFrom")
        val possibleTerritories = territories.filter {
            it.armyNumber >= 2
                    && engine.world.getTerritories().any {
                    territory -> territory !in territories && engine.world.areNeighbours(it, territory)
            }
        }
        if (possibleTerritories.isEmpty()) return null
        return choose(
            message = "$this choose territory to attack from",
            ifDebug = { possibleTerritories.random().name },
            isValid = { it in possibleTerritories },
            cast = { possibleTerritories.find { territory -> it == territory.name } },
            inputSuggestions = possibleTerritories.map { InputSuggestion(it.name, it.toString()) }
        )
    }

    fun chooseTargetToAttackFrom(from: Territory): Territory {
        val possibleTargets = engine.world.getTerritories().filter {
            engine.world.areNeighbours(it, from) && it !in territories
        }
        println("$this.chooseTargetToAttackFrom $from")
        return choose(
            isValid = { it in possibleTargets },
            inputSuggestions = possibleTargets.map { InputSuggestion(it.name, it.toString()) },
            message = "Choose territory to attack from $from",
            cast = { possibleTargets.find { territory -> territory.name == it } },
            ifDebug = { possibleTargets.random().name }
        )
    }

    private fun chooseArmyNumberToTransferFrom(origin: Territory): Int {
        println("$this.chooseArmyNumberToTransferFrom")
        return choose(
            message = "Choose a number between 0 and ${origin.armyNumber - 1}",
            ifDebug = { (0 until origin.armyNumber).random().toString() },
            cast = { it?.toIntOrNull() },
            isValid = { it in 0 until origin.armyNumber }
        )
    }

    fun owns(target: Territory): Boolean {
        val contains = territories.contains(target)
        println("$this.owns $target : $contains")
        return contains
    }

    fun chooseDiceNumberForAttackFrom(from: Territory): Int {
        println("$this.chooseDiceNumberForAttackFrom $from")
        return choose(
            message = "Choose dice number. max :  ${min(from.armyNumber - 1, 3)}",
            ifDebug = { (1..min(from.armyNumber - 1, 3)).random().toString() },
            cast = { it?.toIntOrNull() },
            isValid = { it in 1..min(from.armyNumber - 1, 3) }
        )
    }

    fun chooseDiceNumberForDefenceOf(territory: Territory): Int {
        println("$this.chooseDiceNumberForDefenceOf $territory")
        return choose(
            message = "Choose dice number. max :  ${min(territory.armyNumber, 2)}",
            ifDebug = { (1..min(territory.armyNumber, 2)).random().toString() },
            cast = { it?.toIntOrNull() },
            isValid = { it in 1..min(territory.armyNumber, 2) }
        )
    }

    fun getTerritories() = territories

    private fun <T> choose(
        message: String? = null,
        ifDebug: () -> String?,
        cast: (String?) -> T?,
        inputSuggestions: List<InputSuggestion>? = null,
        isValid: ((T) -> Boolean)? = null
    ): T {
        println("$this.choose")
        // todo : replace ifDebug by random input suggestion
        if (inputSuggestions != null)
            println("Choose between ${inputSuggestions.filter {
                val suggestion = cast(it.value)
                suggestion != null && isValid?.invoke(suggestion) ?: true
            }}")
        var chosen: T?
        var triedInputNumber = 0
        do {
            if (debug && triedInputNumber > maxInputTryNumber)
                throw RuntimeException("Too many input tried")
            if (message != null) println(message)
            if (!debug && engine.lastPendingInput != null) {
                println("sending output")
                engine.sendOutput("${engine.lastPendingInput} processed")
            }
            val input = if (debug) ifDebug() else engine.readlineFor(this)
            if (!debug) {
                engine.lastPendingInput = input
            }
            if (debug) {
                println(">>> $input")
            }
            chosen = cast(input)
            triedInputNumber++
        } while (chosen == null || if (isValid!=null) !isValid(chosen) else false)
        println("chosen $chosen")
        return chosen
    }

    internal fun chooseYesOrNo(message: String): Boolean {
        println("chooseYesOrNo")
        return choose(
            message = message,
            ifDebug = { setOf("yes", "no").random() },
            cast = {
                when (it?.toLowerCase()) {
                    "yes" -> true
                    "no" -> false
                    else -> null
                }
            },
            inputSuggestions = listOf(InputSuggestion("yes"), InputSuggestion("no"))
        )
    }

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

    @TestOnly
    fun getAllPossibleSetOfThreeOwnedCardsForTest() = getAllPossibleSetOfThreeOwnedCards()

    fun isDefeated(): Boolean {
        return territories.isEmpty()
    }

    fun takeCardsOf(player: Player) {
        println("$this takes cards of $player : $cards")
        cards.addAll(player.cards)
        player.cards.removeAll(player.cards)
    }

    fun removeTerritory(territory: Territory) {
        println("$this looses $territory")
        territories.remove(territory)
    }
    //</editor-fold>
}

private fun List<Card>.getBestCombination(): Combination? {
    println("$this.getBestCombination")
    val combination = getAllCombinations().maxBy { it.reinforcement }
    println("Best combination : $combination")
    return combination
}

private fun Collection<Card>.getAllCombinations(): List<Combination> {
    println("$this.getAllCombinations")
    return combinations.filter { it.matches(this) }
}
