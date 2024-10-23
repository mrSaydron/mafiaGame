package ru.mrak.mafiagame

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ru.mrak.mafiagame.service.SpeechService

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        SpeechService.initialize(this)

        APP = this
    }

    override fun onDestroy() {
        SpeechService.destroy()

        super.onDestroy()
    }

    companion object {
        var APP: MainActivity? = null
    }
}