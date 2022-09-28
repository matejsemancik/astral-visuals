# astral-visuals

Just some Processing sketches. Source code for visuals we use at [Soul Ex Machina](https://www.facebook.com/SoulExMachinaDnB).


![](demo-gif.gif)

The project is divided into multiple modules.

The `:core` module contains the core stuff like audio processing, tools, remote control handlers, extensions, etc.

The `:playground` module serves as, well... playground. Used to quickly create a new sketch and play around. I'm using the [Koin](https://insert-koin.io/) DI framework, so you can inject here whatever is defined in the `CoreModule`. Have a look around.

The `:visuals` module is meant to be used in live environment at the parties. There is an abstraction layer in form of `Mixer` and `Layer`s, which allows me to blend multiple scenes together. Also, have a look around, proceed at your own risk, ignore `legacy` package 😅 (I like to change things, API is generally unstable).

The `:raspberrypi` module contains standalone RPi application that can be distributed using the [Application Gradle plugin](https://docs.gradle.org/current/userguide/application_plugin.html).

## How to build

This project depends on local [Processing 4](https://processing.org) installation, so go ahead and install it if you haven't already. Then create a `local.properties` file in project's root directory and configure the core library and contributed libraries' paths:

```
processing.core.jars=/path/to/core/processing/libraries
processing.core.natives=/path/to/core/processing/libraries/<os-architecture>
processing.core.natives.rpi=/path/to/core/processing/libraries/<os-architecture>
processing.libs.jars=/path/to/core/processing/libraries
```

On macOS it might look like this:

```
processing.core.jars=/Applications/Processing.app/Contents/Java/core/library
processing.core.natives=/Applications/Processing.app/Contents/Java/core/library/macos-x86_64
processing.core.natives.rpi=/Applications/Processing.app/Contents/Java/core/library/linux-aarch64
processing.libs.jars=/Users/matsem/Documents/Processing/libraries
```
Note the difference between `processing.core.natives` and `processing.core.natives.rpi`.
The Raspberry Pi libs have to be configured if you wish to use the `:raspberrypi` module.

The Gradle buildscript will look for Processing dependencies at these two paths. Dependencies are defined in CommonDependencies gradle plugin. Open it up, and you can notice that this project depends on some 3rd party libraries, which need to be installed at `processing.libs.jars` path. Open your Processing library manager (Sketch > Import Library > Add library) and install whatever libraries are specified in the `build.gradle` file.

Current list of library dependencies is

```kotlin
val processingLibs = listOf(
    "minim", // audio input everything (input source, fft analysis, etc.)
    "themidibus", // MIDI control protocol implementation
    "VideoExport", // I use this to export video teasers synced with external audio file 
    "box2d_processing", // for physics (look for Gravity sketch in playground module)
    "video", // video playback
    "extruder", // 2d shape -> 3d shape extrusion
    "geomerative", // text -> shape, svg -> shape conversion
    "peasycam", // adds camera handling to the sketches, nice to have when prototyping
    "PostFX", // can apply post-processing shaders to video output
    "oscP5", // OSC control protocol implementation
    "blobDetection" // library to find "blobs" on image
)
```


### :raspberrypi module
The Raspberry Pi app can be installed using `./gradlew raspberrypi:installDist` task and zipped using `./gradlew raspberrypi:distZip` task.
See the [Application Plugin](https://docs.gradle.org/current/userguide/application_plugin.html) docs for more info.

## How to run

You can run the project with Gradle `run` task. Be sure to include the `--sketch-path` argument so sketches can properly resolve the data folder containing resources needed by some Sketches.

```
./gradlew playground:run --args='--sketch-path=/path/to/project/'
./gradlew visuals:run --args='--sketch-path=/path/to/project/'
```

There are also IntelliJ Run configurations in `.run` folder which you can use to run the app from IDE. Just be sure to edit their configuration to match your setup. 

Note: Due to the fragileness of Processing dependencies (namely JogAmp), the project currently works only with some JDK versions, specifically `11.0.11-zulu`. You can find `.sdkmanrc` file in project folder
that sets up the current SDK for you if you use [SDKMAN!](https://sdkman.io/). (`11.0.12` does not work yet, causing [this](https://github.com/processing/processing4/issues/249) issue).

## Remote control
Currently, the project supports 3 remote control options:

- If you own Traktor Kontrol F1, the `KontrolF1` class is for you - I use it for quick prototyping. It handles most of KontrolF1's hardware features, like pad buttons (with colors feature), encoder, knobs and faders.
- If you'd like to try the `:visuals` module, go ahead and get yourself the [TouchOSC](https://hexler.net/products/touchosc) app and load it with `Astral.touchosc` layout that can be found in the `touchosc` folder. This layout uses MIDI and OSC protocol and there is a `Galaxy` class that handles most of TouchOSC MIDI controls. For future, I plan on to get rid of `Galaxy` and migrate everyhing to OSC protocol, which leads us to the last option
- OSC - The most convenient way, though, is to use the [OSC](http://opensoundcontrol.org/introduction-osc) (Open Sound Control) with Delegated Properties

### Osc Delegated Properties
First, make your sketch/class implement the `OscHandler` interface, which makes you provide the `OscManager` class.

```kotlin
class OscHandlerExample : PApplet(), OscHandler {

    override val oscManager: OscManager by lazy {
        OscManager(
            sketch = this,
            inputPort = 7001, // Port that this computer is listening on
            outputIp = "192.168.1.11", // IP of phone running TouchOSC
            outputPort = 7001 // Port, the TouchOSC app is listening on
        )
    }
}
```

Then, you can create all sorts of properties tied to various OSC controls, like buttons, faders, labels, LED indicators, etc. Check out the `dev.matsem.astral.core.tools.osc.delegates` package for full list. Example:

```kotlin
private var fader1: Float by oscFaderDelegate("/1/fader1", defaultValue = 0.5f)
```

Most of the delegated properties support value assign, so, if for example you create the fader variable and at some point in time you assign the value into it, the corresponding control in TouchOSC app will reflect that change.

## Known bugs after Processing4 migration
- movie library does not work (issues with linking native libs)
