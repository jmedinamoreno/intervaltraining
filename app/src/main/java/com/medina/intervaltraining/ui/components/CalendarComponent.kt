package com.medina.intervaltraining.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale

@Composable
fun InteractiveCustomCalendar2(
    modifier: Modifier = Modifier,
    startDate:Calendar,
    entryCurrentDate: Calendar,
    endDate:Calendar,
    dayComponent: @Composable BoxScope.(modifier:Modifier, day:Calendar, outsideMonth:Boolean) -> Unit = { m,d,o ->
        Text(text = d.get(Calendar.DAY_OF_MONTH).toString())
    },
    weekComponent: @Composable RowScope.(modifier:Modifier, weekday:Calendar) -> Unit = { m,w ->
        Text(modifier = m, text = w.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault())?:"",textAlign = TextAlign.Center)
    },
    monthComponent: @Composable RowScope.(modifier:Modifier, month:Calendar, outsideYear:Boolean) -> Unit = { m, mt, o ->
        val monthName = (mt.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())?:"") + if(o){
            " (${mt.get(Calendar.YEAR)})"
        }else{ "" }
        Text(modifier = m, text = monthName, textAlign = TextAlign.Center)
    }
){
    var year:Int by rememberSaveable { mutableStateOf(entryCurrentDate.get(Calendar.YEAR)) }
    var monthIndex:Int by rememberSaveable { mutableStateOf(entryCurrentDate.get(Calendar.MONTH)) }

    val calendarMonth = Calendar.getInstance().apply {
        set(Calendar.YEAR,year)
        set(Calendar.MONTH,monthIndex)
        set(Calendar.DAY_OF_MONTH,1)
    }
    Column(modifier = modifier) {
        // Month Header
        Row(modifier = Modifier
            .padding(top = 2.dp, start = 2.dp, end = 2.dp, bottom = 2.dp)
            .fillMaxWidth()){
            Button(onClick = {
                monthIndex = if(monthIndex==0) 11.also { year-- } else monthIndex - 1
            }) {
                Text(text = "<")
            }
            monthComponent(
                Modifier.weight(1f),
                calendarMonth,
                year!=entryCurrentDate.get(Calendar.YEAR)
            )
            Button(onClick = {
                monthIndex = if(monthIndex==11) 0.also { year++ } else monthIndex + 1
            }) {
                Text(text = ">")
            }
        }
        val monthCount = (endDate.get(Calendar.YEAR) - startDate.get(Calendar.YEAR))*12 + endDate.get(Calendar.MONTH) - startDate.get(Calendar.MONTH) + 1
        Text(text = "$monthCount")
        LazyRow(
            modifier = modifier,
            userScrollEnabled = true,
            reverseLayout = true,
        ) {
            items(
                count = monthCount,
                key = { offset -> offset.toString() },
            ) { offset ->
                val localCalendarMonth = Calendar.getInstance().apply {
                    set(Calendar.YEAR,startDate.get(Calendar.YEAR)+(offset/12))
                    set(Calendar.MONTH,startDate.get(Calendar.MONTH)+(offset%12))
                    set(Calendar.DAY_OF_MONTH,1)
                }
                val firstDayOfWeek = localCalendarMonth.get(Calendar.DAY_OF_WEEK).let {
                    (7 + (it - calendarMonth.firstDayOfWeek))%7
                }
                val daysInMonth = localCalendarMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
                val weeksInMonth = 1 + (daysInMonth + firstDayOfWeek - 1) / 7
                var dayCounter = 1

                Column(Modifier.fillParentMaxWidth().fillParentMaxHeight().padding(4.dp)) {
                    // Weekday Headers
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (dayOfWeek in DayOfWeek.entries) {
                            weekComponent(
                                Modifier.weight(1f),
                                Calendar.getInstance().apply {
                                    set(Calendar.DAY_OF_WEEK,dayOfWeek.value)
                                }
                            )
                        }
                    }
                    // Days Grid
                    Column {
                        repeat(weeksInMonth) { weekIndex ->
                            Row(modifier = Modifier.fillMaxWidth()) {
                                repeat(7) { dayIndex ->
                                    val cellIndex = if (weekIndex == 0 && dayIndex < firstDayOfWeek) {
                                        // Empty cells before the first day of the month
                                        null
                                    } else if (dayCounter > daysInMonth) {
                                        // Empty cells after the last day of the month
                                        null
                                    } else {
                                        dayCounter.also { dayCounter++ }
                                    }
                                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                        dayComponent(Modifier,
                                            Calendar.getInstance().apply {
                                                set(Calendar.DAY_OF_MONTH,dayCounter)
                                            },
                                            cellIndex != null)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun InteractiveCustomCalendar(
    modifier: Modifier = Modifier,
    initialDate: Calendar,
    dayComponent: @Composable BoxScope.(modifier:Modifier, day:Int?) -> Unit = { m,d ->
        Text(text = d.toString())
    },
    weekComponent: @Composable RowScope.(modifier:Modifier, weekday:String) -> Unit = { m,w ->
        Text(modifier = m, text = w,textAlign = TextAlign.Center)
    },
    monthComponent: @Composable RowScope.(modifier:Modifier, month:String) -> Unit = { m, mt ->
        Text(modifier = m, text = mt, textAlign = TextAlign.Center)
    }
){
    var year:Int by rememberSaveable { mutableStateOf(initialDate.get(Calendar.YEAR)) }
    var monthIndex:Int by rememberSaveable { mutableStateOf(initialDate.get(Calendar.MONTH)) }

    val interactiveDayComponent: @Composable BoxScope.(modifier:Modifier, day:Int?) -> Unit =
        { m, d ->
        }

    AnimatedContent(
        targetState = Pair<Int,Int>(year,monthIndex), label = "calendar",
        transitionSpec = {
            // Compare the incoming number with the previous number.
            val targetYearMonth = targetState.first*100 + targetState.second
            val initialYearMonth = initialState.first*100 + initialState.second
            if ( targetYearMonth > initialYearMonth ) {
                // If the target number is larger, it slides up and fades in
                // while the initial (smaller) number slides up and fades out.
                slideInHorizontally { width -> width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> -width } + fadeOut()
            } else {
                // If the target number is smaller, it slides down and fades in
                // while the initial number slides down and fades out.
                slideInHorizontally { width -> -width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> width } + fadeOut()
            }.using(
                // Disable clipping since the faded slide-in/out should
                // be displayed out of bounds.
                SizeTransform(clip = false)
            )
        }
    ){ targetYearMonth ->
        val calendarMonth = Calendar.getInstance().apply {
            set(Calendar.YEAR,targetYearMonth.first)
            set(Calendar.MONTH,targetYearMonth.second)
            set(Calendar.DAY_OF_MONTH,1)
        }
        val monthName = calendarMonth.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
        val firstDayOfWeek = calendarMonth.get(Calendar.DAY_OF_WEEK).let {
            (7 + (it - calendarMonth.firstDayOfWeek))%7
        }
        val daysInMonth = calendarMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
        val weeksInMonth = 1 + (daysInMonth + firstDayOfWeek - 1) / 7
        var dayCounter = 1

        Column(modifier = modifier) {
            // Month Header
            Row(modifier = Modifier
                .padding(top = 2.dp, start = 2.dp, end = 2.dp, bottom = 2.dp)
                .fillMaxWidth()){
                Button(onClick = {
                    monthIndex = if(monthIndex==0) 11.also { year-- } else monthIndex - 1
                }) {
                    Text(text = "<")
                }
                monthComponent(
                    Modifier.weight(1f),
                    if(year!=initialDate.get(Calendar.YEAR)){
                        "$monthName ($year)"
                    }else{
                        monthName
                    }
                )
                Button(onClick = {
                    monthIndex = if(monthIndex==11) 0.also { year++ } else monthIndex + 1
                }) {
                    Text(text = ">")
                }
            }

            // Weekday Headers
            Row(modifier = Modifier.fillMaxWidth()) {
                for (dayOfWeek in DayOfWeek.entries) {
                    weekComponent(
                        Modifier.weight(1f),
                        dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault())
                    )
                }
            }
            // Days Grid
            Column {
                repeat(weeksInMonth) { weekIndex ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        repeat(7) { dayIndex ->
                            val day = if (weekIndex == 0 && dayIndex < firstDayOfWeek) {
                                // Empty cells before the first day of the month
                                null
                            } else if (dayCounter > daysInMonth) {
                                // Empty cells after the last day of the month
                                null
                            } else {
                                dayCounter.also { dayCounter++ }
                            }
                            Box(
                                Modifier
                                    .weight(1f)
                                    .animateEnterExit(), contentAlignment = Alignment.Center) {
                                if(day!=null) {
                                    dayComponent(Modifier,day)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomCalendar(
    modifier: Modifier = Modifier,
    year:Int,
    monthIndex: Int,
    dayComponent: @Composable BoxScope.(modifier:Modifier, day:Int?) -> Unit = { m,d ->
        Text(text = d.toString())
    },
    weekComponent: @Composable RowScope.(modifier:Modifier, weekday:String) -> Unit = { m,w ->
        Text(modifier = m, text = w,textAlign = TextAlign.Center)
    },
    monthComponent: @Composable ColumnScope.(modifier:Modifier, month:String) -> Unit = { m, mt ->
        Text(modifier = m, text = mt, textAlign = TextAlign.Center)
    }

    ) {
    val calendarMonth = Calendar.getInstance().apply {
        set(Calendar.YEAR,year)
        set(Calendar.MONTH,monthIndex)
        set(Calendar.DAY_OF_MONTH,1)
    }
    val monthName = calendarMonth.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) ?:return
    val firstDayOfWeek = calendarMonth.get(Calendar.DAY_OF_WEEK).let {
        (7 + (it - calendarMonth.firstDayOfWeek))%7
    }
    val daysInMonth = calendarMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
    val weeksInMonth = 1 + (daysInMonth + firstDayOfWeek - 1) / 7
    var dayCounter = 1

    Column(modifier = modifier) {
        // Month Header
        monthComponent(
            Modifier.fillMaxWidth(),
            monthName
        )

        // Weekday Headers
        Row(modifier = Modifier.fillMaxWidth()) {
            for (dayOfWeek in DayOfWeek.entries) {
                weekComponent(
                    Modifier.weight(1f),
                    dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault())
                )
            }
        }
        // Days Grid
        Column {
            repeat(weeksInMonth) { weekIndex ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    repeat(7) { dayIndex ->
                        val day = if (weekIndex == 0 && dayIndex < firstDayOfWeek) {
                            // Empty cells before the first day of the month
                            null
                        } else if (dayCounter > daysInMonth) {
                            // Empty cells after the last day of the month
                            null
                        } else {
                            dayCounter.also { dayCounter++ }
                        }
                        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            if(day!=null) {
                                dayComponent(Modifier,day)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarView(
    modifier: Modifier = Modifier,
    year:Int,
    monthIndex: Int,
) {
    val calendarMonth = Calendar.getInstance().apply {
        set(Calendar.YEAR,year)
        set(Calendar.MONTH,monthIndex)
        set(Calendar.DAY_OF_MONTH,1)
    }
    val monthName = calendarMonth.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) ?:return
    val firstDayOfWeek = calendarMonth.get(Calendar.DAY_OF_WEEK).let {
        (7 + (it - calendarMonth.firstDayOfWeek))%7
    }
    val daysInMonth = calendarMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
    val weeksInMonth = 1 + (daysInMonth + firstDayOfWeek - 1) / 7
    var dayCounter = 1

    Column(modifier = modifier) {
        // Month Header
        Text(
            text = monthName,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            textAlign = TextAlign.Center
        )

        // Weekday Headers
        Row(modifier = Modifier.fillMaxWidth()) {
            for (dayOfWeek in DayOfWeek.entries) {
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp),
                    text = dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                    textAlign = TextAlign.Center
                )
            }
        }
        // Days Grid
        Column {
            repeat(weeksInMonth) { weekIndex ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    repeat(7) { dayIndex ->
                        val day = if (weekIndex == 0 && dayIndex < firstDayOfWeek) {
                            // Empty cells before the first day of the month
                            ""
                        } else if (dayCounter > daysInMonth) {
                            // Empty cells after the last day of the month
                            ""
                        } else {
                            dayCounter.toString().also { dayCounter++ }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(2.dp)
                                .aspectRatio(1f)
                                .background(if (day.isNotEmpty()) Color.LightGray else Color.Transparent),
                            contentAlignment = Alignment.Center
                        ) {
                            if (day.isNotEmpty()) {
                                Text(text = day)
                            }
                        }
                    }
                }
            }
        }
    }
}


//@Preview(showBackground = true, group = "Basic")
//@Composable
//fun DefaultCalendarPreview(@PreviewParameter(MonthProvider::class) monthIndex: Int) {
//    CalendarView(
//        modifier = Modifier.width(180.dp),
//        year = 2025,
//        monthIndex = monthIndex
//    )
//}
//@Preview(showBackground = true, group = "Custom")
//@Composable
//fun CustomCalendarPreview(@PreviewParameter(MonthProvider::class) monthIndex: Int) {
//    CustomCalendar(
//        modifier = Modifier
//            .width(220.dp)
//            .background(Color.LightGray),
//        year = 2025,
//        monthIndex = monthIndex,
//        dayComponent = { m,d ->
//            Box(modifier = m
//                .fillMaxWidth()
//                .aspectRatio(1f)
//                .padding(2.dp), contentAlignment = Alignment.Center){
//                Box(modifier = Modifier
//                    .background(Color.Cyan)
//                    .fillMaxSize(), contentAlignment = Alignment.Center){
//                    Text(text = d.toString())
//                }
//            }
//        },
//        weekComponent ={ m,w ->
//            Box(modifier = m
//                .padding(2.dp)
//                .fillMaxWidth()
//                .aspectRatio(1f), contentAlignment = Alignment.Center){
//                Box(modifier = Modifier
//                    .background(Color.Red)
//                    .fillMaxSize(), contentAlignment = Alignment.Center){
//                    Text(text = w)
//                }
//            }
//        },
//        monthComponent = { m, mt ->
//            Box(
//                modifier = m
//                    .background(Color.White)
//                    .padding(top = 2.dp, start = 2.dp, end = 2.dp, bottom = 2.dp)
//                    .fillMaxWidth(),
//                contentAlignment = Alignment
//                    .Center
//            ) {
//                Box(
//                    modifier = Modifier
//                        .background(Color.Green)
//                        .fillMaxWidth(),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Text(text = mt)
//                }
//            }
//        }
//    )
//}

@Preview(showBackground = true, group = "Interactive")
@Composable
fun InteractiveCalendar2Preview() {
    InteractiveCustomCalendar2(
        modifier = Modifier
            .width(220.dp)
            .background(Color.LightGray),
        startDate = Calendar.getInstance().apply { set(Calendar.YEAR,2023) },
        endDate = Calendar.getInstance().apply { set(Calendar.YEAR,2025) },
        entryCurrentDate = Calendar.getInstance()
    )
}
@Preview(showBackground = true, group = "Interactive")
@Composable
fun InteractiveCalendarPreview() {
    InteractiveCustomCalendar(
        modifier = Modifier
            .width(220.dp)
            .background(Color.LightGray),
        initialDate = Calendar.getInstance(),
        dayComponent = { m,d ->
            Box(modifier = m
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(2.dp), contentAlignment = Alignment.Center){
                Box(modifier = Modifier
                    .background(Color.Cyan)
                    .fillMaxSize(), contentAlignment = Alignment.Center){
                    Text(text = d.toString())
                }
            }
        },
        weekComponent ={ m,w ->
            Box(modifier = m
                .padding(2.dp)
                .fillMaxWidth()
                .aspectRatio(1f), contentAlignment = Alignment.Center){
                Box(modifier = Modifier
                    .background(Color.Red)
                    .fillMaxSize(), contentAlignment = Alignment.Center){
                    Text(text = w)
                }
            }
        },
        monthComponent = { m, mt ->
            Box(
                modifier = m
                    .background(Color.White)
                    .padding(top = 2.dp, start = 2.dp, end = 2.dp, bottom = 2.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment
                    .Center
            ) {
                Box(
                    modifier = Modifier
                        .background(Color.Green)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = mt)
                }
            }
        }
    )
}

class MonthProvider : PreviewParameterProvider<Int> {
    override val values = sequenceOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)
}
