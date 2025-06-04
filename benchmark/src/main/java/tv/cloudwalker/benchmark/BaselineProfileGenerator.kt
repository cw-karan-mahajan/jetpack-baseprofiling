package tv.cloudwalker.benchmark

import android.annotation.SuppressLint
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {

    @SuppressLint("NewApi")
    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() = rule.collect(
        packageName = "tv.cloudwalker.cloudwalkercompose",
        maxIterations = 15,
        stableIterations = 3
    ) {
        pressHome()
        startActivityAndWait()

        // Wait for initial app load and network requests
        device.waitForIdle(5000)

        // Critical User Journey 1: Hero Banner Navigation
        // This is the most important interaction in TV launchers
        repeat(6) {
            device.pressDPadRight()
            device.waitForIdle(300)
        }
        repeat(6) {
            device.pressDPadLeft()
            device.waitForIdle(300)
        }

        // Critical User Journey 2: Vertical Row Scrolling
        // Navigate through content rows (ZEE5, Live News Channels, etc.)
        repeat(8) {
            device.pressDPadDown()
            device.waitForIdle(800) // Allow image loading
        }
        repeat(8) {
            device.pressDPadUp()
            device.waitForIdle(800)
        }

        // Critical User Journey 3: Horizontal Content Navigation
        // Navigate within content rows (ZEE5 shows, news channels)
        repeat(4) {
            device.pressDPadDown()
            device.waitForIdle(400)

            // Navigate horizontally within this row
            repeat(10) {
                device.pressDPadRight()
                device.waitForIdle(200)
            }
            repeat(10) {
                device.pressDPadLeft()
                device.waitForIdle(200)
            }
        }

        // Critical User Journey 4: Focus States & Tile Interactions
        // Test focus animations and tile click handlers
        repeat(3) {
            device.pressDPadCenter() // Simulate tile click
            device.waitForIdle(1500) // Allow click handler processing
            device.pressBack()
            device.waitForIdle(500)
        }

        // Critical User Journey 5: Deep Content Navigation
        // Scroll to bottom content (news channels in square layout)
        repeat(15) {
            device.pressDPadDown()
            device.waitForIdle(400)
        }

        // Navigate through news channels
        repeat(8) {
            device.pressDPadRight()
            device.waitForIdle(300)
        }

        // Critical User Journey 6: Additional App Startup Simulation
        // This helps capture cold start optimizations
        pressHome()
        device.waitForIdle(1000)
        startActivityAndWait()
        device.waitForIdle(2000)

        // Return to top for memory cleanup patterns
        repeat(15) {
            device.pressDPadUp()
            device.waitForIdle(300)
        }
    }
}