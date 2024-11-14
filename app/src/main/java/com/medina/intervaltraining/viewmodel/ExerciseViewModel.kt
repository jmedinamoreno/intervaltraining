package com.medina.intervaltraining.viewmodel


import android.util.Log
import androidx.lifecycle.*
import androidx.lifecycle.asLiveData
import com.medina.domain.data.Clock
import com.medina.domain.data.RealClock
import com.medina.domain.data.model.Exercise
import com.medina.domain.data.model.Session
import com.medina.domain.data.model.Training
import com.medina.domain.data.model.toExercise
import com.medina.domain.data.model.toExerciseItem
import com.medina.domain.data.model.toTraining
import com.medina.domain.data.model.toTrainingItem
import com.medina.domain.data.repository.TrainingRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.*

@HiltViewModel(assistedFactory = ExerciseViewModel.ExerciseViewModelFactory::class)
class ExerciseViewModel @AssistedInject constructor(
    @Assisted private val trainingId: UUID,
    private val repository: TrainingRepository,
    private val clock: Clock = RealClock()
) : ViewModel() {

    @AssistedFactory
    interface ExerciseViewModelFactory {
        fun create(trainingId: UUID): ExerciseViewModel
    }

    val session = Session(training = trainingId)

    val training: LiveData<Training> = repository.getTrainingFlow(trainingId).map { it?.toTraining() ?: Training(
        id = trainingId,
        defaultTimeSec = 45,
        defaultRestSec = 15,
        name = "",
        draft = true
    ) }.asLiveData()

    val exercises: LiveData<List<Exercise>> = repository.exercisesFlow(trainingId).map { list ->
        list.map { item -> item.toExercise() }
    }.asLiveData()

    fun deleteTraining() {
        viewModelScope.launch {
            try {
                repository.deleteAllExercises(trainingId)
            }catch (e:Exception){
                e.printStackTrace()
            }
            try {
                repository.deleteTraining(trainingId)
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }

    fun createTraining(newName: String) {
        viewModelScope.launch {
            repository.insert(
                training.value
                    ?.copy(name = newName)
                    ?.toTrainingItem(lastUsed = clock.timestamp())
                    ?: return@launch)
        }
    }

    fun renameTraining(newName: String) {
        viewModelScope.launch {
            repository.update(
                training.value
                    ?.copy(name = newName)
                    ?.toTrainingItem(lastUsed = clock.timestamp())
                    ?: return@launch)
        }
    }

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

    private fun saveExercise(exercise: Exercise, position:Int) {
        viewModelScope.launch {
            repository.insert(exercise = exercise.toExerciseItem(
                trainingId = trainingId,
                position = position
            ))
        }
    }
}

