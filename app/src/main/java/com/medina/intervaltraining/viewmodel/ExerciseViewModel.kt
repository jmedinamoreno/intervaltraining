package com.medina.intervaltraining.viewmodel


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.*

class ExerciseViewModel : ViewModel() {

    // state: todoItems
    private var _exerciseItems = MutableLiveData(listOf<Exercise>())
    val exerciseItems: LiveData<List<Exercise>> = _exerciseItems

    // event: addItem
    fun addItem(item: Exercise) {
        _exerciseItems.value = _exerciseItems.value!! + listOf(item)
    }

    // event: removeItem
    fun removeItem(item: Exercise) {
        _exerciseItems.value = _exerciseItems.value!!.toMutableList().also {
            it.remove(item)
        }
    }
}

data class Exercise(val name:String, val icon: ExerciseIcon = ExerciseIcon.NONE, val timeSec:Int=-1, val restSec:Int=-1,
    // since the user may generate identical tasks, give them each a unique ID
    val id: UUID = UUID.randomUUID()
)

enum class ExerciseIcon{NONE,RUN,JUMP,LEFT_ARM,RIGHT_ARM,SIT_UP,PUSH_UPS,FLEX}

