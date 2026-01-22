package org.mozilla.reference.browser.macrobenchmark

import android.content.Intent
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Simple startup benchmark measuring the total time
 * from when the app is launched to after simple interactions within a webpage.
 */
@RunWith(AndroidJUnit4::class)
class SimpleStartupBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()
    @get:Rule
    val mockRule = MockWebServerRule()

    @Test
    fun startup() = benchmarkRule.measureRepeated(
        packageName = "org.mozilla.reference.browser",
        metrics = listOf(StartupTimingMetric()),
        iterations = 20,
        startupMode = StartupMode.COLD,
        setupBlock = {
            pressHome()
        },
        measureBlock = {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = mockRule.uri(HtmlAsset.LONG)
            intent.setPackage(packageName)

            startActivityAndWait(intent = intent)

            device.flingToEnd(scrollableId = "$packageName:id/engineView")

            device.flingToBeginning("$packageName:id/engineView")

            killProcess()
        }
    )

    private fun UiDevice.flingToEnd(scrollableId: String) {
        val scrollable = UiScrollable(UiSelector().resourceId(scrollableId))
        scrollable.waitForExists(1000L)
        scrollable.flingToEnd(1)
    }

    private fun UiDevice.flingToBeginning(scrollableId: String) {
        val scrollable = UiScrollable(UiSelector().resourceId(scrollableId))
        scrollable.waitForExists(1000L)
        scrollable.flingToBeginning(1)
    }
}
