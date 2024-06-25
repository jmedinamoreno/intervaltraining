package com.medina.intervaltraining.data

interface Clock {
    fun timestapm():Long

}

class RealClock:Clock{
    override fun timestapm():Long{
        return System.currentTimeMillis()
    }
}