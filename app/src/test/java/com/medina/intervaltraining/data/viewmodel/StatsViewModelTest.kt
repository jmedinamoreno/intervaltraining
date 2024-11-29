package com.medina.intervaltraining.data.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.medina.data.Clock
import com.medina.data.local.database.SessionItem
import com.medina.data.model.Session
import com.medina.data.repository.StatsRepository
import com.medina.intervaltraining.viewmodel.StatsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*
import java.util.Calendar
import java.util.Date
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class StatsViewModelTest {

    // Run tasks synchronously
    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Mock
    private lateinit var repository: StatsRepository
    @Mock
    private lateinit var clock: Clock

    private lateinit var viewModel: StatsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = StatsViewModel(
            statsRepository = repository,
            clock = clock
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

//    @Test
//    fun getTimeForTrainingLiveData_returnsTimeForTrainingFromRepository(){
//        val trainingId = UUID.randomUUID()
//        val expectedTime = 30
//        whenever(repository.timeForTrainingMinAsFlow(trainingId)).thenReturn(flowOf(expectedTime))
//        assertEquals(expectedTime, viewModel.getTimeForTrainingLiveData(trainingId).getOrAwaitValue())
//    }

    @Test
    fun getTrainedThisWeek_returnsValueFromRepository(){
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
        whenever(repository.getTotalSessionTimeSecForDateRange(weekstart.timeInMillis,weekend.timeInMillis)).thenReturn(flowOf(7200f))
        assertEquals(2f, viewModel.getTrainedThisWeek().getOrAwaitValue())
        verify(repository).getTotalSessionTimeSecForDateRange(
            weekstart.timeInMillis,
            weekend.timeInMillis
        )
    }

    @Test
    fun saveSession_callsRepositoryInsert(){
        val sessionId = UUID.randomUUID()
        val sessionItem = SessionItem(
            id = sessionId,
            training = UUID.randomUUID(),
            dateTimeStart = Date().time,
            dateTimeEnd = Date().time
        )
        val session = Session(
            id = sessionId,
            training = sessionItem.training,
            complete = sessionItem.complete,
            dateTimeEnd = sessionItem.dateTimeEnd,
            dateTimeStart = sessionItem.dateTimeStart
        )
        viewModel.saveSession(session)
        testScope.runTest {
            verify(repository).insert(sessionItem)
        }
    }

    fun <T> LiveData<T>.getOrAwaitValue(
        time: Long = 200L,
        timeUnit: TimeUnit = TimeUnit.MILLISECONDS
    ): T {
        var data: T? = null
        val latch = CountDownLatch(1)
        val observer = object : Observer<T> {
            override fun onChanged(value: T) {
                data = value
                latch.countDown()
                this@getOrAwaitValue.removeObserver(this)
            }
        }
        testScope.runTest {
            this@getOrAwaitValue.observeForever(observer)
        }
        // Don't wait indefinitely if the LiveData is not set.
        if (!latch.await(time, timeUnit)) {
            throw TimeoutException("LiveData value was never set. $data")
        }
        @Suppress("UNCHECKED_CAST")
        return data as T
    }
}
