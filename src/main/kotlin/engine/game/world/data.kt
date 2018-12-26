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
    val afghanistan = Territory("Afghanistan")
    val chine = Territory("Chine")
    val inde = Territory("Inde")
    val tchita = Territory("Tchita")
    val japon = Territory("Japon")
    val kamtchatka = Territory("Kamtchatka")
    val moyenOrient = Territory("Moyen-Orient")
    val mongolie = Territory("Mongolie")
    val siam = Territory("Siam")
    val siberie = Territory("Sibérie")
    val oural = Territory("Oural")
    val yakoutie = Territory("Yakoutie")
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
                    afghanistan,
                    chine,
                    inde,
                    tchita,
                    japon,
                    kamtchatka,
                    moyenOrient,
                    mongolie,
                    siam,
                    siberie,
                    oural,
                    yakoutie
                )
            )
//            Continent(
//                "Amérique-du-Nord",
//                5,
//                listOf(
//                    Territory("Alaska"),
//                    Territory("Alberta"),
//                    Territory("Amérique centrale"),
//                    Territory("États de l'Est"),
//                    Territory("Groenland"),
//                    Territory("Territoires du Nord-Ouest"),
//                    Territory("Ontario"),
//                    Territory("Québec"),
//                    Territory("États de l'Ouest")
//                )
//            ),
//            Continent(
//                "Amérique-du-Sud",
//                2,
//                listOf(
//                    Territory("Argentine"),
//                    Territory("Brésil"),
//                    Territory("Pérou"),
//                    Territory("Venezuela")
//                )
//            ),
//            Continent(
//                "Océanie",
//                2,
//                listOf(
//                    Territory("Congo"),
//                    Territory("Afrique de l’Est"),
//                    Territory("Égypte"),
//                    Territory("Madagascar"),
//                    Territory("Afrique du Nord"),
//                    Territory("Afrique du Sud")
//                )
//            )
        ),
        listOf(
            Border(europeDuSud, europeDeLOuest),
            Border(europeDuNord, europeDeLOuest),
            Border(europeDuNord, scandinavie),
            Border(europeDuNord, europeDuSud),
            Border(europeDuNord, grandeBretagne),
            Border(europeDuSud, ukraine),
            Border(ukraine, scandinavie),
            Border(ukraine, europeDuNord),
            Border(grandeBretagne, europeDeLOuest),
            Border(grandeBretagne, islande),
            Border(islande, scandinavie),
            Border(grandeBretagne, scandinavie),
            Border(moyenOrient, afghanistan),
            Border(moyenOrient, inde),
            Border(inde, afghanistan),
            Border(inde, chine),
            Border(inde, siam),
            Border(siam, chine),
            Border(chine, mongolie),
            Border(chine, siberie),
            Border(chine, oural),
            Border(chine, afghanistan),
            Border(mongolie, japon),
            Border(mongolie, kamtchatka),
            Border(mongolie, tchita),
            Border(mongolie, siberie),
            Border(japon, kamtchatka),
            Border(kamtchatka, yakoutie),
            Border(kamtchatka, tchita),
            Border(yakoutie, siberie),
            Border(siberie, oural),
            Border(siberie, yakoutie),
            Border(oural, afghanistan)

        )
    )
}

fun buildSimpleWorld(): World {
    val europeDuSud = Territory("Europe-du-Sud")
    val europeDeLOuest = Territory("Europe-Occidentale")
    val europeDuNord = Territory("Europe-du-Nord")
    val grandeBretagne = Territory("Grande-Bretagne")
    val islande = Territory("Islande")
    val ukraine = Territory("Ukraine")
    val scandinavie = Territory("Scandinavie")
    return World(
        listOf(
            Continent("Ouest", 3, listOf(europeDeLOuest, grandeBretagne, islande, europeDuNord)),
            Continent("Est", 2, listOf(europeDuSud, ukraine, scandinavie))
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
