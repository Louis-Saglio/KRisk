package engine.game.world

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

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

    @Test
    fun uniqueName() {
        val t1 = Territory("t1")
        val t2 = Territory("t2")
        val c1 = Continent("c1", 1, listOf(t1, t2))
        val t3 = Territory("t1")
        val c2 = Continent("c2", 1, listOf(t3))
        assertThrows(RuntimeException::class.java) { World(listOf(c1, c2), listOf()) }
    }
}
