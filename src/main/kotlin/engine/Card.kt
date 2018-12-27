package engine

import engine.world.Territory


enum class Symbol {
    INFANTRY, CAVALRY, ARTILLERY
}


class Card(val territory: Territory, val symbol: Symbol) {
    override fun toString(): String {
        return "${territory.name} : $symbol"
    }
}
