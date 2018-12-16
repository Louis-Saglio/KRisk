package engine.game.world

class World(val continents: List<Continent>, val borders: List<Border>) {

    fun getTerritories(): List<Territory> {
        return continents.flatMap { it.territories }
    }

    fun getTerritoryByName(name: String?): Territory? {
        return getTerritories().find { it.name == name }
    }
}
