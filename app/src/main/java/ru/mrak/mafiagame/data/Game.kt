package ru.mrak.mafiagame.data

import ru.mrak.mafiagame.types.PhaseType

data class Game(
    var players: List<Player> = emptyList(),
    var acquaintanceAlready: Boolean = false,
    var currentPhaseType: PhaseType = PhaseType.START_NIGHT,
    var mafiaChose: Player? = null,
    var doctorChose: Player? = null,
    var citizenChose: Player? = null,
    var doctorChoseLast: Player? = null,
)
