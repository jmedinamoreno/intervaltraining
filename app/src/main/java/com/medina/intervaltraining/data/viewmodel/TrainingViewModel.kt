package com.medina.intervaltraining.data.viewmodel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.*
import com.medina.intervaltraining.data.Clock
import com.medina.intervaltraining.data.RealClock
import com.medina.intervaltraining.data.model.Session
import com.medina.intervaltraining.data.model.Training
import com.medina.intervaltraining.data.model.TrainingStatistics
import com.medina.intervaltraining.data.model.TrainingUIModel
import com.medina.intervaltraining.data.model.UserData
import com.medina.intervaltraining.data.repository.TrainingRepository
import com.medina.intervaltraining.data.repository.UserDataDatastoreRepository
import com.medina.intervaltraining.data.repository.UserDataRepository
import com.medina.intervaltraining.data.room.SessionItem
import com.medina.intervaltraining.data.room.TrainingItem
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*



class TrainingViewModel(
    val repository: TrainingRepository,
    private val userDataRepository: UserDataRepository,
    private val clock: Clock = RealClock()
):ViewModel(){

    private val userPreferencesFlow = userDataRepository.userData

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

    // Every time the sort order, the show completed filter or the list of tasks emit,
    // we should recreate the list of tasks
    private val trainingUiModelFlow = combine(
        trainingListFlow,
        userPreferencesFlow
    ) { trainings: List<Training>, userData: UserData ->
        return@combine TrainingUIModel(
            trainings = trainings,
            stats = TrainingStatistics(10f),
            userData = userData
        )
    }
    val trainingUiModel = trainingUiModelFlow.asLiveData()

    fun getTimeForTrainingLiveData(id: UUID):LiveData<Int> {
        return repository.timeForTrainingMinAsFlow(id).asLiveData()
    }

    fun delete(training: UUID) {
        viewModelScope.launch {
            try {
                repository.deleteAllExercises(training)
            }catch (e:Exception){
                e.printStackTrace()
            }
            try {
                repository.deleteTraining(training)
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }

    fun update(training: Training) {
        viewModelScope.launch {
            try {
                repository.insert(TrainingItem(
                    id = training.id,
                    name = training.name,
                    defaultTimeSec = training.defaultTimeSec,
                    defaultRestSec = training.defaultRestSec,
                    lastUsed = clock.timestapm()
                ))
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }

    fun getTrainedThisWeek():LiveData<Float>{
        val weekstart = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR,0)
            set(Calendar.MINUTE,0)
            set(Calendar.SECOND,0)
            set(Calendar.MILLISECOND,0)
        }
        val weekend = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, (firstDayOfWeek+6)%7)
            set(Calendar.HOUR,23)
            set(Calendar.MINUTE,59)
            set(Calendar.SECOND,59)
            set(Calendar.MILLISECOND,0)
        }
        return repository.getTotalSessionTimeSecForDateRange(weekstart.timeInMillis,weekend.timeInMillis).map {
            it / 3600f
        }.asLiveData()
    }

    fun saveSession(session: Session) {
        viewModelScope.launch {
            try {
                repository.insert(
                    SessionItem(
                        id = session.id,
                        training = session.training,
                        complete = session.complete,
                        dateTimeEnd = session.dateTimeEnd,
                        dateTimeStart = session.dateTimeStart
                    )
                )
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }
}


class TrainingViewModelFactory(private val repository: TrainingRepository,private val dataStore: DataStore<Preferences>) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TrainingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TrainingViewModel(
                repository,
                UserDataDatastoreRepository(dataStore)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}