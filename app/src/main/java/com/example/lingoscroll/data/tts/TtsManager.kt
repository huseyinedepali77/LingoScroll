package com.example.lingoscroll.data.tts

interface TtsManager {
    fun speak(text: String)
    fun setSpeechRate(rate: Float)
    fun shutdown()
}
