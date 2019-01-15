package engine.world

import engine.OneOfEachKind
import engine.ThreeArtillery
import engine.ThreeCavalries
import engine.ThreeInfantries

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
    val alaska = Territory("Alaska")
    val alberta = Territory("Alberta")
    val ameriqueCentrale = Territory("Amérique centrale")
    val etatdeLEst = Territory("États de l'Est")
    val groenland = Territory("Groenland")
    val territoireDuNordOuest = Territory("Territoires du Nord-Ouest")
    val ontario = Territory("Ontario")
    val quebec = Territory("Québec")
    val etatsDeLOuest = Territory("États de l'Ouest")
    val argentine = Territory("Argentine")
    val bresil = Territory("Brésil")
    val perou = Territory("Pérou")
    val venezuela = Territory("Venezuela")
    val congo = Territory("Congo")
    val afriqueDeLEst = Territory("Afrique de l’Est")
    val egypte = Territory("Égypte")
    val madagascar = Territory("Madagascar")
    val afriqueDuNord = Territory("Afrique du Nord")
    val afriqueDuSud = Territory("Afrique du Sud")
    val indonesie = Territory("Indonésie")
    val nouvelleGuinee = Territory("Nouvelle Guinée")
    val australieOccidentale = Territory("Australie occidentale")
    val australieOrientale = Territory("Australie orientale")
    return World(
        "classic",
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
            ),
            Continent(
                "Amérique-du-Nord",
                5,
                listOf(
                    alaska,
                    alberta,
                    ameriqueCentrale,
                    etatdeLEst,
                    groenland,
                    territoireDuNordOuest,
                    ontario,
                    quebec,
                    etatsDeLOuest
                )
            ),
            Continent(
                "Amérique-du-Sud",
                2,
                listOf(
                    argentine,
                    bresil,
                    perou,
                    venezuela
                )
            ),
            Continent(
                "Afrique",
                3,
                listOf(
                    congo,
                    afriqueDeLEst,
                    egypte,
                    madagascar,
                    afriqueDuNord,
                    afriqueDuSud
                )
            ),
            Continent(
                "Océanie",
                2,
                listOf(indonesie, nouvelleGuinee, australieOccidentale, australieOrientale)
            )
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
            Border(oural, afghanistan),
            Border(ameriqueCentrale, etatdeLEst),
            Border(ameriqueCentrale, etatsDeLOuest),
            Border(etatdeLEst, quebec),
            Border(etatdeLEst, ontario),
            Border(etatdeLEst, etatsDeLOuest),
            Border(quebec, groenland),
            Border(quebec, ontario),
            Border(groenland, territoireDuNordOuest),
            Border(groenland, ontario),
            Border(territoireDuNordOuest, alberta),
            Border(territoireDuNordOuest, quebec),
            Border(territoireDuNordOuest, alaska),
            Border(alaska, alberta),
            Border(alberta, etatsDeLOuest),
            Border(alberta, ontario),
            Border(etatsDeLOuest, ontario),
            Border(kamtchatka, alaska),
            Border(ukraine, oural),
            Border(ukraine, afghanistan),
            Border(ukraine, moyenOrient),
            Border(europeDuSud, moyenOrient),
            Border(islande, groenland),
            Border(argentine, bresil),
            Border(argentine, perou),
            Border(bresil, venezuela),
            Border(bresil, perou),
            Border(venezuela, perou),
            Border(venezuela, ameriqueCentrale),
            Border(afriqueDuSud, madagascar),
            Border(afriqueDuSud, afriqueDeLEst),
            Border(afriqueDuSud, congo),
            Border(madagascar, afriqueDeLEst),
            Border(afriqueDeLEst, egypte),
            Border(afriqueDeLEst, afriqueDuNord),
            Border(afriqueDeLEst, congo),
            Border(egypte, moyenOrient),
            Border(egypte, europeDuSud),
            Border(egypte, afriqueDuNord),
            Border(afriqueDuNord, europeDuSud),
            Border(afriqueDuNord, europeDeLOuest),
            Border(afriqueDuNord, bresil),
            Border(siam, indonesie),
            Border(indonesie, nouvelleGuinee),
            Border(indonesie, australieOccidentale),
            Border(nouvelleGuinee, australieOccidentale),
            Border(nouvelleGuinee, australieOrientale),
            Border(australieOrientale, australieOccidentale)
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
        "Simple Europe",
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

// todo : move in Combination.kt file
val combinations = setOf(
    ThreeInfantries,
    ThreeCavalries,
    ThreeArtillery,
    OneOfEachKind
)
