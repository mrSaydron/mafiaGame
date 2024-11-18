package ru.mrak.mafiagame.types

import ru.mrak.mafiagame.data.Game

enum class PhaseType(
    val canUse: (Game) -> Boolean,
    var nextPhaseType: PhaseType? = null,
) {
    START_GAME({ false }),
    START_NIGHT({ true }),
    INTRODUCE({ !it.acquaintanceAlready }),
    MAFIA({ it.players.count{ player -> player.role == RoleType.MAFIA && player.isAlive } > 0 }),
    END_MAFIA({ it.players.count{ player -> player.role == RoleType.MAFIA && player.isAlive } > 0 }),
    DOCTOR({ it.players.count { player -> player.role == RoleType.DOCTOR && player.isAlive } > 0 }),
    END_DOCTOR({ it.players.count { player -> player.role == RoleType.DOCTOR && player.isAlive } > 0 }),
    DETECTIVE({ it.players.count { player -> player.role == RoleType.DETECTIVE && player.isAlive } > 0 }),
    END_DETECTIVE({ it.players.count { player -> player.role == RoleType.DETECTIVE && player.isAlive } > 0 }),
    NEWS({ true }),
    VOTE({ true }),
    AFTER_VOTE({ true }),
    END_GAME({ false })
    ;

    companion object {
        init {
            START_GAME.nextPhaseType = START_NIGHT
            START_NIGHT.nextPhaseType = INTRODUCE
            INTRODUCE.nextPhaseType = MAFIA
            MAFIA.nextPhaseType = END_MAFIA
            END_MAFIA.nextPhaseType = DOCTOR
            DOCTOR.nextPhaseType = END_DOCTOR
            END_DOCTOR.nextPhaseType = DETECTIVE
            DETECTIVE.nextPhaseType = END_DETECTIVE
            END_DETECTIVE.nextPhaseType = NEWS
            NEWS.nextPhaseType = VOTE
            VOTE.nextPhaseType = AFTER_VOTE
            AFTER_VOTE.nextPhaseType = START_NIGHT
        }
    }
}