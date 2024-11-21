package com.medina.intervaltraining.viewmodel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.medina.data.Clock
import com.medina.data.RealClock
import com.medina.data.model.Training
import com.medina.data.repository.TrainingRepository
import com.medina.data.local.database.TrainingItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TrainingViewModel @Inject constructor(
    private val trainingRepository: TrainingRepository,
    private val clock: Clock
):ViewModel(){

    val trainingList: LiveData<List<Training>> = trainingRepository.trainingsFlow.asLiveData()

    fun delete(training: UUID) {
        viewModelScope.launch {
            try {
                trainingRepository.deleteAllExercises(training)
            }catch (e:Exception){
                e.printStackTrace()
            }
            try {
                trainingRepository.deleteTraining(training)
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }

    fun update(training: Training) {
        viewModelScope.launch {
            try {
                trainingRepository.insert(
                    TrainingItem(
                    id = training.id,
                    name = training.name,
                    defaultTimeSec = training.defaultTimeSec,
                    defaultRestSec = training.defaultRestSec,
                    lastUsed = clock.timestamp()
                )
                )
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }
}
