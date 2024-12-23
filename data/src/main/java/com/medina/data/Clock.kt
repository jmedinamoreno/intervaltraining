package com.medina.data

interface Clock {
    fun timestamp():Long
}

class RealClock: Clock {
    override fun timestamp():Long{
        return System.currentTimeMillis()
    }
}