package engine.game.world

class World(val continents: List<Continent>, val borders: List<Border>) {

    init {
        val names = getTerritories().map { it.name }
        for (it in names) {
            if (names.count { s -> s == it } > 1)
                throw RuntimeException("Territory name unique constraint violation : more than one territory named $it")
        }
    }

    fun getTerritories(): List<Territory> {
        return continents.flatMap { it.territories }
    }

    fun getTerritoryByName(name: String?): Territory? {
        return getTerritories().find { it.name == name }
    }
}
