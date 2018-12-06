package engine.game

internal class Players(vararg playerArray: Player) : ArrayList<Player>(playerArray.toMutableList()) {
    private var currentIndex = 0

    constructor(armyNumberToPlace: Int, vararg names: String) : this() {
        this.addAll(names.map { Player(it, armyNumberToPlace) })
    }

    fun getActual() = get(currentIndex)

    fun passToNext() {
        currentIndex += 1
        currentIndex %= size
    }

}
