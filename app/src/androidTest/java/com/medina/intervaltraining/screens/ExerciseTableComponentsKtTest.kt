package com.medina.intervaltraining.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.asLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.medina.data.model.ExerciseIcon
import com.medina.intervaltraining.ui.SemanticsKeyDrawableId
import com.medina.intervaltraining.ui.SemanticsKeyIconName
import com.medina.intervaltraining.ui.components.AnimatedIconRow
import com.medina.intervaltraining.ui.components.DialogIconButton
import com.medina.intervaltraining.ui.components.IconRow
import com.medina.intervaltraining.ui.iconToDrawableResource
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DialogIconButtonTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun dialogIconButton_enabled_clickable() {
        var clicked = false
        composeTestRule.setContent {
            DialogIconButton(
                text = "Add",
                icon = Icons.Filled.Add,
                iconDescription = "Add Icon",
                onClick = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("Add")
            .assertIsEnabled()
            .performClick()

        assert(clicked)
    }

    @Test
    fun dialogIconButton_disabled_notClickable() {
        var clicked = false
        composeTestRule.setContent {
            DialogIconButton(
                text = "Add",
                icon = Icons.Filled.Add,
                iconDescription = "Add Icon",
                enabled = false,
                onClick = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("Add")
            .assertIsNotEnabled()
            .performClick() // This click should have no effect

        assert(!clicked)
    }

    @Test
    fun dialogIconButton_contentDisplayed() {
        composeTestRule.setContent {
            DialogIconButton(
                text = "Add",
                icon = Icons.Filled.Add,
                iconDescription = "Add Icon"
            ) {}
        }

        composeTestRule.onNodeWithText("Add").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Add Icon").assertIsDisplayed()
    }
}
@RunWith(AndroidJUnit4::class)
class AnimatedIconRowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun animatedIconRow_isVisibleWhenVisibleTrue() {
        composeTestRule.setContent {
            AnimatedIconRow(
                icon = ExerciseIcon.NONE,
                onIconChange = {}
            )
        }
        composeTestRule.onNodeWithTag("IconRow").assertIsDisplayed()
    }

    @Test
    fun animatedIconRow_isNotVisibleWhenVisibleFalse() {
        composeTestRule.setContent {
            AnimatedIconRow(
                icon = ExerciseIcon.NONE,
                onIconChange = {},
                visible = false
            )
        }
        composeTestRule.onNodeWithTag("IconRow").assertIsNotDisplayed()
    }

    @Test
    fun animatedIconRow_animatesInWhenVisibilityChangesToTrue() {
        val visibilityStateFlow = MutableStateFlow(false)
        composeTestRule.setContent {
            val visibility:Boolean by visibilityStateFlow.asLiveData().observeAsState(initial = false)
            AnimatedIconRow(
                icon = ExerciseIcon.NONE,
                onIconChange = {},
                visible = visibility
            )
        }

        // Initially not visible
        composeTestRule.onNodeWithTag("IconRow").assertIsNotDisplayed()

        // Change visibility to true and verify it appears
        visibilityStateFlow.value = true
        composeTestRule.onNodeWithTag("IconRow").assertIsDisplayed()
    }

    @Test
    fun animatedIconRow_animatesOutWhenVisibilityChangesToFalse() {
        val visibilityStateFlow = MutableStateFlow(true)
        composeTestRule.setContent {
            val visibility:Boolean by visibilityStateFlow.asLiveData().observeAsState(initial = true)
            AnimatedIconRow(
                icon = ExerciseIcon.NONE,
                onIconChange = {},
                visible = visibility
            )
        }

        // Initially visible
        composeTestRule.onNodeWithTag("IconRow").assertIsDisplayed()

        // Change visibility to false and verify it disappears
        visibilityStateFlow.value = false
        composeTestRule.onNodeWithTag("IconRow").assertIsNotDisplayed()
    }
}
@RunWith(AndroidJUnit4::class)
class IconRowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun iconRow_displaysAllIcons() {
        composeTestRule.setContent {
            IconRow(icon = ExerciseIcon.NONE, onIconChange = {})
        }
        ExerciseIcon.entries.forEach { icon ->
            if (icon == ExerciseIcon.NONE) {
                composeTestRule.onNodeWithImageVector(Icons.Default.Clear).assertIsDisplayed()
            } else {
                composeTestRule.onNodeWithDrawableResource(iconToDrawableResource(icon)).assertIsDisplayed()
            }
        }
    }

    @Test
    fun iconRow_selectsCorrectIcon() {
        val selectedIconStateFlow = MutableStateFlow(ExerciseIcon.NONE)
        composeTestRule.setContent {
            val selectedIcon by selectedIconStateFlow.asLiveData().observeAsState(initial = ExerciseIcon.NONE)
            IconRow(
                icon = selectedIcon,
                onIconChange = { selectedIconStateFlow.value = it }
            )
        }

        // Initially, no icon should be selected (except potentially the "NONE" icon)
        ExerciseIcon.entries.forEach { icon ->
            if (icon == ExerciseIcon.NONE) {
                composeTestRule.onNodeWithImageVector(Icons.Default.Clear).assertIsSelected()
            } else {
                composeTestRule.onNodeWithDrawableResource(iconToDrawableResource(icon)).assertIsNotSelected()
            }
        }

        // Select a different icon and verify the selection changes
        val newIcon = ExerciseIcon.RUN
        composeTestRule.onNodeWithDrawableResource(iconToDrawableResource(newIcon)).performClick()
        assertTrue(selectedIconStateFlow.value == newIcon)
        ExerciseIcon.entries.forEach { icon ->
            when (icon) {
                ExerciseIcon.NONE -> {
                    composeTestRule.onNodeWithImageVector(Icons.Default.Clear).assertIsNotSelected()
                }
                newIcon -> {
                    composeTestRule.onNodeWithDrawableResource(iconToDrawableResource(newIcon)).assertIsSelected()
                }
                else -> {
                    composeTestRule.onNodeWithDrawableResource(iconToDrawableResource(icon)).assertIsNotSelected()
                }
            }
        }
    }
}

fun hasImageVector(imageVector: ImageVector): SemanticsMatcher =
    SemanticsMatcher.expectValue(SemanticsKeyIconName, imageVector.name)

fun SemanticsNodeInteractionsProvider.onNodeWithImageVector(imageVector: ImageVector,
    useUnmergedTree: Boolean = false
): SemanticsNodeInteraction = onNode(hasImageVector(imageVector), useUnmergedTree)

fun hasDrawableResource(drawableResId: Int): SemanticsMatcher =
    SemanticsMatcher.expectValue(SemanticsKeyDrawableId, drawableResId)

fun SemanticsNodeInteractionsProvider.onNodeWithDrawableResource(drawableResId: Int,
                                                            useUnmergedTree: Boolean = false
): SemanticsNodeInteraction = onNode(hasDrawableResource(drawableResId), useUnmergedTree)

fun SemanticsNodeInteractionsProvider.onAllNodeWithDrawableResource(drawableResId: Int,
                                                                 useUnmergedTree: Boolean = false
): SemanticsNodeInteractionCollection = onAllNodes(hasDrawableResource(drawableResId), useUnmergedTree)