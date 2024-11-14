package com.medina.domain.data.model

import com.medina.domain.data.room.TrainingItem
import java.util.UUID

data class TrainingUIModel(
    val trainings:List<Training>,
    val stats: TrainingStatistics,
    val userData: UserData,
)

data class TrainingStatistics(val trainedThisWeek:Float)

data class UserData(
    val trainingStartDelaySecs:Int,
    val soundsEnabledTrainingStart:Boolean,
    val soundsEnabledExerciseStart:Boolean,
    val soundsEnabledRestStart:Boolean,
    val soundsEnabledTrainingEnd:Boolean,
    val countdownToChange:Int,
    val themeBrand: ThemeBrand,
    val darkThemeConfig: DarkThemeConfig,
    val useDynamicColor: Boolean,
    val shouldHideOnboarding: Boolean,
)
enum class ThemeBrand {
    DEFAULT,
    ANDROID,
}
enum class DarkThemeConfig {
    FOLLOW_SYSTEM,
    LIGHT,
    DARK,
}

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

enum class ExerciseIcon{NONE,RUN,JUMP,LEFT_ARM,RIGHT_ARM,SIT_UP,PUSH_UPS,FLEX,KNEES}