# astral-visuals

Just some Processing sketches.
Follow [Soul Ex Machina crew](https://www.facebook.com/SoulExMachinaDnB) to see them live.


![](demo-gif.gif)

## TODO v2 How to build

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

The `build.gradle` buildscript will look for Processing dependencies at these two paths. Open it up, and you can notice that this project depends on some 3rd party libraries, which need to be installed at `libraries.dir` path. Open your Processing library manager (Sketch > Import Library > Add library) and install whatever libraries are specified in the `build.gradle` file.

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

## TODO v2 How to run

You can run the project with Gradle `run` task.

```
./gradlew playground:run
./gradlew visuals:run
```

Or you can set up some sort of Build & Run task in your IDE. (hint: In IntelliJ, open `Main.kt` and click the green run icon next to `main` function and select Run. You do this once, and IDE will set up the reusable run task for you in upper toolbar.)

## Remote control
TBD TouchOSC, Traktor Kontrol F1
