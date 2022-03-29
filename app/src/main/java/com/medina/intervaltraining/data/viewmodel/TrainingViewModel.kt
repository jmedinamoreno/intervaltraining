package com.medina.intervaltraining.data.viewmodel

import androidx.lifecycle.*
import com.medina.intervaltraining.data.repository.TrainingRepository
import com.medina.intervaltraining.data.room.TrainingItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

data class Training(
    var name:String,
    var defaultTimeSec:Int,
    var defaultRestSec:Int,
    val id: UUID = UUID.randomUUID(),
    var draft:Boolean = false
)

class TrainingViewModel(val repository: TrainingRepository):ViewModel(){

    private val trainingListFlow = repository.trainingsFlow.map {
        it.map { trainingItem ->
            Training(
                id = trainingItem.id,
                defaultTimeSec = trainingItem.defaultTimeSec,
                defaultRestSec = trainingItem.defaultRestSec,
                name = trainingItem.name
            )
        }
    }
    val trainingList: LiveData<List<Training>> = trainingListFlow.asLiveData()

    fun getTimeForTrainingLiveData(id: UUID):LiveData<Int> {
        return repository.timeForTrainingMinAsFlow(id).asLiveData()
    }

    fun delete(training: UUID) {
        viewModelScope.launch {
            repository.deleteTraining(training)
        }
    }

    fun update(training: Training) {
        viewModelScope.launch {
            repository.insert(TrainingItem(
                id = training.id,
                name = training.name,
                defaultTimeSec = training.defaultTimeSec,
                defaultRestSec = training.defaultRestSec,
                lastUsed = Date().time
            ))
        }
    }
}


class TrainingViewModelFactory(private val repository: TrainingRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TrainingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TrainingViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}