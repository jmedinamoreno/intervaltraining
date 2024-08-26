package com.medina.intervaltraining.data.repository

import android.content.Context
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.medina.intervaltraining.data.model.DarkThemeConfig
import com.medina.intervaltraining.data.model.ThemeBrand
import com.medina.intervaltraining.data.model.UserData
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

interface UserDataRepository {
    /**
     * Stream of [UserData]
     */
    val userData: Flow<UserData>

    /*
     * Updates whether training start sounds are enabled.
     */
    suspend fun updateSoundsEnabledTrainingStart(enabled: Boolean)

    /*
     * Updates whether rest start sounds are enabled.
     */
    suspend fun updateSoundsEnabledRestStart(enabled: Boolean)

    /*
     * Updates whether training end sounds are enabled.
     */
    suspend fun updateSoundsEnabledTrainingEnd(enabled: Boolean)

    /*
     * Updates the training start delay in seconds.
     */
    suspend fun updateTrainingStartDelaySecs(secs: Int)

    /**
     * Sets the desired theme brand.
     */
    suspend fun setThemeBrand(themeBrand: ThemeBrand)

    /**
     * Sets the desired dark theme config.
     */
    suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig)

    /**
     * Sets the preferred dynamic color config.
     */
    suspend fun setDynamicColorPreference(useDynamicColor: Boolean)

    /**
     * Sets whether the user has completed the onboarding process.
     */
    suspend fun setShouldHideOnboarding(shouldHideOnboarding: Boolean)
}

private const val TRAINING_START_DELAY_SECS_KEY = "training_start_delay_secs"
private const val SOUNDS_ENABLED_TRAINING_START_KEY = "sounds_enabled_training_start"
private const val SOUNDS_ENABLED_REST_START_KEY = "sounds_enabled_rest_start"
private const val SOUNDS_ENABLED_TRAINING_END_KEY = "sounds_enabled_training_end"
private const val THEME_BRAND_KEY = "theme_brand"
private const val DARK_THEME_CONFIG_KEY = "dark_theme_config"
private const val USE_DYNAMIC_COLOR_KEY = "use_dynamic_color"
private const val SHOULD_HIDE_ONBOARDING_KEY = "should_hide_onboarding"
private object PreferencesKeys {
    val TRAINING_START_DELAY_SECS = intPreferencesKey(TRAINING_START_DELAY_SECS_KEY)
    val SOUNDS_ENABLED_TRAINING_START = booleanPreferencesKey(SOUNDS_ENABLED_TRAINING_START_KEY)
    val SOUNDS_ENABLED_REST_START = booleanPreferencesKey(SOUNDS_ENABLED_REST_START_KEY)
    val SOUNDS_ENABLED_TRAINING_END = booleanPreferencesKey(SOUNDS_ENABLED_TRAINING_END_KEY)
    val THEME_BRAND = stringPreferencesKey(THEME_BRAND_KEY)
    val DARK_THEME_CONFIG = stringPreferencesKey(DARK_THEME_CONFIG_KEY)
    val USE_DYNAMIC_COLOR = booleanPreferencesKey(USE_DYNAMIC_COLOR_KEY)
    val SHOULD_HIDE_ONBOARDING = booleanPreferencesKey(SHOULD_HIDE_ONBOARDING_KEY)
}

/**
 * Class that handles saving and retrieving user preferences
 */
class UserDataDatastoreRepository @Inject constructor(
    private val userPreferences: DataStore<Preferences>
): UserDataRepository {

    override val userData: Flow<UserData> = userPreferences.data
        .catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            val trainingStartDelaySecs = preferences[PreferencesKeys.TRAINING_START_DELAY_SECS] ?: 5
            val soundsEnabledTrainingStart = preferences[PreferencesKeys.SOUNDS_ENABLED_TRAINING_START] ?: true
            val soundsEnabledRestStart = preferences[PreferencesKeys.SOUNDS_ENABLED_REST_START] ?: true
            val soundsEnabledTrainingEnd = preferences[PreferencesKeys.SOUNDS_ENABLED_TRAINING_END] ?: true
            val themeBrand = preferences[PreferencesKeys.THEME_BRAND]?.let { ThemeBrand.valueOf(it) } ?: ThemeBrand.DEFAULT
            val darkThemeConfig = preferences[PreferencesKeys.DARK_THEME_CONFIG]?.let { DarkThemeConfig.valueOf(it) } ?: DarkThemeConfig.FOLLOW_SYSTEM
            val useDynamicColor = preferences[PreferencesKeys.USE_DYNAMIC_COLOR] ?: false
            val shouldHideOnboarding = preferences[PreferencesKeys.SHOULD_HIDE_ONBOARDING] ?: false

            UserData(
                trainingStartDelaySecs = trainingStartDelaySecs,
                soundsEnabledTrainingStart = soundsEnabledTrainingStart,
                soundsEnabledRestStart = soundsEnabledRestStart,
                soundsEnabledTrainingEnd = soundsEnabledTrainingEnd,
                themeBrand = themeBrand,
                darkThemeConfig = darkThemeConfig,
                useDynamicColor = useDynamicColor,
                shouldHideOnboarding = shouldHideOnboarding
            )
        }

    override suspend fun setThemeBrand(themeBrand: ThemeBrand) {
        userPreferences.edit { preferences ->
            preferences[PreferencesKeys.THEME_BRAND] = themeBrand.name // Store the enum name as a String
        }
    }

    override suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig){
        userPreferences.edit { preferences ->
            preferences[PreferencesKeys.DARK_THEME_CONFIG] = darkThemeConfig.name // Store the enum name as a String
        }
    }

    override suspend fun setDynamicColorPreference(useDynamicColor: Boolean) {
        userPreferences.edit { preferences ->
            preferences[PreferencesKeys.USE_DYNAMIC_COLOR] = useDynamicColor
        }
    }

    override suspend fun setShouldHideOnboarding(shouldHideOnboarding: Boolean) {
        userPreferences.edit { preferences ->
            preferences[PreferencesKeys.SHOULD_HIDE_ONBOARDING] = shouldHideOnboarding
        }
    }

    override suspend fun updateSoundsEnabledTrainingStart(enabled: Boolean) {
        userPreferences.edit { preferences ->
            preferences[PreferencesKeys.SOUNDS_ENABLED_TRAINING_START] = enabled
        }
    }

    override suspend fun updateSoundsEnabledRestStart(enabled: Boolean) {
        userPreferences.edit { preferences ->
            preferences[PreferencesKeys.SOUNDS_ENABLED_REST_START] = enabled
        }
    }

    override suspend fun updateSoundsEnabledTrainingEnd(enabled: Boolean) {
        userPreferences.edit { preferences ->
            preferences[PreferencesKeys.SOUNDS_ENABLED_TRAINING_END] = enabled
        }
    }

    override suspend fun updateTrainingStartDelaySecs(secs: Int) {
        userPreferences.edit { preferences ->
            preferences[PreferencesKeys.TRAINING_START_DELAY_SECS] = secs
        }
    }

}

class UserDataDummyRepository() : UserDataRepository {
    override val userData: Flow<UserData>
        get() = flowOf(UserData(
            soundsEnabledTrainingEnd = true,
            soundsEnabledRestStart = true,
            soundsEnabledTrainingStart = true,
            trainingStartDelaySecs = 5,
            darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
            themeBrand = ThemeBrand.DEFAULT,
            useDynamicColor = false,
            shouldHideOnboarding = false
        ))

    override suspend fun updateSoundsEnabledTrainingStart(enabled: Boolean) {
    }

    override suspend fun updateSoundsEnabledRestStart(enabled: Boolean) {
    }

    override suspend fun updateSoundsEnabledTrainingEnd(enabled: Boolean) {
    }

    override suspend fun updateTrainingStartDelaySecs(secs: Int) {
    }

    override suspend fun setThemeBrand(themeBrand: ThemeBrand) {
    }

    override suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
    }

    override suspend fun setDynamicColorPreference(useDynamicColor: Boolean) {
    }

    override suspend fun setShouldHideOnboarding(shouldHideOnboarding: Boolean) {
    }

}