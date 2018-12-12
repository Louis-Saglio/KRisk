package engine.game

import engine.game.world.Territory

internal class Players(vararg playerArray: Player) : ArrayList<Player>(playerArray.toMutableList()) {
    // Lot of not business logic

    private var currentIndex = 0

    constructor(armyNumberToPlace: Int, vararg names: String) : this() {
        this.addAll(names.map { Player(it, armyNumberToPlace) })
    }

    fun getActual() = get(currentIndex)

    private fun getNextIndex() = (currentIndex + 1) % size

    fun getNext() = get(getNextIndex())

    fun passToNext() {
        currentIndex = getNextIndex()
    }

    fun setToFirst() {
        currentIndex = 0
    }

    fun forEachClaimTerritory(territories: List<Territory>) {
        loopApply(territories, Player::claimTerritory)
    }

    private fun <T> loopApply(items: List<T>, func: Player.(T) -> Unit) {
        items.forEach {
            getActual().func(it)
            passToNext()
        }
    }

}
