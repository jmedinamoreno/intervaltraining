package com.medina.intervaltraining.data.viewmodel

import androidx.lifecycle.*
import com.medina.intervaltraining.data.repository.TrainingRepository
import com.medina.intervaltraining.data.room.SessionItem
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

data class Session(
    val training: UUID,
    var dateTimeStart:Long = 0L,
    var dateTimeEnd:Long = 0L,
    var complete:Boolean = false,
    val id: UUID = UUID.randomUUID()
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
            repository.deleteAllExercises(training)
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

    fun getTrainedThisWeek():LiveData<Float>{
        val weekstart = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR,0)
            set(Calendar.MINUTE,0)
            set(Calendar.SECOND,0)
        }
        val weekend = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, (firstDayOfWeek+6)%7)
            set(Calendar.HOUR,23)
            set(Calendar.MINUTE,59)
            set(Calendar.SECOND,59)
        }
        return repository.getTotalSessionTimeSecForDaterange(weekstart.timeInMillis,weekend.timeInMillis).map {
            it / 3600f
        }.asLiveData()
    }

    fun saveSession(session: Session) {
        viewModelScope.launch {
            repository.insert(SessionItem(
                id = session.id,
                training = session.training,
                complete = session.complete,
                dateTimeEnd = session.dateTimeEnd,
                dateTimeStart = session.dateTimeStart
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