package engine.game

import engine.game.world.Territory


enum class Symbol {
    INFANTRY, CAVALRY, ARTILLERY
}


class Card(val territory: Territory, val symbol: Symbol) {
    override fun toString(): String {
        return "${territory.name} : $symbol"
    }
}
