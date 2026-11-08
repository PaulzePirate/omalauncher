package dev.omalauncher.ui

import android.app.Application
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Uses the real home focus modifiers: clearing a field must not give it focus back on the next frame. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class HomeInputFocusTest {
    @get:Rule val compose = createComposeRule()

    @Test fun releasingAHomeFieldDoesNotFocusItAgain() {
        lateinit var release: () -> Unit
        compose.setContent {
            val home = remember { FocusRequester() }
            val focus = LocalFocusManager.current
            val keyboard = LocalSoftwareKeyboardController.current
            release = { focus.clearFocus(); keyboard?.hide() }
            Box(Modifier.fillMaxSize().keysRoot()) {
                Column(Modifier.focusRequester(home).focusGroup()) {
                    Box(Modifier.passesKeys(home).keyActivated({}, null)) {
                        BasicTextField("query", {}, Modifier.testTag("input").height(54.dp))
                    }
                }
            }
        }
        compose.onNodeWithTag("input").performClick().assertIsFocused()
        compose.runOnIdle { release() }
        compose.waitForIdle()
        compose.onNodeWithTag("input").assertIsNotFocused()
    }
}
