package ru.mrak.mafiagame.data

data class Settings(
    var language: String = "ru",
    var doctorSelfHeal: Boolean = true,
    var doctorHealSame: Boolean = true,
    var revealRole: Boolean = false,
    var showSavedPlayer: Boolean = false,
)
