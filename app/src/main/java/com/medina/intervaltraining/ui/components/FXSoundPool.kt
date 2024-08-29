package com.medina.intervaltraining.ui.components

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.medina.intervaltraining.R

class FXSoundPool(private val context: Context){
    private var soundPool: SoundPool? = null
    private var soundIdPatin: Int = 0
    private var soundIdPati: Int = 0
    private var soundIdPawi: Int = 0
    private var soundIdPetian: Int = 0
    private var soundIdPetiun: Int = 0
    private var soundIdPotuin: Int = 0
    private var soundIdPptoi: Int = 0
    private var soundIdPpto: Int = 0
    private var soundIdPuin: Int = 0
    private var soundIdPuowi: Int = 0
    private var soundIdPuti: Int = 0
    private var soundIdTintinonin: Int = 0
    private var soundIdTin: Int = 0
    fun playSound(sound: FX) {
        val soundPool: SoundPool = soundPool ?: return
        when(sound){
            FX.PUIN -> soundPool.play(soundIdPuin, 1f, 1f, 0, 0, 1f)
            FX.PATIN -> soundPool.play(soundIdPatin, 1f, 1f, 0, 0, 1f)
            FX.PATI -> soundPool.play(soundIdPati, 1f, 1f, 0, 0, 1f)
            FX.PAWI -> soundPool.play(soundIdPawi, 1f, 1f, 0, 0, 1f)
            FX.PETIAN -> soundPool.play(soundIdPetian, 1f, 1f, 0, 0, 1f)
            FX.PETIUN -> soundPool.play(soundIdPetiun, 1f, 1f, 0, 0, 1f)
            FX.POTUIN -> soundPool.play(soundIdPotuin, 1f, 1f, 0, 0, 1f)
            FX.PPTOI -> soundPool.play(soundIdPptoi, 1f, 1f, 0, 0, 1f)
            FX.PPTO -> soundPool.play(soundIdPpto, 1f, 1f, 0, 0, 1f)
            FX.PUOWI -> soundPool.play(soundIdPuowi, 1f, 1f, 0, 0, 1f)
            FX.PUTI -> soundPool.play(soundIdPuti, 1f, 1f, 0, 0, 1f)
            FX.TINTINONIN -> soundPool.play(soundIdTintinonin, 1f, 1f, 0, 0, 1f)
            FX.TIN -> soundPool.play(soundIdTin, 1f, 1f, 0, 0, 1f)
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
                soundIdPatin = it.load(context, R.raw.soundfx_patin, 1)
                soundIdPati = it.load(context, R.raw.soundfx_pati, 1)
                soundIdPawi = it.load(context, R.raw.soundfx_pawi, 1)
                soundIdPetian = it.load(context, R.raw.soundfx_petian, 1)
                soundIdPetiun = it.load(context, R.raw.soundfx_petiun, 1)
                soundIdPotuin = it.load(context, R.raw.soundfx_potuin, 1)
                soundIdPptoi = it.load(context, R.raw.soundfx_pptoi, 1)
                soundIdPpto = it.load(context, R.raw.soundfx_ppto, 1)
                soundIdPuin = it.load(context, R.raw.soundfx_puin, 1)
                soundIdPuowi = it.load(context, R.raw.soundfx_puowi, 1)
                soundIdPuti = it.load(context, R.raw.soundfx_puti, 1)
                soundIdTintinonin = it.load(context, R.raw.soundfx_tintinonin, 1)
                soundIdTin = it.load(context, R.raw.soundfx_tin, 1)
            }
        return this
    }
    fun release(){
        soundPool?.release()
        soundPool = null
    }
    enum class FX {
        @Suppress("unused")
        NONE,
        PATIN,
        PATI,
        PAWI,
        PETIAN,
        PETIUN,
        POTUIN,
        PPTOI,
        PPTO,
        PUIN,
        PUOWI,
        PUTI,
        TINTINONIN,
        TIN,
    }
}