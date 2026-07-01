package com.example.lingoscroll.data.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class NativeTtsManager(context: Context) : TtsManager, TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isReady = false

    init {
        try {
            // Google TTS motorunu (com.google.android.tts) zorunlu kılıyoruz
            tts = TextToSpeech(context, this, "com.google.android.tts")
        } catch (e: Exception) {
            Log.e("NativeTtsManager", "Google TTS yüklenemedi, sistem varsayılanına dönülüyor", e)
            tts = TextToSpeech(context, this)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("NativeTtsManager", "US English desteklenmiyor, UK deneniyor.")
                tts?.setLanguage(Locale.UK)
            }
            isReady = true
        } else {
            Log.e("NativeTtsManager", "TTS başlatma hatası.")
        }
    }

    override fun speak(text: String) {
        if (isReady && tts != null) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    override fun setSpeechRate(rate: Float) {
        tts?.setSpeechRate(rate)
    }

    override fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isReady = false
    }
}
