package engine.game.world

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class WorldTest {

    @Test
    fun getTerritories() {
        val t1 = Territory("t1")
        val t2 = Territory("t2")
        val c1 = Continent("c1", 1, listOf(t1, t2))
        val t3 = Territory("t3")
        val c2 = Continent("c2", 1, listOf(t3))
        val world = World(listOf(c1, c2), listOf())
        assertArrayEquals(listOf(t1, t2, t3).toTypedArray(), world.getTerritories().toTypedArray())
    }
}
