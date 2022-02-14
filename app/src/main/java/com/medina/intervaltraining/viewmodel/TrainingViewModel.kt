package com.medina.intervaltraining.viewmodel

import java.util.*

data class Training(val timeMin:Int, val name:String, val exerciseTable:List<Exercise>,val id: UUID = UUID.randomUUID()){}