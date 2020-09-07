package dev.matsem.astral.visuals.tools.video

import ddf.minim.AudioListener
import ddf.minim.AudioPlayer
import ddf.minim.Minim
import dev.matsem.astral.core.VideoExportConfig
import dev.matsem.astral.visuals.legacy.SketchLoader
import dev.matsem.astral.core.tools.audio.AudioProcessor
import dev.matsem.astral.core.tools.galaxy.Galaxy
import dev.matsem.astral.core.tools.kontrol.KontrolF1
import dev.matsem.astral.core.tools.kontrol.onTogglePad
import dev.matsem.astral.core.tools.kontrol.onTriggerPad
import dev.matsem.astral.core.tools.midi.MidiDevice
import dev.matsem.astral.core.tools.midi.MidiFileParser
import dev.matsem.astral.core.tools.midi.MidiPlayer
import dev.matsem.astral.core.tools.midi.MidiRecorder

class VideoPreparationTool(
    private val sketch: SketchLoader,
    private val midiRecorder: MidiRecorder,
    private val midiPlayer: MidiPlayer,
    private val midiFileParser: MidiFileParser,
    private val minim: Minim,
    private val audioProcessor: AudioProcessor,
    private val kontrol: KontrolF1,
    private val galaxy: Galaxy
) {
    private var audioPlayer: AudioPlayer? = null
    private var blacklistedCCs: List<Int> = listOf()

    init {
        sketch.registerMethod("draw", this)
        midiFileParser.loadFile(VideoExportConfig.MIDI_AUTOMATION_FILE)?.let { automation ->
            midiRecorder.preLoad(automation)
        }

        kontrol.onTogglePad(0, 0, midiHue = 0, shift = true) {
            if (it) {
                startRecording()
            } else {
                stopRecording()
            }
        }

        // Play button
        kontrol.onTriggerPad(0, 1, midiHue = 40, shift = true) {
            if (isPlaying.not()) {
                startReplay()
            } else {
                stopReplay()
            }
        }

        // Save automation to file button
        kontrol.onTriggerPad(0, 2, midiHue = 80, shift = true) {
            saveIntoFile()
        }

        // Capture initial MIDI state from already recorded messages
        kontrol.onTriggerPad(0, 3, midiHue = 80, shift = true) {
            saveInitialStateIntoFile()
        }
    }

    private val isPlaying: Boolean
        get() = midiPlayer.isPlaying

    fun initWithGalaxy(musicFilePath: String) {
        plugInMidiDevice(galaxy)
        setMusicFile(musicFilePath)
    }

    fun initWithKontrol(musicFilePath: String) {
        plugInMidiDevice(kontrol)
        setMusicFile(musicFilePath)

        setBlacklistedMessages(
            listOf(
                kontrol.pad(0, 0, true),
                kontrol.pad(0, 1, true),
                kontrol.pad(0, 2, true),
                kontrol.pad(0, 3, true)
            )
                .map { it.cc }
        )
    }

    fun draw() {
        midiPlayer.update()
    }

    private fun plugInMidiDevice(device: MidiDevice) = apply {
        midiRecorder.plugIn(device)
        midiPlayer.plugIn(device)
    }

    private fun setMusicFile(path: String) = apply {
        try {
            this.audioPlayer = minim.loadFile(path)
            this.audioPlayer?.addListener(object : AudioListener {
                override fun samples(p0: FloatArray?) {
                    audioProcessor.fft.forward(audioPlayer?.mix)
                    audioProcessor.beatDetect.detect(p0)
                }

                override fun samples(p0: FloatArray?, p1: FloatArray?) {
                    audioProcessor.fft.forward(audioPlayer?.mix)
                    audioProcessor.beatDetect.detect(p0)
                }
            })
        } catch (e: Throwable) {
            // ¯\_(ツ)_/¯
        }
    }

    private fun setBlacklistedMessages(ccs: List<Int>) = apply {
        this.blacklistedCCs = ccs
    }

    private fun startRecording() {
        midiRecorder.startRecording()
        midiPlayer.enqueue(midiRecorder.getMessages(excludedCCs = blacklistedCCs))
        midiPlayer.play()
        audioPlayer?.play()
    }

    private fun stopRecording() {
        midiRecorder.stopRecording()
        midiPlayer.stop()
        audioPlayer?.pause()
        audioPlayer?.rewind()
    }

    private fun startReplay() {
        midiPlayer.enqueue(midiRecorder.getMessages(excludedCCs = blacklistedCCs))
        midiPlayer.play()
        audioPlayer?.play()
    }

    private fun stopReplay() {
        midiPlayer.stop()
        audioPlayer?.pause()
        audioPlayer?.rewind()
    }

    private fun saveIntoFile(fileName: String = VideoExportConfig.MIDI_AUTOMATION_FILE) =
        midiFileParser.saveFile(midiRecorder.getMessages(excludedCCs = blacklistedCCs), fileName)

    private fun saveInitialStateIntoFile(fileName: String = VideoExportConfig.MIDI_AUTOMATION_FILE) {
        val messages = midiRecorder.getMessages(excludedCCs = blacklistedCCs)
            .sortedBy { it.millis }
            .toMutableList()
            .groupBy { it.control }
            .entries
            .map { it.value.takeLast(1) }
            .flatMap { it }
            .map { it.copy(millis = 0, frame = 0) }

        midiFileParser.saveFile(messages, fileName)
    }
}