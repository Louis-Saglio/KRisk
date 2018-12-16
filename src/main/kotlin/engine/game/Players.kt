package engine.game

import engine.RiskEngine
import engine.game.world.Territory

internal class Players(vararg playerArray: Player) : ArrayList<Player>(playerArray.toMutableList()) {
    // Lot of not business logic

    init {
        for (player in this) {
            if (count { it.name == player.name } != 1)
                throw RuntimeException(
                    "Player name unique constraint violation : more than one player named ${player.name}"
                )
        }
    }

    private var currentIndex = 0

    constructor(engine: RiskEngine, armyNumberToPlace: Int, vararg names: String) : this() {
        this.addAll(names.map { Player(engine, it, armyNumberToPlace) })
    }

    fun getActual() = get(currentIndex)

    private fun getNextIndex() = (currentIndex + 1) % size

    fun passToNext() {
        currentIndex = getNextIndex()
    }

    fun setToFirst() {
        currentIndex = 0
    }

    fun forEachClaimTerritory(territories: List<Territory>) {
        // business logic
        currentIndex = (0 until size).random()
        loopApply(territories, Player::claimTerritory)
    }

    private fun <T> loopApply(items: List<T>, func: Player.(T) -> Unit) {
        items.forEach {
            getActual().func(it)
            passToNext()
        }
    }

}
