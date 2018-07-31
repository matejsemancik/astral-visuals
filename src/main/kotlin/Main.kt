
import processing.core.PApplet
import sketches.SketchLoader

fun main(args: Array<String>) {
    val processingArgs = arrayOf("Sketch")
//    val sketch = PolygonalSketch()
    val sketch = SketchLoader()
    PApplet.runSketch(processingArgs, sketch)
}