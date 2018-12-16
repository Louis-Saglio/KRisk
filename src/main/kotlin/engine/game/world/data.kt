package engine.game.world

import engine.game.OneOfEachKind
import engine.game.ThreeArtillery
import engine.game.ThreeCavalries
import engine.game.ThreeInfantries

fun buildWorld(): World {
    val europeDuSud = Territory("Europe-du-Sud")
    val europeDeLOuest = Territory("Europe-Occidentale")
    val europeDuNord = Territory("Europe-du-Nord")
    val grandeBretagne = Territory("Grande-Bretagne")
    val islande = Territory("Islande")
    val ukraine = Territory("Ukraine")
    val scandinavie = Territory("Scandinavie")
    return World(
        listOf(
            Continent(
                "Europe",
                5,
                listOf(europeDuSud, europeDeLOuest, europeDuNord, grandeBretagne, islande, ukraine, scandinavie)
            ),
            Continent(
                "Asie",
                7,
                listOf(
                    Territory("Afghanistan"),
                    Territory("Chine"),
                    Territory("Inde"),
                    Territory("Tchita"),
                    Territory("Japon"),
                    Territory("Kamtchatka"),
                    Territory("Moyen-Orient"),
                    Territory("Mongolie"),
                    Territory("Siam"),
                    Territory("Sibérie"),
                    Territory("Oural"),
                    Territory("Yakoutie")
                )
            ),
            Continent(
                "Amérique-du-Nord",
                5,
                listOf(
                    Territory("Alaska"),
                    Territory("Alberta"),
                    Territory("Amérique centrale"),
                    Territory("États de l'Est"),
                    Territory("Groenland"),
                    Territory("Territoires du Nord-Ouest"),
                    Territory("Ontario"),
                    Territory("Québec"),
                    Territory("États de l'Ouest")
                )
            ),
            Continent(
                "Amérique-du-Sud",
                2,
                listOf(
                    Territory("Argentine"),
                    Territory("Brésil"),
                    Territory("Pérou"),
                    Territory("Venezuela")
                )
            ),
            Continent(
                "Océanie",
                2,
                listOf(
                    Territory("Congo"),
                    Territory("Afrique de l’Est"),
                    Territory("Égypte"),
                    Territory("Madagascar"),
                    Territory("Afrique du Nord"),
                    Territory("Afrique du Sud")
                )
            )
        ),
        listOf(
            Border(europeDuSud, europeDeLOuest),
            Border(europeDuNord, europeDeLOuest),
            Border(europeDuNord, scandinavie),
            Border(europeDuNord, europeDuSud),
            Border(europeDuSud, ukraine),
            Border(ukraine, scandinavie),
            Border(ukraine, europeDuNord),
            Border(grandeBretagne, europeDeLOuest),
            Border(grandeBretagne, islande),
            Border(islande, scandinavie),
            Border(grandeBretagne, scandinavie)
        )
    )
}

val combinations = setOf(
    ThreeInfantries,
    ThreeCavalries,
    ThreeArtillery,
    OneOfEachKind
)
