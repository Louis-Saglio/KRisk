package engine

import engine.world.Territory
import kotlinx.serialization.Serializable


enum class Symbol {
    INFANTRY, CAVALRY, ARTILLERY
}


class Card(val territory: Territory, val symbol: Symbol) {
    override fun toString(): String {
        return "${territory.name} : $symbol"
    }
}

// Enums are not serializable
@Serializable
class SerializableCard(val territory: Territory, val symbol: String)
