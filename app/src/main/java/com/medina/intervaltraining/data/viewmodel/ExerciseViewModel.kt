package com.medina.intervaltraining.data.viewmodel


import android.util.Log
import androidx.lifecycle.*
import androidx.lifecycle.asLiveData
import com.medina.intervaltraining.data.repository.TrainingRepository
import com.medina.intervaltraining.data.room.ExerciseItem
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
    fun newCopy() = this.copy(id = UUID.randomUUID())
}

enum class ExerciseIcon{NONE,RUN,JUMP,LEFT_ARM,RIGHT_ARM,SIT_UP,PUSH_UPS,FLEX}

class ExerciseViewModel(
    private val repository: TrainingRepository,
    val trainingId: UUID) : ViewModel() {

    val session = Session(training = trainingId)

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

    fun saveExerciseList(newList: List<Exercise>) {
        if(newList.isEmpty()){
            Log.e("JMMLOG", "ExerciseViewModel: error saving the list")
            return
        }
        val toDelete = exercises.value?.filter { !newList.contains(it) }
        toDelete?.forEach {
            viewModelScope.launch {
                repository.deleteExercise(it.id)
            }
        }
        Log.d("JMMLOG", "ExerciseViewModel: saveList:  $newList")
        newList.forEachIndexed{ index, exercise ->
            Log.d("JMMLOG", "ExerciseViewModel: saveList:  $index -> $exercise")
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

