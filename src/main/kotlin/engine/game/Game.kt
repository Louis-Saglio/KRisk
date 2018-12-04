package engine.game

import engine.game.world.World
import engine.game.world.buildWorld

class Game(val players: Players, private val world: World = buildWorld()) {

    internal fun setupTerritories() {
        for (territory in world.getTerritories().shuffled()) {
            players.getActual().addTerritory(territory)
        }
    }
}
