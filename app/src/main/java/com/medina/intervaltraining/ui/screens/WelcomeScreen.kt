package com.medina.intervaltraining.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.medina.data.model.Training
import com.medina.data.repository.StatsDummyRepository
import com.medina.data.repository.TrainingDummyRepository
import com.medina.data.repository.UserInfoDummyRepository
import com.medina.intervaltraining.R
import com.medina.intervaltraining.ui.floatToHours
import com.medina.intervaltraining.ui.theme.IntervalTrainingTheme
import com.medina.intervaltraining.viewmodel.SettingsViewModel
import com.medina.intervaltraining.viewmodel.StatsViewModel
import com.medina.intervaltraining.viewmodel.TrainingViewModel

enum class IntervalTrainingSections { TRAININGS, STATS, SETTINGS }
@Composable
fun IntervalTrainingScreen(
    welcomePanel: @Composable ColumnScope.(modifier: Modifier) -> Unit = { modifier ->
        TrainedHoursComponent(modifier)
    },
    settingsPanel: @Composable ColumnScope.(modifier: Modifier) -> Unit = { modifier ->
        SettingsPanel(modifier)
    },
    trainingListPanel: @Composable ColumnScope.(
        modifier: Modifier,
        onPlay: (training: Training, immediate: Boolean) -> Unit
    ) -> Unit = { modifier, onPlayCall ->
        TrainingListPanel(modifier, onPlay = onPlayCall)
    },
    statsPanel: @Composable ColumnScope.(modifier: Modifier) -> Unit = { modifier ->
        StatsPanel(modifier)
    },
    onNewTraining: () -> Unit = {},
    onPlay: (training: Training, immediate:Boolean) -> Unit = { _, _->},
){
    var navSelectedItem by rememberSaveable { mutableStateOf(IntervalTrainingSections.TRAININGS) }
    Scaffold(
        floatingActionButton = {
            if(navSelectedItem == IntervalTrainingSections.TRAININGS) {
                ExtendedFloatingActionButton(
                    onClick = onNewTraining,
                    icon = {
                        Icon(
                            Icons.Default.Add,
                            stringResource(id = R.string.new_training_button_icon)
                        )
                    },
                    text = { Text(text = stringResource(id = R.string.new_training_button_label)) },
                )
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = navSelectedItem == IntervalTrainingSections.TRAININGS,
                    onClick = { navSelectedItem = IntervalTrainingSections.TRAININGS },
                    icon = { Icon(Icons.Default.AccountBox, null) },
                    label = { Text(stringResource(id = R.string.bottom_bar_trainings_label)) }
                )
                NavigationBarItem(
                    selected =  navSelectedItem ==  IntervalTrainingSections.STATS,
                    onClick = { navSelectedItem = IntervalTrainingSections.STATS },
                    icon = { Icon(Icons.Default.DateRange, null) },
                    label = { Text(stringResource(id = R.string.bottom_bar_stats_label)) }
                )
                NavigationBarItem(
                    selected =  navSelectedItem == IntervalTrainingSections.SETTINGS,
                    onClick = { navSelectedItem = IntervalTrainingSections.SETTINGS },
                    icon = { Icon(Icons.Default.Settings, null) },
                    label = { Text(stringResource(id = R.string.bottom_bar_settings_label)) }
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            welcomePanel(
                Modifier
                    .fillMaxWidth()
                    .padding(all = 16.dp)
            )
            when(navSelectedItem) {
                IntervalTrainingSections.TRAININGS ->
                    trainingListPanel(
                        Modifier
                            .fillMaxWidth()
                            .padding(all = 16.dp)
                            .weight(1f),
                        onPlay
                    )
                IntervalTrainingSections.STATS ->
                    statsPanel(
                        Modifier
                            .fillMaxWidth()
                            .padding(all = 16.dp))
                IntervalTrainingSections.SETTINGS ->
                    settingsPanel(
                        Modifier
                            .fillMaxWidth()
                            .padding(all = 16.dp)
                            .weight(1f),
                    )
            }
        }
    }
}


@Composable
fun TrainedHoursComponent(
    modifier: Modifier,
    statsViewModel: StatsViewModel = hiltViewModel()
){
    Column(modifier = modifier) {
        val hours:Float by statsViewModel.getTrainedThisWeek().observeAsState(initial = 0f)
        Text(stringResource(id = R.string.welcome_trained_time_first_line),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.CenterHorizontally))
        Text(stringResource(id = R.string.welcome_trained_time_second_line, floatToHours(float = hours)),
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.CenterHorizontally))
        Text(stringResource(id = R.string.welcome_trained_time_third_line),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}

@Composable
fun TrainingListPanel(
    modifier: Modifier,
    trainingViewModel: TrainingViewModel = hiltViewModel(),
    statsViewModel: StatsViewModel = hiltViewModel(),
    onPlay: (training: Training, immediate:Boolean) -> Unit = { _, _->},){
    val items: List<Training> by trainingViewModel.trainingList.observeAsState(listOf())
    LazyColumn(modifier = modifier) {
        items(items) { training ->
            val timeMin: Int by statsViewModel.getTimeForTrainingLiveData(training.id).observeAsState(0)
            TrainingItemComponent(training, timeMin, Modifier.padding(2.dp),
                {onPlay(training,false)},{onPlay(training,true)})
        }
    }
}

@Composable
fun TrainingItemComponent(training: Training, timeMin: Int, modifier: Modifier, onClick:()->Unit, onPlay:()->Unit){
    Surface(modifier = modifier, shape = MaterialTheme.shapes.medium, shadowElevation = 1.dp) {
        Row (modifier = Modifier.fillMaxWidth()){
            Text(stringResource(id = R.string.welcome_training_min,timeMin),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .padding(start = 8.dp, end = 4.dp)
                    .align(Alignment.CenterVertically),
            )
            Text(training.name,  modifier = Modifier
                .weight(1f)
                .padding(end = 4.dp)
                .align(Alignment.CenterVertically)
                .clickable { onClick() },)
            IconButton(onClick = onPlay, modifier = Modifier.align(Alignment.CenterVertically) ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = stringResource(id = R.string.ic_description_play_icon))
            }
        }
    }
}


// PREVIEWS
@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun TrainingListPreview() {
    IntervalTrainingTheme {
        TrainingListPanel(
            modifier = Modifier.fillMaxWidth(),
            trainingViewModel = TrainingViewModel(
                trainingRepository = TrainingDummyRepository(),
            ),
            statsViewModel = StatsViewModel(
                statsRepository = StatsDummyRepository()
            )
            ,onPlay = { _, _->}
        )
    }
}

// PREVIEWS
@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun InternalTrainingScreenPreview() {
    IntervalTrainingTheme {
        IntervalTrainingScreen(
            welcomePanel = { modifier ->
                TrainedHoursComponent(
                    modifier = modifier,
                    statsViewModel = StatsViewModel(
                        statsRepository = StatsDummyRepository()
                    )
                )
            },
            settingsPanel = { modifier ->
                SettingsPanel(
                    modifier = modifier,
                    viewModel = SettingsViewModel(
                        userInfoRepository = UserInfoDummyRepository()
                    )
                )
            },
            trainingListPanel = { modifier, onPlayCall ->
                TrainingListPanel(
                    modifier = modifier,
                    trainingViewModel = TrainingViewModel(
                        trainingRepository = TrainingDummyRepository(),
                    ),
                    statsViewModel = StatsViewModel(
                        statsRepository = StatsDummyRepository()
                    ), onPlay = onPlayCall
                )
            },
            statsPanel = { modifier ->
                StatsPanel(modifier)
            },
        )
    }
}
