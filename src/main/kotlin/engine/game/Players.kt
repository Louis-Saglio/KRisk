package engine.game

class Players(vararg playerArray: Player) : ArrayList<Player>(playerArray.toMutableList()) {
    private var currentIndex = 0

    fun getActual() = get(currentIndex)

    fun passToNext() {
        currentIndex += 1
        currentIndex %= size
    }

    fun getNext(): Player {
        passToNext()
        return getActual()
    }

}
