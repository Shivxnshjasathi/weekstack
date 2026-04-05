package com.zincstate.hepta

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class WeekstackFlowTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testFrictionlessTaskFlow() {
        // 1. Identify today's header and expand it
        val today = LocalDate.now().dayOfWeek.name
        val dayHeaderTag = "day_header_$today"
        
        composeTestRule.onNodeWithTag(dayHeaderTag).performClick()

        // 2. Click "Add a task..." input and type
        val inputTag = "add_task_input"
        val testTaskText = "Finish Play Store submission"
        
        composeTestRule.onNodeWithTag(inputTag).performTextInput(testTaskText)
        composeTestRule.onNodeWithTag(inputTag).performImeAction()

        // 3. Verify task is added
        composeTestRule.onNodeWithText(testTaskText).assertIsDisplayed()

        // 4. Toggle completion
        // Since we don't have the task ID easily, we find by text and verify checkbox near it
        // Or we use the text itself to find the node
        composeTestRule.onNodeWithText(testTaskText).performClick()

        // 5. Verify it's still there
        composeTestRule.onNodeWithText(testTaskText).assertExists()
    }

    @Test
    fun testIdentityAndThemeFlow() {
        // 1. Navigate to the About/Identity screen via the Nexus Node
        composeTestRule.onNodeWithText("H").performClick()
        
        // 2. Verify we are on the Identity screen
        composeTestRule.onNodeWithText("IDENTITY").assertIsDisplayed()
        
        // 3. Find a theme (e.g., NORD) and click it
        // We find by the display name uppercase as defined in the UI
        composeTestRule.onNodeWithText("NORD").performClick()
        
        // 4. Navigate back to Home
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        
        // 5. Verify we are back on the Home screen
        composeTestRule.onNodeWithText("HEPTA").assertIsDisplayed()
    }

    @Test
    fun testVaultToggleVisibility() {
        // 1. Navigate to Identity
        composeTestRule.onNodeWithText("H").performClick()
        
        // 2. Verify Vault section exists
        composeTestRule.onNodeWithText("VAULT").assertIsDisplayed()
        composeTestRule.onNodeWithText("BIOMETRIC LOCK").assertIsDisplayed()
    }
}
