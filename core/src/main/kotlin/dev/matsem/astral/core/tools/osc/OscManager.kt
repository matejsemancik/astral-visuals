package dev.matsem.astral.core.tools.osc

import netP5.NetAddress
import oscP5.OscEventListener
import oscP5.OscMessage
import oscP5.OscP5
import oscP5.OscStatus
import processing.core.PApplet

/**
 * This class handles connection to OSC device (TouchOSC app).
 *
 * @param inputPort port that sketch will be listening on
 * @param outputIp IP address of remote OSC device
 * @param outputPort port that the remote OSC device is listening on
 */
class OscManager(
    sketch: PApplet,
    inputPort: Int,
    outputIp: String,
    outputPort: Int
) {
    companion object {
        const val INPUT_PORT = 7001
        const val OUTPUT_PORT = 7001
        const val OUTPUT_IP = "192.168.1.11"
    }
    private val outputDevice = NetAddress(outputIp, outputPort)
    private val oscP5: OscP5 by lazy { OscP5(sketch, inputPort) }

    fun addMessageListener(onMessage: (OscMessage) -> Unit) {
        oscP5.addListener(object : OscEventListener {
            override fun oscStatus(status: OscStatus) = Unit
            override fun oscEvent(message: OscMessage) = onMessage(message)
        })
    }

    fun sendMessage(oscMessage: OscMessage) = oscP5.send(oscMessage, outputDevice)
}