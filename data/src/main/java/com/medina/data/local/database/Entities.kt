package com.medina.data.local.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.medina.data.model.ExerciseIcon
import java.util.UUID

@Entity(
    tableName = "session_table",
    foreignKeys = [
        ForeignKey(
            entity = TrainingItem::class,
            parentColumns = ["id"],
            childColumns = ["training"],
            onUpdate = ForeignKey.NO_ACTION,
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SessionItem(
    val training: UUID,
    var dateTimeStart:Long = 0L,
    var dateTimeEnd:Long = 0L,
    var complete:Boolean = false,
    @PrimaryKey val id: UUID = UUID.randomUUID()
)

@Entity(
    tableName = "exercise_table",
    indices = [
        Index("training"),
        Index("position"),
    ],
    foreignKeys = [
        ForeignKey(
            entity = TrainingItem::class,
            parentColumns = ["id"],
            childColumns = ["training"],
            onUpdate = ForeignKey.NO_ACTION,
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ExerciseItem(
    val training: UUID,
    val name:String,
    val icon: ExerciseIcon = ExerciseIcon.NONE,
    val position:Int,
    val timeSec:Int,
    val restSec:Int,
    @PrimaryKey val id: UUID = UUID.randomUUID()
)

@Entity(
    tableName = "training_table",
)
data class TrainingItem(
    val name:String,
    val lastUsed:Long,
    val defaultTimeSec:Int,
    val defaultRestSec:Int,
    val totalTimeSec: Int = 0,
    @PrimaryKey val id: UUID = UUID.randomUUID()
)