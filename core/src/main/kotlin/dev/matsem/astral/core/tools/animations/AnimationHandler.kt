package dev.matsem.astral.core.tools.animations

import processing.core.PConstants

/**
 * Provides access to various time-based animation functions, based on elapsed time since program run.
 * The purpose of this interface is to provide animation functionality compatible between live and video export mode.
 */
interface AnimationHandler {

    /**
     * Return number of millis since program was started.
     * In live environment, you can provide real [PApplet.millis()] time, while in video export mode, you can provide
     * [VideoExport.currentTime * 1000f] and the animations functions will behave same in both environments.
     */
    fun provideMillis(): Int
}

/**
 * Generates saw signal with given frequency in range from 0f to 1f
 */
fun AnimationHandler.saw(fHz: Float, offset: Int = 0): Float =
    ((provideMillis() - offset) % (1000f * 1 / fHz)) / (1000f * 1 / fHz)

/**
 * Generates sweep from 0f..TWO_PI in period given by [periodSeconds].
 * You could use the returned value to create a periodic motion, for instance by feeding
 * the returned value into the sin() function, and using the result to drive the rotation of an object.
 */
fun AnimationHandler.radianSeconds(periodSeconds: Float) = provideMillis() / 1000f * PConstants.TWO_PI / periodSeconds

/**
 * Generates sweep from 0f..TWO_PI at frequency given by [hz].
 * You could use the returned value to create a periodic motion, for instance by feeding
 * the returned value into the sin() function, and using the result to drive the rotation of an object.
 */
fun AnimationHandler.radianHz(hz: Float) = provideMillis() / 1000f * PConstants.TWO_PI / (1f / hz)