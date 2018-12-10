package engine.game.world

class Territory(val name: String) {
    private var armyNumber = 0

    internal fun increaseArmyNumber(delta: Int) {
        armyNumber += delta
    }

    internal fun getArmyNumberForTest(): Int {
        return armyNumber
    }

    override fun toString(): String {
        return name
    }
}
