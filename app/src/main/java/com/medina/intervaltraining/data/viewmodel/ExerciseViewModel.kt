package com.medina.intervaltraining.data.viewmodel


import androidx.lifecycle.*
import com.medina.intervaltraining.data.repository.TrainingRepository
import com.medina.intervaltraining.data.room.ExerciseItem
import com.medina.intervaltraining.data.room.TrainingItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

import kotlinx.coroutines.launch
import java.util.*

data class Exercise(
    val name:String,
    val icon: ExerciseIcon = ExerciseIcon.NONE,
    val timeSec:Int=-1,
    val restSec:Int=-1,
    // since the user may generate identical tasks, give them each a unique ID
    val id: UUID = UUID.randomUUID()
){
    fun copy() = this.copy(id = UUID.randomUUID())
}

enum class ExerciseIcon{NONE,RUN,JUMP,LEFT_ARM,RIGHT_ARM,SIT_UP,PUSH_UPS,FLEX}

class ExerciseViewModel(
    private val repository: TrainingRepository,
    private val trainingId: UUID) : ViewModel() {

    val training: LiveData<Training> = repository.getTrainingFlow(trainingId).map { it?.let { Training(
        id = it.id,
        defaultTimeSec = it.defaultTimeSec,
        defaultRestSec = it.defaultRestSec,
        name = it.name,
    ) }?: Training(
        id = trainingId,
        defaultTimeSec = 45,
        defaultRestSec = 15,
        name = "",
        draft = true
    ) }.asLiveData()

    val exercises: LiveData<List<Exercise>> = repository.exercisesFlow(trainingId).map { list ->
        list.map { item ->
                        Exercise(name = item.name,
                            icon = item.icon,
                            timeSec = item.timeSec,
                            restSec = item.restSec,
                            id = item.id,
                        ) }
    }.asLiveData()

    fun saveTraining(training: Training) {
        viewModelScope.launch {
            val ti = TrainingItem(
                name = training.name,
                defaultRestSec = training.defaultRestSec,
                defaultTimeSec = training.defaultTimeSec,
                lastUsed = Date().time
            )
            repository.insert(ti)
        }
    }

    fun saveExerciseList(newList: List<Exercise>) {
        val toDelete = exercises.value?.filter { !newList.contains(it) }
        toDelete?.forEach {
            viewModelScope.launch {
                repository.deleteExercise(it.id)
            }
        }
        newList.forEachIndexed{ index, exercise ->
            saveExercise(exercise = exercise, position = index)
        }
    }

    fun saveExercise(exercise: Exercise, position:Int) {
        viewModelScope.launch {
            val ei = ExerciseItem(
                id = exercise.id,
                training = trainingId,
                name = exercise.name,
                icon = exercise.icon,
                restSec = exercise.restSec,
                timeSec = exercise.timeSec,
                position = position
            )
            repository.insert(ei)
        }
    }
}

