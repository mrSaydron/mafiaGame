package ru.mrak.mafiagame.service

import android.content.Context
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import android.util.Log
import kotlinx.coroutines.delay
import ru.mrak.mafiagame.MainActivity.Companion.APP
import java.io.File
import java.util.Locale

object SpeechService {
    private var tts: TextToSpeech? = null
    private var savedPhrasesCount = 0
    private var mapOfPhrases = mutableMapOf<String, File>()
    private var mediaPlayer: MediaPlayer? = null

    fun initialize(context: Context) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale("ru", "RU"))
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "Язык не поддерживается")
                }

                for (phrase in initPhrases) {
                    val file = File(context.cacheDir, "$savedPhrasesCount.wav")
                    tts?.synthesizeToFile(phrase, null, file, "$savedPhrasesCount")
                    mapOfPhrases[phrase] = file
                    savedPhrasesCount++
                }

                Log.i("TTS", "Инициализация успешна")
            } else {
                Log.e("TTS", "Инициализация не удалась")
            }
        }

        mediaPlayer = MediaPlayer()
    }

    fun speak(text: String) {
        if (!mapOfPhrases.containsKey(text)) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            mediaPlayer?.apply {
                reset()
                setDataSource(mapOfPhrases[text]!!.path)
                prepare()
                start()
            }
        }
    }

    fun speakToQueue(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_ADD, null, null)
    }

    fun shutdown() {
        tts?.shutdown()
    }

    fun isSpeaking(): Boolean {
        return mediaPlayer?.isPlaying ?: false
    }

    fun destroy() {
        if (tts != null) {
            tts?.stop()
            tts?.shutdown()
        }
    }

    private val initPhrases = listOf(
        "Ночь начинается, все засыпают",
        "Мафия просыпается",
        "Мафия засыпает",
        "Доктор просыпается",
        "Доктор засыпает",
        "Детектив просыпается",
        "Детектив засыпает",
        "Наступил день, все просыпаются",
        "5",
        "4",
        "3",
        "2",
        "1",
        "0",
        "Горожане победили",
        "Мафия победила"
    )

}