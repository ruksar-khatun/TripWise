package com.tripwise.app

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File

/** Helpers for the scripted emulator tour. Screenshots land in the app's external files dir. */
@OptIn(ExperimentalTestApi::class)
class Tour(private val rule: AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>) {
    private var index = 0
    private val dir: File = InstrumentationRegistry.getInstrumentation().targetContext
        .getExternalFilesDir("tour")!!.apply { deleteRecursively(); mkdirs() }

    fun waitFor(text: String, timeoutMs: Long = 20_000, substring: Boolean = false) {
        rule.waitUntil(timeoutMs) { rule.onAllNodesWithText(text, substring = substring).fetchSemanticsNodes().isNotEmpty() }
    }

    /** Clicks the first node with [text]. Optional steps log and continue instead of failing the tour. */
    fun click(text: String, substring: Boolean = false, required: Boolean = true): Boolean {
        val ok = runCatching {
            waitFor(text, timeoutMs = if (required) 20_000 else 5_000, substring = substring)
            clickNode(rule.onAllNodesWithText(text, substring = substring).onFirst())
        }.onFailure { if (required) throw it else Log.w(TAG, "optional click '$text' skipped") }.isSuccess
        rule.waitForIdle()
        return ok
    }

    fun clickDescription(description: String): Boolean {
        val ok = runCatching {
            rule.waitUntil(5_000) { rule.onAllNodesWithContentDescription(description).fetchSemanticsNodes().isNotEmpty() }
            clickNode(rule.onAllNodesWithContentDescription(description).onFirst())
        }.onFailure { Log.w(TAG, "optional click on '$description' skipped") }.isSuccess
        rule.waitForIdle()
        return ok
    }

    /**
     * Invokes the node's click action directly (works even if the node is partly under a top bar),
     * falling back to a real touch for nodes without an OnClick action.
     */
    private fun clickNode(node: androidx.compose.ui.test.SemanticsNodeInteraction) {
        val hasAction = node.fetchSemanticsNode().config.contains(androidx.compose.ui.semantics.SemanticsActions.OnClick)
        if (hasAction) node.performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.OnClick) else node.performClick()
    }

    fun type(label: String, value: String) {
        waitFor(label)
        rule.onAllNodesWithText(label).onFirst().performTextInput(value)
    }

    fun scrollTo(text: String) {
        runCatching { rule.onAllNodes(hasScrollAction()).onFirst().performScrollToNode(hasText(text, substring = true)) }
            .onFailure { Log.w(TAG, "scrollTo($text) failed: ${it.message}") }
        rule.waitForIdle()
    }

    fun swipeUp() {
        runCatching { rule.onAllNodes(hasScrollAction()).onFirst().performTouchInput { swipeUp() } }
        rule.waitForIdle()
    }

    fun back() {
        InstrumentationRegistry.getInstrumentation().uiAutomation
            .performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK)
        rule.waitForIdle()
    }

    /** Waits for images/animations to settle, then captures the whole screen (status bar included). */
    fun shot(name: String, settleMs: Long = 1_500) {
        rule.waitForIdle()
        Thread.sleep(settleMs)
        val bitmap = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot() ?: return
        val file = File(dir, "%02d-%s.png".format(++index, name))
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        Log.i(TAG, "Saved ${file.absolutePath}")
    }

    fun has(text: String, substring: Boolean = false) =
        rule.onAllNodesWithText(text, substring = substring).fetchSemanticsNodes().isNotEmpty()

    private companion object { const val TAG = "AppTour" }
}

