
import processing.core.PApplet
import sketches.androidtalk.WallpaperSketch

fun main(args: Array<String>) {
    val processingArgs = arrayOf("Sketch")
//    val sketch = SketchLoader()
    val sketch = WallpaperSketch()
    PApplet.runSketch(processingArgs, sketch)
}