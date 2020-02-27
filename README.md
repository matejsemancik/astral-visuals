# astral-visuals

Just some Processing sketches.
Follow [Soul Ex Machina crew](https://www.facebook.com/SoulExMachinaDnB) to see them live.


![](demo-gif.gif)

## How to build

This project depends on local [Processing](https://processing.org) installation, so go ahead and install it if you haven't already. Then create a `local.properties` file in project's root directory and configure the core library and contributed libraries' paths:

```
libraries.dir=/path/to/your/processing/libraries/dir
processing_core.dir=/path/to/core/processing/libraries
```

On macOS it might look like this:

```
libraries.dir=/Users/username/Documents/Processing/libraries
processing_core.dir=/Applications/Processing.app/Contents/Java/core/library
```

The `build.gradle` buildscript will look for dependencies at these two paths. Open it up, and you can notice that this project depends on some 3rd party libraries, which need to be installed at `libraries.dir` path. Open your Processing library manager (Sketch > Import Library > Add library) and install whatever libraries are specified in the `build.gradle` file.

Current list of used libraries is

```
Minim // audio processing
The MidiBus // for remote control
Video Export // I use this to export video teasers synced with external audio file
Box2D for Processing // for physics, duh (Looks for BoxesSketch)
Video // video playback
extruder // 2d shape -> 3d shape extrusion
geomerative // for generating shapes from text
PostFX for Processing // can apply post-processing shaders
```

If you set up everything correctly, you should be able to build the project with `./gradlew build` command. (Use `gradlew.bat` on Windows, go figure.)

## How to run

You can run the project with `./gradlew run` task. Or you can set up some sort of Build & Run task in your IDE. (hint: In IntelliJ, go to `Main.kt` and click the lil' green icon next to `main` function and select Run. You do this once, and IDE will set up the task for you (look for run icon in upper right corner of IDE)).

Now you've just run the project and nothing, right? Look at the console output. There's a font missing. It's called [FFF Forward](https://www.1001fonts.com/fff-forward-font.html). Download it to `data/fonts/` and name it `fffforward.ttf`. (I do not include fonts I use in repo, because of licences.)