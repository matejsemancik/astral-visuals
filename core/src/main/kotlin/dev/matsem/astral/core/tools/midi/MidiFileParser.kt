package dev.matsem.astral.core.tools.midi

import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import processing.core.PApplet
import java.io.File
import java.io.FileNotFoundException

class MidiFileParser(private val sketch: PApplet) {

    private val json = Json(JsonConfiguration.Stable.copy(prettyPrint = true))

    fun loadFile(filePath: String): List<MidiMessage>? = with(sketch) {
        try {
            File(dataPath(filePath)).readText().let { jsonString ->
                json.parse(MidiMessage.serializer().list, jsonString)
            }
        } catch (e: FileNotFoundException) {
            null
        }
    }

    fun saveFile(messages: List<MidiMessage>, filePath: String) = with(sketch) {
        val jsonString = json.stringify(MidiMessage.serializer().list, messages)
        val writer = createWriter(dataPath(filePath))
        writer.write(jsonString)
        writer.flush()
        writer.close()
        println("MIDI automation saved")
    }
}