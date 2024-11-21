package com.medina.generation.repository

import com.medina.data.model.EmptyTraining
import com.medina.data.model.Exercise
import com.medina.data.model.Training
import com.medina.generation.local.TextResources
import javax.inject.Inject

interface GenerationRepository{
    fun suggestTrainingNames(): List<String>
    fun suggestTraining(): Training
    fun suggestExercise(): Exercise
}

class GenerationDataRepository  @Inject constructor(
    private val textResources: TextResources
): GenerationRepository{
    override fun suggestTrainingNames(): List<String> =
        textResources.trainingNames

    override fun suggestTraining(): Training = Training(
        name = textResources.randomTrainingName(),
        defaultTimeSec = 45,
        defaultRestSec = 15,
        lastUsed = 0,
        totalTimeSec = 0,
        draft = true
    )

    override fun suggestExercise(): Exercise  = textResources.predefinedExercises.random().let {
        (names, icon) -> Exercise(
            name = names.random(),
            icon = icon,
            timeSec = 45,
            restSec = 15
        )
    }
}

class GenerationDummyRepository: GenerationRepository{
    override fun suggestTrainingNames(): List<String> {
        return listOf("Test Training 1", "Test Training 2")
    }

    override fun suggestTraining(): Training {
        return EmptyTraining.copy(name =  "Suggested Training")
    }

    override fun suggestExercise(): Exercise {
        return Exercise("Suggested Exercise")
    }

}