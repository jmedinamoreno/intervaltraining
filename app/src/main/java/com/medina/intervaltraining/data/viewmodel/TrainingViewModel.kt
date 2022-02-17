package com.medina.intervaltraining.data.viewmodel

import androidx.lifecycle.*
import com.medina.intervaltraining.data.repository.TrainingRepository
import com.medina.intervaltraining.data.room.ExerciseItem
import com.medina.intervaltraining.data.room.TrainingItem
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

data class Training(
    val name:String,
    val defaultTimeSec:Int,
    val defaultRestSec:Int,
    val id: UUID = UUID.randomUUID()
)

class TrainingViewModel(val repository: TrainingRepository):ViewModel(){

    fun newTraining(name: String) {
        viewModelScope.launch {
            val ti = TrainingItem(
                name = name,
                defaultRestSec = 15,
                defaultTimeSec = 45,
                lastUsed = Date().time
            )
            val ei = ExerciseItem(
                training = ti.id,
                name = "Jump",
                icon = ExerciseIcon.JUMP,
                position = 1,
                timeSec = ti.defaultTimeSec,
                restSec = ti.defaultRestSec
            )
            repository.insert(ti)
            repository.insert(ei)
        }
    }

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