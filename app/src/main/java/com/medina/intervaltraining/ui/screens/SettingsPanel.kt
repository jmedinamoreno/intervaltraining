package com.medina.intervaltraining.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.medina.intervaltraining.R
import com.medina.intervaltraining.data.model.DarkThemeConfig
import com.medina.intervaltraining.data.repository.UserDataDummyRepository
import com.medina.intervaltraining.data.viewmodel.SettingsUiState
import com.medina.intervaltraining.data.viewmodel.SettingsViewModel
import com.medina.intervaltraining.ui.components.SettingsEntryEnum
import com.medina.intervaltraining.ui.components.SettingsEntryNum
import com.medina.intervaltraining.ui.components.SettingsEntryToggle
import com.medina.intervaltraining.ui.theme.IntervalTrainingTheme
import com.medina.intervaltraining.ui.theme.supportsDynamicTheming


@Composable
fun SettingsPanel(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
){
    val state by viewModel.settingsUiState.collectAsStateWithLifecycle()
    when(state){
        is SettingsUiState.Loading -> {

        }
        is SettingsUiState.Success -> {
            val settings = (state as? SettingsUiState.Success)?.settings ?: return
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Start
            ) {
                // Delay Setting
                SettingsEntryNum(
                    labelTitle = stringResource(R.string.settings_panel_training_start_delay_label),
                    labelDescription = stringResource(R.string.settings_panel_training_start_delay_value),
                    extraDescription = stringResource(R.string.settings_panel_training_start_delay_extra),
                    value = settings.trainingStartDelaySecs,
                ) {
                    viewModel.updateTrainingStartDelay(it.coerceIn(1,10))
                }

                //Countdown
                SettingsEntryNum(
                    labelTitle = stringResource(R.string.settings_panel_countdown_sounds_label),
                    labelDescription = stringResource(R.string.settings_panel_countdown_sounds_value),
                    extraDescription = stringResource(R.string.settings_panel_countdown_sounds_extra),
                    value = settings.countdownToChange,
                ) {
                    viewModel.updateCountdownToChange(it)
                }

                // Sound effects
                SettingsEntryToggle(
                    labelTitle = stringResource(R.string.settings_panel_start_trainig_sound_label),
                    labelDescription = stringResource(R.string.settings_panel_start_trainig_sound_desc),
                    checked = settings.soundsEnabledTrainingStart
                ) {
                    viewModel.updateSoundsEnabledTrainingStart(it)
                }
                SettingsEntryToggle(
                    labelTitle = stringResource(R.string.settings_panel_rest_sound_label),
                    labelDescription = stringResource(R.string.settings_panel_rest_sound_desc),
                    checked = settings.soundsEnabledRestStart
                ) {
                    viewModel.updateSoundsEnabledRestStart(it)
                }
                SettingsEntryToggle(
                    labelTitle = stringResource(R.string.settings_panel_end_training_sound_label),
                    labelDescription = stringResource(R.string.settings_panel_end_training_sound_desc),
                    checked = settings.soundsEnabledTrainingEnd
                ) {
                    viewModel.updateSoundsEnabledTrainingEnd(it)
                }

                //Dark mode
                SettingsEntryEnum(
                    labelTitle = stringResource(R.string.settings_panel_dark_mode_label),
                    labelDescription = stringResource(R.string.settings_panel_dark_mode_value),
                    extraDescription = stringResource(R.string.settings_panel_dark_mode_label_extra),
                    selectedValue = settings.darkThemeConfig.name,
                    values = DarkThemeConfig.entries.associate { Pair(it.name,
                        darkModeConfigTexts(it.name)
                    ) }
                ) {
                    viewModel.updateDarkThemeConfig(DarkThemeConfig.valueOf(it))
                }

                if(supportsDynamicTheming()) {
                    //useDynamicColor
                    SettingsEntryToggle(
                        labelTitle = stringResource(R.string.settings_panel_dynamic_color_label),
                        labelDescription = stringResource(R.string.settings_panel_dynamic_color_desc),
                        checked = settings.useDynamicColor
                    ) {
                        viewModel.updateDynamicColorPreference(it)
                    }
                }
            }
        }
    }
}

@Composable
fun darkModeConfigTexts(key:String):String = when(key){
    DarkThemeConfig.DARK.name -> stringResource(R.string.settings_panel_dark_mode_option_dark)
    DarkThemeConfig.LIGHT.name -> stringResource(R.string.settings_panel_dark_mode_option_light)
    else -> stringResource(R.string.settings_panel_dark_mode_option_system)
}

@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun SettingsComponentPreview() {
    IntervalTrainingTheme {
        SettingsPanel(
            modifier = Modifier.fillMaxWidth().height(300.dp),
            viewModel = SettingsViewModel(userDataRepository = UserDataDummyRepository()))
    }
}
