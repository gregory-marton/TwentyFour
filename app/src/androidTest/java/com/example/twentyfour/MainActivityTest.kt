package com.example.twentyfour

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    private lateinit var idlingResource: IdlingResource

    @Before
    fun registerIdlingResource() {
        idlingResource = ElapsedTimeIdlingResource(1000)
        IdlingRegistry.getInstance().register(idlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(idlingResource)
    }

    @Test
    fun testCancellationAndButtonRestoration() = runTest {
        // Launch the activity
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        // Enter input
        onView(withId(R.id.digitsInput)).perform(typeText("1234"), closeSoftKeyboard())

        // Click "Solve"
        onView(withId(R.id.solveButton)).perform(click())

        // Verify "Calculating..."
        onView(withId(R.id.solutionsText)).check(matches(withText("Calculating...")))

        // Click "Calculating..."
        onView(withId(R.id.solutionsText)).perform(click())

        // Click "Yes" in the dialog
        onView(withText("Yes")).inRoot(isDialog()).perform(click())

        // Verify that the calculation is canceled
        onView(withId(R.id.solutionsText)).check(matches(withText("")))

        // Verify button enabled
        onView(withId(R.id.solveButton)).check(matches(isEnabled()))

        // Close the activity
        scenario.close()
    }
}