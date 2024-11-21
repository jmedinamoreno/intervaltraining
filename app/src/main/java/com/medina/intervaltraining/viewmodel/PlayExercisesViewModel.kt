package com.medina.intervaltraining.viewmodel


import androidx.lifecycle.*
import androidx.lifecycle.asLiveData
import com.medina.data.Clock
import com.medina.data.RealClock
import com.medina.data.model.Exercise
import com.medina.data.model.Session
import com.medina.data.model.Training
import com.medina.data.model.toExercise
import com.medina.data.model.toTraining
import com.medina.data.repository.TrainingRepository
import com.medina.generation.repository.GenerationDummyRepository
import com.medina.generation.repository.GenerationRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import java.util.*

@HiltViewModel(assistedFactory = PlayExercisesViewModel.ViewModelFactory::class)
class PlayExercisesViewModel @AssistedInject constructor(
    @Assisted private val trainingId: UUID,
    trainingRepository: TrainingRepository,
) : ViewModel() {

    @AssistedFactory
    interface ViewModelFactory {
        fun create(trainingId: UUID): PlayExercisesViewModel
    }

    val session = Session(training = trainingId)

    val training: LiveData<Training> = trainingRepository.getTrainingFlow(trainingId).map { it?.toTraining() ?: Training(
        id = trainingId,
        defaultTimeSec = 45,
        defaultRestSec = 15,
        name = "",
        lastUsed = 0,
        totalTimeSec = 0,
        draft = true
    ) }.asLiveData()

    val exercises: LiveData<List<Exercise>> = trainingRepository.exercisesFlow(trainingId).map { list ->
        list.map { item -> item.toExercise() }
    }.asLiveData()

}

