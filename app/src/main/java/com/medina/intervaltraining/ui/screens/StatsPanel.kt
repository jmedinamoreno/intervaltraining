package com.medina.intervaltraining.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.medina.intervaltraining.data.repository.StatsDummyRepository
import com.medina.intervaltraining.data.viewmodel.StatsViewModel
import com.medina.intervaltraining.ui.components.CustomCalendar
import com.medina.intervaltraining.ui.components.InteractiveCustomCalendar
import com.medina.intervaltraining.ui.theme.IntervalTrainingTheme
import java.util.Calendar

@Composable
fun StatsPanel(
    modifier: Modifier = Modifier,
    viewModel: StatsViewModel = hiltViewModel(),
){
    val currentDate = Calendar.getInstance()
    val trainedMonth by viewModel.getTrainedSecForEachDayInMonth(currentDate).observeAsState(emptyList())
    CustomCalendar(
        modifier = modifier,
        year = currentDate.get(Calendar.YEAR),
        monthIndex = currentDate.get(Calendar.MONTH),
        dayComponent = { m, d ->
            if(d!=null) {
                val trainedHours = trainedMonth.getOrElse(d) { 0 } / 3600f
                Box(modifier = m.padding(4.dp), contentAlignment = Alignment.Center) {
                    CalendarDay(
                        modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                        day = d,
                        trainedHours = trainedHours
                    )
                }
            }
        },
    )
}

@Composable
fun CalendarDay(
    modifier: Modifier = Modifier,
    day: Int,
    trainedHours: Float
){
    val textMeasurer = rememberTextMeasurer()
    val backgroundColor = MaterialTheme.colorScheme.surface
    val surfaceColor = MaterialTheme.colorScheme.surfaceDim
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val styleBase = MaterialTheme.typography.labelSmall.copy(color = onSurfaceColor)

    Canvas(modifier = modifier) {
        val dotRadius = size.minDimension / 2
        val style = styleBase.copy(fontSize = size.minDimension.sp / 6)
        drawRect(
            color = backgroundColor,
        )
        clipRect {
            when {
                trainedHours <= 1 -> {
                    val barWidth = size.width * 0.33f
                    val barHeight = (size.height * 0.9f) * trainedHours
                    drawRect(
                        color = primaryColor,
                        style = Fill,
                        size = Size(width = barWidth, height = barHeight),
                        topLeft = Offset(
                            x = (size.width * 0.7F) - barWidth / 2,
                            y = size.height - barHeight
                        )
                    )
                }

                trainedHours <= 2 -> {
                    val barWidth = size.width * 0.22f
                    val barHeight = (size.height * 0.9f)
                    val barHeightPartial = barHeight * (trainedHours - 1)
                    drawRect(
                        color = primaryColor,
                        style = Fill,
                        size = Size(width = barWidth, height = barHeight),
                        topLeft = Offset(
                            x = size.width * 0.5f,
                            y = size.height - barHeight
                        )
                    )
                    drawRect(
                        color = primaryColor,
                        style = Fill,
                        size = Size(width = barWidth, height = barHeightPartial),
                        topLeft = Offset(
                            x = size.width * 0.52f + barWidth,
                            y = size.height - barHeightPartial
                        )
                    )
                }

                else -> {
                    val trainedHoursInt = trainedHours.toInt()
                    val barWidth = size.width * (0.67f / trainedHoursInt)
                    val barHeightTotal = (size.height * 0.9f)
                    for (i in 0..trainedHoursInt) {
                        val barHeight = if (i != trainedHoursInt) {
                            barHeightTotal
                        } else {
                            barHeightTotal * (trainedHours - trainedHoursInt)
                        }
                        drawRect(
                            color = primaryColor,
                            style = Fill,
                            size = Size(width = barWidth, height = barHeight),
                            topLeft = Offset(
                                x = (size.width * 0.02f) * (i + 1) + (barWidth * i),
                                y = size.height - barHeight
                            )
                        )
                    }
                }
            }
            drawRect(
                color = secondaryColor,
                style = Fill,
                size = Size(width = size.width, height = (size.height * 0.05f)),
                topLeft = Offset(x = 0f, y = (size.height * 0.95f))
            )

            drawCircle(
                color = surfaceColor,
                radius = dotRadius,
                center = Offset(size.minDimension * 0.16f, size.minDimension * 0.16f),
                style = Fill
            )
            val dayText = day.toString()
            drawText(
                textMeasurer = textMeasurer,
                text = dayText,
                style = style,
                topLeft = Offset(
                    (size.minDimension * 0.26f) - (textMeasurer.measure(
                        dayText,
                        style
                    ).size.width / 2f), 0f
                )
            )
        }
    }
}

@Preview
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun CalendarDayPreview(@PreviewParameter(DayProvider::class) param: Pair<Int,Float>){
    IntervalTrainingTheme {
        CalendarDay(
            modifier = Modifier
                .width(50.dp)
                .height(50.dp),
            day = param.first,
            trainedHours = param.second
        )
    }
}
class DayProvider : PreviewParameterProvider<Pair<Int,Float>> {
    override val values = sequenceOf(Pair(1,0.8f), Pair(5,1.3f), Pair(12,3.5f))
}

@Preview
@Composable
fun StatsPanelPreview(){
    StatsPanel(viewModel = StatsViewModel(StatsDummyRepository()))
}
