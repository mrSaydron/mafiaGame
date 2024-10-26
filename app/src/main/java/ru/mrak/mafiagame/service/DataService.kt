package ru.mrak.mafiagame.service

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.mrak.mafiagame.MainActivity
import ru.mrak.mafiagame.data.Player
import ru.mrak.mafiagame.data.Role
import ru.mrak.mafiagame.data.Settings

object DataService {

    private lateinit var app: MainActivity
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private val gson = Gson()

    var tutorialShow: Boolean? = null // todo перенести в настройки
        set(value) {
            field = value
            value?.let {
                editor.putBoolean("tutorialShow", value).apply()
            } ?: {
                editor.remove("tutorialShow").apply()
            }
        }
        get() {
            if (field == null) {
                field = sharedPreferences.getBoolean("tutorialShow", false)
            }
            return field
        }

    var players: MutableList<Player>? = null
        set(value) {
            field = value
            value?.let {
                val playersToSave: List<Player> = it.map { player -> Player(player.name, player.avatar) }
                val jsonPlayers = gson.toJson(playersToSave)
                editor.putString("players", jsonPlayers).apply()
            } ?: {
                editor.remove("players").apply()
            }
        }
        get() {
            if (field == null) {
                val jsonPlayers = sharedPreferences.getString("players", null)

                field = mutableListOf()
                if (jsonPlayers != null) {
                    val type = object : TypeToken<List<Player>>() {}.type
                    gson.fromJson<List<Player>>(jsonPlayers, type).forEach {
                        field!!.add(it)
                    }
                }
            }
            return field
        }

    var roles: MutableList<Role>? = null
        set(value) {
            field = value
            value?.let {
                val jsonRoles = gson.toJson(it)
                editor.putString("roles", jsonRoles).apply()
            } ?: {
                editor.remove("roles").apply()
            }
        }
        get() {
            if (field == null) {
                val json = sharedPreferences.getString("roles", null)

                field = mutableListOf()
                if (json != null) {
                    val type = object : TypeToken<List<Role>>() {}.type
                    gson.fromJson<List<Role>>(json, type).forEach {
                        field!!.add(it)
                    }
                }
            }
            return field
        }

    var settings: Settings? = null
        set(value) {
            field = value
            value?.let {
                val jsonSettings = gson.toJson(it)
                editor.putString("settings", jsonSettings).apply()
            } ?: {
                editor.remove("settings").apply()
            }
        }
        get() {
            if (field == null) {
                val jsonSettings = sharedPreferences.getString("settings", null)
                field = if (jsonSettings != null) {
                    gson.fromJson(jsonSettings, Settings::class.java)
                } else {
                    Settings()
                }
            }
            return field
        }

    fun initialize(application: MainActivity) {
        app = application
        sharedPreferences = app.getSharedPreferences("mafia_game", Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()
    }


}