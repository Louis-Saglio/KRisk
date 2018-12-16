package engine.game

abstract class Combination {
    abstract val reinforcement: Int
    abstract fun matches(cards: List<Card>): Boolean
}

object ThreeInfantries: Combination() {
    override val reinforcement = 4

    override fun matches(cards: List<Card>) = cards.count { it.symbol == Symbol.INFANTRY } == 3
}

object ThreeCavalries: Combination() {
    override val reinforcement = 6

    override fun matches(cards: List<Card>) = cards.count { it.symbol == Symbol.CAVALRY } == 3
}

object ThreeArtillery: Combination() {
    override val reinforcement = 8

    override fun matches(cards: List<Card>) = cards.count { it.symbol == Symbol.ARTILLERY } == 3
}

object OneOfEachKind: Combination() {
    override val reinforcement = 10

    override fun matches(cards: List<Card>) = (
            cards.count { it.symbol == Symbol.INFANTRY } == 1
            && cards.count { it.symbol == Symbol.CAVALRY } == 1
            && cards.count { it.symbol == Symbol.ARTILLERY } == 1
    )
}