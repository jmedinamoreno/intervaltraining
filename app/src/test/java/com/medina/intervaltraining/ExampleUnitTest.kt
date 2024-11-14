package com.medina.intervaltraining

import org.junit.Test

import org.junit.Assert.*
import kotlin.system.measureTimeMillis

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val input = ArrayList<Int>()
        for(i in 1..100000){
            input.add(i/2)
        }
        input.add(100000)
        val metrics = LongArray(10)
        for(veces in 0..9) {
            input.shuffle()
            val time = measureTimeMillis {
                findUnpairedElementInIA(input)
            }
            metrics[veces] = time
        }
        println("IA Found in average ${metrics.average()} ms")

        for(veces in 0..9) {
            input.shuffle()
            val time = measureTimeMillis {
                findUnpairedElementInMine(input)
            }
            metrics[veces] = time
        }
        println("You found in average ${metrics.average()} ms")
    }

    /**
     * A non-empty array A consisting of N integers is given.
     * The array contains an odd number of elements, and each element of the array
     * can be paired with another element that has the same value,
     * except for one element that is left unpaired.
     * Find that element.
     */
    fun findUnpairedElementInIA(input: ArrayList<Int>): Int {
        val occurrences = mutableMapOf<Int, Int>()

        for (element in input) {
            occurrences[element] = occurrences.getOrDefault(element, 0) + 1
        }

        for ((element, count) in occurrences) {
            if (count % 2 != 0) {
                return element
            }
        }
        throw IllegalArgumentException("Input array does not contain an unpaired element.")
    }

    fun findUnpairedElementInMine(input: ArrayList<Int>): Int {
        val oddElements = input.toIntArray()
        val setElements = oddElements.toSet()
        return setElements.first { num ->
            oddElements.count { it == num } == 1
        }
    }
}