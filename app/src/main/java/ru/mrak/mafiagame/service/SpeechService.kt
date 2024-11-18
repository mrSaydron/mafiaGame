package ru.mrak.mafiagame.service

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.CompletableDeferred
import java.io.File
import java.util.Locale

object SpeechService {
    private var tts: TextToSpeech? = null
    private var savedPhrasesCount = 0
    private var mapOfPhrases = mutableMapOf<String, File>()
    private var mediaPlayer: MediaPlayer? = null
    private val ttsListener = object : UtteranceProgressListener() {

        var callbackFunction: (() -> Unit)? = null

        override fun onStart(utteranceId: String?) {}

        override fun onDone(utteranceId: String?) {
            callbackFunction?.invoke()
        }

        override fun onError(utteranceId: String?) {}
    }

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
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    suspend fun speakAndWait(text: String): Boolean {
        val completion = CompletableDeferred<Boolean>()
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {
                completion.complete(true)
            }
            override fun onError(utteranceId: String?) {
                completion.complete(false)
            }
        })

        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "utteranceId")
        }

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "utteranceId")
        return completion.await()
    }

    fun speakToQueue(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_ADD, null, null)
    }

    fun stop() {
        tts?.stop()
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