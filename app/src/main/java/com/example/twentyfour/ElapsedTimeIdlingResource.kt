package com.example.twentyfour

import androidx.test.espresso.IdlingResource
import java.util.concurrent.atomic.AtomicInteger

class ElapsedTimeIdlingResource(private val waitingTime: Long) : IdlingResource {
    private val startTime: Long = System.currentTimeMillis()
    private val resourceCallback = AtomicInteger(0)

    override fun getName(): String {
        return "ElapsedTimeIdlingResource"
    }

    override fun isIdleNow(): Boolean {
        val elapsed = System.currentTimeMillis() - startTime
        val idle = elapsed >= waitingTime
        if (idle && resourceCallback.get() != 0) {
            resourceCallback.decrementAndGet()
        }
        return idle
    }

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback) {
        resourceCallback.incrementAndGet()
    }
}