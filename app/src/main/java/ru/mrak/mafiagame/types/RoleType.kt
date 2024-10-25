package ru.mrak.mafiagame.types

import ru.mrak.mafiagame.R

enum class RoleType(
    val iconResId: Int
) {
    CIVILIAN(R.drawable.ic_citizen),
    MAFIA(R.drawable.ic_mafia),
    DETECTIVE(R.drawable.ic_detective),
    DOCTOR(R.drawable.ic_doctor),
}