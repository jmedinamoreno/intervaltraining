package com.medina.intervaltraining.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.medina.domain.data.Clock
import com.medina.domain.data.RealClock
import com.medina.domain.data.model.Session
import com.medina.domain.data.repository.StatsRepository
import com.medina.domain.data.room.SessionItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val statsRepository: StatsRepository,
    private val clock: Clock = RealClock()
): ViewModel(){

    fun getTimeForTrainingLiveData(id: UUID): LiveData<Int> {
        return statsRepository.timeForTrainingMinAsFlow(id).asLiveData()
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
        return statsRepository.getTotalSessionTimeSecForDateRange(weekstart.timeInMillis,weekend.timeInMillis).map {
            it / 3600f
        }.asLiveData()
    }

    fun getTrainedSecForEachDayInMonth(calendar: Calendar):LiveData<List<Long>>{
        return statsRepository.getSessionTimeSecForEachDayInMonth(calendar).asLiveData()
    }

    fun saveSession(session: Session) {
        viewModelScope.launch {
            try {
                statsRepository.insert(
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

class StatsViewModelFactory(private val repository: StatsRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StatsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StatsViewModel(
                repository,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}