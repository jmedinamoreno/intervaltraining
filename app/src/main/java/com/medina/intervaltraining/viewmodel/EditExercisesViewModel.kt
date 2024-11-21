package com.medina.intervaltraining.viewmodel


import android.util.Log
import androidx.lifecycle.*
import androidx.lifecycle.asLiveData
import com.medina.data.Clock
import com.medina.data.RealClock
import com.medina.data.model.EmptyTraining
import com.medina.data.model.Exercise
import com.medina.data.model.Session
import com.medina.data.model.Training
import com.medina.data.model.toExercise
import com.medina.data.model.toExerciseItem
import com.medina.data.model.toTraining
import com.medina.data.model.toTrainingItem
import com.medina.data.repository.TrainingDummyRepository
import com.medina.data.repository.TrainingRepository
import com.medina.generation.repository.GenerationDummyRepository
import com.medina.generation.repository.GenerationRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.*

val fakeExercisesViewModel = EditExercisesViewModel(
    trainingId = UUID.randomUUID(),
    trainingRepository = TrainingDummyRepository(),
    generationRepository = GenerationDummyRepository(),
    clock = RealClock()
)

@HiltViewModel(assistedFactory = EditExercisesViewModel.ViewModelFactory::class)
class EditExercisesViewModel @AssistedInject constructor(
    @Assisted private val trainingId: UUID,
    private val trainingRepository: TrainingRepository,
    private val generationRepository: GenerationRepository,
    private val clock: Clock
) : ViewModel() {

    @AssistedFactory
    interface ViewModelFactory {
        fun create(trainingId: UUID): EditExercisesViewModel
    }

    val training: LiveData<Training> = trainingRepository.getTrainingFlow(trainingId).map {
        it?.toTraining() ?: EmptyTraining.copy(id = trainingId)
    }.asLiveData()

    val exercises: LiveData<List<Exercise>> = trainingRepository.exercisesFlow(trainingId).map { list ->
        list.map { item -> item.toExercise() }
    }.asLiveData()

    fun deleteTraining() {
        viewModelScope.launch {
            try {
                trainingRepository.deleteAllExercises(trainingId)
            }catch (e:Exception){
                e.printStackTrace()
            }
            try {
                trainingRepository.deleteTraining(trainingId)
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }

    fun updateTrainingName(newName: String){
        if(newName.isNotBlank() && newName != training.value?.name) {
            if(training.value?.name?.isNotBlank()==true) {
                renameTraining(newName)
            }else {
                createTraining(newName)
            }
        }
    }

    fun updateExercise(newExercise:Exercise){
        val newList = ArrayList(exercises.value?: emptyList())
        val pos = newList.indexOfFirst { e -> e.id == newExercise.id }
        if (pos >= 0) {
            newList[pos] = newExercise
        }else{
            newList.add(newExercise)
        }
        saveExerciseList(newList)
    }

    fun duplicateExercise(index:Int){
        val newList = exercises.value?.takeIf{ it.size > index }?.let { ArrayList(it) } ?: return
        newList.add(index+1, newList[index].newCopy())
        saveExerciseList(newList)
    }

    fun deleteExercise (index:Int){
        val newList = exercises.value?.takeIf{ it.size > index }?.let { ArrayList(it) } ?: return
        newList.removeAt(index)
        saveExerciseList(newList)
    }

    fun moveExercise(oldIndex:Int, toNewIndex:Int){
        val newList = exercises.value?.takeIf{ it.size > oldIndex && it.size > toNewIndex }?.let { ArrayList(it) } ?: return
        val exercise = newList[oldIndex]
        newList.removeAt(oldIndex)
        if(toNewIndex>=newList.size){
            newList.add(exercise)
        }else {
            newList.add(toNewIndex, exercise)
        }
        saveExerciseList(newList)
    }

    private fun createTraining(newName: String) {
        viewModelScope.launch {
            trainingRepository.insert(
                training.value
                    ?.copy(name = newName)
                    ?.toTrainingItem(lastUsed = clock.timestamp())
                    ?: return@launch)
        }
    }

    private fun renameTraining(newName: String) {
        viewModelScope.launch {
            trainingRepository.update(
                training.value
                    ?.copy(name = newName)
                    ?.toTrainingItem(lastUsed = clock.timestamp())
                    ?: return@launch)
        }
    }

    private fun saveExerciseList(newList: List<Exercise>) {
        if(newList.isEmpty()){
            Log.e("JMMLOG", "ExerciseViewModel: error saving the list")
            return
        }
        val toDelete = exercises.value?.filter { !newList.contains(it) }
        toDelete?.forEach {
            viewModelScope.launch {
                trainingRepository.deleteExercise(it.id)
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
            trainingRepository.insert(exercise = exercise.toExerciseItem(
                trainingId = trainingId,
                position = position
            ))
        }
    }

    fun suggestTrainingName(): String = generationRepository.suggestTrainingNames().random()
    fun suggestExercise(): Exercise = generationRepository.suggestExercise()
}

