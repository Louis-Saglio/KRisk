package engine.game

import engine.chooseYesOrNo
import engine.game.world.Territory

internal class PlayerTerritory(val player: Player, val territory: Territory)

private class DiceComparisonResult(val attackerDeaths: Int, val defenderDeath: Int)

internal class BattleManager(private val attacker: PlayerTerritory, private val defender: PlayerTerritory) {
    private val diceFaceNbr = 6

    private fun fightOneTurn() {
        println("BattleManager.fightOneTurn")
        val attackerDices = rollDices(attacker.player.chooseDiceNumberForAttackFrom(attacker.territory))
        println("attackerDices : $attackerDices")
        val defenderDices = rollDices(defender.player.chooseDiceNumberForDefenceOf(defender.territory))
        println("defenderDices $defenderDices")
        val result = compareDices(attackerDices, defenderDices)
        println("result $result")
        attacker.territory.increaseArmyNumber(-result.attackerDeaths)
        defender.territory.increaseArmyNumber(-result.defenderDeath)
    }

    private fun continueFight() =
        attacker.territory.armyNumber > 1 || defender.territory.armyNumber > 0 || chooseYesOrNo("Continue fight ?")

    fun fight() {
        println("BattleManager.fight")
        do {
            fightOneTurn()
        } while (continueFight())
    }

    private fun rollDices(diceNbr: Int): List<Int> {
        println("BattleManager.rollDices with $diceNbr dices")
        return (0..diceNbr).map { (1..diceFaceNbr).random() }
    }

    private fun compareDices(attackerDices: List<Int>, defenderDices: List<Int>): DiceComparisonResult {
        println("BattleManager.compareDices")
        println("attackerDices = [${attackerDices}], defenderDices = [${defenderDices}]")
        val dices = (attackerDices.sortedDescending() zip defenderDices.sortedDescending())
        var attackerDeaths = 0
        var defenderDeaths = 0
        for (dicePair in dices) {
            if (dicePair.first > dicePair.second) defenderDeaths++
            else attackerDeaths++
        }
        return DiceComparisonResult(attackerDeaths, defenderDeaths)
    }
}
