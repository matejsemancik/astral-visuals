package dev.matsem.astral.core.tools.midi

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import processing.core.PApplet
import java.io.File
import java.io.FileNotFoundException

class MidiFileParser(private val sketch: PApplet) {

    private val json = Json {
        this.prettyPrint = true
    }

    fun loadFile(filePath: String): List<MidiMessage>? = with(sketch) {
        try {
            File(dataPath(filePath)).readText().let { jsonString ->
                json.decodeFromString(ListSerializer(MidiMessage.serializer()), jsonString)
            }
        } catch (e: FileNotFoundException) {
            null
        }
    }

    fun saveFile(messages: List<MidiMessage>, filePath: String) = with(sketch) {
        val jsonString = json.encodeToString(ListSerializer(MidiMessage.serializer()), messages)
        val writer = createWriter(dataPath(filePath))
        writer.write(jsonString)
        writer.flush()
        writer.close()
        println("MIDI automation saved")
    }
}