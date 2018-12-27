package engine.world

class Territory(val name: String) {
    internal var armyNumber = 0
        set(value) {
            if (value < 0) throw RuntimeException("Can't set territory army number inferior to 0")
            field = value
        }

    internal fun increaseArmyNumber(delta: Int) {
        armyNumber += delta
    }

    override fun toString(): String {
        return "$name : $armyNumber"
    }
}
