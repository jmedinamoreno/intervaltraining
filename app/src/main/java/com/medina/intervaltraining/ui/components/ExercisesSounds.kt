package com.medina.intervaltraining.ui.components

import android.util.Log
import com.medina.intervaltraining.viewmodel.SettingsUiState

fun FXSoundPool.playTrainingStartSound(settingsState: SettingsUiState){
    Log.d("JMMLOG", "ExercisesSounds: playTrainingStartSound (${(settingsState as? SettingsUiState.Success)?.settings?.soundsEnabledTrainingStart == true})")
    if((settingsState as? SettingsUiState.Success)?.settings?.soundsEnabledTrainingStart == true){
        playSound(FXSoundPool.FX.PATIN)
    }
}
fun FXSoundPool.playCountdownSound(settingsState: SettingsUiState){
    Log.d("JMMLOG", "ExercisesSounds: playCountdownSound (${((settingsState as? SettingsUiState.Success)?.settings?.countdownToChange?: Int.MAX_VALUE) > 0})")
    if(((settingsState as? SettingsUiState.Success)?.settings?.countdownToChange?: Int.MAX_VALUE) > 0){
        playSound(FXSoundPool.FX.TIN)
    }
}

fun FXSoundPool.playExerciseStartSound(settingsState: SettingsUiState){
    Log.d("JMMLOG", "ExercisesSounds: playExerciseStartSound (${(settingsState as? SettingsUiState.Success)?.settings?.soundsEnabledExerciseStart == true})")
    if((settingsState as? SettingsUiState.Success)?.settings?.soundsEnabledExerciseStart == true){
        playSound(FXSoundPool.FX.PUTI)
    }
}

fun FXSoundPool.playRestStartSound(settingsState: SettingsUiState){
    Log.d("JMMLOG", "ExercisesSounds: playRestStartSound (${(settingsState as? SettingsUiState.Success)?.settings?.soundsEnabledRestStart == true})")
    if((settingsState as? SettingsUiState.Success)?.settings?.soundsEnabledRestStart == true){
        playSound(FXSoundPool.FX.PUIN)
    }
}

fun FXSoundPool.playTrainingEndSound(settingsState: SettingsUiState){
    Log.d("JMMLOG", "ExercisesSounds: playTrainingEndSound (${(settingsState as? SettingsUiState.Success)?.settings?.soundsEnabledTrainingEnd == true})")
    if((settingsState as? SettingsUiState.Success)?.settings?.soundsEnabledTrainingEnd == true){
        playSound(FXSoundPool.FX.TINTINONIN)
    }
}