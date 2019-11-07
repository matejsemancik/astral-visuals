package dev.matsem.astral.tools.video

import ddf.minim.AudioPlayer
import ddf.minim.Minim
import dev.matsem.astral.Config
import dev.matsem.astral.sketches.SketchLoader
import dev.matsem.astral.tools.audio.AudioProcessor
import dev.matsem.astral.tools.galaxy.Galaxy
import dev.matsem.astral.tools.kontrol.KontrolF1
import dev.matsem.astral.tools.kontrol.onTogglePad
import dev.matsem.astral.tools.kontrol.onTriggerPad
import dev.matsem.astral.tools.midi.MidiDevice
import dev.matsem.astral.tools.midi.MidiFileParser
import dev.matsem.astral.tools.midi.MidiPlayer
import dev.matsem.astral.tools.midi.MidiRecorder

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
        midiFileParser.loadFile(Config.VideoExport.MIDI_AUTOMATION_FILE)?.let { automation ->
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
    }

    private val isPlaying: Boolean
        get() = midiPlayer.isPlaying

    fun initWithGalaxy(musicFilePath: String) {
        plugInMidiDevice(galaxy)
        setMusicFile(musicFilePath)
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
            this.audioPlayer?.addListener(audioProcessor)
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

    private fun saveIntoFile(fileName: String = Config.VideoExport.MIDI_AUTOMATION_FILE) =
            midiFileParser.saveFile(midiRecorder.getMessages(excludedCCs = blacklistedCCs), fileName)
}