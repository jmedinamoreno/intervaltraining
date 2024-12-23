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

package com.medina.intervaltraining.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medina.data.model.Training
import com.medina.data.model.TrainingStatistics
import com.medina.data.model.TrainingUIModel
import com.medina.data.model.UserInfo
import com.medina.data.repository.StatsRepository
import com.medina.data.repository.TrainingRepository
import com.medina.data.repository.UserInfoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    trainingRepository: TrainingRepository,
    statsRepository: StatsRepository,
    userInfoRepository: UserInfoRepository
) : ViewModel() {

    private val trainingUiModelFlow = combine(
        trainingRepository.trainingsFlow,
        userInfoRepository.userInfoFlow
    ) { trainings: List<Training>, userInfo: UserInfo ->
        return@combine TrainingUIModel(
            trainings = trainings,
            stats = TrainingStatistics(10f),
            userInfo = userInfo
        )
    }

    val uiState: StateFlow<MainActivityUiState> = trainingUiModelFlow.map {
        MainActivityUiState.Success(it)
    }.stateIn(
        scope = viewModelScope,
        initialValue = MainActivityUiState.Loading,
        started = SharingStarted.WhileSubscribed(5_000),
    )
}

sealed interface MainActivityUiState {
    data object Loading : MainActivityUiState
    data class Success(val uiData: TrainingUIModel) : MainActivityUiState
}
