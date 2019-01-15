package engine.world

import com.fasterxml.jackson.annotation.JsonIgnore
import kotlinx.serialization.Serializable

@Serializable
class World(val name: String, val continents: List<Continent>, private val borders: List<Border>) {

    init {
        val names = getTerritories().map { it.name }
        for (it in names) {
            if (names.count { s -> s == it } > 1)
                throw RuntimeException("Territory name unique constraint violation : more than one territory named $it")
        }
    }

    @JsonIgnore
    fun getTerritories(): List<Territory> {
        return continents.flatMap { it.territories }
    }

    fun getTerritoryByName(name: String?): Territory? {
        return getTerritories().find { it.name == name }
    }

    fun areNeighbours(territory1: Territory, territory2: Territory): Boolean {
        return borders.any { setOf(it.territory1, it.territory2) == setOf(territory1, territory2) }
    }

    override fun toString(): String {
        return continents.joinToString("\n") {
            "${it.name}\n${it.territories.joinToString { territory -> territory.name }}"
        }
    }
}
