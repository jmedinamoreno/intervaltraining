/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.medina.intervaltraining.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medina.intervaltraining.data.model.DarkThemeConfig
import com.medina.intervaltraining.data.model.ThemeBrand
import com.medina.intervaltraining.data.repository.UserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository,
) : ViewModel() {
    val settingsUiState: StateFlow<SettingsUiState> =
        userDataRepository.userData
            .map { userData ->
                SettingsUiState.Success(
                    settings = UserEditableSettings(
                        trainingStartDelaySecs = userData.trainingStartDelaySecs,
                        soundsEnabledRestStart = userData.soundsEnabledRestStart,
                        soundsEnabledTrainingEnd = userData.soundsEnabledTrainingEnd,
                        soundsEnabledTrainingStart = userData.soundsEnabledTrainingStart,
                        useDynamicColor = userData.useDynamicColor,
                        darkThemeConfig = userData.darkThemeConfig,
                    ),
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = WhileSubscribed(5.seconds.inWholeMilliseconds),
                initialValue = SettingsUiState.Loading,
            )


    fun updateDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
        viewModelScope.launch {
            userDataRepository.setDarkThemeConfig(darkThemeConfig)
        }
    }

    fun updateDynamicColorPreference(useDynamicColor: Boolean) {
        viewModelScope.launch {
            userDataRepository.setDynamicColorPreference(useDynamicColor)
        }
    }

    fun updateTrainingStartDelay(delayInSeconds: Int) {
        viewModelScope.launch {
            userDataRepository.updateTrainingStartDelaySecs(delayInSeconds)
        }
    }

    fun updateSoundsEnabledTrainingStart(enabled: Boolean) {
        viewModelScope.launch {
            userDataRepository.updateSoundsEnabledTrainingStart(enabled)
        }
    }

    fun updateSoundsEnabledRestStart(enabled: Boolean) {
        viewModelScope.launch {
            userDataRepository.updateSoundsEnabledRestStart(enabled)
        }
    }

    fun updateSoundsEnabledTrainingEnd(enabled: Boolean) {
        viewModelScope.launch {
            userDataRepository.updateSoundsEnabledTrainingEnd(enabled)
        }
    }

    fun updateShouldHideOnboarding(shouldHide: Boolean) {
        viewModelScope.launch {
            userDataRepository.setShouldHideOnboarding(shouldHide)
        }
    }
}

fun SettingsUiState.getTrainingStartDelaySecs():Int = (this as? SettingsUiState.Success)?.settings?.trainingStartDelaySecs ?: 5

fun SettingsUiState.isSoundsEnabledTrainingStart():Boolean = (this as? SettingsUiState.Success)?.settings?.soundsEnabledTrainingStart ?: false

/**
 * Represents the settings which the user can edit within the app.
 */
data class UserEditableSettings(
    val trainingStartDelaySecs:Int,
    val soundsEnabledTrainingStart:Boolean,
    val soundsEnabledRestStart:Boolean,
    val soundsEnabledTrainingEnd:Boolean,
    val useDynamicColor: Boolean,
    val darkThemeConfig: DarkThemeConfig,
)

sealed interface SettingsUiState {
    data object Loading : SettingsUiState
    data class Success(val settings: UserEditableSettings) : SettingsUiState
}
