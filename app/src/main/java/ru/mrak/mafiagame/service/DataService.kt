package ru.mrak.mafiagame.service

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.mrak.mafiagame.MainActivity
import ru.mrak.mafiagame.data.Player

object DataService {

    private lateinit var app: MainActivity
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    var tutorialShow: Boolean? = null
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
                editor.putString("players", it.toString()).apply()
                val playersToSave: List<Player> = it.map { player -> Player(player.name, player.avatar) }

                val gson = Gson()
                val jsonPlayers = gson.toJson(playersToSave)

                editor.putString("players", jsonPlayers).apply()
            } ?: {
                sharedPreferences.edit().remove("players").apply()
            }
        }
        get() {
            if (field == null) {
                val jsonPlayers = sharedPreferences.getString("players", null)

                field = mutableListOf()
                if (jsonPlayers != null) {
                    val gson = Gson()
                    val type = object : TypeToken<List<Player>>() {}.type
                    gson.fromJson<List<Player>>(jsonPlayers, type).forEach {
                        field!!.add(it)
                    }
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