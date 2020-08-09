# astral-visuals

Just some Processing sketches.
Follow [Soul Ex Machina crew](https://www.facebook.com/SoulExMachinaDnB) to see them live.


![](demo-gif.gif)

The project is divided into 3 modules, `:core` module cointains the core stuff (duh) like audio processing, tools, remote control handlers, extensions, etc. Then there are two application modules - the `:playground` and `:visuals` module.

The `:playground` module serves as, well... playground. You can quickly create a new sketch and play around. I'm using the [Koin](https://insert-koin.io/) DI framework, so you can inject here whatever is defined in the `CoreModule`. Have a look around.

The `:visuals` module is meant to be used in live environment at the parties. There is an abstraction layer in form of `BaseSketch` which allows me to switch between multiple sketches on demand. Also, have a look around.

## How to build

This project depends on local [Processing](https://processing.org) installation, so go ahead and install it if you haven't already. Then create a `local.properties` file in project's root directory and configure the core library and contributed libraries' paths:

```
processingLibsDir=/path/to/your/processing/libraries/dir
processingCoreDir=/path/to/core/processing/libraries
```

On macOS it might look like this:

```
processingLibsDir=/Users/username/Documents/Processing/libraries
processingCoreDir=/Applications/Processing.app/Contents/Java/core/library
```

The `build.gradle` buildscript will look for Processing dependencies at these two paths. Open it up, and you can notice that this project depends on some 3rd party libraries, which need to be installed at `processingLibsDir` path. Open your Processing library manager (Sketch > Import Library > Add library) and install whatever libraries are specified in the `build.gradle` file.

Current list of library dependencies is

```
Minim // audio processing
The MidiBus // for remote control
Video Export // I use this to export video teasers synced with external audio file
Box2D for Processing // for physics (look for BoxesSketch)
Video // video playback
extruder // 2d shape -> 3d shape extrusion
geomerative // for generating shapes from text
PostFX for Processing // can apply post-processing shaders
```

If you've set up everything correctly, you should be able to build the project using Gradle `build` task.

```
./gradlew build
```

## How to run

You can run the project with Gradle `run` task. Be sure to include the `--sketch-path` argument so sketches can properly resolve the data folder with resources.

```
./gradlew playground:run --args='--sketch-path=/path/to/project/'
./gradlew visuals:run --args='--sketch-path=/path/to/project/'
```

## Remote control
Currently, the project supports 3 remote control options:

- If you own Traktor Kontrol F1, the `KontrolF1` class is for you - I use it for quick prototyping. It handles most of KontrolF1's hardware features, like pad buttons (with colors feature), encoder, knobs and faders.
- If you'd like to try the `:visuals` module, go ahead and get yourself the [TouchOSC](`https://hexler.net/products/touchosc`) app and load it with `Astral.touchosc` layout that can be found in the `touchosc` folder. This layout uses MIDI protocol and there is a `Galaxy` class that handles most of TouchOSC MIDI controls.
- The most convinient way, though, is to use the [OSC](http://opensoundcontrol.org/introduction-osc) (Open Sound Control) with Delegated Properties

### Osc Delegated Properties
First, make your sketch implement the `OscHandler` interface, which makes you provide the `OscManager` class.

```kotlin
class OscHandlerExample : PApplet(), OscHandler {

    override val oscManager: OscManager by lazy {
        OscManager(
            sketch = this,
            inputPort = 7001, // Port that this computer is listening on
            outputIp = "192.168.1.11", // IP of phone running TOuchOSC
            outputPort = 7001 // Port, the TouchOSC app is listening on
        )
    }
}
```

Then, you can create all sorts of properties tied to various OSC controls, like buttons, faders, labels, LED indicators, etc. Check out the `dev.matsem.astral.core.tools.osc.delegates` package for full list.

```kotlin
    private var fader1: Float by oscFader("/1/fader1", defaultValue = 0.5f)
    private var knob1: Float by oscKnob("/1/rotary1")
    private var toggle1: Boolean by oscToggleButton("/1/toggle1", defaultValue = false)
    private val push1: Boolean by oscPushButton("/1/push1") { 
        println("Trigger!") // Called when button pushed
    }
    private var xy1: PVector by oscXyPad("/1/xy1", defaultValue = PVector(0.5f, 0.5f))
    private var led1: Float by oscLedIndicator("/1/led1")
    private var label1: String by oscLabelIndicator("/1/label1")
    private val encoder1: Float by oscEncoder(
        address = "/1/encoder1",
        defaultValue = 100f,
        increment = 1f,
        cw = { println("-> $it") },
        ccw = { println("<- $it") }
    )
```

Most of the delegated properties support writing, so, if for example you create the fader variable and at some point in time you assing the value into it, the corresponding control in TouchOSC app will reflect that change.
