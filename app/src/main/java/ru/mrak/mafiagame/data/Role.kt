package ru.mrak.mafiagame.data

import ru.mrak.mafiagame.types.RoleType

data class Role(
    val role: RoleType,
    var count: Int = 0
)
