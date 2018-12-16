package engine.game

import engine.RiskEngine
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.Mockito

internal class PlayersTest {

    @Test
    fun uniqueName() {
        val engine = Mockito.mock(RiskEngine::class.java)
        assertThrows(RuntimeException::class.java) {
            Players(Player(engine, "one", 0), Player(engine, "one", 0))
        }
    }

    @Disabled("To be done")
    @Test
    fun getActual() {
    }

    @Disabled("To be done")
    @Test
    fun passToNext() {
    }

    @Disabled("To be done")
    @Test
    fun setToFirst() {
    }

    @Disabled("To be done")
    @Test
    fun forEachClaimTerritory() {
    }
}
