package ru.mrak.mafiagame

enum class Phase(
    val canUse: (List<Player>) -> Boolean,
    var nextPhase: Phase? = null,
) {
    START_GAME({ false }),
    START_NIGHT({ true }),
    MAFIA({ it.count{ player -> player.role == RoleType.MAFIA && player.isAlive } > 0 }),
    END_MAFIA({ it.count{ player -> player.role == RoleType.MAFIA && player.isAlive } > 0 }),
    DOCTOR({ it.count { player -> player.role == RoleType.DOCTOR && player.isAlive } > 0 }),
    END_DOCTOR({ it.count { player -> player.role == RoleType.DOCTOR && player.isAlive } > 0 }),
    DETECTIVE({ it.count { player -> player.role == RoleType.DETECTIVE && player.isAlive } > 0 }),
    END_DETECTIVE({ it.count { player -> player.role == RoleType.DETECTIVE && player.isAlive } > 0 }),
    NEWS({ true }),
    VOTE({ true }),
    AFTER_VOTE({ true }),
    END_GAME({ false })
    ;

    companion object {
        init {
            START_GAME.nextPhase = START_NIGHT
            START_NIGHT.nextPhase = MAFIA
            MAFIA.nextPhase = END_MAFIA
            END_MAFIA.nextPhase = DOCTOR
            DOCTOR.nextPhase = END_DOCTOR
            END_DOCTOR.nextPhase = DETECTIVE
            DETECTIVE.nextPhase = END_DETECTIVE
            END_DETECTIVE.nextPhase = NEWS
            NEWS.nextPhase = VOTE
            VOTE.nextPhase = AFTER_VOTE
            AFTER_VOTE.nextPhase = START_NIGHT
        }
    }
}