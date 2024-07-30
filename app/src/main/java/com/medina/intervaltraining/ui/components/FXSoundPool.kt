package com.medina.intervaltraining.ui.components

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.medina.intervaltraining.R

class FXSoundPool(private val context: Context){
    private var soundPool: SoundPool? = null
    private var soundIdPuin: Int = 0
    private var soundIdTin: Int = 0
    private var soundIdTintinonin: Int = 0
    fun playSound(sound: FX) {
        val soundPool: SoundPool = soundPool ?: return
        when(sound){
            FX.TIN -> soundPool.play(soundIdTin, 1f, 1f, 0, 0, 1f)
            FX.PUIN -> soundPool.play(soundIdPuin, 1f, 1f, 0, 0, 1f)
            FX.TINTINONIN -> soundPool.play(soundIdTintinonin, 1f, 1f, 0, 0, 1f)
            else -> {}
        }
    }
    fun build(): FXSoundPool {
        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build().also {
                soundIdPuin= it.load(context, R.raw.soundfx_puin, 1)
                soundIdTin= it.load(context, R.raw.soundfx_tin, 1)
                soundIdTintinonin = it.load(context, R.raw.soundfx_tintinonin, 1)
            }
        return this
    }
    fun release(){
        soundPool?.release()
        soundPool = null
    }
    enum class FX { NONE, PUIN, TIN, TINTINONIN }
}