package engine

abstract class Combination {
    abstract val reinforcement: Int
    abstract fun matches(cards: Collection<Card>): Boolean

    override fun toString(): String {
        return this.javaClass.simpleName
    }
}

object ThreeInfantries: Combination() {
    override val reinforcement = 4

    override fun matches(cards: Collection<Card>) = cards.count { it.symbol == Symbol.INFANTRY } == 3
}

object ThreeCavalries: Combination() {
    override val reinforcement = 6

    override fun matches(cards: Collection<Card>) = cards.count { it.symbol == Symbol.CAVALRY } == 3
}

object ThreeArtillery: Combination() {
    override val reinforcement = 8

    override fun matches(cards: Collection<Card>) = cards.count { it.symbol == Symbol.ARTILLERY } == 3
}

object OneOfEachKind: Combination() {
    override val reinforcement = 10

    override fun matches(cards: Collection<Card>) = (
            cards.count { it.symbol == Symbol.INFANTRY } == 1
            && cards.count { it.symbol == Symbol.CAVALRY } == 1
            && cards.count { it.symbol == Symbol.ARTILLERY } == 1
    )
}
