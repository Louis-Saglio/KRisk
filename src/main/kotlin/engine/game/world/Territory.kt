package engine.game.world

class Territory(val name: String) {
    internal var armyNumber = 0
        set(value) {
            if (value < 1) throw RuntimeException("Can't set territory army number inferior to 1")
            field = value
        }

    internal fun increaseArmyNumber(delta: Int) {
        armyNumber += delta
    }

    internal fun getArmyNumberForTest(): Int {
        // todo deprecated
        return armyNumber
    }

    override fun toString(): String {
        return name
    }
}
