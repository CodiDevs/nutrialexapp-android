package com.codidevs.nutriapp.data.audio

import android.media.AudioManager
import android.media.ToneGenerator

/**
 * Gestor central de sonidos efímeros de la app.
 */
object SoundManager {

    private var toneGenerator: ToneGenerator? = null

    fun init() {
        if (toneGenerator == null) {
            toneGenerator = try {
                ToneGenerator(AudioManager.STREAM_MUSIC, 90)
            } catch (_: Exception) {
                null
            }
        }
    }

    fun click() {
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
    }

    fun ruletaTick() {
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 30)
    }

    fun ruletaGiro() {
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
    }

    fun ruletaParo() {
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 200)
    }

    fun medallaCanjeada() {
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 350)
    }

    fun release() {
        toneGenerator?.release()
        toneGenerator = null
    }
}

fun onClickConSonido(block: () -> Unit): () -> Unit = {
    SoundManager.click()
    block()
}
