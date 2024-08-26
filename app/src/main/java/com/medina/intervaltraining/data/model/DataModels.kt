package com.medina.intervaltraining.data.model

import java.util.UUID

data class TrainingUIModel(
    val trainings:List<Training>,
    val stats:TrainingStatistics,
    val userData: UserData,
)

data class TrainingStatistics(val trainedThisWeek:Float)

data class UserData(
    val trainingStartDelaySecs:Int,
    val soundsEnabledTrainingStart:Boolean,
    val soundsEnabledRestStart:Boolean,
    val soundsEnabledTrainingEnd:Boolean,
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