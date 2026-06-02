package com.example

import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.compose.ui.test.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `test launch MainActivity`() {
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.mainClock.advanceTimeBy(100)
        composeTestRule.waitForIdle()
        
        composeTestRule.onNodeWithText("Phone Number").performTextInput("1234567890")
        composeTestRule.onNodeWithText("Login").performClick()
        
        composeTestRule.waitForIdle()
        composeTestRule.mainClock.advanceTimeBy(1500)
        
        composeTestRule.onNodeWithText("6-Digit OTP").performTextInput("123456")
        composeTestRule.onNodeWithText("Verify & Proceed").performClick()
        
        composeTestRule.mainClock.autoAdvance = true
        composeTestRule.waitForIdle()
        
        // At this point we should print out the semantics tree
        composeTestRule.onRoot().printToLog("RoboTestLog")
        
        // Try to add an entry
        composeTestRule.onNodeWithContentDescription("Add Entry").performClick()
        
        composeTestRule.waitForIdle()
        composeTestRule.mainClock.advanceTimeBy(500)
    }
}
