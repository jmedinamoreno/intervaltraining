package com.medina.intervaltraining.data.viewmodel


import androidx.lifecycle.*
import com.medina.intervaltraining.data.repository.TrainingRepository

import kotlinx.coroutines.launch
import java.util.*

class ExerciseViewModel(private val repository: TrainingRepository, trainingId: UUID) : ViewModel() {

    private var _training = MutableLiveData<Training>()
    private var _exerciseList = MutableLiveData(listOf<Exercise>())
    val loaded = MutableLiveData(false)
    val training: LiveData<Training> = _training
    val exercises: LiveData<List<Exercise>> = _exerciseList

    init {
        viewModelScope.launch {
            val trainingItem = repository.getTraining(uuid = trainingId)
            _training.value = Training(
                id = trainingItem.id,
                defaultTimeSec = trainingItem.defaultTimeSec,
                defaultRestSec = trainingItem.defaultRestSec,
                name = trainingItem.name,
            )
            val exercises = repository.exercises(training = trainingId)
            _exerciseList.value = exercises.map { item ->
                        Exercise(name = item.name,
                            icon = item.icon,
                            timeSec = item.timeSec,
                            restSec = item.restSec,
                            id = item.id,
                        ) }
            loaded.value = true
        }
    }

    // event: addItem
    fun addItem(item: Exercise) {

    }

    // event: removeItem
    fun removeItem(item: Exercise) {

    }
}

data class Exercise(
    val name:String,
    val icon: ExerciseIcon = ExerciseIcon.NONE,
    val timeSec:Int=-1,
    val restSec:Int=-1,
    // since the user may generate identical tasks, give them each a unique ID
    val id: UUID = UUID.randomUUID()
)

enum class ExerciseIcon{NONE,RUN,JUMP,LEFT_ARM,RIGHT_ARM,SIT_UP,PUSH_UPS,FLEX}

